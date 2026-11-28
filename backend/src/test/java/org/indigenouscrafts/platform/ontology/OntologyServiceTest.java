package org.indigenouscrafts.platform.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.dto.CreateTermRequest;
import org.indigenouscrafts.platform.ontology.dto.TermNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    // ---- tree --------------------------------------------------------------

    @Test
    void tree_putsChildrenUnderTheirParent() {
        OntologyTerm ceramics = term(1L, Vocabulary.CATEGORY, "cat-ceramics", "Ceramics", null, null);
        OntologyTerm cini = term(2L, Vocabulary.CATEGORY, "cat-cini", "Çini", ceramics, "glazed tiles; glazed ware");
        OntologyTerm kutahya = term(3L, Vocabulary.CATEGORY, "cat-kutahya-cinisi", "Kütahya çinisi", cini, null);
        OntologyTerm textiles = term(4L, Vocabulary.CATEGORY, "cat-textiles", "Textiles", null, null);
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
        OntologyTerm red = term(10L, Vocabulary.COLOR, "color-red", "Red", null, null);
        OntologyTerm blue = term(11L, Vocabulary.COLOR, "color-blue", "Blue", null, null);
        when(termRepository.findByVocabularyOrderBySortOrderAscLabelAsc(Vocabulary.COLOR))
                .thenReturn(List.of(red, blue));

        List<TermNode> roots = ontologyService.tree(Vocabulary.COLOR);

        assertEquals(2, roots.size());
        assertTrue(roots.get(0).children().isEmpty());
        assertEquals(List.of(), roots.get(0).altLabels());
    }

    // ---- create ------------------------------------------------------------

    @Test
    void create_savesTermUnderParent_withSlugSortOrderAndCreator() {
        OntologyTerm animal = term(5L, Vocabulary.MATERIAL, "mat-animal-materials", "Animal materials", null, null);
        when(termRepository.findById(5L)).thenReturn(Optional.of(animal));
        when(termRepository.findBySlug("mat-ox-gall")).thenReturn(Optional.empty());
        when(termRepository.countByParent(animal)).thenReturn(3L);
        when(termRepository.save(any(OntologyTerm.class))).thenAnswer(call -> call.getArgument(0));

        CreateTermRequest request = new CreateTermRequest(
                Vocabulary.MATERIAL, " Ox gall ", "Öd", 5L, null, null, null, null);
        TermNode result = ontologyService.create(request, 42L);

        // Look at the entity that was handed to save().
        ArgumentCaptor<OntologyTerm> saved = ArgumentCaptor.forClass(OntologyTerm.class);
        verify(termRepository).save(saved.capture());
        assertEquals("mat-ox-gall", saved.getValue().getSlug());
        assertEquals("Ox gall", saved.getValue().getLabel());      // trimmed
        assertEquals(animal, saved.getValue().getParent());
        assertEquals(4, saved.getValue().getSortOrder());          // after 3 siblings
        assertEquals(42L, saved.getValue().getCreatedById());      // from the JWT, not the body
        assertEquals("Öd", result.localName());
    }

    @Test
    void create_duplicateLabel_throwsAndSavesNothing() {
        OntologyTerm ebru = term(48L, Vocabulary.CATEGORY, "cat-ebru", "Ebru", null, null);
        when(termRepository.findBySlug("cat-ebru")).thenReturn(Optional.of(ebru));

        CreateTermRequest request = new CreateTermRequest(
                Vocabulary.CATEGORY, "EBRU", null, null, null, null, null, null);

        TermAlreadyExistsException ex = assertThrows(TermAlreadyExistsException.class,
                () -> ontologyService.create(request, 42L));
        assertEquals(48L, ex.getExistingId());
        verify(termRepository, never()).save(any());
    }

    @Test
    void create_parentFromAnotherVocabulary_throws() {
        OntologyTerm red = term(10L, Vocabulary.COLOR, "color-red", "Red", null, null);
        when(termRepository.findById(10L)).thenReturn(Optional.of(red));

        CreateTermRequest request = new CreateTermRequest(
                Vocabulary.MATERIAL, "Ox gall", null, 10L, null, null, null, null);

        InvalidTermException ex = assertThrows(InvalidTermException.class,
                () -> ontologyService.create(request, 42L));
        assertEquals("parentId", ex.getField());
        verify(termRepository, never()).save(any());
    }

    @Test
    void create_colorCodeOnMaterial_throws() {
        CreateTermRequest request = new CreateTermRequest(
                Vocabulary.MATERIAL, "Indigo", null, null, null, "#1E4FA0", null, null);

        InvalidTermException ex = assertThrows(InvalidTermException.class,
                () -> ontologyService.create(request, 42L));
        assertEquals("colorHex", ex.getField());
    }

    @Test
    void slugFor_turnsTurkishLettersIntoAscii() {
        assertEquals("mat-od-agaci", OntologyService.slugFor(Vocabulary.MATERIAL, "Öd ağacı"));
        assertEquals("cat-iznik-cinisi", OntologyService.slugFor(Vocabulary.CATEGORY, "İznik çinisi"));
        assertEquals("cat-ebru", OntologyService.slugFor(Vocabulary.CATEGORY, "  Ebru "));
        assertThrows(InvalidTermException.class, () -> OntologyService.slugFor(Vocabulary.TOOL, "!!!"));
    }

    // OntologyTerm has no setters (seeded terms never change), so the test
    // fills the private fields with Spring's ReflectionTestUtils.
    private static OntologyTerm term(Long id, Vocabulary vocabulary, String slug, String label,
                                     OntologyTerm parent, String altLabels) {
        OntologyTerm term = new OntologyTerm();
        ReflectionTestUtils.setField(term, "id", id);
        ReflectionTestUtils.setField(term, "vocabulary", vocabulary);
        ReflectionTestUtils.setField(term, "slug", slug);
        ReflectionTestUtils.setField(term, "label", label);
        ReflectionTestUtils.setField(term, "parent", parent);
        ReflectionTestUtils.setField(term, "altLabels", altLabels);
        return term;
    }
}