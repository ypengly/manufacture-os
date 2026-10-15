package com.manufactureos.scheduler;

import com.manufactureos.entity.Machine;
import com.manufactureos.entity.Material;
import com.manufactureos.entity.NotificationType;
import com.manufactureos.entity.OrderStatus;
import com.manufactureos.entity.ProductionOrder;
import com.manufactureos.entity.Role;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.repository.MaterialRepository;
import com.manufactureos.repository.ProductionOrderRepository;
import com.manufactureos.service.MaintenanceService;
import com.manufactureos.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Background scans that raise notifications without any user action: low stock,
 * machines due for maintenance, and production orders that have run past their
 * planned end date. Each notification carries a dedupeKey scoped to today's date so a
 * scan that runs every few minutes doesn't re-notify for the same condition all day;
 * the notifications table's unique index on dedupe_key is the actual backstop.
 */
@Component
public class OperationsScheduler {

    private static final Logger log = LoggerFactory.getLogger(OperationsScheduler.class);

    private static final Set<OrderStatus> DELAY_CANDIDATE_STATUSES = EnumSet.of(
            OrderStatus.PLANNED, OrderStatus.PENDING_APPROVAL, OrderStatus.APPROVED,
            OrderStatus.MATERIALS_RESERVED, OrderStatus.READY, OrderStatus.IN_PROGRESS, OrderStatus.PAUSED);

    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;
    private final MachineRepository machineRepository;
    private final MaintenanceService maintenanceService;
    private final ProductionOrderRepository productionOrderRepository;
    private final NotificationService notificationService;

    public OperationsScheduler(MaterialRepository materialRepository,
                                InventoryRepository inventoryRepository,
                                MachineRepository machineRepository,
                                MaintenanceService maintenanceService,
                                ProductionOrderRepository productionOrderRepository,
                                NotificationService notificationService) {
        this.materialRepository = materialRepository;
        this.inventoryRepository = inventoryRepository;
        this.machineRepository = machineRepository;
        this.maintenanceService = maintenanceService;
        this.productionOrderRepository = productionOrderRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${manufactureos.scheduler.low-stock-rate-ms:900000}")
    public void scanLowStock() {
        String today = LocalDate.now().toString();
        List<Material> materials = materialRepository.findByActiveTrue();
        for (Material material : materials) {
            if (material.getReorderLevel() == null) {
                continue;
            }
            var available = inventoryRepository.totalAvailableForMaterialAnyFactory(material.getId());
            if (available.compareTo(material.getReorderLevel()) <= 0) {
                notificationService.notifyRole(Role.INVENTORY_MANAGER, "Low stock: " + material.getName(),
                        "Available stock (" + available + ") is at or below the reorder level ("
                                + material.getReorderLevel() + ") for " + material.getName() + ".",
                        NotificationType.LOW_STOCK, "LOW_STOCK:" + material.getId() + ":" + today);
            }
        }
        log.debug("Low stock scan complete: {} active materials checked", materials.size());
    }

    @Scheduled(fixedRateString = "${manufactureos.scheduler.maintenance-due-rate-ms:3600000}")
    public void scanMaintenanceDue() {
        String today = LocalDate.now().toString();
        List<Machine> due = maintenanceService.findMachinesDue();
        for (Machine machine : due) {
            notificationService.notifyRole(Role.PRODUCTION_MANAGER, "Maintenance due: " + machine.getCode(),
                    "Machine " + machine.getCode() + " (" + machine.getName() + ") is due for maintenance.",
                    NotificationType.MAINTENANCE_DUE, "MAINTENANCE_DUE:" + machine.getId() + ":" + today);
        }
        log.debug("Maintenance-due scan complete: {} machine(s) due", due.size());
    }

    @Scheduled(fixedRateString = "${manufactureos.scheduler.delay-scan-rate-ms:900000}")
    public void scanDelayedOrders() {
        Instant now = Instant.now();
        String today = LocalDate.now().toString();
        List<ProductionOrder> candidates = productionOrderRepository.findAll().stream()
                .filter(o -> DELAY_CANDIDATE_STATUSES.contains(o.getStatus()))
                .filter(o -> o.getPlannedEnd() != null && o.getPlannedEnd().isBefore(now))
                .toList();
        for (ProductionOrder order : candidates) {
            notificationService.notifyRole(Role.PRODUCTION_MANAGER, "Production order running late",
                    "Order " + order.getOrderNumber() + " was planned to finish by " + order.getPlannedEnd()
                            + " and is still " + order.getStatus() + ".",
                    NotificationType.PRODUCTION_DELAY, "PRODUCTION_DELAY:" + order.getId() + ":" + today);
        }
        log.debug("Delay scan complete: {} order(s) flagged", candidates.size());
    }
}
