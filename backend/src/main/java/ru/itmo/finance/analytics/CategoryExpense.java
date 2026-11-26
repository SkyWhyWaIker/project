package ru.itmo.finance.analytics;

import java.math.BigDecimal;

record CategoryExpense(long categoryId, String categoryName, BigDecimal amount) {
}
