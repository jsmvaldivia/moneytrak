package dev.juanvaldivia.moneytrak.expenses;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/expenses")
class ExpenseController {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
      this.expenseService = expenseService;
  }

  @PostMapping
  public ResponseEntity<ExpenseDto> createNewExpense(
      @Valid @RequestBody ExpenseCreationDto expenseCreationDto) {

    ExpenseDto expense = expenseService.createExpense(expenseCreationDto);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{eid}")
            .buildAndExpand(expense.eid())
            .toUri();

    return ResponseEntity.created(location).body(expense);
  }
}
