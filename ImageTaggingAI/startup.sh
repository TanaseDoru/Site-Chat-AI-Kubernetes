#!/bin/bash
# Mută-te în directorul backend-ului (dacă este necesar)
cd backend

# Instalează dependențele Python
pip install -r requirements.txt

# Pornește serverul Uvicorn (sau Gunicorn+Uvicorn)
# Variante:
# uvicorn main:app --host 0.0.0.0 --port 8000
gunicorn -w 2 -k uvicorn.workers.UvicornWorker -b 0.0.0.0:8000 main:app
