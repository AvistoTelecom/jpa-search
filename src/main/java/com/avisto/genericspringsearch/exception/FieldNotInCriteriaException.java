package com.avisto.genericspringsearch.exception;

/**
 * Returns an exception when the search method has called a filter that does not exist in the CriteriaEnum.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public class FieldNotInCriteriaException extends GenericSearchException {
    public FieldNotInCriteriaException(String message) {
        super(message);
    }
}
