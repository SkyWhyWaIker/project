package ru.itmo.finance.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class UserAccountRepository {

    private final JdbcClient jdbcClient;

    UserAccountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    Optional<UserAccount> findByEmail(String email) {
        return jdbcClient.sql("SELECT id, email, password_hash FROM app_users WHERE email = :email")
                .param("email", email)
                .query((result, rowNumber) -> new UserAccount(
                        result.getObject("id", UUID.class), result.getString("email"), result.getString("password_hash")))
                .optional();
    }

    boolean existsById(UUID userId) {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM app_users WHERE id = :userId)")
                .param("userId", userId)
                .query(Boolean.class)
                .single();
    }

    void save(UserAccount account) {
        jdbcClient.sql("INSERT INTO app_users (id, email, password_hash) VALUES (:id, :email, :passwordHash)")
                .param("id", account.id())
                .param("email", account.email())
                .param("passwordHash", account.passwordHash())
                .update();
    }
}
