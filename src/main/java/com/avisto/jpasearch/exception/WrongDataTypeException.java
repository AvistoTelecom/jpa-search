package com.avisto.jpasearch.exception;

/**
 * @author Gabriel Revelli
 * @version 1.0
 */
public class WrongDataTypeException extends JpaSearchException {
    public WrongDataTypeException(String message) {
        super(message);
    }
}
