package com.manufactureos.service;

import com.manufactureos.dto.inventoryOps.MaterialRequirementView;
import com.manufactureos.entity.BomComponent;
import com.manufactureos.repository.BomComponentRepository;
import com.manufactureos.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Basic Material Requirements Planning: given a BOM and a planned production quantity,
 * works out how much of each material is needed, how much is actually available in the
 * factory's raw-material stock right now, and the resulting shortage (if any). This is a
 * read-only snapshot used before a production order is approved - it does not reserve
 * anything itself (see InventoryService.reserveMaterial for that).
 */
@Service
public class MrpService {

    private final BomComponentRepository bomComponentRepository;
    private final InventoryRepository inventoryRepository;

    public MrpService(BomComponentRepository bomComponentRepository, InventoryRepository inventoryRepository) {
        this.bomComponentRepository = bomComponentRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirementView> calculateRequirements(Long bomId, BigDecimal plannedQuantity, Long factoryId) {
        List<BomComponent> components = bomComponentRepository.findByBomId(bomId);
        return components.stream().map(c -> {
            BigDecimal perUnit = c.getQuantity();
            BigDecimal wasteMultiplier = BigDecimal.ONE.add(
                    c.getWastePercent().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            BigDecimal required = perUnit.multiply(plannedQuantity).multiply(wasteMultiplier)
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal available = inventoryRepository.totalAvailableForMaterial(c.getMaterialId(), factoryId);
            return new MaterialRequirementView(c.getMaterialId(), required, available);
        }).toList();
    }

    @Transactional(readOnly = true)
    public boolean hasAnyShortage(Long bomId, BigDecimal plannedQuantity, Long factoryId) {
        return calculateRequirements(bomId, plannedQuantity, factoryId).stream()
                .anyMatch(r -> r.getShortageQuantity().signum() > 0);
    }
}
