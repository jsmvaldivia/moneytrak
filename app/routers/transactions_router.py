from fastapi import APIRouter, Form, UploadFile, File, HTTPException, Depends
import os

from app.dependencies.transactions_deps import get_transaction_service
from app.services.transactions_service import TransactionsService

ALLOWED_EXTENSIONS = {".xlsx", ".csv"}
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB

router = APIRouter()


@router.post("/upload", tags=["transactions"])
async def upload_transactions_file(
    bank: str = Form(...),
    file: UploadFile = File(...),
    service: TransactionsService = Depends(get_transaction_service),
):
    if file.filename is None:
        raise HTTPException(status_code=400, detail="No file uploaded.")

    _, extension = os.path.splitext(file.filename)
    if extension not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=400, detail="Unsupported file extension.")

    if bank not in ["BPI"]:
        # Temporary solution to test the factory
        raise HTTPException(status_code=400, detail="Unsupported bank extract file")

    await service.save_transactions(bank, file.filename)
    return {"filename": file.filename, "bank": bank}
