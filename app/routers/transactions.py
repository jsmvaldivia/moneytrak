from fastapi import APIRouter

from app.handlers.file_handler_factory import FileHandlerFactory

router = APIRouter()

@router.get("/hello", tags=["hello"])
def hello():
    return {"Hello": "World"}

def handle_files(bank, file_path):
    try:
        handler = FileHandlerFactory.get_handler(bank)
        df = handler.read_file(file_path)
        handler.validate_file(df)
        transactions = handler.transform(df)

        for transaction in transactions:
            transaction_data = {
                "accountId": transaction.account_id,
                "dateMovement": transaction.date.isoformat(),
                "valueDate": transaction.value_date.isoformat(),
                "description": transaction.description,
                "amount": transaction.amount,
                "balance": transaction.balance,
                "entity_code": transaction.entity_code,
                "extraction_date": transaction.extraction_date.isoformat(),
            }
            print(transaction_data)

    except ValueError as e:
        print(f"Error: {e}")

#if __name__ == "__main__":
#    bank = "BPI"  # This could be dynamically determined or user-provided
#    file_path = "bpi_2212233828_20241225.xlsx"
#    handle_files(bank, file_path)
