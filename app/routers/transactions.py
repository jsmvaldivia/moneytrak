from fastapi import APIRouter, Form, UploadFile, File, HTTPException
import os

from app.handlers.file_handler_factory import FileHandlerFactory

ALLOWED_EXTENSIONS = {".xlsx", ".csv"}
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB

router = APIRouter()


@router.get("/hello", tags=["hello"])
def hello():
    test = "Hello World"
    return test


@router.post("/upload", tags=["transactions"])
async def upload_transactions_file(bank: str = Form(...), file: UploadFile = File(...)):
    if file.filename is None:
        raise HTTPException(status_code=400, detail="No file uploaded.")

    _, ext = os.path.splitext(file.filename)
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=400, detail="Unsupported file extension.")

    if bank not in ["BPI"]:
        # Temporary solution to test the factory
        raise HTTPException(status_code=400, detail="Unsupported bank extract file")

    bank_file_handler = FileHandlerFactory.get_handler(bank)
    pd = bank_file_handler.read_file(file.filename)
    print(pd)


# if __name__ == "__main__":
#    bank = "BPI"  # This could be dynamically determined or user-provided
#    file_path = "bpi_2212233828_20241225.xlsx"
#    handle_files(bank, file_path)
