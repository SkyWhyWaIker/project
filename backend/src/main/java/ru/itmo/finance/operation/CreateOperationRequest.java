package ru.itmo.finance.operation;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.itmo.finance.common.OperationType;

public record CreateOperationRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) @Schema(example = "1250.50") BigDecimal amount,
        @NotNull OperationType type,
        @NotNull @Positive @Schema(example = "1") Long categoryId,
        @NotNull @Schema(example = "2026-11-27") LocalDate operationDate,
        @Size(max = 1000) @Schema(example = "Покупка продуктов") String description) {
}
