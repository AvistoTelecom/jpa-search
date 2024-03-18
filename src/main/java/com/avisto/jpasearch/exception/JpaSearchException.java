package com.avisto.jpasearch.exception;

/**
 * This class is used to prioritize errors in our project.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class JpaSearchException extends RuntimeException {
    public JpaSearchException(String message) {
        super(message);
    }
}
