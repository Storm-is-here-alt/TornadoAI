#!/data/data/com.termux/files/usr/bin/python3
# -*- coding: utf-8 -*-

import os, sys, sqlite3, pathlib, hashlib, time, subprocess, shutil, resource

DB_PATH  = "/storage/6F3A-4D77/TornadoAI/corpus.db"
DATA_DIR = "/storage/6F3A-4D77/TornadoAI/data"
LOG_DIR  = "/storage/6F3A-4D77/TornadoAI/logs"
LOG_FILE = os.path.join(LOG_DIR, "ingest.log")

ALLOWED_EXT = {".txt", ".md", ".pdf"}
OCR_LANG = os.environ.get("OCR_LANG", "eng")
MIN_OK_LEN = 40

os.makedirs(LOG_DIR, exist_ok=True)

# ---------------- Logging ----------------
def log(line: str):
    ts = time.strftime("%Y-%m-%d %H:%M:%S")
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(f"[{ts}] {line}\n")

# ---------------- Proc usage ----------------
_START_WALL = time.time()
_START_CPU  = resource.getrusage(resource.RUSAGE_SELF)

def _cpu_seconds(ru) -> float:
    return float(ru.ru_utime + ru.ru_stime)

def proc_usage_snapshot():
    """Return dict with %CPU, %MEM, RSS(kB), maxRSS(kB), wall_since_start(s), cpu_since_start(s)."""
    pid = str(os.getpid())
    cpu_pct = mem_pct = rss_ps = "na"
    # ps snapshot (instantaneous)
    try:
        out = subprocess.check_output(["ps","-p",pid,"-o","%cpu,%mem,rss"], text=True).strip().splitlines()
        if len(out) >= 2:
            parts = out[1].split()
            if len(parts) >= 3:
                cpu_pct, mem_pct, rss_ps = parts[0], parts[1], parts[2]
    except Exception:
        pass
    # ru_maxrss on Linux is kB in Python’s resource module
    ru = resource.getrusage(resource.RUSAGE_SELF)
    max_rss_kb = int(ru.ru_maxrss)
    cpu_delta_s = _cpu_seconds(ru) - _cpu_seconds(_START_CPU)
    wall_delta_s = time.time() - _START_WALL
    return {
        "cpu_pct": cpu_pct,
        "mem_pct": mem_pct,
        "rss_kb": rss_ps,
        "maxrss_kb": max_rss_kb,
        "cpu_s": round(cpu_delta_s, 3),
        "wall_s": round(wall_delta_s, 3),
    }

def log_proc_usage(context: str):
    u = proc_usage_snapshot()
    log(f"[usage] {context} cpu={u['cpu_pct']}% mem={u['mem_pct']}% rss={u['rss_kb']}kB maxrss={u['maxrss_kb']}kB cpu_s={u['cpu_s']} wall_s={u['wall_s']}")

# ---------------- Utils ----------------
def sha256_file(path: pathlib.Path, chunk=1024*1024) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for b in iter(lambda: f.read(chunk), b""):
            h.update(b)
    return h.hexdigest()

def have(cmd: str) -> bool:
    return shutil.which(cmd) is not None

# ---------------- PDF helpers ----------------
def pdf_page_count(pdf: pathlib.Path) -> int:
    if not have("pdfinfo"): return 0
    try:
        out = subprocess.check_output(["pdfinfo", str(pdf)], stderr=subprocess.DEVNULL, timeout=20).decode("utf-8","ignore")
        for line in out.splitlines():
            if line.lower().startswith("pages:"):
                return int(line.split(":")[1].strip())
    except Exception as e:
        log(f"[pdfinfo fail] {pdf.name}: {e}")
    return 0

def extract_pdf_text_pdftotext(pdf: pathlib.Path, page: int) -> str:
    if not have("pdftotext"): return ""
    try:
        out = subprocess.check_output(
            ["pdftotext","-layout","-nopgbrk","-f",str(page),"-l",str(page), str(pdf), "-"],
            stderr=subprocess.DEVNULL, timeout=60
        )
        return out.decode("utf-8","ignore")
    except Exception as e:
        log(f"[pdftotext fail] {pdf.name} p{page}: {e}")
        return ""

