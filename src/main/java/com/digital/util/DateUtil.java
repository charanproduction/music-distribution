package com.digital.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    private static final DateTimeFormatter CSV_DATE =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public YearMonth parseToYearMonth(String raw) {

        if (raw == null)
            throw new IllegalArgumentException("Null date");

        raw = raw.trim().replace("\"", "");

        // yyyy/MM/dd
        try {
            LocalDate d = LocalDate.parse(raw, CSV_DATE);
            return YearMonth.of(d.getYear(), d.getMonth());
        } catch (DateTimeParseException ignored) {}

        // yyyy-MM-dd
        try {
            LocalDate d = LocalDate.parse(raw);
            return YearMonth.of(d.getYear(), d.getMonth());
        } catch (DateTimeParseException ignored) {}

        // yyyy-MM or yyyy/MM -> convert to yyyy-MM
        String normalized = raw.replace('/', '-');
        if (normalized.length() == 7) { // yyyy-MM
            return YearMonth.parse(normalized);
        }

        throw new IllegalArgumentException("Unsupported date: " + raw);
    }

    public String normalizeMonth(String raw) {
        return parseToYearMonth(raw).toString();   // yyyy-MM
    }

    public String toQuarter(YearMonth ym) {
        int q = ((ym.getMonthValue() - 1) / 3) + 1;
        return "Q" + q;
    }

    public String monthName(YearMonth ym) {
        return ym.getMonth()
                .getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH);
    }
}
