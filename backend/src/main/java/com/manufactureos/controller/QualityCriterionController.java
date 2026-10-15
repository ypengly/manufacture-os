package com.manufactureos.controller;

import com.manufactureos.dto.qualityCriterion.QualityCriterionRequest;
import com.manufactureos.dto.qualityCriterion.QualityCriterionResponse;
import com.manufactureos.service.QualityCriterionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality/criteria")
public class QualityCriterionController {

    private final QualityCriterionService service;

    public QualityCriterionController(QualityCriterionService service) {
        this.service = service;
    }

    @GetMapping
    public List<QualityCriterionResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public QualityCriterionResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public ResponseEntity<QualityCriterionResponse> create(@Valid @RequestBody QualityCriterionRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public QualityCriterionResponse update(@PathVariable Long id, @Valid @RequestBody QualityCriterionRequest req) {
        return service.update(id, req);
    }
}
