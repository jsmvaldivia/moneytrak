from unittest.mock import patch
import pandas as pd
from handlers.banks.bank_bpi_handler import BankBPIHandler

# Mock valid data
valid_data = pd.DataFrame({
    'Data Mov.': ['2021-01-01', '2021-01-02'],
    'Data Valor': ['2021-01-01', '2021-01-02'],
    'Descrição do Movimento': ['Test', 'Test'],
    'Valor em EUR': [1.0, 2.0],
    'Saldo em EUR': [2.0, 3.0]
})


@patch("pandas.read_excel")
def test_read_file_bpi_success(mock_read_excel):
    """
    Test that the read_file method successfully reads an Excel file.
    """
    # Arrange
    mock_read_excel.return_value = valid_data
    handler = BankBPIHandler()

    # Act
    df = handler.read_file("bpi_2212233828_20241225.xlsx")

    # Assert
    assert df.equals(valid_data)
    # mock_read_excel.assert_called_once_with("mock_file.xlsx", engine="openpyxl")
