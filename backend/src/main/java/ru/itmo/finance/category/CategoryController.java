package ru.itmo.finance.category;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.itmo.finance.common.OperationType;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Категории")
public class CategoryController {

    private final CategoryRepository repository;

    public CategoryController(CategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Получить предустановленные категории", description = "Необязательный фильтр type: INCOME или EXPENSE")
    public List<CategoryResponse> listCategories(@RequestParam(required = false) OperationType type) {
        return repository.findAll(type);
    }
}
