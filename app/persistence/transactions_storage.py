from app.persistence.db_client import DBClient


class TransactionsStorage:
    def __init__(self, db: DBClient):
        self.db = db

    async def insert_transctions_batch(self, transactions: list):
        query = """
            INSERT INTO transactions (
                        date, value_date, description, amount, balance, category, source, account_id, extraction_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        await self.db.execute(query, ())

    async def get_transactions(self):
        query = """
            SELECT * FROM transactions
        """
        return await self.db.fetch_all(query)
