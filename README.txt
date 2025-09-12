Place eng.traineddata here (assets/tessdata/eng.traineddata) or ensure OcrPipeline uses context.filesDir/tessdata at runtime.

## Ingestion (Termux)

This repo ships code only. No database or docs are tracked.

Usage:
```bash
pkg install -y python poppler tesseract
export OCR_LANG=eng
bash init.sh


Put your PDFs/TXT/MD in data/.

Logs go into logs/.

Local DB (corpus.db) is auto-created and ignored by git.


---

👉 Result: anyone cloning gets a **clean repo**, and your `corpus.db`, `logs/`, and `data/` stay private on your device.  
