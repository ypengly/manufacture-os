package com.manufactureos.controller;

import com.manufactureos.dto.bom.BomCreateRequest;
import com.manufactureos.dto.bom.BomDetailResponse;
import com.manufactureos.service.BomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping("/{id}")
    public BomDetailResponse get(@PathVariable Long id) {
        return bomService.get(id);
    }

    @GetMapping
    public List<BomDetailResponse> listForProduct(@RequestParam Long productId) {
        return bomService.listForProduct(productId);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public BomDetailResponse create(@Valid @RequestBody BomCreateRequest request) {
        return bomService.create(request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public BomDetailResponse submit(@PathVariable Long id) {
        return bomService.submitForApproval(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public BomDetailResponse approve(@PathVariable Long id) {
        return bomService.approve(id);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public BomDetailResponse archive(@PathVariable Long id) {
        return bomService.archive(id);
    }
}
