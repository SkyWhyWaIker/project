package ru.itmo.finance.operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import ru.itmo.finance.common.OperationType;

@Repository
class OperationRepository {

    private final JdbcClient jdbcClient;

    OperationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    OperationResponse insert(UUID userId, CreateOperationRequest request) {
        UUID operationId = UUID.randomUUID();
        jdbcClient.sql("""
                        INSERT INTO financial_operations
                            (id, user_id, amount, operation_type, category_id, operation_date, description)
                        VALUES (:id, :userId, :amount, :type, :categoryId, :operationDate, :description)
                        """)
                .param("id", operationId)
                .param("userId", userId)
                .param("amount", request.amount())
                .param("type", request.type().name())
                .param("categoryId", request.categoryId())
                .param("operationDate", request.operationDate())
                .param("description", request.description(), java.sql.Types.VARCHAR)
                .update();

        return jdbcClient.sql("""
                        SELECT operation.*, category.name AS category_name
                        FROM financial_operations operation
                        JOIN categories category ON category.id = operation.category_id
                        WHERE operation.id = :id AND operation.user_id = :userId
                        """)
                .param("id", operationId)
                .param("userId", userId)
                .query(this::mapOperation)
                .single();
    }

    OperationPage findByUserId(UUID userId, OperationFilter filter) {
        StringBuilder conditions = new StringBuilder(" WHERE operation.user_id = :userId");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", userId);

        if (filter.dateFrom() != null) {
            conditions.append(" AND operation.operation_date >= :dateFrom");
            parameters.put("dateFrom", filter.dateFrom());
        }
        if (filter.dateTo() != null) {
            conditions.append(" AND operation.operation_date <= :dateTo");
            parameters.put("dateTo", filter.dateTo());
        }
        if (filter.type() != null) {
            conditions.append(" AND operation.operation_type = :type");
            parameters.put("type", filter.type().name());
        }
        if (filter.categoryId() != null) {
            conditions.append(" AND operation.category_id = :categoryId");
            parameters.put("categoryId", filter.categoryId());
        }

        long totalElements = jdbcClient.sql("SELECT COUNT(*) FROM financial_operations operation" + conditions)
                .params(parameters)
                .query(Long.class)
                .single();

        List<OperationResponse> operations = jdbcClient.sql("""
                        SELECT operation.*, category.name AS category_name
                        FROM financial_operations operation
                        JOIN categories category ON category.id = operation.category_id
                        """ + conditions + """
                         ORDER BY operation.operation_date DESC, operation.created_at DESC, operation.id DESC
                         LIMIT :limit OFFSET :offset
                        """)
                .params(parameters)
                .param("limit", filter.size())
                .param("offset", (long) filter.page() * filter.size())
                .query(this::mapOperation)
                .list();

        long totalPages = totalElements / filter.size() + (totalElements % filter.size() == 0 ? 0 : 1);
        return new OperationPage(operations, filter.page(), filter.size(), totalElements, totalPages);
    }

    private OperationResponse mapOperation(ResultSet result, int rowNumber) throws SQLException {
        return new OperationResponse(result.getObject("id", UUID.class), result.getBigDecimal("amount"), "RUB",
                OperationType.valueOf(result.getString("operation_type")), result.getLong("category_id"),
                result.getString("category_name"), result.getDate("operation_date").toLocalDate(),
                result.getString("description"), result.getTimestamp("created_at").toInstant());
    }
}
