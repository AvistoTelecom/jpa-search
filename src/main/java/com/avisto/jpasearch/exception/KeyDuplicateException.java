package com.avisto.jpasearch.exception;

/**
 * This exception is called when you want to create two filters with the same key.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class KeyDuplicateException extends JpaSearchException {
    public KeyDuplicateException(String message) {
        super(message);
    }
}
