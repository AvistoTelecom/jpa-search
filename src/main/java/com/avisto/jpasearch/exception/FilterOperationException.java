package com.avisto.jpasearch.exception;

/**
 * This exception is called when you try to compare two objects of different types.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FilterOperationException extends JpaSearchException {
    public FilterOperationException(String message) {
        super(message);
    }
}
