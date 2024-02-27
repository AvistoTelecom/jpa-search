package com.avisto.genericspringsearch.exception;

/**
 * This exception is called when you try to filter a type that is not managed by our library.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class TypeNotHandledException extends GenericSearchException {
    public TypeNotHandledException(String message) {
        super(message);
    }
}
