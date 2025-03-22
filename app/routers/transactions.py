from fastapi import APIRouter

router = APIRouter()

@router.get("/hello", tags=["hello"])
def hello():
    return {"Hello": "World"}



# if __name__ == "__main__":
#    bank = "BPI"  # This could be dynamically determined or user-provided
#    file_path = "bpi_2212233828_20241225.xlsx"
#    handle_files(bank, file_path)
