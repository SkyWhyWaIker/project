package ru.itmo.finance.category;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import ru.itmo.finance.common.OperationType;

@Repository
public class CategoryRepository {

    private final JdbcClient jdbcClient;

    public CategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CategoryResponse> findAll(OperationType type) {
        String query = "SELECT id, name, operation_type FROM categories";
        JdbcClient.StatementSpec statement = type == null
                ? jdbcClient.sql(query + " ORDER BY id")
                : jdbcClient.sql(query + " WHERE operation_type = :type ORDER BY id").param("type", type.name());

        return statement.query((result, rowNumber) -> new CategoryResponse(result.getLong("id"),
                result.getString("name"), OperationType.valueOf(result.getString("operation_type")))).list();
    }

    public Optional<CategoryResponse> findById(long categoryId) {
        return jdbcClient.sql("SELECT id, name, operation_type FROM categories WHERE id = :categoryId")
                .param("categoryId", categoryId)
                .query((result, rowNumber) -> new CategoryResponse(result.getLong("id"),
                        result.getString("name"), OperationType.valueOf(result.getString("operation_type"))))
                .optional();
    }
}
