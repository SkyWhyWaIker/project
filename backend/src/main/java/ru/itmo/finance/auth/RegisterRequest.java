package ru.itmo.finance.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) @Schema(example = "user@example.com") String email,
        @NotBlank @Size(min = 8, max = 72) @Schema(format = "password", example = "StrongPassword123") String password) {
}
