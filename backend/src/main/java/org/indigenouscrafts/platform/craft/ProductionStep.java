package org.indigenouscrafts.platform.craft;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One numbered production step (a row in {@code production_step}). */
@Embeddable
public class ProductionStep {

    @Column(name = "step_no", nullable = false)
    private int stepNo;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    /** Required by JPA. */
    protected ProductionStep() {
    }

    public ProductionStep(int stepNo, String text) {
        this.stepNo = stepNo;
        this.text = text;
    }

    public int getStepNo() { return stepNo; }
    public String getText() { return text; }
}