package org.indigenouscrafts.platform.ontology;

import java.util.List;

import org.indigenouscrafts.platform.ontology.OntologyTerm.Vocabulary;
import org.indigenouscrafts.platform.ontology.dto.TermNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only endpoints for the vocabularies, used by the pickers on the craft form. */
@RestController
@RequestMapping("/api")
public class OntologyController {

    private final OntologyService ontologyService;

    public OntologyController(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    @GetMapping("/categories")
    public List<TermNode> categories() {
        return ontologyService.tree(Vocabulary.CATEGORY);
    }

    @GetMapping("/materials")
    public List<TermNode> materials() {
        return ontologyService.tree(Vocabulary.MATERIAL);
    }

    @GetMapping("/techniques")
    public List<TermNode> techniques() {
        return ontologyService.tree(Vocabulary.TECHNIQUE);
    }

    @GetMapping("/tools")
    public List<TermNode> tools() {
        return ontologyService.tree(Vocabulary.TOOL);
    }

    @GetMapping("/colors")
    public List<TermNode> colors() {
        return ontologyService.tree(Vocabulary.COLOR);
    }

    @GetMapping("/regions")
    public List<TermNode> regions() {
        return ontologyService.tree(Vocabulary.REGION);
    }

    @GetMapping("/periods")
    public List<TermNode> periods() {
        return ontologyService.tree(Vocabulary.PERIOD);
    }
}