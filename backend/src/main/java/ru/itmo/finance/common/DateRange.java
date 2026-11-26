package ru.itmo.finance.common;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;

public record DateRange(LocalDate dateFrom, LocalDate dateTo) {

    public static DateRange forAnalytics(LocalDate dateFrom, LocalDate dateTo, Clock clock) {
        if (dateFrom == null && dateTo == null) {
            LocalDate today = LocalDate.now(clock);
            return new DateRange(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
        }

        if (dateFrom == null || dateTo == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите обе границы периода: dateFrom и dateTo");
        }

        validateOrder(dateFrom, dateTo);
        return new DateRange(dateFrom, dateTo);
    }

    public static void validateOrder(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateFrom не может быть позже dateTo");
        }
    }
}