def _tesseract_png(png: pathlib.Path) -> str:
    out_txt = str(png) + ".txt"
    try:
        subprocess.check_call(["tesseract", str(png), str(png), "-l", OCR_LANG],
                              stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=120)
        if os.path.exists(out_txt):
            return open(out_txt,"r",errors="ignore").read().strip()
        return ""
    finally:
        try:
            if os.path.exists(out_txt): os.remove(out_txt)
            if png.exists(): os.remove(str(png))
        except Exception:
            pass

def ocr_pdf_page_with_dpi(pdf: pathlib.Path, page: int, dpi: int) -> str:
    """Render page -> PNG (pdftoppm singlefile; fallback pdftocairo) -> tesseract."""
    tmpdir = pathlib.Path("/data/data/com.termux/files/usr/tmp")
    tmpdir.mkdir(parents=True, exist_ok=True)
    base = f"ocr_{int(time.time()*1000)}"
    prefix = tmpdir / base

    # Try pdftoppm singlefile
    try:
        if have("pdftoppm"):
            png_single = tmpdir / f"{base}.png"
            subprocess.check_call(["pdftoppm","-png","-singlefile","-r",str(dpi),"-f",str(page),"-l",str(page), str(pdf), str(prefix)],
                                  stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=120)
            if png_single.exists():
                txt = _tesseract_png(png_single)
                if txt: return txt
            else:
                for candidate in (tmpdir / f"{base}-1.png", tmpdir / f"{base}-{page}.png"):
                    if candidate.exists():
                        txt = _tesseract_png(candidate)
                        if txt: return txt
                log(f"[ocr render miss] {pdf.name} p{page} dpi={dpi} (pdftoppm no png)")
    except Exception as e:
        log(f"[ocr fail pdftoppm] {pdf.name} p{page} dpi={dpi}: {e}")

    # Fallback: pdftocairo
    try:
        if have("pdftocairo"):
            subprocess.check_call(["pdftocairo","-png","-r",str(dpi),"-f",str(page),"-l",str(page), str(pdf), str(prefix)],
                                  stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=120)
            png_c = tmpdir / f"{base}-1.png"
            if not png_c.exists(): png_c = tmpdir / f"{base}.png"
            if png_c.exists():
                return _tesseract_png(png_c)
            else:
                log(f"[ocr render miss] {pdf.name} p{page} dpi={dpi} (pdftocairo no png)")
    except Exception as e:
        log(f"[ocr fail pdftocairo] {pdf.name} p{page} dpi={dpi}: {e}")

    return ""

def ocr_pdf_page_escalate(pdf: pathlib.Path, page: int, start_dpi: int):
    ladder = [150, 200, 300, 400, 500]
    try:
        idx = next(i for i, d in enumerate(ladder) if d >= start_dpi)
    except StopIteration:
        idx = len(ladder)-1
    for dpi in ladder[idx:]:
        txt = ocr_pdf_page_with_dpi(pdf, page, dpi)
        if len(txt) >= MIN_OK_LEN:
            log(f"[ocr ok] {pdf.name} p{page} dpi={dpi} len={len(txt)}")
            return txt, dpi
    log(f"[ocr empty] {pdf.name} p{page} tried>={start_dpi}")
    return "", start_dpi

# ---------------- DB ----------------
def ensure_schema(con: sqlite3.Connection):
    cur = con.cursor()
    cur.execute("""
      CREATE TABLE IF NOT EXISTS docs(
        id INTEGER PRIMARY KEY,
        title TEXT,
        url TEXT UNIQUE,
        content TEXT
      );
    """)
    cur.execute("""
      CREATE VIRTUAL TABLE IF NOT EXISTS docs_fts
      USING fts5(content, content='docs', content_rowid='id');
    """)
    cur.execute("""
      CREATE TABLE IF NOT EXISTS docs_meta(
        url TEXT PRIMARY KEY,
        sha256 TEXT,
        size INTEGER,
        mtime REAL,
        last_ingested_ts REAL,
        status TEXT,
        pages_total INTEGER,
        pages_done  INTEGER
      );
    """)
    _ensure_meta_column(con, "pref_method", "TEXT")
    _ensure_meta_column(con, "ocr_dpi", "INTEGER")
    cur.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_docs_url ON docs(url);")
    con.commit()

