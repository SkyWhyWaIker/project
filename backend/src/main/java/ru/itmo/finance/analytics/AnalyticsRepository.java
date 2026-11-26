package ru.itmo.finance.analytics;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import ru.itmo.finance.common.DateRange;

@Repository
class AnalyticsRepository {

    private final JdbcClient jdbcClient;

    AnalyticsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    List<CategoryExpense> calculateExpensesByCategory(UUID userId, DateRange range) {
        return jdbcClient.sql("""
                        SELECT category.id AS category_id, category.name AS category_name, SUM(operation.amount) AS amount
                        FROM financial_operations operation
                        JOIN categories category ON category.id = operation.category_id
                        WHERE operation.user_id = :userId AND operation.operation_type = 'EXPENSE'
                            AND operation.operation_date BETWEEN :dateFrom AND :dateTo
                        GROUP BY category.id, category.name
                        ORDER BY amount DESC, category.id
                        """)
                .param("userId", userId)
                .param("dateFrom", range.dateFrom())
                .param("dateTo", range.dateTo())
                .query((result, rowNumber) -> new CategoryExpense(result.getLong("category_id"),
                        result.getString("category_name"), result.getBigDecimal("amount")))
                .list();
    }
}
