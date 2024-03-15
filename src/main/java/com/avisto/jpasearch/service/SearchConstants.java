package com.avisto.jpasearch.service;

/**
 * This class contains the constants used in the library.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public final class SearchConstants {
    private SearchConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String JPA_SEARCH_PACKAGE_NAME = "com.avisto.jpasearch";

    public static final class Strings {
        private Strings() {
            throw new IllegalStateException("Utility class");
        }

        public static final String EMPTY_STRING = "";
        public static final String DOT = ".";
        public static final String REGEX_DOT = "[.]";
        public static final String COMMA = ",";
        public static final String SPACE = " ";
    }

    public static final class ClassNames {
        private ClassNames() {
            throw new IllegalStateException("Utility class");
        }

        public static final String FLOAT = "Float";
        public static final String PRIMITIVE_FLOAT = "float";
        public static final String STRING = "String";
        public static final String UUID = "UUID";
        public static final String BIG_DECIMAL = "BigDecimal";
        public static final String INTEGER = "Integer";
        public static final String PRIMITIVE_INTEGER = "int";
        public static final String LOCAL_DATE = "LocalDate";
        public static final String LOCAL_DATE_TIME = "LocalDateTime";
        public static final String ZONED_DATE_TIME = "ZonedDateTime";
        public static final String BOOLEAN = "Boolean";
        public static final String PRIMITIVE_BOOLEAN = "boolean";
        public static final String DOUBLE = "Double";
        public static final String PRIMITIVE_DOUBLE = "double";
        public static final String LONG = "Long";
        public static final String PRIMITIVE_LONG = "long";
        public static final String LIST = "List";
        public static final String MAP = "Map";
    }

    public static final class Patterns {
        private Patterns() {
            throw new IllegalStateException("Utility class");
        }

        public static final String GENERIC_ISO_DATETIME_REGEX = "(\\d{4})-(\\d{2})-(\\d{2})(?:T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d+))?(Z|[+-]\\d{2}:\\d{2})?.*)?";
    }

    public static final class KeyWords {
        private KeyWords() {
            throw new IllegalStateException("Utility class");
        }

        public static final String PAGE = "page";
        public static final String SIZE = "size";
        public static final String SORTS = "sorts";
    }
}
