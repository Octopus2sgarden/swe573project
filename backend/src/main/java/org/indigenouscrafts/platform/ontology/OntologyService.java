package org.indigenouscrafts.platform.ontology;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.dto.CreateTermRequest;
import org.indigenouscrafts.platform.ontology.dto.TermNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads the controlled vocabularies as trees and lets users add terms (#55). */
@Service
public class OntologyService {

    private static final int MAX_SLUG_LENGTH = 80;   // slug column is VARCHAR(80)

    private final OntologyTermRepository termRepository;

    public OntologyService(OntologyTermRepository termRepository) {
        this.termRepository = termRepository;
    }

    /**
     * All terms of one vocabulary as a tree: top-level terms in the list,
     * narrower terms inside {@code children}. A vocabulary without parents
     * (e.g. COLOR) simply comes back as a flat list.
     */
    @Transactional(readOnly = true)
    public List<TermNode> tree(Vocabulary vocabulary) {
        // One query. Sorted by sort_order, so children are added in the right order.
        List<OntologyTerm> terms = termRepository.findByVocabularyOrderBySortOrderAscLabelAsc(vocabulary);

        // Pass 1: turn every term into a node and remember it by id.
        Map<Long, TermNode> nodesById = new HashMap<>();
        for (OntologyTerm term : terms) {
            nodesById.put(term.getId(), TermNode.from(term));
        }

        // Pass 2: hang each node under its parent; nodes without a parent are roots.
        List<TermNode> roots = new ArrayList<>();
        for (OntologyTerm term : terms) {
            TermNode node = nodesById.get(term.getId());
            TermNode parent = term.getParent() == null ? null : nodesById.get(term.getParent().getId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }

    /**
     * Adds a term for a logged-in user. No approval step (decision 28 Nov 2026),
     * so the rules below are the only protection the vocabulary has.
     */
    @Transactional
    public TermNode create(CreateTermRequest request, Long userId) {
        Vocabulary vocabulary = request.vocabulary();

        // Rule 1: the parent must exist and be in the same vocabulary
        // (a material cannot sit under a color).
        OntologyTerm parent = null;
        if (request.parentId() != null) {
            parent = termRepository.findById(request.parentId())
                    .orElseThrow(() -> new InvalidTermException("parentId", "Parent term not found."));
            if (parent.getVocabulary() != vocabulary) {
                throw new InvalidTermException("parentId", "Parent term must be in the same vocabulary.");
            }
        }

        // Rule 2: color codes only on colors, years only on periods.
        if (request.colorHex() != null && vocabulary != Vocabulary.COLOR) {
            throw new InvalidTermException("colorHex", "Only colors can have a color code.");
        }
        if ((request.yearFrom() != null || request.yearTo() != null) && vocabulary != Vocabulary.PERIOD) {
            throw new InvalidTermException("yearFrom", "Only periods can have years.");
        }
        if (request.yearFrom() != null && request.yearTo() != null && request.yearTo() < request.yearFrom()) {
            throw new InvalidTermException("yearTo", "End year must not be before start year.");
        }

        // Rule 3: no duplicates. "Ebru", "ebru " and "EBRU" all give the same slug.
        String slug = slugFor(vocabulary, request.label());
        termRepository.findBySlug(slug).ifPresent(existing -> {
            throw new TermAlreadyExistsException(existing.getId());
        });

        // New terms go to the end of their sibling list, like in the seed.
        long siblings = parent == null
                ? termRepository.countByVocabularyAndParentIsNull(vocabulary)
                : termRepository.countByParent(parent);

        OntologyTerm term = OntologyTerm.createdByUser(
                vocabulary, slug, request.label().trim(),
                trimToNull(request.localName()), trimToNull(request.definition()),
                parent, request.colorHex(), request.yearFrom(), request.yearTo(),
                (int) siblings + 1, userId);
        return TermNode.from(termRepository.save(term));
    }

    /**
     * "Öd ağacı" in MATERIAL → "mat-od-agaci". Same prefixes as the seed (V4).
     * Package-private so the test can call it directly.
     */
    static String slugFor(Vocabulary vocabulary, String label) {
        String prefix = switch (vocabulary) {
            case CATEGORY -> "cat";
            case MATERIAL -> "mat";
            case TECHNIQUE -> "tech";
            case TOOL -> "tool";
            case COLOR -> "color";
            case REGION -> "region";
            case PERIOD -> "period";
        };
        // ı and İ have no "plain letter + accent" form, so we replace them by hand.
        String text = label.trim().replace('ı', 'i').replace('İ', 'I');
        // NFD splits "ö" into "o" + "¨"; \p{M} removes the accent marks.
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        text = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")    // spaces and symbols → one dash
                .replaceAll("(^-+|-+$)", "");     // no dash at start or end
        if (text.isEmpty()) {
            throw new InvalidTermException("label", "Label must contain letters or digits.");
        }
        String slug = prefix + "-" + text;
        return slug.length() > MAX_SLUG_LENGTH ? slug.substring(0, MAX_SLUG_LENGTH) : slug;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}