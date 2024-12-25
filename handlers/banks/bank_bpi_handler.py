import pandas as pd
from handlers.file_handler import FileHandler
from models import Transaction
from datetime import datetime

class BankBPIHandler(FileHandler):
    def read_file(self, file_path: str) -> pd.DataFrame:
        """
        Reads a file and returns its content as a DataFrame.
        """
        data_frame = pd.read_excel(file_path, engine='openpyxl', skipfooter=3, header=12)
        return data_frame

    def validate_file(self, df: pd.DataFrame) -> None:
        """
        Validates the structure of the DataFrame.
        """
        errors = []
        
        #Check if the file is empty
        if df.empty:
            errors.append('Empty file')
        
        #Check if the file has the expected columns
        #TODO - Configurable columns
        required_columns = ['Data Mov.', 'Data Valor', 'Descrição do Movimento', 'Valor em EUR','Saldo em EUR']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            errors.append(f"Missing columns: {', '.join(missing_columns)}")
        
        critical_columns = ['Data Mov.', 'Descrição do Movimento', 'Valor em EUR']
        if df[critical_columns].isnull().any().any():
            errors.append("Missing values in critical columns.")

        # Raise all errors at once
        if errors:
            raise ValueError(" | ".join(errors))


    def transform(self, df: pd.DataFrame) -> Transaction:
        date = datetime(2021, 1, 1)
        description = 'Test'
        amount = 1.0
        return Transaction(date=date, value_date=date, description=description, amount=amount, balance= 2.0, category= "category", source="source")