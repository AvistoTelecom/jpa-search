package com.avisto.genericspringsearch.service;

import com.avisto.genericspringsearch.exception.FieldPathNotFoundException;
import com.avisto.genericspringsearch.model.TestEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchUtilsTest {
    @Test
    public void testGetEntityClass() {
        // Test a simple field path
        String[] paths = {"field1"};
        Class<?> entityClass = SearchUtils.getEntityClass(TestEntity.class, paths);
        assertEquals(String.class, entityClass);

        // Test a nested collection field path
        String[] nestedListPaths = {"nestedList[nestedField]"};
        Class<?> nestedListEntityClass = SearchUtils.getEntityClass(TestEntity.class, nestedListPaths);
        assertEquals(Integer.class, nestedListEntityClass);

        // Test a nested field path
        String[] nestedPaths = {"nestedEntity", "nestedField"};
        Class<?> nestedEntityClass = SearchUtils.getEntityClass(TestEntity.class, nestedPaths);
        assertEquals(Integer.class, nestedEntityClass);
    }

    @Test
    public void testGetEntityClass_FieldNotFound() {
        // Test a field path that does not exist in the class structure
        String[] invalidPaths = {"nonExistentField"};
        assertThrows(FieldPathNotFoundException.class, () -> SearchUtils.getEntityClass(TestEntity.class, invalidPaths));
    }

    @Test
    public void testNormalizeAccentsAndDashes() {
        String input = "Café & Château";
        String expectedOutput = "Cafe & Chateau";

        String normalized = SearchUtils.normalizeAccentsAndDashes(input);
        assertEquals(expectedOutput, normalized);
    }

    @Test
    public void testToRootLowerCase() {
        String input = "This IS A tEST String";
        String expectedOutput = "this is a test string";

        String lowercase = SearchUtils.toRootLowerCase(input);
        assertEquals(expectedOutput, lowercase);
    }

    @Test
    public void testStripAccents() {
        String input = "Děkujeme, že jste nás navštívili";
        String expectedOutput = "Dekujeme, ze jste nas navstivili";

        String stripped = SearchUtils.stripAccents(input);
        assertEquals(expectedOutput, stripped);
    }

    @Test
    public void testIsBlank() {
        assertTrue(SearchUtils.isBlank(null));
        assertTrue(SearchUtils.isBlank(""));
        assertTrue(SearchUtils.isBlank("     "));
        assertFalse(SearchUtils.isBlank("   hello   "));
    }

    @Test
    public void testIsBlank_NullString() {
        // Test with a null string
        String nullString = null;
        assertTrue(SearchUtils.isBlank(nullString));
    }
}
