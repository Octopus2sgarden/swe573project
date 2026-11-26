package org.indigenouscrafts.platform.craft;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * One measured dimension of a craft (a row in {@code craft_dimension}).
 * {@code valueTo} is set when the size is a range, e.g. 30–50 cm.
 */
@Embeddable
public class CraftDimension {

    public enum Type { WIDTH, HEIGHT, DEPTH, DIAMETER, WEIGHT }

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension", nullable = false, length = 10)
    private Type type;

    @Column(name = "value_from", nullable = false, precision = 10, scale = 2)
    private BigDecimal valueFrom;

    @Column(name = "value_to", precision = 10, scale = 2)
    private BigDecimal valueTo;

    /** Required by JPA. */
    protected CraftDimension() {
    }

    public CraftDimension(Type type, BigDecimal valueFrom, BigDecimal valueTo) {
        this.type = type;
        this.valueFrom = valueFrom;
        this.valueTo = valueTo;
    }

    public Type getType() { return type; }
    public BigDecimal getValueFrom() { return valueFrom; }
    public BigDecimal getValueTo() { return valueTo; }
}