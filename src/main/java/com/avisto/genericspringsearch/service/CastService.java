package com.avisto.genericspringsearch.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avisto.genericspringsearch.exception.TypeNotHandledException;
import com.avisto.genericspringsearch.exception.ValueNotFoundInEnumException;
import com.avisto.genericspringsearch.exception.WrongDateFormatException;

import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.BIG_DECIMAL;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.BOOLEAN;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.DOUBLE;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.FLOAT;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.INTEGER;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.LOCAL_DATE;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.LOCAL_DATE_TIME;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.LONG;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.PRIMITIVE_BOOLEAN;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.PRIMITIVE_DOUBLE;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.PRIMITIVE_FLOAT;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.PRIMITIVE_INTEGER;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.PRIMITIVE_LONG;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.STRING;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.UUID;
import static com.avisto.genericspringsearch.service.SearchConstants.ClassNames.ZONED_DATE_TIME;
import static com.avisto.genericspringsearch.service.SearchConstants.Patterns.GENERIC_ISO_DATETIME_REGEX;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.COMMA;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.DOT;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.EMPTY_STRING;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.SPACE;

public class CastService {
    private CastService() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern TEMPORAL_REGEX = Pattern.compile(GENERIC_ISO_DATETIME_REGEX);

    public static <X> X cast(String value, Class<X> clazz) {
        return cast(value, clazz, null);
    }

    public static Double castToDouble(String value) {
        return cast(value, Double.class);
    }

    @SuppressWarnings("unchecked")
    public static <X> X cast(String value, Class<X> clazz, String datePattern) {
        DateTimeFormatter formatter = null;
        if (datePattern != null) {
            formatter = DateTimeFormatter.ofPattern(datePattern);
        }
        if (value == null || value.equals(EMPTY_STRING)) {
            return null;
        }
        if (clazz.isEnum()) {
            return Arrays.stream(clazz.getEnumConstants())
                    .filter(e -> ((Enum<?>) e).name().equals(value))
                    .findFirst()
                    .orElseThrow(() -> new ValueNotFoundInEnumException(String.format("Value %s not found in enum %s", value, clazz.getSimpleName())));
        }
        return (X) switch (clazz.getSimpleName()) {
            case STRING -> value;
            case UUID -> java.util.UUID.fromString(value);
            case BIG_DECIMAL -> new BigDecimal(value.replace(COMMA, DOT));
            case PRIMITIVE_FLOAT, FLOAT -> Float.valueOf(value.replace(COMMA, DOT));
            case PRIMITIVE_INTEGER, INTEGER -> Integer.valueOf(value.replaceAll(SPACE, EMPTY_STRING));
            case PRIMITIVE_LONG, LONG -> Long.valueOf(value);
            case PRIMITIVE_DOUBLE, DOUBLE -> Double.parseDouble(value.replace(COMMA, DOT).replaceAll(SPACE, EMPTY_STRING));
            case PRIMITIVE_BOOLEAN, BOOLEAN -> Boolean.valueOf(value);
            case LOCAL_DATE -> formatter == null ? convertTo(value, LocalDate.class) : LocalDate.parse(value, formatter);
            case LOCAL_DATE_TIME -> formatter == null ? convertTo(value, LocalDateTime.class) : LocalDate.parse(value, formatter).atStartOfDay();
            case ZONED_DATE_TIME -> formatter == null ? convertTo(value, ZonedDateTime.class) : LocalDate.parse(value, formatter).atStartOfDay().atZone(ZoneOffset.UTC);
            default -> throw new TypeNotHandledException(String.format("Cannot cast String to type %s : Type not handled", clazz.getSimpleName()));
        };
    }

    private static <X> Temporal convertTo(String value, Class<X> clazz) {
        Matcher m = TEMPORAL_REGEX.matcher(value);
        if (!m.matches()) {
            throw new WrongDateFormatException(String.format("Date %s does not match ISO DateTime format", value));
        }

        int year = m.group(1) == null ? 0 : Integer.parseInt(m.group(1));
        int month = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        int day = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        int hours = m.group(4) == null ? 0 : Integer.parseInt(m.group(4));
        int minutes = m.group(5) == null ? 0 : Integer.parseInt(m.group(5));
        int seconds = m.group(6) == null ? 0 : Integer.parseInt(m.group(6));
        int nanoSeconds = m.group(7) == null ? 0 : Integer.parseInt(m.group(7));
        ZoneOffset offset = m.group(8) == null ? ZoneOffset.UTC : ZoneOffset.of(m.group(8));


        if (clazz == LocalDateTime.class) {
            return LocalDateTime.of(year, month, day, hours, minutes, seconds, nanoSeconds);
        } else if (clazz == LocalDate.class) {
            return LocalDate.of(year, month, day);
        } else if (clazz == ZonedDateTime.class) {
            return ZonedDateTime.of(year, month, day, hours, minutes, seconds, nanoSeconds, offset);
        }
        throw new TypeNotHandledException(String.format("Cannot cast String to type %s : Type not handled", clazz.getSimpleName()));
    }
}

