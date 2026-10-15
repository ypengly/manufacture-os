package com.manufactureos.integration;

import com.manufactureos.entity.*;
import com.manufactureos.exception.InsufficientMaterialException;
import com.manufactureos.repository.*;
import com.manufactureos.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is the test the spec calls out by name: "Available Material: 100 units. User A
 * requests 70, User B requests 50 - only one or a valid combination should succeed; the
 * system must never reserve more than 100 units." It runs against a real Postgres
 * (via Testcontainers) because the guarantee being tested is exactly the one SELECT ...
 * FOR UPDATE row locking provides under real concurrent transactions - an H2-backed or
 * mocked test would not exercise the same locking behavior at all.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class MaterialReservationConcurrencyIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private FactoryRepository factoryRepository;
    @Autowired
    private StorageLocationRepository storageLocationRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    private Long materialId;
    private Long factoryId;

    @BeforeEach
    void setUp() {
        Factory factory = new Factory();
        factory.setCode("T-FAC");
        factory.setName("Test Factory");
        factory.setStatus("ACTIVE");
        factory = factoryRepository.save(factory);
        factoryId = factory.getId();

        StorageLocation location = new StorageLocation();
        location.setFactoryId(factoryId);
        location.setCode("T-LOC");
        location.setName("Test Location");
        location.setType("RAW_MATERIAL");
        location = storageLocationRepository.save(location);

        Supplier supplier = new Supplier();
        supplier.setCode("T-SUP");
        supplier.setCompanyName("Test Supplier");
        supplier.setStatus("ACTIVE");
        supplier = supplierRepository.save(supplier);

        Material material = new Material();
        material.setCode("T-MAT");
        material.setName("Test Material");
        material.setCategory("RAW_MATERIAL");
        material.setUnitOfMeasure("EA");
        material.setStandardCost(BigDecimal.ONE);
        material.setActive(true);
        material = materialRepository.save(material);
        materialId = material.getId();

        InventoryBatch batch = new InventoryBatch();
        batch.setBatchNumber("T-BATCH-100");
        batch.setMaterialId(materialId);
        batch.setSupplierId(supplier.getId());
        batch.setStatus(BatchStatus.AVAILABLE);
        batch = inventoryBatchRepository.save(batch);

        Inventory inventory = new Inventory();
        inventory.setFactoryId(factoryId);
        inventory.setLocationId(location.getId());
        inventory.setBatchId(batch.getId());
        inventory.setCategory(InventoryCategory.RAW_MATERIAL);
        inventory.setQuantityOnHand(BigDecimal.valueOf(100));
        inventory.setReservedQuantity(BigDecimal.ZERO);
        inventoryRepository.save(inventory);
    }

    @Test
    void concurrentReservationsNeverExceedAvailableStock() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Exception> requestAError = new AtomicReference<>();
        AtomicReference<Exception> requestBError = new AtomicReference<>();

        Runnable requestA = () -> {
            readyLatch.countDown();
            await(startLatch);
            try {
                inventoryService.reserveMaterial(1001L, materialId, BigDecimal.valueOf(70), factoryId, false);
            } catch (Exception e) {
                requestAError.set(e);
            }
        };
        Runnable requestB = () -> {
            readyLatch.countDown();
            await(startLatch);
            try {
                inventoryService.reserveMaterial(1002L, materialId, BigDecimal.valueOf(50), factoryId, false);
            } catch (Exception e) {
                requestBError.set(e);
            }
        };

        pool.submit(requestA);
        pool.submit(requestB);
        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // release both threads at (almost) the same instant
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        // Exactly one of the two 70+50=120-against-100 requests must have been rejected,
        // because allowPartial=false and only one of them can fit.
        boolean exactlyOneFailed = (requestAError.get() != null) ^ (requestBError.get() != null);
        assertThat(exactlyOneFailed)
                .withFailMessage("Expected exactly one request to fail with InsufficientMaterialException; A=%s B=%s",
                        requestAError.get(), requestBError.get())
                .isTrue();

        Exception failure = requestAError.get() != null ? requestAError.get() : requestBError.get();
        assertThat(failure).isInstanceOf(InsufficientMaterialException.class);

        // The core invariant: total reserved quantity must never exceed what was on hand.
        List<Inventory> rows = inventoryRepository.findByBatchId(
                inventoryBatchRepository.findByBatchNumber("T-BATCH-100").orElseThrow().getId());
        BigDecimal totalReserved = rows.stream().map(Inventory::getReservedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalReserved).isLessThanOrEqualTo(BigDecimal.valueOf(100));

        // And it should be exactly the quantity of whichever single request succeeded.
        boolean reservedSeventy = totalReserved.compareTo(BigDecimal.valueOf(70)) == 0;
        boolean reservedFifty = totalReserved.compareTo(BigDecimal.valueOf(50)) == 0;
        assertThat(reservedSeventy || reservedFifty)
                .withFailMessage("Expected total reserved to be exactly 70 or 50, was %s", totalReserved)
                .isTrue();
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
