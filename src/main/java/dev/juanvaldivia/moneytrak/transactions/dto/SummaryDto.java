package dev.juanvaldivia.moneytrak.transactions.dto;

import java.math.BigDecimal;

/**
 * DTO for transaction summary responses.
 * Wraps aggregate totals for extensibility — fields can be added without breaking the wire format.
 *
 * @param total sum of transaction amounts for the requested type
 */
public record SummaryDto(BigDecimal total) {
}
