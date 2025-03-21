from app.handlers.file_handler import FileHandler

class BankCGDHandler(FileHandler):
    def read_file(self, file_path: str):
        raise NotImplementedError("The 'read_file' method is not implemented yet.")

    def validate_file(self, df):
        raise NotImplementedError("The 'validate_file' method is not implemented yet.")

    def transform(self, df):
        raise NotImplementedError("The 'transform' method is not implemented yet.")
