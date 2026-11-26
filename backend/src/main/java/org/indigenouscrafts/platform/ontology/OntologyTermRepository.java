package org.indigenouscrafts.platform.ontology;

import java.util.List;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for vocabulary terms (used by the pickers on the craft form, #55). */
public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Long> {

    List<OntologyTerm> findByVocabularyOrderBySortOrderAscLabelAsc(Vocabulary vocabulary);
}