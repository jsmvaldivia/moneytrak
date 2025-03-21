import pandas as pd
import os
from typing import List

from app.handlers.file_handler import FileHandler
from app.models import Transaction


class BankBPIHandler(FileHandler):
    def read_file(self, file_path: str) -> pd.DataFrame:
        """
        Reads a file and returns its content as a DataFrame with additional metadata.
        """
        # Extract metadata from file name
        file_name = os.path.basename(file_path)
        parts = file_name.split("_")
        bank_code = parts[0]
        account_id = parts[1]
        extract_date_str = parts[2].split(".")[0]

        # Convert the extract date to datetime
        extract_date = pd.to_datetime(
            extract_date_str, format="%Y%m%d", errors="coerce"
        )

        # Read the Excel file
        data_frame = pd.read_excel(
            file_path, engine="openpyxl", skipfooter=3, header=12
        )

        # Add metadata as new columns
        data_frame["source"] = bank_code
        data_frame["account_id"] = account_id
        data_frame["extraction_date"] = extract_date

        return data_frame

    def validate_file(self, df: pd.DataFrame) -> None:
        """
        Validates the structure of the DataFrame.
        """
        errors = []

        # Check if the file is empty
        if df.empty:
            errors.append("Empty file")

        # Check if the file has the expected columns
        # TODO - Configurable columns
        required_columns = [
            "Data Mov.",
            "Data Valor",
            "Descrição do Movimento",
            "Valor em EUR",
            "Saldo em EUR",
        ]
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            errors.append(f"Missing columns: {', '.join(missing_columns)}")

        critical_columns = ["Data Mov.", "Descrição do Movimento", "Valor em EUR"]
        if df[critical_columns].isnull().any().any():
            errors.append("Missing values in critical columns.")

        # Raise all errors at once
        if errors:
            raise ValueError(" | ".join(errors))

    def transform(self, df: pd.DataFrame) -> List[Transaction]:
        """
        Transforms the DataFrame to a normalized structure.
        """

        # df.drop(columns=['Saldo em EUR'], inplace=True)

        df["Data Mov."] = pd.to_datetime(
            df["Data Mov."], format="%d-%m-%Y", errors="coerce"
        )
        df["Data Valor"] = pd.to_datetime(
            df["Data Valor"], format="%d-%m-%Y", errors="coerce"
        )
        df["Descrição do Movimento"] = (
            df["Descrição do Movimento"].fillna("").astype(str)
        )
        df["Valor em EUR"] = pd.to_numeric(df["Valor em EUR"], errors="coerce")

        df.rename(
            columns={
                "Data Mov.": "date",
                "Data Valor": "value_date",
                "Descrição do Movimento": "description",
                "Valor em EUR": "amount",
                "Saldo em EUR": "balance",
            },
            inplace=True,
        )

        print(df.dtypes)

        transactions_list = df.apply(
            lambda row: Transaction(
                date=row["date"],
                value_date=row["value_date"],
                description=row["description"],
                amount=row["amount"],
                balance=row["balance"],
                category="",
                source=row["source"],
                account_id=row["account_id"],
                extraction_date=row["extraction_date"],
                entity_code="bpi",
            ),
            axis=1,
        ).tolist()

        return transactions_list
