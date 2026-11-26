package ru.itmo.finance.operation;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.itmo.finance.auth.CurrentUser;
import ru.itmo.finance.common.OperationType;

@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Операции")
public class OperationController {

    private final OperationService service;
    private final CurrentUser currentUser;

    public OperationController(OperationService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить доход или расход", description = "Сумма положительная, не более двух знаков после запятой. Валюта RUB.")
    public OperationResponse createOperation(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody CreateOperationRequest request) {
        return service.createOperation(currentUser.requireId(jwt), request);
    }

    @GetMapping
    @Operation(summary = "Получить свои операции", description = "Сначала новые. Границы дат включаются. Нумерация страниц с нуля.")
    public OperationPage listOperations(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) OperationType type,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listOperations(currentUser.requireId(jwt),
                new OperationFilter(dateFrom, dateTo, type, categoryId, page, size));
    }
}
