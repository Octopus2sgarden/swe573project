package org.indigenouscrafts.platform.ontology;

import java.util.List;
import java.util.Optional;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for vocabulary terms (used by the pickers on the craft form, #55). */
public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Long> {

    List<OntologyTerm> findByVocabularyOrderBySortOrderAscLabelAsc(Vocabulary vocabulary);

    // Duplicate check before a user adds a term.
    Optional<OntologyTerm> findBySlug(String slug);

    // How many siblings a new term gets, so it can go to the end of the list.
    long countByVocabularyAndParentIsNull(Vocabulary vocabulary);

    long countByParent(OntologyTerm parent);
}