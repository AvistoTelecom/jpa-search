package com.avisto.genericspringsearch.exception;

/**
 * Returns an exception when the search method has apply filter to an invalid field path.
 */
public class FieldPathNotFoundException extends GenericSearchException {

    /**
     * Display the exception with the message passed in param
     * @param message Message to display when the exception is display
     */
    public FieldPathNotFoundException(String message) {
        super(message);
    }
}
