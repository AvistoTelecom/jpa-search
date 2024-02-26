package com.avisto.genericspringsearch.exception;

/**
 *
 */
public class EmptyCriteriaException extends GenericSearchException {

    /**
     * Display the exception with the message passed in param
     * @param message Message to display when the exception is display
     */
    public EmptyCriteriaException(String message) {
        super(message);
    }
}
