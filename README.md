# moneytrak

## Project Description

Moneytrak is a financial data processing tool designed to handle bank transaction files from various banks, validate their structure, and transform them into a normalized format. The processed transactions can then be sent to an API for further processing or storage.

## Purpose

The purpose of this project is to provide a unified way to handle different bank transaction files, ensuring data consistency and facilitating integration with other systems.

## How to Run

### Prerequisites

- Python 3.10 or higher

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/yourusername/moneytrak.git
   cd moneytrak
   ```

2. Install the required packages:
    ```sh
    uv sync
    ```
It creates the virtual environment and installs the required packages out from [pyproject.toml](pyproject.toml) file

### Usage

1. Start the FastAPI server:

   ```sh
   uv run fastapi dev
   ```

2. The API will be available at `http://localhost:8000`

3. Use the API endpoints to upload and process bank transaction files

### Testing

Run the unit tests with:

```sh
uv run pytest
```

### Adding New Bank Handlers

To add support for a new bank:

1. Create a new handler class in the `handlers/banks` directory, inheriting from `FileHandler`.
2. Implement the `read_file`, `validate_file`, and `transform` methods.
3. Register the new handler in the `FileHandlerFactory`.

### Contributing

Feel free to submit issues or pull requests if you have suggestions or improvements.

### License

This project is licensed under the MIT License.
