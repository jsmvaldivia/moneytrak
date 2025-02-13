# moneytrak

## Project Description

Moneytrak is a financial data processing tool designed to handle bank transaction files from various banks, validate their structure, and transform them into a normalized format. The processed transactions can then be sent to an API for further processing or storage.

## Purpose

The purpose of this project is to provide a unified way to handle different bank transaction files, ensuring data consistency and facilitating integration with other systems.

## How to Run

### Prerequisites

- Python 3.8 or higher
- Required Python packages (listed in `requirements.txt`)

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/yourusername/moneytrak.git
   cd moneytrak
   ```

2. Install the required packages:
   ```sh
   pip install -r requirements.txt
   ```

### Usage

1. Set the bank and file path in the `app.py` file:

   ```python
   bank = "BPI"  # This could be dynamically determined or user-provided
   file_path = "path_to_your_file.xlsx"
   ```

2. Run the application:
   ```sh
   python app.py
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
