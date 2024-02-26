package com.avisto.genericspringsearch.config;

import java.util.Objects;

/**
 * This class : AbstractCriteria is the abstract class of all Config interfaces.
 *
 * @author Gabriel Revelli
 * @version 1.0
 */
public abstract class AbstractCriteria {

    /**
     * Key of the config
     */
    protected String key;

    /**
     * Return the key of the config
     * @return The key of the config
     */
    public String getKey() {
        return key;
    }

    /**
     * Check if the object passed in param is equal to this object.
     * @param o Object to compare
     * @return True or False
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCriteria)) return false;
        AbstractCriteria that = (AbstractCriteria) o;
        return Objects.equals(getKey(), that.getKey());
    }

    /**
     * Get the hashCode of the key
     * @return the hashCode of the key
     */
    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }
}
