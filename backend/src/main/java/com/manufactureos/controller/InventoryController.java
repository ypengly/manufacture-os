package com.manufactureos.controller;

import com.manufactureos.dto.inventoryOps.*;
import com.manufactureos.entity.Inventory;
import com.manufactureos.entity.InventoryCategory;
import com.manufactureos.entity.InventoryTransaction;
import com.manufactureos.entity.MaterialConsumption;
import com.manufactureos.entity.MaterialReservation;
import com.manufactureos.service.InventoryService;
import com.manufactureos.service.MrpService;
import com.manufactureos.service.ProductionOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final MrpService mrpService;
    private final ProductionOrderService productionOrderService;

    public InventoryController(InventoryService inventoryService, MrpService mrpService,
                                ProductionOrderService productionOrderService) {
        this.inventoryService = inventoryService;
        this.mrpService = mrpService;
        this.productionOrderService = productionOrderService;
    }

    @GetMapping
    public List<InventoryView> list(@RequestParam(required = false) Long factoryId,
                                     @RequestParam(required = false) InventoryCategory category) {
        return inventoryService.list(factoryId, category);
    }

    @GetMapping("/transactions")
    public Page<InventoryTransaction> transactions(@RequestParam(required = false) Long factoryId,
                                                     @RequestParam(required = false) String referenceType,
                                                     @RequestParam(required = false) Long referenceId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryService.listTransactions(factoryId, referenceType, referenceId, pageable);
    }

    @GetMapping("/mrp")
    public List<MaterialRequirementView> mrp(@RequestParam Long bomId,
                                              @RequestParam BigDecimal quantity,
                                              @RequestParam Long factoryId) {
        return mrpService.calculateRequirements(bomId, quantity, factoryId);
    }

    @PostMapping("/receive")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public Inventory receive(@Valid @RequestBody ReceiveMaterialRequest request) {
        return inventoryService.receive(request);
    }

    @PostMapping("/reservations")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER')")
    public List<MaterialReservation> reserve(@Valid @RequestBody ReservationRequest request) {
        return productionOrderService.reserveMaterialForOrder(request.getProductionOrderId(), request.getMaterialId(),
                request.getQuantity(), request.getFactoryId(), request.isAllowPartial());
    }

    @PostMapping("/consumption")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'INVENTORY_MANAGER', 'WORKER')")
    public List<MaterialConsumption> consume(@Valid @RequestBody ConsumptionRequest request) {
        return productionOrderService.consumeMaterial(request.getProductionOrderId(), request.getMaterialId(),
                request.getQuantity(), request.getIdempotencyKey());
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public Inventory adjust(@Valid @RequestBody AdjustmentRequest request) {
        return inventoryService.adjust(request);
    }
}
