package com.avisto.genericspringsearch.exception;

/**
 * This exception is returned when a Sorter is called but the key of the sorter does not exist.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class CannotSortException extends GenericSearchException {

    /**
     * Display the exception with the message passed in param
     * @param message Message to display when the exception is display
     */
    public CannotSortException(String message) {
        super(message);
    }
}
