package com.avisto.genericspringsearch.exception;

/**
 * This exception is called when you try to access a value that is not in an Enum.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class ValueNotFoundInEnumException extends GenericSearchException {
    public ValueNotFoundInEnumException(String message) {
        super(message);
    }
}
