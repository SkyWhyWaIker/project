package ru.itmo.finance.operation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import ru.itmo.finance.category.CategoryRepository;
import ru.itmo.finance.category.CategoryResponse;
import ru.itmo.finance.common.ApiException;
import ru.itmo.finance.common.DateRange;

@Service
class OperationService {

    private final OperationRepository repository;
    private final CategoryRepository categoryRepository;

    OperationService(OperationRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public OperationResponse createOperation(UUID userId, CreateOperationRequest request) {
        CategoryResponse category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Категория не найдена"));

        if (category.type() != request.type()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Тип категории не соответствует типу операции");
        }

        return repository.insert(userId, request);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public OperationPage listOperations(UUID userId, OperationFilter filter) {
        DateRange.validateOrder(filter.dateFrom(), filter.dateTo());
        return repository.findByUserId(userId, filter);
    }
}
