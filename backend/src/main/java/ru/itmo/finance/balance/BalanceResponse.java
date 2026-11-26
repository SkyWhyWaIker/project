package ru.itmo.finance.balance;

import java.math.BigDecimal;

public record BalanceResponse(String currency, BigDecimal balance, BigDecimal totalIncome, BigDecimal totalExpenses) {
}
