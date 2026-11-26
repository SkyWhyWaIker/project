package ru.itmo.finance.operation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import ru.itmo.finance.common.OperationType;

public record OperationResponse(UUID id, BigDecimal amount, String currency, OperationType type,
                                long categoryId, String categoryName, LocalDate operationDate,
                                String description, Instant createdAt) {
}
