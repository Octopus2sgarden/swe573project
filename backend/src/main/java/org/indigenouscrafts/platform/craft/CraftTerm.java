package org.indigenouscrafts.platform.craft;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.indigenouscrafts.platform.ontology.OntologyTerm;

/**
 * Links a craft to a vocabulary term (a row in {@code craft_term}).
 * {@code role} is only used for REGION terms.
 */
@Embeddable
public class CraftTerm {

    public enum RegionRole { ORIGIN, PRACTICED, SPREAD }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private OntologyTerm term;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RegionRole role;

    /** Required by JPA. */
    protected CraftTerm() {
    }

    public CraftTerm(OntologyTerm term, RegionRole role) {
        this.term = term;
        this.role = role;
    }

    public OntologyTerm getTerm() { return term; }
    public RegionRole getRole() { return role; }
}