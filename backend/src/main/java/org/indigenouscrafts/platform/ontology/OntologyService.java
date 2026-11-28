package org.indigenouscrafts.platform.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.dto.TermNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads the controlled vocabularies and returns each one as a tree (#55). */
@Service
public class OntologyService {

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
}