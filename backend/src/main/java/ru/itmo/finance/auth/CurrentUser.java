package ru.itmo.finance.auth;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import ru.itmo.finance.common.ApiException;

@Component
public class CurrentUser {

    private final UserAccountRepository repository;

    public CurrentUser(UserAccountRepository repository) {
        this.repository = repository;
    }

    public UUID requireId(Jwt jwt) {
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Некорректный токен пользователя");
        }

        if (!repository.existsById(userId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден");
        }

        return userId;
    }
}
