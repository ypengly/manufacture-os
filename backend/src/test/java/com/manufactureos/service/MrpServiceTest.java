package com.manufactureos.service;

import com.manufactureos.dto.inventoryOps.MaterialRequirementView;
import com.manufactureos.entity.BomComponent;
import com.manufactureos.repository.BomComponentRepository;
import com.manufactureos.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * MrpService is pure calculation over two repository lookups, so it's tested with
 * mocks rather than a real database - the goal here is to pin down the required-vs-
 * available-vs-shortage arithmetic, including the waste-percent multiplier, which is
 * exactly the kind of off-by-a-rounding-rule bug that's easy to introduce silently.
 */
@ExtendWith(MockitoExtension.class)
class MrpServiceTest {

    @Mock
    private BomComponentRepository bomComponentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Test
    void appliesWastePercentOnTopOfPerUnitQuantity() {
        MrpService mrpService = new MrpService(bomComponentRepository, inventoryRepository);

        BomComponent steel = new BomComponent();
        steel.setMaterialId(1L);
        steel.setQuantity(BigDecimal.valueOf(2)); // 2kg per unit
        steel.setWastePercent(BigDecimal.valueOf(10)); // +10% waste

        when(bomComponentRepository.findByBomId(100L)).thenReturn(List.of(steel));
        when(inventoryRepository.totalAvailableForMaterial(1L, 5L)).thenReturn(BigDecimal.valueOf(100));

        // Plan to build 50 units: 2kg * 50 * 1.10 = 110kg required.
        List<MaterialRequirementView> result = mrpService.calculateRequirements(100L, BigDecimal.valueOf(50), 5L);

        assertThat(result).hasSize(1);
        MaterialRequirementView view = result.get(0);
        assertThat(view.getMaterialId()).isEqualTo(1L);
        assertThat(view.getRequiredQuantity()).isEqualByComparingTo("110.0000");
        assertThat(view.getAvailableQuantity()).isEqualByComparingTo("100");
        // 110 required - 100 available = 10 short.
        assertThat(view.getShortageQuantity()).isEqualByComparingTo("10.0000");
    }

    @Test
    void shortageIsFlooredAtZeroWhenStockCoversTheRequirement() {
        MrpService mrpService = new MrpService(bomComponentRepository, inventoryRepository);

        BomComponent bolt = new BomComponent();
        bolt.setMaterialId(2L);
        bolt.setQuantity(BigDecimal.valueOf(4));
        bolt.setWastePercent(BigDecimal.ZERO);

        when(bomComponentRepository.findByBomId(200L)).thenReturn(List.of(bolt));
        when(inventoryRepository.totalAvailableForMaterial(2L, 5L)).thenReturn(BigDecimal.valueOf(1000));

        boolean shortage = mrpService.hasAnyShortage(200L, BigDecimal.valueOf(50), 5L);

        assertThat(shortage).isFalse();
    }
}
