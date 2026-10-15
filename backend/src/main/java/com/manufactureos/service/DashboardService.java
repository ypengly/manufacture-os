package com.manufactureos.service;

import com.manufactureos.entity.DefectStatus;
import com.manufactureos.entity.MachineStatus;
import com.manufactureos.entity.Material;
import com.manufactureos.entity.OrderStatus;
import com.manufactureos.entity.PurchaseOrderStatus;
import com.manufactureos.entity.ReworkStatus;
import com.manufactureos.entity.Worker;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.repository.MaterialRepository;
import com.manufactureos.repository.NotificationRepository;
import com.manufactureos.repository.ProductionOrderRepository;
import com.manufactureos.repository.PurchaseOrderRepository;
import com.manufactureos.repository.QualityDefectRepository;
import com.manufactureos.repository.QualityInspectionRepository;
import com.manufactureos.repository.ReworkOrderRepository;
import com.manufactureos.repository.WorkerAssignmentRepository;
import com.manufactureos.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes role-specific dashboard summaries directly from the repositories - every
 * number here is a live aggregate query against the current data, not a cached or
 * mocked figure.
 */
@Service
public class DashboardService {

    private final ProductionOrderRepository productionOrderRepository;
    private final MachineRepository machineRepository;
    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityDefectRepository qualityDefectRepository;
    private final ReworkOrderRepository reworkOrderRepository;
    private final NotificationRepository notificationRepository;
    private final WorkerRepository workerRepository;
    private final WorkerAssignmentRepository workerAssignmentRepository;

    public DashboardService(ProductionOrderRepository productionOrderRepository,
                             MachineRepository machineRepository,
                             MaterialRepository materialRepository,
                             InventoryRepository inventoryRepository,
                             PurchaseOrderRepository purchaseOrderRepository,
                             QualityInspectionRepository qualityInspectionRepository,
                             QualityDefectRepository qualityDefectRepository,
                             ReworkOrderRepository reworkOrderRepository,
                             NotificationRepository notificationRepository,
                             WorkerRepository workerRepository,
                             WorkerAssignmentRepository workerAssignmentRepository) {
        this.productionOrderRepository = productionOrderRepository;
        this.machineRepository = machineRepository;
        this.materialRepository = materialRepository;
        this.inventoryRepository = inventoryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.qualityDefectRepository = qualityDefectRepository;
        this.reworkOrderRepository = reworkOrderRepository;
        this.notificationRepository = notificationRepository;
        this.workerRepository = workerRepository;
        this.workerAssignmentRepository = workerAssignmentRepository;
    }

    public Map<String, Object> adminSummary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ordersInProgress", productionOrderRepository.countByStatus(OrderStatus.IN_PROGRESS));
        m.put("ordersPendingApproval", productionOrderRepository.countByStatus(OrderStatus.PENDING_APPROVAL));
        m.put("ordersOnHold", productionOrderRepository.countByStatus(OrderStatus.ON_HOLD));
        m.put("machinesAvailable", machineRepository.countByStatus(MachineStatus.AVAILABLE));
        m.put("machinesInUse", machineRepository.countByStatus(MachineStatus.IN_USE));
        m.put("machinesBroken", machineRepository.countByStatus(MachineStatus.BROKEN));
        m.put("machinesInMaintenance", machineRepository.countByStatus(MachineStatus.MAINTENANCE));
        m.put("lowStockMaterials", lowStockCount());
        m.put("pendingInspections", qualityInspectionRepository.countByFinalizedFalse());
        m.put("openDefects", qualityDefectRepository.countByStatus(DefectStatus.OPEN));
        m.put("purchaseOrdersAwaitingApproval", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SUBMITTED));
        return m;
    }

    public Map<String, Object> productionManagerSummary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ordersPendingApproval", productionOrderRepository.countByStatus(OrderStatus.PENDING_APPROVAL));
        m.put("ordersApproved", productionOrderRepository.countByStatus(OrderStatus.APPROVED));
        m.put("ordersInProgress", productionOrderRepository.countByStatus(OrderStatus.IN_PROGRESS));
        m.put("ordersOnHold", productionOrderRepository.countByStatus(OrderStatus.ON_HOLD));
        m.put("ordersCompleted", productionOrderRepository.countByStatus(OrderStatus.COMPLETED));
        m.put("purchaseOrdersAwaitingApproval", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SUBMITTED));
        m.put("lowStockMaterials", lowStockCount());
        return m;
    }

    public Map<String, Object> supervisorSummary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ordersInProgress", productionOrderRepository.countByStatus(OrderStatus.IN_PROGRESS));
        m.put("ordersReady", productionOrderRepository.countByStatus(OrderStatus.READY));
        m.put("ordersPaused", productionOrderRepository.countByStatus(OrderStatus.PAUSED));
        m.put("machinesInUse", machineRepository.countByStatus(MachineStatus.IN_USE));
        m.put("machinesBroken", machineRepository.countByStatus(MachineStatus.BROKEN));
        m.put("openDefects", qualityDefectRepository.countByStatus(DefectStatus.OPEN));
        m.put("reworkInProgress", reworkOrderRepository.countByStatus(ReworkStatus.IN_PROGRESS));
        return m;
    }

    public Map<String, Object> qualityInspectorSummary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pendingInspections", qualityInspectionRepository.countByFinalizedFalse());
        m.put("openDefects", qualityDefectRepository.countByStatus(DefectStatus.OPEN));
        m.put("defectsUnderReview", qualityDefectRepository.countByStatus(DefectStatus.UNDER_REVIEW));
        m.put("reworkRequested", reworkOrderRepository.countByStatus(ReworkStatus.REQUESTED));
        m.put("reworkInProgress", reworkOrderRepository.countByStatus(ReworkStatus.IN_PROGRESS));
        return m;
    }

    public Map<String, Object> inventoryManagerSummary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lowStockMaterials", lowStockCount());
        m.put("purchaseOrdersApproved", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.APPROVED));
        m.put("purchaseOrdersPartiallyReceived", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED));
        m.put("purchaseOrdersSubmitted", purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SUBMITTED));
        return m;
    }

    public Map<String, Object> workerSummary(Long userId) {
        Map<String, Object> m = new LinkedHashMap<>();
        Worker worker = workerRepository.findByUserId(userId).orElse(null);
        if (worker == null) {
            m.put("activeAssignments", 0);
            m.put("note", "No worker profile is linked to this user account.");
            return m;
        }
        long active = workerAssignmentRepository.findByWorkerId(worker.getId()).stream()
                .filter(a -> a.getStatus() == com.manufactureos.entity.AssignmentStatus.ACTIVE)
                .count();
        m.put("workerId", worker.getId());
        m.put("activeAssignments", active);
        return m;
    }

    /** Active materials whose total available raw-material stock has fallen to or below their reorder level. */
    private long lowStockCount() {
        List<Material> materials = materialRepository.findByActiveTrue();
        return materials.stream()
                .filter(m -> m.getReorderLevel() != null)
                .filter(m -> inventoryRepository.totalAvailableForMaterialAnyFactory(m.getId())
                        .compareTo(m.getReorderLevel()) <= 0)
                .count();
    }
}
