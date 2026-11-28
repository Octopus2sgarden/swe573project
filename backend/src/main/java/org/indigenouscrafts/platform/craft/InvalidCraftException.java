package org.indigenouscrafts.platform.craft;

/**
 * A new craft breaks a rule that the annotations on the request cannot check,
 * e.g. a term id from the wrong vocabulary. Answered with 400 + the field name.
 */
public class InvalidCraftException extends RuntimeException {

    private final String field;

    public InvalidCraftException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}