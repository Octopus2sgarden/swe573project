package org.indigenouscrafts.platform.craft;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A free keyword that any user can add, stored in {@code tag} (V3). */
@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Required by JPA. */
    protected Tag() {
    }

    /** Tags are always stored trimmed and in lowercase ("Paper-Art " -> "paper-art"). */
    public Tag(String name) {
        this.name = normalize(name);
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}