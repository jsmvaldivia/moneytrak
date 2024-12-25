from datetime import datetime

class Transaction:
    def __init__(self, date: datetime, value_date: datetime, description: str, amount: float, balance: float, category: str, source: str):
        self.date = date # Date of transaction
        self.value_date = value_date # Date when the value is actually posted
        self.description = description
        self.amount = amount
        self.balance = balance
        self.category = category
        self.source = source