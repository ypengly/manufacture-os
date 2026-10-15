package com.manufactureos.controller;

import com.manufactureos.dto.qualityOps.ScrapRequest;
import com.manufactureos.dto.scrapRecord.ScrapRecordResponse;
import com.manufactureos.service.ScrapService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality/scrap")
@PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER', 'INVENTORY_MANAGER')")
public class ScrapController {

    private final ScrapService service;

    public ScrapController(ScrapService service) {
        this.service = service;
    }

    @GetMapping
    public List<ScrapRecordResponse> list(@RequestParam Long productionOrderId) {
        return service.listForProductionOrder(productionOrderId);
    }

    @GetMapping("/{id}")
    public ScrapRecordResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ScrapRecordResponse scrap(@Valid @RequestBody ScrapRequest request) {
        return service.scrap(request);
    }
}
