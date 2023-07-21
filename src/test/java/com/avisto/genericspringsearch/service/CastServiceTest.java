package com.avisto.genericspringsearch.service;

import com.avisto.genericspringsearch.exception.TypeNotHandledException;
import com.avisto.genericspringsearch.exception.ValueNotFoundInEnumException;
import com.avisto.genericspringsearch.exception.WrongDateFormatException;
import com.avisto.genericspringsearch.model.TestEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

public class CastServiceTest {

    @Test
    public void testCastToString() {
        String value = "Test String";
        String castedValue = CastService.cast(value, String.class);
        assertEquals(value, castedValue);
    }

    @Test
    public void testCastToDouble() {
        String value = "123.45";
        Double expectedValue = 123.45;
        Double castedValue = CastService.cast(value, Double.class);
        assertEquals(expectedValue, castedValue);
    }

    @Test
    public void testCastToDouble_InvalidValue() {
        String value = "123.45.67"; // Invalid double value

        assertThrows(IllegalArgumentException.class, () -> CastService.cast(value, Double.class));
    }

    @Test
    public void testCastToEnum() {
        String value = "TEST2";
        TestEnum expectedEnum = TestEnum.TEST2;

        TestEnum castedValue = CastService.cast(value, TestEnum.class);
        assertEquals(expectedEnum, castedValue);
    }

    @Test
    public void testCastToEnum_InvalidValue() {
        String value = "INVALID_VALUE";

        assertThrows(ValueNotFoundInEnumException.class, () -> CastService.cast(value, TestEnum.class));
    }

    @Test
    public void testCastToCustomClass_LocalDate() {
        String value = "2023-07-21";
        LocalDate expectedDate = LocalDate.of(2023, 7, 21);

        LocalDate castedValue = CastService.cast(value, LocalDate.class);
        assertEquals(expectedDate, castedValue);
    }

    @Test
    public void testCastToCustomClass_LocalDateTime() {
        String value = "2023-07-21T12:34:56";
        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 7, 21, 12, 34, 56);

        LocalDateTime castedValue = CastService.cast(value, LocalDateTime.class);
        assertEquals(expectedDateTime, castedValue);
    }

    @Test
    public void testCastToCustomClass_ZonedDateTime() {
        String value = "2023-07-21T12:34:56Z";
        ZonedDateTime expectedZonedDateTime = ZonedDateTime.of(2023, 7, 21, 12, 34, 56, 0, ZoneOffset.UTC);

        ZonedDateTime castedValue = CastService.cast(value, ZonedDateTime.class);
        assertEquals(expectedZonedDateTime, castedValue);
    }

    @Test
    public void testCastToCustomClass_WrongDateFormat() {
        String value = "2023/07/21";

        assertThrows(WrongDateFormatException.class, () ->  CastService.cast(value, LocalDate.class));
    }

    @Test
    public void testCast_NullValue() {
        String value = null;
        String castedValue = CastService.cast(value, String.class);
        assertNull(castedValue);
    }

    @Test
    public void testCastToBigDecimal() {
        String value = "123.45";
        BigDecimal expectedBigDecimal = new BigDecimal("123.45");

        BigDecimal castedValue = CastService.cast(value, BigDecimal.class);
        assertEquals(expectedBigDecimal, castedValue);
    }

    @Test
    public void testCastToBigDecimal_InvalidValue() {
        String value = "123.45.67"; // Invalid BigDecimal value

        assertThrows(NumberFormatException.class, () -> CastService.cast(value, BigDecimal.class));
    }
    @Test
    public void testCastToFloat() {
        String value = "123.45";
        Float expectedFloat = 123.45f;

        Float castedValue = CastService.cast(value, Float.class);
        assertEquals(expectedFloat, castedValue);
    }

    @Test
    public void testCastToFloat_InvalidValue() {
        String value = "123.45.67"; // Invalid float value

        assertThrows(NumberFormatException.class, () -> CastService.cast(value, Float.class));
    }

    @Test
    public void testCastToInteger() {
        String value = "123";
        Integer expectedInteger = 123;

        Integer castedValue = CastService.cast(value, Integer.class);
        assertEquals(expectedInteger, castedValue);
    }

    @Test
    public void testCastToInteger_InvalidValue() {
        String value = "123.45"; // Invalid integer value

        assertThrows(NumberFormatException.class, () -> CastService.cast(value, Integer.class));
    }

    @Test
    public void testCastToLong() {
        String value = "123";
        Long expectedLong = 123L;

        Long castedValue = CastService.cast(value, Long.class);
        assertEquals(expectedLong, castedValue);
    }

    @Test
    public void testCastToLong_InvalidValue() {
        String value = "123.45"; // Invalid long value

        assertThrows(NumberFormatException.class, () -> CastService.cast(value, Long.class));
    }

    @Test
    public void testCastToBoolean() {
        String trueValue = "true";
        String falseValue = "false";

        assertTrue(CastService.cast(trueValue, Boolean.class));
        assertFalse(CastService.cast(falseValue, Boolean.class));
    }

    @Test
    public void testCastToBoolean_InvalidValue() {
        String value = "InvalidValue"; // Invalid boolean value

        assertFalse(CastService.cast(value, Boolean.class));
    }

    @Test
    public void testCastToUUID() {
        String value = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        UUID expectedUUID = UUID.fromString(value);

        UUID castedValue = CastService.cast(value, UUID.class);
        assertEquals(expectedUUID, castedValue);
    }

    @Test
    public void testCastToUUID_InvalidValue() {
        String value = "InvalidUUID"; // Invalid UUID value

        assertThrows(IllegalArgumentException.class, () -> CastService.cast(value, UUID.class));
    }

    @Test
    public void testCastToByte_InvalidValue() {
        String value = "256"; // Invalid byte value

        assertThrows(TypeNotHandledException.class, () -> CastService.cast(value, byte.class));
    }

    @Test
    public void testCastToShort_InvalidValue() {
        String value = "32768"; // Invalid short value

        assertThrows(TypeNotHandledException.class, () -> CastService.cast(value, short.class));
    }

    @Test
    public void testCastToChar_InvalidValue() {
        String value = "AB"; // Invalid char value

        assertThrows(TypeNotHandledException.class, () -> CastService.cast(value, char.class));
    }
}

