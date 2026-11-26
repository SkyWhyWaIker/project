package ru.itmo.finance.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.itmo.finance.auth.CurrentUser;

@RestController
@RequestMapping("/api/v1/balance")
@Tag(name = "Баланс")
public class BalanceController {

    private final BalanceRepository repository;
    private final CurrentUser currentUser;

    public BalanceController(BalanceRepository repository, CurrentUser currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Получить текущий баланс", description = "Доходы минус расходы. Начальный баланс — 0 RUB.")
    public BalanceResponse getBalance(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return repository.calculateBalance(currentUser.requireId(jwt));
    }
}
