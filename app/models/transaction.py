from datetime import datetime


class Transaction:
    def __init__(
        self,
        date: datetime,
        value_date: datetime,
        description: str,
        amount: float,
        balance: float,
        category: str,
        source: str,
        account_id: str,
        extraction_date: datetime,
        entity_code: str,
    ):
        self.date = date  # Date of transaction
        self.value_date = value_date  # Date when the value is actually posted
        self.description = description
        self.amount = amount
        self.balance = balance
        self.category = category
        self.source = source
        self.account_id = account_id
        self.extraction_date = extraction_date
        self.entity_code = entity_code

    def __str__(self):
        return (
            f"Transaction(date={self.date}, value_date={self.value_date}, "
            f"description={self.description}, amount={self.amount}, balance={self.balance}, "
            f"category={self.category}, source={self.source}, account_id={self.account_id}, extraction_date={self.extraction_date}, entity_code={self.entity_code})"
        )

    def __repr__(self):
        return (
            f"Transaction(date={self.date}, value_date={self.value_date}, "
            f"description={self.description}, amount={self.amount}, balance={self.balance}, "
            f"category={self.category}, source={self.source}, account_id={self.account_id}, extraction_date={self.extraction_date}), entity_code={self.entity_code}"
        )
