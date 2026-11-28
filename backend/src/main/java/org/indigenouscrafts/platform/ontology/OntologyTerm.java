package org.indigenouscrafts.platform.ontology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One term of the controlled vocabulary, stored in {@code ontology_term} (V3).
 * All seven vocabularies share this table; {@link #vocabulary} says which one.
 * Terms come from the seed (V4) and from users, who can add them directly (#55).
 */
@Entity
@Table(name = "ontology_term")
public class OntologyTerm {

    public enum Vocabulary { CATEGORY, MATERIAL, TECHNIQUE, TOOL, COLOR, REGION, PERIOD }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Vocabulary vocabulary;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "local_name", length = 100)
    private String localName;

    @Column(name = "alt_labels", columnDefinition = "text")
    private String altLabels;

    @Column(columnDefinition = "text")
    private String definition;

    // Tree: the broader term (null for top-level terms). LAZY = loaded only when used.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OntologyTerm parent;

    @Column(name = "wikidata_id", length = 20)
    private String wikidataId;

    // The column is CHAR(7) ("#1E4FA0"), not VARCHAR, so we tell Hibernate the exact type.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "year_from")
    private Integer yearFrom;

    @Column(name = "year_to")
    private Integer yearTo;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    // Who added the term (V5). NULL for seeded terms. We keep only the id:
    // nothing here needs the whole user, and an admin can look the user up later.
    @Column(name = "created_by")
    private Long createdById;

    /** Required by JPA. */
    protected OntologyTerm() {
    }

    /** A term a user adds from the craft form. The service has already checked the values. */
    public static OntologyTerm createdByUser(Vocabulary vocabulary, String slug, String label,
            String localName, String definition, OntologyTerm parent, String colorHex,
            Integer yearFrom, Integer yearTo, int sortOrder, Long createdById) {
        OntologyTerm term = new OntologyTerm();
        term.vocabulary = vocabulary;
        term.slug = slug;
        term.label = label;
        term.localName = localName;
        term.definition = definition;
        term.parent = parent;
        term.colorHex = colorHex;
        term.yearFrom = yearFrom;
        term.yearTo = yearTo;
        term.sortOrder = sortOrder;
        term.createdById = createdById;
        return term;
    }

    public Long getId() { return id; }
    public Vocabulary getVocabulary() { return vocabulary; }
    public String getSlug() { return slug; }
    public String getLabel() { return label; }
    public String getLocalName() { return localName; }
    public String getAltLabels() { return altLabels; }
    public String getDefinition() { return definition; }
    public OntologyTerm getParent() { return parent; }
    public String getWikidataId() { return wikidataId; }
    public String getColorHex() { return colorHex; }
    public Integer getYearFrom() { return yearFrom; }
    public Integer getYearTo() { return yearTo; }
    public int getSortOrder() { return sortOrder; }
    public Long getCreatedById() { return createdById; }
}