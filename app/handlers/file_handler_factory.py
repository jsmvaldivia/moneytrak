from app.handlers.banks.bank_bpi_handler import BankBPIHandler
from app.handlers.banks.bank_cgd_handler import BankCGDHandler
from app.handlers.banks.bank_revolut_handler import BankRevolutHandler
from app.handlers.file_handler import FileHandler


class FileHandlerFactory:
    _handlers = {
        "BPI": BankBPIHandler,
        "CGD": BankCGDHandler,
        "Revolut": BankRevolutHandler,
    }

    @classmethod
    def get_handler(cls, bank: str) -> FileHandler:
        handler_cls = cls._handlers.get(bank)
        if not handler_cls:
            raise ValueError(f"No handler available for bank: {bank}")
        return handler_cls()
