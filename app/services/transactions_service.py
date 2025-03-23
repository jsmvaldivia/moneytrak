from app.persistence.transactions_storage import TransactionsStorage
from app.handlers.file_handler_factory import FileHandlerFactory


class TransactionsService:
    def __init__(self, transactionsStorage: TransactionsStorage):
        self.transactionsStorage = transactionsStorage

    async def save_transactions(self, bank: str, filename: str):
        bank_file_handler = FileHandlerFactory.get_handler(bank)
        df = bank_file_handler.read_file(filename)
        bank_file_handler.validate_file(df)
        transactions = bank_file_handler.transform(df)

        await self.transactionsStorage.insert_transctions_batch(transactions)
