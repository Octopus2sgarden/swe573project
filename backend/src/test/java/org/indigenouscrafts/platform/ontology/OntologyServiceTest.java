package org.indigenouscrafts.platform.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.dto.TermNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OntologyServiceTest {

    @Mock
    private OntologyTermRepository termRepository;

    @InjectMocks
    private OntologyService ontologyService;

    @Test
    void tree_putsChildrenUnderTheirParent() {
        OntologyTerm ceramics = term(1L, "cat-ceramics", "Ceramics", null, null);
        OntologyTerm cini = term(2L, "cat-cini", "Çini", ceramics, "glazed tiles; glazed ware");
        OntologyTerm kutahya = term(3L, "cat-kutahya-cinisi", "Kütahya çinisi", cini, null);
        OntologyTerm textiles = term(4L, "cat-textiles", "Textiles", null, null);
        when(termRepository.findByVocabularyOrderBySortOrderAscLabelAsc(Vocabulary.CATEGORY))
                .thenReturn(List.of(ceramics, cini, kutahya, textiles));

        List<TermNode> roots = ontologyService.tree(Vocabulary.CATEGORY);

        // Two top-level terms, in the order the repository gave them.
        assertEquals(2, roots.size());
        assertEquals("cat-ceramics", roots.get(0).slug());
        assertEquals("cat-textiles", roots.get(1).slug());
        // Ceramics → Çini → Kütahya çinisi
        TermNode ciniNode = roots.get(0).children().get(0);
        assertEquals("cat-cini", ciniNode.slug());
        assertEquals("cat-kutahya-cinisi", ciniNode.children().get(0).slug());
        // Alt labels are split into a list.
        assertEquals(List.of("glazed tiles", "glazed ware"), ciniNode.altLabels());
        assertTrue(roots.get(1).children().isEmpty());
    }

    @Test
    void tree_withoutParents_isAFlatList() {
        OntologyTerm red = term(10L, "color-red", "Red", null, null);
        OntologyTerm blue = term(11L, "color-blue", "Blue", null, null);
        when(termRepository.findByVocabularyOrderBySortOrderAscLabelAsc(Vocabulary.COLOR))
                .thenReturn(List.of(red, blue));

        List<TermNode> roots = ontologyService.tree(Vocabulary.COLOR);

        assertEquals(2, roots.size());
        assertTrue(roots.get(0).children().isEmpty());
        assertEquals(List.of(), roots.get(0).altLabels());
    }

    // OntologyTerm has no setters (terms come from migrations), so the test
    // fills the private fields with Spring's ReflectionTestUtils.
    private static OntologyTerm term(Long id, String slug, String label, OntologyTerm parent, String altLabels) {
        OntologyTerm term = new OntologyTerm();
        ReflectionTestUtils.setField(term, "id", id);
        ReflectionTestUtils.setField(term, "slug", slug);
        ReflectionTestUtils.setField(term, "label", label);
        ReflectionTestUtils.setField(term, "parent", parent);
        ReflectionTestUtils.setField(term, "altLabels", altLabels);
        return term;
    }
}