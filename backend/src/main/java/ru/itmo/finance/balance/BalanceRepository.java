package ru.itmo.finance.balance;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class BalanceRepository {

    private final JdbcClient jdbcClient;

    BalanceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    BalanceResponse calculateBalance(UUID userId) {
        return jdbcClient.sql("""
                        SELECT
                            COALESCE(SUM(amount) FILTER (WHERE operation_type = 'INCOME'), 0) AS total_income,
                            COALESCE(SUM(amount) FILTER (WHERE operation_type = 'EXPENSE'), 0) AS total_expenses
                        FROM financial_operations
                        WHERE user_id = :userId
                        """)
                .param("userId", userId)
                .query((result, rowNumber) -> {
                    BigDecimal totalIncome = result.getBigDecimal("total_income").setScale(2);
                    BigDecimal totalExpenses = result.getBigDecimal("total_expenses").setScale(2);
                    return new BalanceResponse("RUB", totalIncome.subtract(totalExpenses), totalIncome, totalExpenses);
                })
                .single();
    }
}
