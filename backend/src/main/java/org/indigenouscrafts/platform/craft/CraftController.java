package org.indigenouscrafts.platform.craft;

import java.net.URI;
import java.util.Map;

import org.indigenouscrafts.platform.craft.dto.CreateCraftRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** REST endpoints for crafts. #32: create. #33 adds list and detail, #34 update and delete. */
@RestController
@RequestMapping("/api/crafts")
public class CraftController {

    private final CraftService craftService;

    public CraftController(CraftService craftService) {
        this.craftService = craftService;
    }

    /**
     * 201 Created, with the new craft's address in the Location header and its id
     * in the body. The form then opens the detail page (#33/#37) with this id.
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(@Valid @RequestBody CreateCraftRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        Long id = craftService.create(request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.created(URI.create("/api/crafts/" + id)).body(Map.of("id", id));
    }
}