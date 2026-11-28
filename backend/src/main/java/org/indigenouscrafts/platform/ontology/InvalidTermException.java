package org.indigenouscrafts.platform.ontology;

/** A new term breaks a rule that needs the database to check (e.g. wrong parent). Answered with 400. */
public class InvalidTermException extends RuntimeException {

    private final String field;

    public InvalidTermException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}