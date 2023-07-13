package com.avisto.genericspringsearch.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import com.avisto.genericspringsearch.exception.FieldPathNotFoundException;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.EMPTY_STRING;

public final class SearchUtils {

    private SearchUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern STRIP_ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static Class<?> getEntityClass(Class<?> clazz, String[] paths) {
        if (paths.length == 0) {
            return clazz;
        }
        if (paths[0].contains("[")) {
            paths = Arrays.stream(paths).map(s -> s.split("\\[|\\]")).flatMap(Arrays::stream).toArray(String[]::new);
            return getEntityClass(getFieldCollectionClass(clazz, paths[0]), Arrays.copyOfRange(paths, 1, paths.length));
        } else {
            return getEntityClass(getFieldClass(clazz, paths[0]), Arrays.copyOfRange(paths, 1, paths.length));
        }
    }

    static Class<?> getFieldClass(Class<?> clazz, String value) {
        return getField(clazz, value).getType();
    }

    static Field getField(Class<?> clazz, String value) {
        if (clazz == null) {
            throw new FieldPathNotFoundException(String.format("Field path %s not found", value));
        }
        try {
            return clazz.getDeclaredField(value);
        } catch (NoSuchFieldException e) {
            return getField(clazz.getSuperclass(), value);
        }
    }


    static Class<?> getFieldCollectionClass(Class<?> clazz, String value) {
        if (clazz == null) {
            throw new FieldPathNotFoundException(String.format("Field path %s not found", value));
        }
        try {
            ParameterizedType stringListType = (ParameterizedType) clazz.getDeclaredField(value).getGenericType();
            return (Class<?>) stringListType.getActualTypeArguments()[0];
        } catch (NoSuchFieldException e) {
            return getFieldClass(clazz.getSuperclass(), value);
        }
    }

    public static String normalizeAccentsAndDashes(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{Pd}", "-");
        return stripAccents(normalized);
    }

    public static String toRootLowerCase(final String source) {
        return source == null ? null : source.toLowerCase(Locale.ROOT);
    }

    public static String stripAccents(final String input) {
        if (input == null) {
            return null;
        }
        final StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        convertRemainingAccentCharacters(decomposed);
        // Note that this doesn't correctly remove ligatures...
        return STRIP_ACCENTS_PATTERN.matcher(decomposed).replaceAll(EMPTY_STRING);
    }

    private static void convertRemainingAccentCharacters(final StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == '\u0141') {
                decomposed.setCharAt(i, 'L');
            } else if (decomposed.charAt(i) == '\u0142') {
                decomposed.setCharAt(i, 'l');
            }
        }
    }

    public static boolean isBlank(final String source) {
        if (source == null || source.isEmpty()) {
            return true;
        }
        for (char c : source.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
}
