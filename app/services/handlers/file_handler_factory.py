from handlers.banks.bank_bpi_handler import BankBPIHandler
from handlers.banks.bank_cgd_handler import BankCGDHandler
from handlers.banks.bank_revolut_handler import BankRevolutHandler


class FileHandlerFactory:
    @staticmethod
    def get_handler(bank: str):
        if bank == "BPI":
            return BankBPIHandler()
        elif bank == "CGD":
            return BankCGDHandler()
        elif bank == "Revolut":
            return BankRevolutHandler()
        else:
            raise ValueError(f"No handler available for bank: {bank}")
