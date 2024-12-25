from handlers.file_handler_factory import FileHandlerFactory

def handle_files(bank, file_path):
    try:
        handler = FileHandlerFactory.get_handler(bank)
        df = handler.read_file(file_path)
        validated_df = handler.validate_file(df)
        normalized_df = handler.transform(validated_df)
        print(normalized_df)
    except ValueError as e:
        print(f"Error: {e}")
    
if __name__ == "__main__":
    
    bank = "BPI"  # This could be dynamically determined or user-provided
    file_path = "extmovs_bpi2212233828.xlsx"
    handle_files(bank, file_path)

