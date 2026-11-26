package org.indigenouscrafts.platform.craft;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.indigenouscrafts.platform.user.AppUser;

/**
 * A craft entry, stored in the {@code craft} table (V3).
 *
 * <p>Parts that only exist together with the craft (dimensions, production steps,
 * vocabulary links) are mapped as element collections: they have no life of their own
 * and are saved and deleted together with the craft.
 */
@Entity
@Table(name = "craft")
public class Craft {

    public enum Status { DRAFT, PUBLISHED, REMOVED }
    public enum Shape { FLAT, OBJECT_3D }
    public enum LengthUnit { MM, CM, M, IN }
    public enum WeightUnit { G, KG, LB }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who added the craft. Used for the owner check on update/delete (#34).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private AppUser createdBy;

    // "Kind of": the craft this one is a variant of (optional).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_craft_id")
    private Craft parent;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "local_name", length = 120)
    private String localName;

    @Column(name = "alt_names", columnDefinition = "text")
    private String altNames;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "cultural_relevance", columnDefinition = "text")
    private String culturalRelevance;

    @Column(columnDefinition = "text")
    private String history;

    @Column(name = "heritage_recognition", length = 200)
    private String heritageRecognition;

    @Column(name = "distinctive_features", columnDefinition = "text")
    private String distinctiveFeatures;

    @Column(columnDefinition = "text")
    private String transmission;

    @Column(name = "dye_making", columnDefinition = "text")
    private String dyeMaking;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Shape shape;

    @Enumerated(EnumType.STRING)
    @Column(name = "length_unit", length = 2)
    private LengthUnit lengthUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit", length = 2)
    private WeightUnit weightUnit;

    @Column(name = "size_note", length = 200)
    private String sizeNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.PUBLISHED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "craft_dimension", joinColumns = @JoinColumn(name = "craft_id"))
    private List<CraftDimension> dimensions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "production_step", joinColumns = @JoinColumn(name = "craft_id"))
    @OrderBy("stepNo ASC")
    private List<ProductionStep> steps = new ArrayList<>();

    // Category, materials, techniques, tools, colors, regions and periods.
    @ElementCollection
    @CollectionTable(name = "craft_term", joinColumns = @JoinColumn(name = "craft_id"))
    private List<CraftTerm> terms = new ArrayList<>();

    // Tags are shared between crafts, so this is a real many-to-many relation.
    @ManyToMany
    @JoinTable(name = "craft_tag",
            joinColumns = @JoinColumn(name = "craft_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    /** Required by JPA. */
    protected Craft() {
    }

    public Craft(AppUser createdBy, String name, String description) {
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /** True if the given user added this craft (owner check, #34). */
    public boolean isOwnedBy(Long userId) {
        return createdBy.getId().equals(userId);
    }

    /** Replaces all steps; numbers them 1, 2, 3 ... in the given order. */
    public void replaceSteps(List<String> texts) {
        steps.clear();
        for (int i = 0; i < texts.size(); i++) {
            steps.add(new ProductionStep(i + 1, texts.get(i)));
        }
    }

    public void replaceDimensions(List<CraftDimension> newDimensions) {
        dimensions.clear();
        dimensions.addAll(newDimensions);
    }

    public void replaceTerms(List<CraftTerm> newTerms) {
        terms.clear();
        terms.addAll(newTerms);
    }

    public void replaceTags(List<Tag> newTags) {
        tags.clear();
        tags.addAll(newTags);
    }

    public Long getId() { return id; }
    public AppUser getCreatedBy() { return createdBy; }
    public Craft getParent() { return parent; }
    public String getName() { return name; }
    public String getLocalName() { return localName; }
    public String getAltNames() { return altNames; }
    public String getDescription() { return description; }
    public String getCulturalRelevance() { return culturalRelevance; }
    public String getHistory() { return history; }
    public String getHeritageRecognition() { return heritageRecognition; }
    public String getDistinctiveFeatures() { return distinctiveFeatures; }
    public String getTransmission() { return transmission; }
    public String getDyeMaking() { return dyeMaking; }
    public Shape getShape() { return shape; }
    public LengthUnit getLengthUnit() { return lengthUnit; }
    public WeightUnit getWeightUnit() { return weightUnit; }
    public String getSizeNote() { return sizeNote; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<CraftDimension> getDimensions() { return dimensions; }
    public List<ProductionStep> getSteps() { return steps; }
    public List<CraftTerm> getTerms() { return terms; }
    public List<Tag> getTags() { return tags; }

    public void setParent(Craft parent) { this.parent = parent; }
    public void setName(String name) { this.name = name; }
    public void setLocalName(String localName) { this.localName = localName; }
    public void setAltNames(String altNames) { this.altNames = altNames; }
    public void setDescription(String description) { this.description = description; }
    public void setCulturalRelevance(String culturalRelevance) { this.culturalRelevance = culturalRelevance; }
    public void setHistory(String history) { this.history = history; }
    public void setHeritageRecognition(String heritageRecognition) { this.heritageRecognition = heritageRecognition; }
    public void setDistinctiveFeatures(String distinctiveFeatures) { this.distinctiveFeatures = distinctiveFeatures; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public void setDyeMaking(String dyeMaking) { this.dyeMaking = dyeMaking; }
    public void setShape(Shape shape) { this.shape = shape; }
    public void setLengthUnit(LengthUnit lengthUnit) { this.lengthUnit = lengthUnit; }
    public void setWeightUnit(WeightUnit weightUnit) { this.weightUnit = weightUnit; }
    public void setSizeNote(String sizeNote) { this.sizeNote = sizeNote; }
    public void setStatus(Status status) { this.status = status; }
}