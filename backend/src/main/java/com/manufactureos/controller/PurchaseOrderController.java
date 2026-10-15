package com.manufactureos.controller;

import com.manufactureos.dto.purchaseOrderOps.PurchaseOrderCreateRequest;
import com.manufactureos.dto.purchaseOrderOps.PurchaseOrderDetailResponse;
import com.manufactureos.dto.purchaseOrderOps.ReceivePurchaseOrderRequest;
import com.manufactureos.entity.PurchaseOrderStatus;
import com.manufactureos.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER')")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<PurchaseOrderDetailResponse> list(@RequestParam(required = false) PurchaseOrderStatus status,
                                                    @RequestParam(required = false) Long supplierId) {
        return service.list(status, supplierId);
    }

    @GetMapping("/{id}")
    public PurchaseOrderDetailResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public PurchaseOrderDetailResponse create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/submit")
    public PurchaseOrderDetailResponse submit(@PathVariable Long id) {
        return service.submit(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public PurchaseOrderDetailResponse approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderDetailResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public PurchaseOrderDetailResponse receive(@PathVariable Long id, @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return service.receive(id, request);
    }
}
