package org.indigenouscrafts.platform.ontology.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.indigenouscrafts.platform.ontology.OntologyTerm;

/**
 * One term as the API returns it, with its narrower terms nested in {@code children}.
 * We return this record instead of the entity so the JSON shape is under our control
 * and no lazy-loaded entity leaks out of the transaction.
 */
public record TermNode(
        Long id,
        String slug,
        String label,
        String localName,
        List<String> altLabels,
        String definition,
        String colorHex,
        Integer yearFrom,
        Integer yearTo,
        List<TermNode> children) {

    /** Copies the fields of one term; children start empty and are filled by the service. */
    public static TermNode from(OntologyTerm term) {
        return new TermNode(
                term.getId(),
                term.getSlug(),
                term.getLabel(),
                term.getLocalName(),
                splitAltLabels(term.getAltLabels()),
                term.getDefinition(),
                term.getColorHex(),
                term.getYearFrom(),
                term.getYearTo(),
                new ArrayList<>());
    }

    // In the database alt labels are one text, e.g. "glazed tiles; glazed ware".
    // The frontend gets a proper list: ["glazed tiles", "glazed ware"].
    private static List<String> splitAltLabels(String altLabels) {
        if (altLabels == null || altLabels.isBlank()) {
            return List.of();
        }
        return Arrays.stream(altLabels.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}