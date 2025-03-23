from app.services.transactions_service import TransactionsService
from app.persistence.transactions_storage import TransactionsStorage
from app.persistence.sqlite_db_client import SQLiteClient


def get_transaction_service():
    db = SQLiteClient("transactions.db")
    storage = TransactionsStorage(db)
    return TransactionsService(storage)
