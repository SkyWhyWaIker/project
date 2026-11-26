package ru.itmo.finance.operation;

import java.util.List;

public record OperationPage(List<OperationResponse> items, int page, int size, long totalElements, long totalPages) {
}