def _ensure_meta_column(con: sqlite3.Connection, name: str, decl: str):
    cur = con.cursor()
    cols = [r[1] for r in cur.execute("PRAGMA table_info(docs_meta)").fetchall()]
    if name not in cols:
        cur.execute(f"ALTER TABLE docs_meta ADD COLUMN {name} {decl};")

def need_ingest(cur: sqlite3.Cursor, url: str, sha: str, size: int, mtime: float) -> bool:
    row = cur.execute("SELECT sha256, size, mtime FROM docs_meta WHERE url=?", (url,)).fetchone()
    if not row: return True
    old_sha, old_size, old_mtime = row
    return (old_sha != sha) or (old_size != size) or (float(old_mtime) != float(mtime))

def upsert_doc(con: sqlite3.Connection, title: str, url: str, content: str) -> int:
    cur = con.cursor()
    cur.execute("""
      INSERT INTO docs(title, url, content)
      VALUES(?, ?, ?)
      ON CONFLICT(url) DO UPDATE SET
        title=excluded.title,
        content=excluded.content;
    """, (title, url, content))
    rid = cur.execute("SELECT id FROM docs WHERE url=?", (url,)).fetchone()[0]
    cur.execute("DELETE FROM docs_fts WHERE rowid=?", (rid,))
    cur.execute("INSERT INTO docs_fts(rowid, content) SELECT id, content FROM docs WHERE id=?", (rid,))
    con.commit()
    return rid

def update_meta(con: sqlite3.Connection, url: str, **kv):
    if not kv: return
    cur = con.cursor()
    cur.execute("INSERT INTO docs_meta(url) VALUES (?) ON CONFLICT(url) DO NOTHING;", (url,))
    sets = ", ".join([f"{k}=:{k}" for k in kv.keys()])
    cur.execute(f"UPDATE docs_meta SET {sets}, last_ingested_ts=:ts WHERE url=:url", dict(kv, url=url, ts=time.time()))
    con.commit()

def get_meta(cur: sqlite3.Cursor, url: str):
    return cur.execute("SELECT * FROM docs_meta WHERE url=?", (url,)).fetchone()

def _get(row, key, default=None):
    try:
        v = row[key]
        return default if v is None else v
    except Exception:
        return default

# ---------------- Processors ----------------
def process_text_like(con, path: pathlib.Path, url: str, sha: str, size: int, mtime: float):
    title = path.stem
    text = path.read_text(errors="ignore").strip()
    if not text:
        update_meta(con, url, sha256=sha, size=size, mtime=mtime, status="empty", pages_total=1, pages_done=1)
        return
    rid = upsert_doc(con, title, url, text)
    update_meta(con, url, sha256=sha, size=size, mtime=mtime, status="ok", pages_total=1, pages_done=1)
    log(f"[ok] id={rid} {path.name}")

