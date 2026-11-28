package org.indigenouscrafts.platform.craft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.indigenouscrafts.platform.craft.CraftTerm.RegionRole;
import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest;
import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest.DimensionInput;
import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest.RegionInput;
import org.indigenouscrafts.platform.ontology.OntologyTerm;
import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.OntologyTermRepository;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CraftServiceTest {

    @Mock private CraftRepository craftRepository;
    @Mock private OntologyTermRepository termRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CraftService craftService;

    private final OntologyTerm paperMarbling = term(48L, Vocabulary.CATEGORY, "cat-paper-marbling");
    private final OntologyTerm oxGall = term(269L, Vocabulary.MATERIAL, "mat-ox-gall");
    private final OntologyTerm anatolia = term(200L, Vocabulary.REGION, "region-anatolia");
    private final OntologyTerm red = term(300L, Vocabulary.COLOR, "color-red");

    @BeforeEach
    void ownerExists() {
        when(userRepository.getReferenceById(42L)).thenReturn(AppUser.local("seda", "seda@example.com", "hash"));
    }

    @Test
    void create_savesCraftWithStepsTermsAndTags() {
        when(termRepository.findAllById(anyIterable())).thenReturn(List.of(paperMarbling, oxGall, anatolia));
        Tag existing = new Tag("paper art");
        when(tagRepository.findByName("paper art")).thenReturn(Optional.of(existing));
        when(tagRepository.findByName("water")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(call -> call.getArgument(0));
        when(craftRepository.save(any(Craft.class))).thenAnswer(call -> {
            Craft c = call.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 7L);   // the database would set the id
            return c;
        });

        CreateCraftRequest request = request(
                List.of(48L, 48L),                                   // duplicate id is dropped
                List.of(269L),
                List.of(new RegionInput(200L, RegionRole.ORIGIN)),
                List.of(new DimensionInput(CraftDimension.Type.WIDTH, new BigDecimal("35"), new BigDecimal("50"))),
                Craft.LengthUnit.CM,
                List.of(" Paper Art ", "water", "PAPER ART"));       // "paper art" twice

        Long id = craftService.create(request, 42L);

        assertEquals(7L, id);
        ArgumentCaptor<Craft> saved = ArgumentCaptor.forClass(Craft.class);
        verify(craftRepository).save(saved.capture());
        Craft craft = saved.getValue();
        assertEquals("Ebru", craft.getName());                   // trimmed
        assertEquals(Craft.Status.PUBLISHED, craft.getStatus()); // FR-3.2: no approval
        assertEquals(List.of(1, 2), craft.getSteps().stream().map(ProductionStep::getStepNo).toList());
        // category + material + region = 3 links; the duplicate 48 is gone
        assertEquals(3, craft.getTerms().size());
        assertNull(craft.getTerms().get(0).getRole());
        assertEquals(RegionRole.ORIGIN, craft.getTerms().get(2).getRole());
        // two tags: the existing one is reused, "water" is new
        assertEquals(List.of("paper art", "water"), craft.getTags().stream().map(Tag::getName).toList());
        assertEquals(existing, craft.getTags().get(0));
    }

    @Test
    void create_colorIdInCategories_throwsAndSavesNothing() {
        when(termRepository.findAllById(anyIterable())).thenReturn(List.of(red));
        CreateCraftRequest request = request(List.of(300L), null, null, null, null, null);

        InvalidCraftException ex = assertThrows(InvalidCraftException.class,
                () -> craftService.create(request, 42L));
        assertEquals("categoryIds", ex.getField());
        verify(craftRepository, never()).save(any());
    }

    @Test
    void create_unknownTermId_throws() {
        when(termRepository.findAllById(anyIterable())).thenReturn(List.of(paperMarbling));
        CreateCraftRequest request = request(List.of(48L), List.of(999L), null, null, null, null);

        InvalidCraftException ex = assertThrows(InvalidCraftException.class,
                () -> craftService.create(request, 42L));
        assertEquals("materialIds", ex.getField());
    }

    @Test
    void create_dimensionWithoutUnit_throws() {
        CreateCraftRequest request = request(List.of(48L), null, null,
                List.of(new DimensionInput(CraftDimension.Type.WIDTH, new BigDecimal("35"), null)),
                null, null);                                         // no length unit

        InvalidCraftException ex = assertThrows(InvalidCraftException.class,
                () -> craftService.create(request, 42L));
        assertEquals("lengthUnit", ex.getField());
        verify(craftRepository, never()).save(any());
    }

    // ---- helpers -------------------------------------------------------------

    /** An Ebru craft with two steps; the rest varies per test. */
    private static CreateCraftRequest request(List<Long> categoryIds, List<Long> materialIds,
                                              List<RegionInput> regions, List<DimensionInput> dimensions,
                                              Craft.LengthUnit lengthUnit, List<String> tags) {
        return new CreateCraftRequest(
                " Ebru ", "Ebru", List.of("paper marbling"),
                "Patterns made by floating paint on thickened water.",
                null, null, null, null, null, null, null,
                Craft.Shape.FLAT, lengthUnit, null, null, dimensions,
                List.of("Prepare the size from gum tragacanth.", "Sprinkle the paints."),
                categoryIds, materialIds, null, null, null, null, regions,
                tags);
    }

    private static OntologyTerm term(Long id, Vocabulary vocabulary, String slug) {
        OntologyTerm term = OntologyTerm.createdByUser(vocabulary, slug, slug, null, null,
                null, null, null, null, 1, null);
        ReflectionTestUtils.setField(term, "id", id);
        return term;
    }
}