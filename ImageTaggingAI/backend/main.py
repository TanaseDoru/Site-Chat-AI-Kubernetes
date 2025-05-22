from fastapi import FastAPI, File, UploadFile, Request
from fastapi.middleware.cors import CORSMiddleware
from azure.storage.blob import BlobServiceClient
from azure.ai.vision.imageanalysis import ImageAnalysisClient
from azure.ai.vision.imageanalysis.models import VisualFeatures
from azure.core.credentials import AzureKeyCredential
import pyodbc
import os
import json
import uuid
import datetime
from dotenv import load_dotenv

load_dotenv()

app = FastAPI()

# Enable CORS - Allow all origins for debugging, tighten this in production
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins temporarily
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configurare Azure din variabile de mediu
STORAGE_CONN_STR = os.environ.get("AZURE_STORAGE_CONNECTION_STRING")
STORAGE_CONTAINER = os.environ.get("AZURE_STORAGE_CONTAINER", "images")
VISION_ENDPOINT = os.environ.get("VISION_ENDPOINT")
VISION_KEY = os.environ.get("VISION_KEY")
SQL_CONN = os.environ.get("AZURE_SQL_CONNECTIONSTRING")

# Inițializare clienți Azure
blob_service_client = BlobServiceClient.from_connection_string(STORAGE_CONN_STR)
vision_client = ImageAnalysisClient(endpoint=VISION_ENDPOINT, credential=AzureKeyCredential(VISION_KEY))

# Debug middleware to log requests
@app.middleware("http")
async def log_requests(request: Request, call_next):
    print(f"[DEBUG] Received request: {request.method} {request.url.path}")
    print(f"[DEBUG] Headers: {request.headers}")
    response = await call_next(request)
    print(f"[DEBUG] Response status: {response.status_code}")
    return response

@app.post("/upload")
async def upload_image(file: UploadFile = File(...)):
    # Generare nume unic pentru fișier
    unique_filename = f"{uuid.uuid4()}_{file.filename}"

    # DEBUG: afișează numele fișierului unic
    print(f"[DEBUG] Unique filename: {unique_filename}")

    # Încărcare în Azure Blob Storage
    file_data = await file.read()
    blob_client = blob_service_client.get_blob_client(container=STORAGE_CONTAINER, blob=unique_filename)
    blob_client.upload_blob(file_data, overwrite=True)
    blob_url = blob_client.url

    # DEBUG: dimensiune și URL blob
    print(f"[DEBUG] Uploaded {len(file_data)} bytes to blob URL: {blob_url}")

    # Generare tag-uri cu Azure Computer Vision
    analysis_result = vision_client.analyze(
        image_data=file_data,
        visual_features=[VisualFeatures.TAGS]
    )
    # DEBUG: conținut raw al rezultatului
    print(f"[DEBUG] Raw analysis_result: {analysis_result!r}")

    tag_values = analysis_result.tags["values"]  # accesează lista de dict-uri
    tags_extracted = [
        {"name": tag["name"], "confidence": tag["confidence"]}
        for tag in tag_values
    ]

    # DEBUG: lista finală de tag-uri
    print(f"[DEBUG] Extracted tags: {tags_extracted}")

    # Stocare metadate în Azure SQL
    try:
        # DEBUG: string de conexiune SQL
        conn = pyodbc.connect(SQL_CONN)
        cursor = conn.cursor()
        now = datetime.datetime.utcnow()
        cursor.execute(
            "INSERT INTO images (file_name, blob_url, uploaded_at, tags) VALUES (?, ?, ?, ?)",
            (unique_filename, blob_url, now, json.dumps(tags_extracted))
        )

        conn.commit()
        conn.close()
        # DEBUG: inserare reușită
        print("[DEBUG] Metadata saved to database successfully")
    except pyodbc.Error as e:
        # DEBUG: eroare SQL
        print(f"[DEBUG] Database error: {e}")
        raise Exception(f"Database error: {e}")

    return {"url": blob_url, "tags": tags_extracted}


@app.get("/history")
async def get_history():
    print("[DEBUG] History endpoint called")
    try:
        conn = pyodbc.connect(SQL_CONN)
        cursor = conn.cursor()
        cursor.execute("SELECT file_name, blob_url, uploaded_at, tags FROM images ORDER BY uploaded_at DESC")
        rows = cursor.fetchall()
        result = [
            {
                "fileName": row[0],
                "url": row[1],
                "uploadedAt": row[2].isoformat(),
                "tags": json.loads(row[3]) if row[3] else []
            }
            for row in rows
        ]
        conn.close()
        print(f"[DEBUG] Returning {len(result)} history items")
        return result
    except pyodbc.Error as e:
        print(f"[DEBUG] Database error on history: {e}")
        raise Exception(f"Database error: {e}")

# Add a health check endpoint
@app.get("/health")
async def health_check():
    return {"status": "ok"}