package com.avisto.jpasearch.exception;

/**
 * This exception is returned when a Sorter is called but the key of the sorter does not exist.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class CannotSortException extends JpaSearchException {
    public CannotSortException(String message) {
        super(message);
    }
}
