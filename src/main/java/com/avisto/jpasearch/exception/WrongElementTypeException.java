package com.avisto.jpasearch.exception;

/**
 * @author Gabriel Revelli
 * @version 1.0
 */
public class WrongElementTypeException extends JpaSearchException {
    public WrongElementTypeException(String message) {
        super(message);
    }
}
