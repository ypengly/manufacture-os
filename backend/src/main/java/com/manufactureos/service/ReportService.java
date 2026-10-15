package com.manufactureos.service;

import com.manufactureos.entity.MachineTask;
import com.manufactureos.entity.ProductionOrder;
import com.manufactureos.entity.ProductionReport;
import com.manufactureos.entity.QualityDefect;
import com.manufactureos.entity.QualityInspection;
import com.manufactureos.entity.WorkerAssignment;
import com.manufactureos.repository.MachineTaskRepository;
import com.manufactureos.repository.ProductionOrderRepository;
import com.manufactureos.repository.ProductionReportRepository;
import com.manufactureos.repository.QualityDefectRepository;
import com.manufactureos.repository.QualityInspectionRepository;
import com.manufactureos.repository.WorkerAssignmentRepository;
import com.manufactureos.util.CsvWriter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Produces CSV exports for the reporting views (production, inventory, machine,
 * quality, worker), each filterable by factory/line and a date range applied to the
 * record's own timestamp. These read straight from the same repositories the rest of
 * the app uses - there is no separate reporting/warehouse store to keep in sync.
 */
@Service
public class ReportService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionReportRepository productionReportRepository;
    private final MachineTaskRepository machineTaskRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityDefectRepository qualityDefectRepository;
    private final WorkerAssignmentRepository workerAssignmentRepository;

    public ReportService(ProductionOrderRepository productionOrderRepository,
                          ProductionReportRepository productionReportRepository,
                          MachineTaskRepository machineTaskRepository,
                          QualityInspectionRepository qualityInspectionRepository,
                          QualityDefectRepository qualityDefectRepository,
                          WorkerAssignmentRepository workerAssignmentRepository) {
        this.productionOrderRepository = productionOrderRepository;
        this.productionReportRepository = productionReportRepository;
        this.machineTaskRepository = machineTaskRepository;
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.qualityDefectRepository = qualityDefectRepository;
        this.workerAssignmentRepository = workerAssignmentRepository;
    }

    public String productionReportCsv(Long factoryId, Long lineId, Instant from, Instant to) {
        List<ProductionOrder> orders = productionOrderRepository.findAll().stream()
                .filter(o -> factoryId == null || factoryId.equals(o.getFactoryId()))
                .filter(o -> lineId == null || lineId.equals(o.getLineId()))
                .filter(o -> inRange(o.getCreatedAt(), from, to))
                .toList();

        List<List<Object>> rows = new ArrayList<>();
        for (ProductionOrder o : orders) {
            rows.add(List.of(o.getId(), nullToEmpty(o.getOrderNumber()), o.getProductId(), o.getFactoryId(),
                    nullToEmpty(o.getLineId()), o.getStatus(), o.getPlannedQuantity(), o.getCompletedQuantity(),
                    o.getRejectedQuantity(), nullToEmpty(o.getPlannedStart()), nullToEmpty(o.getActualStart()),
                    nullToEmpty(o.getActualEnd()), o.getCreatedAt()));
        }
        return CsvWriter.write(List.of("id", "orderNumber", "productId", "factoryId", "lineId", "status",
                "plannedQuantity", "completedQuantity", "rejectedQuantity", "plannedStart", "actualStart",
                "actualEnd", "createdAt"), rows);
    }

    public String machineUtilizationCsv(Instant from, Instant to) {
        List<MachineTask> tasks = machineTaskRepository.findAll().stream()
                .filter(t -> inRange(t.getCreatedAt(), from, to))
                .toList();
        List<List<Object>> rows = new ArrayList<>();
        for (MachineTask t : tasks) {
            rows.add(List.of(t.getId(), t.getMachineId(), nullToEmpty(t.getProductionOrderId()), t.getStatus(),
                    t.getStartTime(), t.getEndTime(), nullToEmpty(t.getActualStart()), nullToEmpty(t.getActualEnd())));
        }
        return CsvWriter.write(List.of("taskId", "machineId", "productionOrderId", "status", "scheduledStart",
                "scheduledEnd", "actualStart", "actualEnd"), rows);
    }

    public String qualityReportCsv(Long productionOrderId, Instant from, Instant to) {
        List<QualityInspection> inspections = (productionOrderId != null
                ? qualityInspectionRepository.findByProductionOrderId(productionOrderId)
                : qualityInspectionRepository.findAll()).stream()
                .filter(i -> inRange(i.getInspectedAt(), from, to))
                .toList();
        List<List<Object>> rows = new ArrayList<>();
        for (QualityInspection i : inspections) {
            rows.add(List.of(i.getId(), nullToEmpty(i.getInspectionNumber()), i.getProductionOrderId(),
                    i.getProductId(), i.getResult(), i.getPassedQuantity(), i.getFailedQuantity(),
                    i.getFinalized(), i.getInspectedAt()));
        }
        return CsvWriter.write(List.of("inspectionId", "inspectionNumber", "productionOrderId", "productId",
                "result", "passedQuantity", "failedQuantity", "finalized", "inspectedAt"), rows);
    }

    public String defectReportCsv(Long productionOrderId, Instant from, Instant to) {
        List<QualityDefect> defects = (productionOrderId != null
                ? qualityDefectRepository.findByProductionOrderId(productionOrderId)
                : qualityDefectRepository.findAll()).stream()
                .filter(d -> inRange(d.getCreatedAt(), from, to))
                .toList();
        List<List<Object>> rows = new ArrayList<>();
        for (QualityDefect d : defects) {
            rows.add(List.of(d.getId(), nullToEmpty(d.getDefectCode()), nullToEmpty(d.getDefectType()),
                    d.getSeverity(), d.getStatus(), nullToEmpty(d.getProductionOrderId()), d.getCreatedAt()));
        }
        return CsvWriter.write(List.of("defectId", "defectCode", "defectType", "severity", "status",
                "productionOrderId", "createdAt"), rows);
    }

    public String workerPerformanceCsv(Long workerId, Instant from, Instant to) {
        List<WorkerAssignment> assignments = (workerId != null
                ? workerAssignmentRepository.findByWorkerId(workerId)
                : workerAssignmentRepository.findAll()).stream()
                .filter(a -> inRange(a.getCreatedAt(), from, to))
                .toList();
        List<List<Object>> rows = new ArrayList<>();
        for (WorkerAssignment a : assignments) {
            rows.add(List.of(a.getId(), a.getWorkerId(), nullToEmpty(a.getMachineTaskId()),
                    nullToEmpty(a.getProductionOrderId()), a.getStatus(), a.getStartTime(), a.getEndTime()));
        }
        return CsvWriter.write(List.of("assignmentId", "workerId", "machineTaskId", "productionOrderId",
                "status", "startTime", "endTime"), rows);
    }

    public String productionReportsCsv(Long productionOrderId, Instant from, Instant to) {
        List<ProductionReport> reports = (productionOrderId != null
                ? productionReportRepository.findByProductionOrderId(productionOrderId)
                : productionReportRepository.findAll()).stream()
                .filter(r -> inRange(r.getCreatedAt(), from, to))
                .toList();
        List<List<Object>> rows = new ArrayList<>();
        for (ProductionReport r : reports) {
            rows.add(List.of(r.getId(), r.getProductionOrderId(), nullToEmpty(r.getUserId()),
                    r.getCompletedQuantity(), r.getRejectedQuantity(), nullToEmpty(r.getDowntimeMinutes()),
                    r.getCreatedAt()));
        }
        return CsvWriter.write(List.of("reportId", "productionOrderId", "userId", "completedQuantity",
                "rejectedQuantity", "downtimeMinutes", "createdAt"), rows);
    }

    private boolean inRange(Instant value, Instant from, Instant to) {
        if (value == null) {
            return from == null && to == null;
        }
        if (from != null && value.isBefore(from)) {
            return false;
        }
        return to == null || !value.isAfter(to);
    }

    private Object nullToEmpty(Object value) {
        return value == null ? "" : value;
    }
}
