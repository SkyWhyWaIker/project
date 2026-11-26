package ru.itmo.finance.analytics;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.itmo.finance.auth.CurrentUser;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Аналитика")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    public AnalyticsController(AnalyticsService analyticsService, CurrentUser currentUser) {
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/expenses")
    @Operation(summary = "Получить распределение расходов по категориям",
            description = "Без дат — текущий календарный месяц в Europe/Moscow. Для другого периода укажите обе даты. "
                    + "Границы включаются. Категории отсортированы по убыванию расходов; доли округлены до двух знаков.")
    public ExpenseAnalyticsResponse getExpenses(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return analyticsService.getExpenses(currentUser.requireId(jwt), dateFrom, dateTo);
    }
}
