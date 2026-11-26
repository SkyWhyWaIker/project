package ru.itmo.finance.auth;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.itmo.finance.common.ApiException;

@Service
class AuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration tokenLifetime;
    private final String issuer;
    private final String dummyPasswordHash;

    AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
                Clock clock, @Value("${app.jwt.lifetime}") Duration tokenLifetime,
                @Value("${app.jwt.issuer}") String issuer) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.tokenLifetime = tokenLifetime;
        this.issuer = issuer;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validatePasswordLength(request.password());
        UserAccount account = new UserAccount(UUID.randomUUID(), normalizeEmail(request.email()),
                passwordEncoder.encode(request.password()));

        try {
            repository.save(account);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
        }

        return createToken(account);
    }

    public AuthResponse login(LoginRequest request) {
        validatePasswordLength(request.password());
        UserAccount account = repository.findByEmail(normalizeEmail(request.email())).orElse(null);
        String passwordHash = account == null ? dummyPasswordHash : account.passwordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (account == null || !passwordMatches) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }

        return createToken(account);
    }

    private AuthResponse createToken(UserAccount account) {
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(account.id().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(tokenLifetime))
                .id(UUID.randomUUID().toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AuthResponse(token, "Bearer", tokenLifetime.toSeconds(), account.id(), account.email());
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Пароль должен занимать не более 72 байт в UTF-8");
        }
    }
}
