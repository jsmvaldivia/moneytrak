package dev.juanvaldivia.moneytrak.expenses;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ExpenseMapper {

    public static Expense expenseFromDto(ExpenseCreationDto expenseCreationDto) {
        var id = UUID.randomUUID();

        ZonedDateTime date;
        if(expenseCreationDto.date() == null){
            date = ZonedDateTime.now();
        } else {
            date = expenseCreationDto.date();
        }

        var description = expenseCreationDto.description();
        var amount = expenseCreationDto.amount();
        var currency = expenseCreationDto.currency();

        return new Expense(id,date,description, amount,currency);
    }
}
