package ru.itmo.finance.operation;

import java.time.LocalDate;

import ru.itmo.finance.common.OperationType;

record OperationFilter(LocalDate dateFrom, LocalDate dateTo, OperationType type, Long categoryId, int page, int size) {
}
