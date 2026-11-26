package ru.itmo.finance.category;

import ru.itmo.finance.common.OperationType;

public record CategoryResponse(long id, String name, OperationType type) {
}
