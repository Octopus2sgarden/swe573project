package org.indigenouscrafts.platform.ontology;

/** A term with the same slug exists already. Answered with 409 + the existing term's id. */
public class TermAlreadyExistsException extends RuntimeException {

    private final Long existingId;

    public TermAlreadyExistsException(Long existingId) {
        super("This term already exists.");
        this.existingId = existingId;
    }

    public Long getExistingId() {
        return existingId;
    }
}