#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
export OCR_LANG="${OCR_LANG:-eng}"

mkdir -p logs data tmp

if [ ! -f corpus.db ]; then
  echo "Creating clean corpus.db ..."
  python3 - <<'PY'
import sqlite3
con = sqlite3.connect("corpus.db")
con.execute("PRAGMA journal_mode=WAL;")
con.commit(); con.close()
PY
fi

python3 db_import.py
echo "Done. Check logs/ingest.log"