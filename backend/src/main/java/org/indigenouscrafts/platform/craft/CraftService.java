package org.indigenouscrafts.platform.craft;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest;
import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest.DimensionInput;
import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest.RegionInput;
import org.indigenouscrafts.platform.ontology.OntologyTerm;
import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.OntologyTermRepository;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates crafts (#32). Listing and detail come in #33, update and delete in #34. */
@Service
public class CraftService {

    // Dimensions measured in length units; WEIGHT uses the weight unit.
    private static final Set<CraftDimension.Type> LENGTH_TYPES = EnumSet.of(
            CraftDimension.Type.WIDTH, CraftDimension.Type.HEIGHT,
            CraftDimension.Type.DEPTH, CraftDimension.Type.DIAMETER);

    private final CraftRepository craftRepository;
    private final OntologyTermRepository termRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public CraftService(CraftRepository craftRepository, OntologyTermRepository termRepository,
                        TagRepository tagRepository, UserRepository userRepository) {
        this.craftRepository = craftRepository;
        this.termRepository = termRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new craft for the logged-in user and returns its id.
     * The craft is published at once (FR-3.2): there is no approval step.
     * One transaction: if any rule fails, nothing is saved.
     */
    @Transactional
    public Long create(CreateCraftRequest request, Long userId) {
        // getReferenceById does not run a SELECT; it only needs the id for the foreign key.
        AppUser owner = userRepository.getReferenceById(userId);
        Craft craft = new Craft(owner, request.name().trim(), request.description().trim());

        craft.setLocalName(trimToNull(request.localName()));
        craft.setAltNames(joinOrNull(request.altNames()));
        craft.setCulturalRelevance(trimToNull(request.culturalRelevance()));
        craft.setHistory(trimToNull(request.history()));
        craft.setHeritageRecognition(trimToNull(request.heritageRecognition()));
        craft.setDistinctiveFeatures(trimToNull(request.distinctiveFeatures()));
        craft.setTransmission(trimToNull(request.transmission()));
        craft.setDyeMaking(trimToNull(request.dyeMaking()));
        craft.setStatus(Craft.Status.PUBLISHED);

        if (request.parentCraftId() != null) {
            Craft parent = craftRepository.findById(request.parentCraftId())
                    .orElseThrow(() -> new InvalidCraftException("parentCraftId", "Parent craft not found."));
            craft.setParent(parent);
        }

        applySize(craft, request);
        craft.replaceSteps(trimAll(request.steps()));
        craft.replaceTerms(resolveTerms(request));
        craft.replaceTags(resolveTags(request.tags()));

        return craftRepository.save(craft).getId();
    }

    // ---- size ----------------------------------------------------------------

    private void applySize(Craft craft, CreateCraftRequest request) {
        craft.setShape(request.shape());
        craft.setLengthUnit(request.lengthUnit());
        craft.setWeightUnit(request.weightUnit());
        craft.setSizeNote(trimToNull(request.sizeNote()));

        List<CraftDimension> dimensions = new ArrayList<>();
        Set<CraftDimension.Type> seen = EnumSet.noneOf(CraftDimension.Type.class);
        for (DimensionInput d : orEmpty(request.dimensions())) {
            // The table's primary key is (craft_id, dimension): one row per type.
            if (!seen.add(d.type())) {
                throw new InvalidCraftException("dimensions", d.type() + " is given twice.");
            }
            if (d.valueTo() != null && d.valueTo().compareTo(d.valueFrom()) < 0) {
                throw new InvalidCraftException("dimensions", d.type() + ": the range end is smaller than the start.");
            }
            // A number without a unit means nothing ("30" — cm? inches?).
            if (LENGTH_TYPES.contains(d.type()) && request.lengthUnit() == null) {
                throw new InvalidCraftException("lengthUnit", "Choose a length unit for the dimensions.");
            }
            if (d.type() == CraftDimension.Type.WEIGHT && request.weightUnit() == null) {
                throw new InvalidCraftException("weightUnit", "Choose a weight unit for the weight.");
            }
            dimensions.add(new CraftDimension(d.type(), d.valueFrom(), d.valueTo()));
        }
        craft.replaceDimensions(dimensions);
    }

    // ---- vocabulary terms ----------------------------------------------------

    private List<CraftTerm> resolveTerms(CreateCraftRequest request) {
        List<RegionInput> regions = orEmpty(request.regions());

        // Load every term the request mentions with ONE query instead of one per id.
        Set<Long> allIds = Stream.of(
                        orEmpty(request.categoryIds()), orEmpty(request.materialIds()),
                        orEmpty(request.techniqueIds()), orEmpty(request.toolIds()),
                        orEmpty(request.colorIds()), orEmpty(request.periodIds()),
                        regions.stream().map(RegionInput::termId).toList())
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        Map<Long, OntologyTerm> termsById = termRepository.findAllById(allIds).stream()
                .collect(Collectors.toMap(OntologyTerm::getId, Function.identity()));

        List<CraftTerm> links = new ArrayList<>();
        addTerms(links, request.categoryIds(), Vocabulary.CATEGORY, "categoryIds", termsById);
        addTerms(links, request.materialIds(), Vocabulary.MATERIAL, "materialIds", termsById);
        addTerms(links, request.techniqueIds(), Vocabulary.TECHNIQUE, "techniqueIds", termsById);
        addTerms(links, request.toolIds(), Vocabulary.TOOL, "toolIds", termsById);
        addTerms(links, request.colorIds(), Vocabulary.COLOR, "colorIds", termsById);
        addTerms(links, request.periodIds(), Vocabulary.PERIOD, "periodIds", termsById);

        // Regions carry a role. The primary key is (craft_id, term_id), so a region
        // can be linked once; if it comes twice, the first role wins.
        Set<Long> regionIds = new HashSet<>();
        for (RegionInput region : regions) {
            OntologyTerm term = checkedTerm(region.termId(), Vocabulary.REGION, "regions", termsById);
            if (regionIds.add(term.getId())) {
                links.add(new CraftTerm(term, region.role()));
            }
        }
        return links;
    }

    private void addTerms(List<CraftTerm> links, List<Long> ids, Vocabulary vocabulary,
                          String field, Map<Long, OntologyTerm> termsById) {
        // LinkedHashSet: drops duplicate ids but keeps the user's order.
        for (Long id : new LinkedHashSet<>(orEmpty(ids))) {
            // role = null: only region links have a role.
            links.add(new CraftTerm(checkedTerm(id, vocabulary, field, termsById), null));
        }
    }

    // The wiki rule: a term linked as a category must really be a CATEGORY term, etc.
    private OntologyTerm checkedTerm(Long id, Vocabulary vocabulary, String field,
                                     Map<Long, OntologyTerm> termsById) {
        OntologyTerm term = termsById.get(id);
        if (term == null) {
            throw new InvalidCraftException(field, "Term " + id + " not found.");
        }
        if (term.getVocabulary() != vocabulary) {
            throw new InvalidCraftException(field, "Term " + id + " is not a " + vocabulary + " term.");
        }
        return term;
    }

    // ---- tags ----------------------------------------------------------------

    /** Reuses existing tags and creates the missing ones ("Paper Art" and "paper art" are one tag). */
    private List<Tag> resolveTags(List<String> names) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String name : orEmpty(names)) {
            normalized.add(Tag.normalize(name));
        }
        List<Tag> tags = new ArrayList<>();
        for (String name : normalized) {
            tags.add(tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name))));
        }
        return tags;
    }

    // ---- small helpers -------------------------------------------------------

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static List<String> trimAll(List<String> texts) {
        return orEmpty(texts).stream().map(String::trim).toList();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    // Stored like alt labels of terms: one text, separated by "; ".
    private static String joinOrNull(List<String> names) {
        List<String> trimmed = trimAll(names);
        return trimmed.isEmpty() ? null : String.join("; ", trimmed);
    }
}