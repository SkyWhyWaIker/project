package ru.itmo.finance.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.itmo.finance.common.DateRange;

@Service
class AnalyticsService {

    private final AnalyticsRepository repository;
    private final Clock clock;

    AnalyticsService(AnalyticsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ExpenseAnalyticsResponse getExpenses(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        DateRange range = DateRange.forAnalytics(dateFrom, dateTo, clock);
        List<CategoryExpense> expenses = repository.calculateExpensesByCategory(userId, range);
        BigDecimal totalExpenses = expenses.stream()
                .map(CategoryExpense::amount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        List<CategoryExpenseResponse> categories = expenses.stream()
                .map(expense -> new CategoryExpenseResponse(expense.categoryId(), expense.categoryName(),
                        expense.amount(), expense.amount().multiply(BigDecimal.valueOf(100))
                                .divide(totalExpenses, 2, RoundingMode.HALF_UP)))
                .toList();

        return new ExpenseAnalyticsResponse(range.dateFrom(), range.dateTo(), "RUB", totalExpenses, categories);
    }
}
