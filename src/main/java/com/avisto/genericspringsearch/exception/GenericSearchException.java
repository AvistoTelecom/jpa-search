package com.avisto.genericspringsearch.exception;

/**
 * This class is used to prioritize errors in our project.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class GenericSearchException extends RuntimeException {
    public GenericSearchException(String message) {
        super(message);
    }
}
