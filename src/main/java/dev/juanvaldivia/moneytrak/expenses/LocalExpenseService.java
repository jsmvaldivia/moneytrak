package dev.juanvaldivia.moneytrak.expenses;

import java.util.ArrayList;
import java.util.List;

import static dev.juanvaldivia.moneytrak.expenses.ExpenseMapper.expenseFromDto;

public class LocalExpenseService implements ExpenseService {

    private static final List<Expense> EXPENSES =  new ArrayList<>();

    @Override
    public ExpenseDto createExpense(ExpenseCreationDto expenseCreationDto) {
        Expense expense = expenseFromDto(expenseCreationDto);
        EXPENSES.add(expense);
        return
    }

