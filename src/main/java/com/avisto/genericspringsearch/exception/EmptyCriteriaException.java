package com.avisto.genericspringsearch.exception;

/**
 * This exception is called when you try to create an empty Criteria.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class EmptyCriteriaException extends GenericSearchException {
    public EmptyCriteriaException(String message) {
        super(message);
    }
}
