package dev.juanvaldivia.moneytrak.expenses;

import org.springframework.stereotype.Service;

@Service
interface ExpenseService {

  ExpenseDto createExpense(ExpenseCreationDto expenseCreationDto);
}
