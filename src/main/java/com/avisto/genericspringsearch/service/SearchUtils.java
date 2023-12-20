package com.avisto.genericspringsearch.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.Key;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.avisto.genericspringsearch.SearchableEntity;
import com.avisto.genericspringsearch.config.IFilterConfig;
import com.avisto.genericspringsearch.config.ISearchConfig;
import com.avisto.genericspringsearch.config.ISearchCriteriaConfig;
import com.avisto.genericspringsearch.config.ISorterConfig;
import com.avisto.genericspringsearch.exception.CannotSortException;
import com.avisto.genericspringsearch.exception.EmptyCriteriaException;
import com.avisto.genericspringsearch.exception.FieldNotInCriteriaException;
import com.avisto.genericspringsearch.exception.FieldPathNotFoundException;
import com.avisto.genericspringsearch.exception.KeyDuplicateException;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import static com.avisto.genericspringsearch.service.SearchConstants.Strings.EMPTY_STRING;
import static com.avisto.genericspringsearch.service.SearchConstants.Strings.REGEX_DOT;

/**
 * Utility class containing helper methods for generic Spring search functionality.
 * @author Gabriel Revelli
 * @version 1.0
 */
public final class SearchUtils {

    private SearchUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern STRIP_ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Get the entity class for a given class and an array of field paths.
     *
     * @param clazz The base class to start the search.
     * @param paths An array of field paths to navigate through the class structure.
     * @return The entity class that corresponds to the final field path in the array.
     * @throws FieldPathNotFoundException If a field path is not found in the class structure.
     */
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

    /**
     * Get the class of a specific field for a given class and field name.
     *
     * @param clazz The class to search for the field.
     * @param value The name of the field.
     * @return The class representing the type of the field.
     */
    static Class<?> getFieldClass(Class<?> clazz, String value) {
        return getField(clazz, value).getType();
    }

    /**
     * Get the field object for a given class and field name.
     *
     * @param clazz The class to search for the field.
     * @param value The name of the field.
     * @return The {@link Field} object representing the field.
     * @throws FieldPathNotFoundException If the field path is not found in the class structure.
     */
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

    /**
     * Get the class of the elements in a collection field for a given class and field name.
     *
     * @param clazz The class to search for the collection field.
     * @param value The name of the collection field.
     * @return The class representing the type of elements in the collection field.
     * @throws FieldPathNotFoundException If the collection field path is not found in the class structure.
     */
    static Class<?> getFieldCollectionClass(Class<?> clazz, String value) {
        if (clazz == null) {
            throw new FieldPathNotFoundException(String.format("Field path %s not found", value));
        }
        ParameterizedType stringListType = (ParameterizedType) getField(clazz, value).getGenericType();
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    /**
     * Normalize accents and dashes in a given input string.
     *
     * @param input The input string to be normalized.
     * @return The normalized string with accents and dashes replaced.
     */
    public static String normalizeAccentsAndDashes(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{Pd}", "-");
        return stripAccents(normalized);
    }

    /**
     * Convert a string to lowercase using the root locale.
     *
     * @param source The input string to be converted to lowercase.
     * @return The string converted to lowercase.
     */
    public static String toRootLowerCase(final String source) {
        return source == null ? null : source.toLowerCase(Locale.ROOT);
    }

    /**
     * Strip accents from a given input string.
     *
     * @param input The input string from which accents will be removed.
     * @return The string with accents removed.
     */
    public static String stripAccents(final String input) {
        if (input == null) {
            return null;
        }
        final StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        convertRemainingAccentCharacters(decomposed);
        // Note that this doesn't correctly remove ligatures...
        return STRIP_ACCENTS_PATTERN.matcher(decomposed).replaceAll(EMPTY_STRING);
    }

    /**
     * Convert remaining accent characters in the input string.
     *
     * @param decomposed The {@link StringBuilder} containing the decomposed characters.
     */
    private static void convertRemainingAccentCharacters(final StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == '\u0141') {
                decomposed.setCharAt(i, 'L');
            } else if (decomposed.charAt(i) == '\u0142') {
                decomposed.setCharAt(i, 'l');
            }
        }
    }

    /**
     * Check if a given string is blank (empty or contains only whitespace characters).
     *
     * @param source The input string to check.
     * @return {@code true} if the string is blank, {@code false} otherwise.
     */
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


    /**
     * Get specific SearchConfig by its key and cast it to Filter or Sort
     *
     * @param configurations Config elements to search in.
     * @param key The key of search config.
     * @param castClass The class of cast needed.
     * @return The search config with the key mentioned in params.
     */
    public static <E extends Enum<?> & ISearchCriteriaConfig<?>, T extends ISearchConfig<?>> T getSearchConfig(E[] configurations, String key, Class<T> castClass) {
        return Arrays.stream(configurations).
                filter(e -> Objects.equals(key,e.getKey()) && castClass.isAssignableFrom(e.getSearchConfig().getClass())).
                map(e -> (T) e.getSearchConfig()).
                findFirst().
                orElseThrow(() -> new FieldNotInCriteriaException(String.format("Field %s is not specified in criteria", key)));
    }

    /**
     * Get path from Root with String path
     *
     * @param from Root or From.
     * @param fieldPath String field path.
     * @return The Path of a field from Root.
     */
    public static <R extends SearchableEntity> Path<String> getPath(From<R, ?> from, String fieldPath) {
        if (SearchUtils.isBlank(fieldPath)) {
            return (From<R, String>) from;
        }
        String[] paths = fieldPath.split(REGEX_DOT);
        Path<String> entityPath = null;
        for (String path : paths) {
            if (entityPath == null) {
                entityPath = from.get(path);
            } else {
                entityPath = entityPath.get(path);
            }
        }
        return entityPath;
    }

    /**
     * Check if the configuration criteria is well declared
     *
     * @param configClazz criteria config to check.
     */
    public static <R extends SearchableEntity, E extends Enum<E> & ISearchCriteriaConfig<R>> void checkCriteriaConfig(Class<E> configClazz) {
        E[] configurations = configClazz.getEnumConstants();
        if (configurations.length == 0) {
            throw new EmptyCriteriaException("Criteria needs at least one configuration");
        }
        E firstConfiguration = configurations[0];
        Class<R> rootClazz = firstConfiguration.getRootClass();
        Set<String> filterKeys = new HashSet<>();
        Set<String> sorterKeys = new HashSet<>();
        Arrays.stream(configurations).forEach(configuration -> {
            ISearchConfig<R> searchConfig = configuration.getSearchConfig();
            if (IFilterConfig.class.isAssignableFrom(searchConfig.getClass())
                    && !filterKeys.add(searchConfig.getKey())) {
                throw new KeyDuplicateException("Cannot have multiple filters with the same key");
            }
            if (ISorterConfig.class.isAssignableFrom(searchConfig.getClass())
                    && !sorterKeys.add(searchConfig.getKey())) {
                throw new KeyDuplicateException("Cannot have multiple sorters with the same key");
            }
            configuration.getSearchConfig().checkConfig(rootClazz);
        });
        if (!sorterKeys.contains(firstConfiguration.getDefaultOrderCriteria().getKey())){
            throw new CannotSortException("Default sort key is not defined as a sorter in ISearchCriteriaConfig");
        }
    }
}
