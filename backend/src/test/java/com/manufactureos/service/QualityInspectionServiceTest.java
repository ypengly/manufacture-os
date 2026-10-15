package com.manufactureos.service;

import com.manufactureos.dto.qualityInspection.QualityInspectionResponse;
import com.manufactureos.entity.*;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.repository.InventoryBatchRepository;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.ProductionOutputRepository;
import com.manufactureos.repository.QualityInspectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins down the "never auto-release failed or uninspected goods" rule from the spec:
 * finalizing a partially-passed inspection must move only the passed quantity to
 * FINISHED_GOOD, must leave the failed quantity exactly where it was (QUARANTINED, for
 * ReworkService/ScrapService to resolve), and must refuse to run twice.
 */
@ExtendWith(MockitoExtension.class)
class QualityInspectionServiceTest {

    @Mock private QualityInspectionRepository qualityInspectionRepository;
    @Mock private ProductionOutputRepository productionOutputRepository;
    @Mock private InventoryBatchRepository inventoryBatchRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private InventoryService inventoryService;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;

    private QualityInspectionService service;

    @BeforeEach
    void setUp() {
        service = new QualityInspectionService(qualityInspectionRepository, productionOutputRepository,
                inventoryBatchRepository, inventoryRepository, inventoryService, notificationService, auditService);
    }

    @Test
    void finalizingAPartialPassReleasesOnlyThePassedQuantityAndLeavesTheRestQuarantined() {
        QualityInspection inspection = new QualityInspection();
        inspection.setId(1L);
        inspection.setOutputId(10L);
        inspection.setBatchId(20L);
        inspection.setPassedQuantity(BigDecimal.valueOf(80));
        inspection.setFailedQuantity(BigDecimal.valueOf(20));
        inspection.setFinalized(false);
        when(qualityInspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));

        ProductionOutput output = new ProductionOutput();
        output.setId(10L);
        output.setBatchId(20L);
        output.setQuantity(BigDecimal.valueOf(100));
        output.setReleasedQuantity(BigDecimal.ZERO);
        output.setRejectedQuantity(BigDecimal.ZERO);
        output.setStatus(OutputStatus.PENDING_INSPECTION);
        when(productionOutputRepository.lockById(10L)).thenReturn(Optional.of(output));

        InventoryBatch batch = new InventoryBatch();
        batch.setId(20L);
        batch.setBatchNumber("LOT-1");
        when(inventoryBatchRepository.findById(20L)).thenReturn(Optional.of(batch));

        Inventory quarantined = new Inventory();
        quarantined.setId(99L);
        quarantined.setCategory(InventoryCategory.QUARANTINED);
        when(inventoryRepository.findByBatchId(20L)).thenReturn(List.of(quarantined));

        QualityInspectionResponse response = service.finalizeInspection(1L);

        // Only the 80 passed units move out of quarantine - the 20 failed units are never touched here.
        ArgumentCaptor<BigDecimal> qtyCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(inventoryService).moveCategory(eq(99L), eq(InventoryCategory.FINISHED_GOOD), qtyCaptor.capture(),
                any(), anyLong(), any());
        assertThat(qtyCaptor.getValue()).isEqualByComparingTo("80");

        assertThat(output.getReleasedQuantity()).isEqualByComparingTo("80");
        assertThat(output.getRejectedQuantity()).isEqualByComparingTo("20");
        assertThat(output.getPendingDispositionQuantity()).isEqualByComparingTo("20");
        assertThat(output.getStatus()).isEqualTo(OutputStatus.QUARANTINED);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.QUARANTINED);
        assertThat(response.getFinalized()).isTrue();
    }

    @Test
    void finalizingTheSameInspectionTwiceIsRejected() {
        QualityInspection inspection = new QualityInspection();
        inspection.setId(1L);
        inspection.setFinalized(true);
        when(qualityInspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> service.finalizeInspection(1L))
                .isInstanceOf(ConflictException.class);
    }
}
