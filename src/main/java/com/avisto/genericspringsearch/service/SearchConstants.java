package com.avisto.genericspringsearch.service;

public final class SearchConstants {

    private SearchConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final class Strings {

        private Strings() {
            throw new IllegalStateException("Utility class");
        }
        public static final String EMPTY_STRING = "";
        public static final String DOT = ".";
        public static final String DASH = "-";
        public static final String REGEX_DOT = "[.]";
        public static final String COMMA = ",";
        public static final String SLASH = "/";
        public static final String SPACE = " ";
        public static final String AMPERSAND = "&";
        public static final String QUESTION_MARK = "?";
        public static final String TABULATION = "\t";
        public static final String NEW_LINE = "\n";
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
    }

    public static final class Patterns {
        private Patterns() {
            throw new IllegalStateException("Utility class");
        }

        public static final String FRENCH_DATE = "dd/MM/yyyy";
        public static final String ISO_DATE = "yyyy-MM-dd";
        public static final String DATE_TIME_FILE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
        public static final String GENERIC_ISO_DATETIME_REGEX = "(\\d{4})-(\\d{2})-(\\d{2})(?:T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d+))?(Z|[+-]\\d{2}:\\d{2})?.*)?";

        // Source : https://www.baeldung.com/java-date-regular-expressions
        public static final String ISO_DATE_REGEX = "^((2000|2400|2800|(19|2[0-9])(0[48]|[2468][048]|[13579][26]))-02-29)$"
                + "|^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$"
                + "|^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$"
                + "|^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$";

        public static final String EMAIL = "^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+$";

        public static final String NUMERIC = "^[0-9]*$";

        public static final String REGEX_ALLOWED_CHARACTERS = "^[-a-zA-ZÀ-ÿ0-9' !\\\"#$£€%&()\\[\\]*+`./:;<>=?@^¨_,{}|~µ²¤§]*$";
        public static final String REGEX_ALLOWED_IMAGES = "^data:image\\/(png|jpg|jpeg|gif);base64,.*$";
    }

    public static final class KeyWords {
        private KeyWords() {
            throw new IllegalStateException("Utility class");
        }

        public static final String PAGE = "page";
        public static final String SIZE = "size";
        public static final String SORTS = "sorts";
        public static final String ASC = "asc";
        public static final String DESC = "desc";
    }
}
