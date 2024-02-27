package com.avisto.genericspringsearch.exception;

/**
 * Returns an exception when the search method has applied a filter to an invalid field path.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FieldPathNotFoundException extends GenericSearchException {
    public FieldPathNotFoundException(String message) {
        super(message);
    }
}
