package ru.itmo.finance.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseAnalyticsResponse(LocalDate dateFrom, LocalDate dateTo, String currency,
                                       BigDecimal totalExpenses, List<CategoryExpenseResponse> categories) {
}
