package com.manufactureos.controller;

import com.manufactureos.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'QUALITY_INSPECTOR', 'INVENTORY_MANAGER')")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/production")
    public ResponseEntity<byte[]> production(@RequestParam(required = false) Long factoryId,
                                              @RequestParam(required = false) Long lineId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("production-orders.csv", service.productionReportCsv(factoryId, lineId, from, to));
    }

    @GetMapping("/production/output")
    public ResponseEntity<byte[]> productionOutput(@RequestParam(required = false) Long productionOrderId,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("production-output.csv", service.productionReportsCsv(productionOrderId, from, to));
    }

    @GetMapping("/machines")
    public ResponseEntity<byte[]> machines(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("machine-utilization.csv", service.machineUtilizationCsv(from, to));
    }

    @GetMapping("/quality")
    public ResponseEntity<byte[]> quality(@RequestParam(required = false) Long productionOrderId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("quality-inspections.csv", service.qualityReportCsv(productionOrderId, from, to));
    }

    @GetMapping("/defects")
    public ResponseEntity<byte[]> defects(@RequestParam(required = false) Long productionOrderId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("quality-defects.csv", service.defectReportCsv(productionOrderId, from, to));
    }

    @GetMapping("/workers")
    public ResponseEntity<byte[]> workers(@RequestParam(required = false) Long workerId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return csv("worker-performance.csv", service.workerPerformanceCsv(workerId, from, to));
    }

    private ResponseEntity<byte[]> csv(String filename, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
