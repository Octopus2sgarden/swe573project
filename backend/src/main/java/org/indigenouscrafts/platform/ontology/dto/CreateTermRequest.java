package org.indigenouscrafts.platform.ontology.dto;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/terms. Only vocabulary and label are required.
 * Rules that need the database (parent exists, no duplicate) are checked in OntologyService.
 */
public record CreateTermRequest(
        @NotNull Vocabulary vocabulary,
        @NotBlank @Size(max = 100) String label,
        @Size(max = 100) String localName,
        Long parentId,
        @Size(max = 1000) String definition,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must look like #1E4FA0") String colorHex,
        Integer yearFrom,
        Integer yearTo) {
}