from handlers.file_handler_factory import FileHandlerFactory


def handle_files(bank, file_path):
    try:
        handler = FileHandlerFactory.get_handler(bank)
        df = handler.read_file(file_path)
        handler.validate_file(df)
        transactions = handler.transform(df)
        # Http client to send transactions to an API

        # Send transactions to the API
        url = "http://your-api-url/transactions"
        headers = {'Content-Type': 'application/json'}
        for transaction in transactions:
            transaction_data = {
                "accountId": transaction.account_id,
                "dateMovement": transaction.date_movement.isoformat(),
                "valueDate": transaction.value_date.isoformat(),
                "description": transaction.description,
                "amount": transaction.amount,
                "balance": transaction.balance,
                "bankCode": transaction.bank_code,
                "extractDate": transaction.extract_date.isoformat()
            }
            response = requests.post(
                url, json=transaction_data, headers=headers)
            if response.status_code != 200:
                print(f"Failed to send transaction: {response.text}")

    except ValueError as e:
        print(f"Error: {e}")


if __name__ == "__main__":

    bank = "BPI"  # This could be dynamically determined or user-provided
    file_path = "bpi_2212233828_20241225.xlsx"
    handle_files(bank, file_path)
