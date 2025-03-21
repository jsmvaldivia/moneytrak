from abc import ABC, abstractmethod
from typing import List
from app.models.transaction import Transaction
import pandas as pd


class FileHandler(ABC):
    @abstractmethod
    def read_file(self, file_path: str) -> pd.DataFrame:
        """
        Reads a file and returns its content as a DataFrame.
        """
        pass

    @abstractmethod
    def validate_file(self, df: pd.DataFrame) -> None:
        """
        Validates the structure of the DataFrame.
        """
        pass

    @abstractmethod
    def transform(self, df: pd.DataFrame) -> List[Transaction]:
        """
        Transforms the DataFrame to a normalized structure.
        """
        pass
