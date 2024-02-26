package com.avisto.genericspringsearch.exception;

/**
 * Returns an exception when the search method has called a filter that does not exist in the CriteriaEnum.
 */
public class FieldNotInCriteriaException extends GenericSearchException {

    /**
     * Display the exception with the message passed in param
     * @param message Message to display when the exception is display
     */
    public FieldNotInCriteriaException(String message) {
        super(message);
    }
}