def process_pdf(con, path: pathlib.Path, url: str, sha: str, size: int, mtime: float):
    """
    One page per run.
    Reuses last working method & DPI from docs_meta:
      - Try pref_method first.
      - If short/empty, fallback (text->ocr).
      - If OCR short, escalate DPI and persist new good DPI.
    """
    title = path.stem
    cur = con.cursor()
    total = pdf_page_count(path) or 1

    meta = get_meta(cur, url)
    done = int(_get(meta, "pages_done", 0)) if meta else 0
    pref = _get(meta, "pref_method", None)
    ocr_dpi = int(_get(meta, "ocr_dpi", 200))

    if meta is None:
        update_meta(con, url, pages_total=total, pages_done=0, status="init",
                    sha256=sha, size=size, mtime=mtime, pref_method=None, ocr_dpi=ocr_dpi)

    page = min(done + 1, total)

    row = cur.execute("SELECT content FROM docs WHERE url=?", (url,)).fetchone()
    base_text = row[0] if row else ""

    got_text = ""
    used_method = None
    used_dpi = ocr_dpi

    def ok(t: str) -> bool:
        return len(t.strip()) >= MIN_OK_LEN

    if pref == "text":
        t = extract_pdf_text_pdftotext(path, page).strip()
        if ok(t):
            got_text, used_method = t, "text"
        else:
            t2, good_dpi = ocr_pdf_page_escalate(path, page, ocr_dpi)
            if ok(t2):
                got_text, used_method, used_dpi = t2, "ocr", good_dpi
    elif pref == "ocr":
        t, good_dpi = ocr_pdf_page_escalate(path, page, ocr_dpi)
        if ok(t):
            got_text, used_method, used_dpi = t, "ocr", good_dpi
        else:
            t2 = extract_pdf_text_pdftotext(path, page).strip()
            if ok(t2):
                got_text, used_method = t2, "text"
    else:
        t = extract_pdf_text_pdftotext(path, page).strip()
        if ok(t):
            got_text, used_method = t, "text"
        else:
            t2, good_dpi = ocr_pdf_page_escalate(path, page, ocr_dpi)
            if ok(t2):
                got_text, used_method, used_dpi = t2, "ocr", good_dpi

    if not got_text:
        got_text = ""

    combined = (base_text + ("\n\n" if base_text and got_text else "") + got_text).strip()
    rid = upsert_doc(con, title, url, combined)

    new_done = page
    status = "ok_part" if new_done < total else "ok"

    new_pref = pref
    new_dpi = ocr_dpi
    if got_text:
        if used_method == "text":
            new_pref = "text"
        elif used_method == "ocr":
            new_pref = "ocr"
            new_dpi = used_dpi

    update_meta(con, url,
                sha256=sha, size=size, mtime=mtime,
                pages_total=total, pages_done=new_done, status=status,
                pref_method=new_pref, ocr_dpi=new_dpi)

    # Per-page usage log
    log(f"[ok p{page}/{total}] id={rid} {path.name} method={used_method or 'none'} dpi={new_dpi} added={len(got_text)}")
    log_proc_usage(f"after_page p{page} {path.name}")

# ---------------- Main ----------------
def main():
    data_dir = pathlib.Path(DATA_DIR)
    if not data_dir.exists():
        print(f"Data dir not found: {DATA_DIR}")
        sys.exit(1)

    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row
    ensure_schema(con)
    cur = con.cursor()

    total = changed = skipped = failed = 0

    for path in sorted(data_dir.rglob("*")):
        if not path.is_file(): continue
        ext = path.suffix.lower()
        if ext not in ALLOWED_EXT: continue
        total += 1

        url = "file://" + str(path)
        st = path.stat()
        mtime = st.st_mtime
        size = st.st_size

        try:
            sha = sha256_file(path)
        except Exception as e:
            log(f"[hash fail] {path}: {e}")
            failed += 1
            continue

        try:
            if ext in {".txt", ".md"}:
                if need_ingest(cur, url, sha, size, mtime):
                    process_text_like(con, path, url, sha, size, mtime)
                    changed += 1
                else:
                    skipped += 1
                continue

            if need_ingest(cur, url, sha, size, mtime):
                update_meta(con, url, pages_done=0, pages_total=None, status="changed", pref_method=None)

            process_pdf(con, path, url, sha, size, mtime)
            changed += 1

        except Exception as e:
            failed += 1
            update_meta(con, url, sha256=sha, size=size, mtime=mtime, status=f"error: {e}")
            log(f"[error] {path.name}: {e}")

    cur.execute("""
      INSERT INTO docs_fts(rowid, content)
      SELECT id, content FROM docs
      WHERE id NOT IN (SELECT rowid FROM docs_fts);
    """)
    con.commit()
    con.close()

    summary = f"done: total={total} changed={changed} skipped={skipped} failed={failed}"
    print(summary)
    log("[summary] " + summary)
    log_proc_usage("end_of_run")

if __name__ == "__main__":
    main()
