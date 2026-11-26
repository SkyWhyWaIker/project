package ru.itmo.finance.auth;

import java.util.UUID;

record UserAccount(UUID id, String email, String passwordHash) {
}
