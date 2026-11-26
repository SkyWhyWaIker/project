package ru.itmo.finance.auth;

import java.util.UUID;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UUID userId, String email) {
}
