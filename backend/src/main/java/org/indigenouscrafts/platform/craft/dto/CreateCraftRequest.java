package org.indigenouscrafts.platform.craft.dto;

import java.math.BigDecimal;
import java.util.List;

import org.indigenouscrafts.platform.craft.Craft.LengthUnit;
import org.indigenouscrafts.platform.craft.Craft.Shape;
import org.indigenouscrafts.platform.craft.Craft.WeightUnit;
import org.indigenouscrafts.platform.craft.CraftDimension;
import org.indigenouscrafts.platform.craft.CraftTerm.RegionRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/crafts (#32). Only name, description and at least one
 * category are required (FR-3.3); everything else is optional (FR-3.4 – FR-3.10).
 *
 * Vocabulary terms come in one id list per vocabulary, the same way the form
 * has one picker per vocabulary. The service checks that each id really belongs
 * to that vocabulary (a color id in materialIds is rejected).
 */
public record CreateCraftRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String localName,
        List<@NotBlank @Size(max = 120) String> altNames,
        @NotBlank @Size(max = 20000) String description,
        @Size(max = 20000) String culturalRelevance,
        @Size(max = 20000) String history,
        @Size(max = 200) String heritageRecognition,
        @Size(max = 20000) String distinctiveFeatures,
        @Size(max = 20000) String transmission,
        @Size(max = 20000) String dyeMaking,
        Long parentCraftId,

        Shape shape,
        LengthUnit lengthUnit,
        WeightUnit weightUnit,
        @Size(max = 200) String sizeNote,
        List<@Valid DimensionInput> dimensions,

        List<@NotBlank @Size(max = 5000) String> steps,

        @NotEmpty(message = "choose at least one category") List<@NotNull Long> categoryIds,
        List<@NotNull Long> materialIds,
        List<@NotNull Long> techniqueIds,
        List<@NotNull Long> toolIds,
        List<@NotNull Long> colorIds,
        List<@NotNull Long> periodIds,
        List<@Valid RegionInput> regions,

        List<@NotBlank @Size(max = 50) String> tags) {

    /** One size value, e.g. WIDTH 30–50 (valueTo only for a range). */
    public record DimensionInput(
            @NotNull CraftDimension.Type type,
            @NotNull @DecimalMin("0") BigDecimal valueFrom,
            @DecimalMin("0") BigDecimal valueTo) {
    }

    /** A region term with its role: where the craft comes from, is practiced or spread to. */
    public record RegionInput(
            @NotNull Long termId,
            @NotNull RegionRole role) {
    }
}