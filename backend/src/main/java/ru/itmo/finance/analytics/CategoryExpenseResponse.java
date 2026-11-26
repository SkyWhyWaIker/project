package ru.itmo.finance.analytics;

import java.math.BigDecimal;

public record CategoryExpenseResponse(long categoryId, String categoryName, BigDecimal amount, BigDecimal percentage) {
}
