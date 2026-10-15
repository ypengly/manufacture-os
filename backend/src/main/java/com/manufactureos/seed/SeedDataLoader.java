package com.manufactureos.seed;

import com.manufactureos.dto.inventoryOps.ReceiveMaterialRequest;
import com.manufactureos.entity.*;
import com.manufactureos.repository.*;
import com.manufactureos.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Populates a minimal but coherent dataset for local development and demos: one user
 * per role, a factory with its lines/workstations/storage, suppliers and materials, one
 * product with an ACTIVE BOM, two machines, a shift and two workers, plus opening raw
 * material stock received through InventoryService (so the normal batch/transaction
 * rules apply to the seed data too, not a shortcut around them).
 *
 * Gated by manufactureos.seed.enabled, which defaults to false and must stay false in
 * any production configuration - the seeded accounts below use a single publicly
 * documented password and would be a serious credential-stuffing risk if ever enabled
 * against a real deployment.
 */
@Component
@ConditionalOnProperty(prefix = "manufactureos.seed", name = "enabled", havingValue = "true")
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);
    private static final String DEV_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final ProductionLineRepository productionLineRepository;
    private final WorkstationRepository workstationRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;
    private final BomComponentRepository bomComponentRepository;
    private final MachineRepository machineRepository;
    private final ShiftRepository shiftRepository;
    private final WorkerRepository workerRepository;
    private final WorkerSkillRepository workerSkillRepository;
    private final InventoryService inventoryService;
    private final PasswordEncoder passwordEncoder;

    public SeedDataLoader(UserRepository userRepository, FactoryRepository factoryRepository,
                           DepartmentRepository departmentRepository, ProductionLineRepository productionLineRepository,
                           WorkstationRepository workstationRepository, StorageLocationRepository storageLocationRepository,
                           SupplierRepository supplierRepository, MaterialRepository materialRepository,
                           ProductRepository productRepository, BomRepository bomRepository,
                           BomComponentRepository bomComponentRepository, MachineRepository machineRepository,
                           ShiftRepository shiftRepository, WorkerRepository workerRepository,
                           WorkerSkillRepository workerSkillRepository, InventoryService inventoryService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.factoryRepository = factoryRepository;
        this.departmentRepository = departmentRepository;
        this.productionLineRepository = productionLineRepository;
        this.workstationRepository = workstationRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
        this.productRepository = productRepository;
        this.bomRepository = bomRepository;
        this.bomComponentRepository = bomComponentRepository;
        this.machineRepository = machineRepository;
        this.shiftRepository = shiftRepository;
        this.workerRepository = workerRepository;
        this.workerSkillRepository = workerSkillRepository;
        this.inventoryService = inventoryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Seed data already present - skipping.");
            return;
        }
        log.warn("Loading DEV SEED DATA. This must never run against a production database "
                + "(manufactureos.seed.enabled must stay false there).");

        seedUsers();

        Factory factory = seedFactory();
        Department department = seedDepartment(factory);
        ProductionLine line = seedLine(factory, department);
        seedWorkstation(line);
        StorageLocation rawLocation = seedStorageLocation(factory, "RM-01", "Raw Material Store", "RAW_MATERIAL");
        seedStorageLocation(factory, "FG-01", "Finished Goods Store", "FINISHED_GOOD");

        Supplier supplier = seedSupplier();
        Material steel = seedMaterial("MAT-STEEL", "Steel Sheet 2mm", "RAW_MATERIAL", "KG", supplier,
                BigDecimal.valueOf(50), BigDecimal.valueOf(100), BigDecimal.valueOf(3.25));
        Material bolt = seedMaterial("MAT-BOLT", "M8 Bolt", "RAW_MATERIAL", "EA", supplier,
                BigDecimal.valueOf(500), BigDecimal.valueOf(1000), BigDecimal.valueOf(0.08));

        Product product = seedProduct();
        seedActiveBom(product, steel, bolt);

        seedMachines(factory, line);
        Shift shift = seedShift(factory);
        seedWorkers(shift);

        receiveOpeningStock(steel, factory, rawLocation, supplier, BigDecimal.valueOf(500));
        receiveOpeningStock(bolt, factory, rawLocation, supplier, BigDecimal.valueOf(5000));

        log.info("Seed data loaded. Dev login: any of admin/production_manager/supervisor/quality_inspector/"
                + "inventory_manager/worker, password '{}'.", DEV_PASSWORD);
    }

    private void seedUsers() {
        for (Role role : Role.values()) {
            User u = new User();
            u.setUsername(role.name().toLowerCase());
            u.setEmail(role.name().toLowerCase() + "@manufactureos.dev");
            u.setPasswordHash(passwordEncoder.encode(DEV_PASSWORD));
            u.setFullName(toTitleCase(role.name()) + " (Dev)");
            u.setActive(true);
            u.setCreatedAt(Instant.now());
            u.setRoles(Set.of(role));
            userRepository.save(u);
        }
    }

    private Factory seedFactory() {
        Factory f = new Factory();
        f.setCode("FAC-01");
        f.setName("Riverside Manufacturing Plant");
        f.setAddress("100 Industrial Way, Springfield");
        f.setManagerName("Pat Rivera");
        f.setStatus("ACTIVE");
        f.setTimeZone("UTC");
        f.setCreatedAt(Instant.now());
        return factoryRepository.save(f);
    }

    private Department seedDepartment(Factory factory) {
        Department d = new Department();
        d.setFactoryId(factory.getId());
        d.setName("Assembly");
        return departmentRepository.save(d);
    }

    private ProductionLine seedLine(Factory factory, Department department) {
        ProductionLine line = new ProductionLine();
        line.setFactoryId(factory.getId());
        line.setDepartmentId(department.getId());
        line.setCode("LINE-A");
        line.setName("Assembly Line A");
        line.setActive(true);
        return productionLineRepository.save(line);
    }

    private Workstation seedWorkstation(ProductionLine line) {
        Workstation ws = new Workstation();
        ws.setLineId(line.getId());
        ws.setName("Station 1");
        return workstationRepository.save(ws);
    }

    private StorageLocation seedStorageLocation(Factory factory, String code, String name, String type) {
        StorageLocation loc = new StorageLocation();
        loc.setFactoryId(factory.getId());
        loc.setCode(code);
        loc.setName(name);
        loc.setType(type);
        return storageLocationRepository.save(loc);
    }

    private Supplier seedSupplier() {
        Supplier s = new Supplier();
        s.setCode("SUP-01");
        s.setCompanyName("Acme Raw Materials Co.");
        s.setContactPerson("Jordan Lee");
        s.setEmail("sales@acme-raw.example");
        s.setPhone("555-0100");
        s.setAddress("200 Commerce Blvd");
        s.setStatus("ACTIVE");
        return supplierRepository.save(s);
    }

    private Material seedMaterial(String code, String name, String category, String uom, Supplier supplier,
                                   BigDecimal minStock, BigDecimal reorderLevel, BigDecimal standardCost) {
        Material m = new Material();
        m.setCode(code);
        m.setName(name);
        m.setCategory(category);
        m.setUnitOfMeasure(uom);
        m.setMinStockLevel(minStock);
        m.setReorderLevel(reorderLevel);
        m.setStandardCost(standardCost);
        m.setSupplierId(supplier.getId());
        m.setActive(true);
        return materialRepository.save(m);
    }

    private Product seedProduct() {
        Product p = new Product();
        p.setCode("PROD-BRACKET");
        p.setSku("SKU-BRK-100");
        p.setName("Steel Mounting Bracket");
        p.setDescription("Welded steel mounting bracket assembly");
        p.setCategory("FINISHED_GOOD");
        p.setVersion(1);
        p.setUnitOfMeasure("EA");
        p.setStandardMinutes(BigDecimal.valueOf(12));
        p.setStandardCost(BigDecimal.valueOf(9.50));
        p.setSellingPrice(BigDecimal.valueOf(24.99));
        p.setActive(true);
        return productRepository.save(p);
    }

    private void seedActiveBom(Product product, Material steel, Material bolt) {
        Bom bom = new Bom();
        bom.setBomNumber("BOM-BRACKET-1");
        bom.setProductId(product.getId());
        bom.setVersion(1);
        bom.setStatus(BomStatus.ACTIVE);
        bom.setEffectiveFrom(LocalDate.now().minusDays(1));
        bom.setApprovedAt(Instant.now());
        bom.setCreatedAt(Instant.now());
        bom = bomRepository.save(bom);

        BomComponent c1 = new BomComponent();
        c1.setBomId(bom.getId());
        c1.setMaterialId(steel.getId());
        c1.setQuantity(BigDecimal.valueOf(0.75));
        c1.setWastePercent(BigDecimal.valueOf(5));
        bomComponentRepository.save(c1);

        BomComponent c2 = new BomComponent();
        c2.setBomId(bom.getId());
        c2.setMaterialId(bolt.getId());
        c2.setQuantity(BigDecimal.valueOf(4));
        c2.setWastePercent(BigDecimal.ZERO);
        bomComponentRepository.save(c2);
    }

    private void seedMachines(Factory factory, ProductionLine line) {
        Machine press = new Machine();
        press.setCode("MCH-PRESS-1");
        press.setName("Hydraulic Press 1");
        press.setMachineType("PRESS");
        press.setFactoryId(factory.getId());
        press.setLineId(line.getId());
        press.setStatus(MachineStatus.AVAILABLE);
        press.setCapacityPerHour(BigDecimal.valueOf(40));
        press.setCrewSize(1);
        press.setRequiredSkill("Press Operation");
        press.setOperatingHours(BigDecimal.ZERO);
        press.setMaintenanceIntervalDays(90);
        press.setLastMaintenanceDate(LocalDate.now().minusDays(30));
        press.setNextMaintenanceDate(LocalDate.now().plusDays(60));
        machineRepository.save(press);

        Machine welder = new Machine();
        welder.setCode("MCH-WELD-1");
        welder.setName("Robotic Welder 1");
        welder.setMachineType("WELDER");
        welder.setFactoryId(factory.getId());
        welder.setLineId(line.getId());
        welder.setStatus(MachineStatus.AVAILABLE);
        welder.setCapacityPerHour(BigDecimal.valueOf(25));
        welder.setCrewSize(1);
        welder.setRequiredSkill("Welding");
        welder.setOperatingHours(BigDecimal.ZERO);
        welder.setMaintenanceIntervalDays(60);
        welder.setLastMaintenanceDate(LocalDate.now().minusDays(50));
        welder.setNextMaintenanceDate(LocalDate.now().plusDays(10));
        machineRepository.save(welder);
    }

    private Shift seedShift(Factory factory) {
        Shift shift = new Shift();
        shift.setName("Day Shift");
        shift.setStartTime(LocalTime.of(7, 0));
        shift.setEndTime(LocalTime.of(15, 0));
        shift.setFactoryId(factory.getId());
        shift.setDepartment("Assembly");
        shift.setStatus("ACTIVE");
        return shiftRepository.save(shift);
    }

    private void seedWorkers(Shift shift) {
        User workerUser = userRepository.findByUsernameIgnoreCase("worker").orElse(null);

        Worker w1 = new Worker();
        w1.setEmployeeId("EMP-001");
        w1.setFullName("Alex Morgan");
        w1.setEmail("alex.morgan@manufactureos.dev");
        w1.setPhone("555-0111");
        w1.setDepartment("Assembly");
        w1.setJobTitle("Machine Operator");
        w1.setSkillLevel(3);
        w1.setShiftId(shift.getId());
        w1.setUserId(workerUser != null ? workerUser.getId() : null);
        w1.setEmploymentStatus("ACTIVE");
        w1.setHireDate(LocalDate.now().minusYears(2));
        w1 = workerRepository.save(w1);

        WorkerSkill skill1 = new WorkerSkill();
        skill1.setWorkerId(w1.getId());
        skill1.setSkill("Press Operation");
        workerSkillRepository.save(skill1);

        WorkerSkill skill2 = new WorkerSkill();
        skill2.setWorkerId(w1.getId());
        skill2.setSkill("Welding");
        workerSkillRepository.save(skill2);

        Worker w2 = new Worker();
        w2.setEmployeeId("EMP-002");
        w2.setFullName("Sam Taylor");
        w2.setEmail("sam.taylor@manufactureos.dev");
        w2.setPhone("555-0112");
        w2.setDepartment("Assembly");
        w2.setJobTitle("Assembler");
        w2.setSkillLevel(2);
        w2.setShiftId(shift.getId());
        w2.setEmploymentStatus("ACTIVE");
        w2.setHireDate(LocalDate.now().minusMonths(8));
        workerRepository.save(w2);
    }

    private void receiveOpeningStock(Material material, Factory factory, StorageLocation location,
                                      Supplier supplier, BigDecimal quantity) {
        ReceiveMaterialRequest req = new ReceiveMaterialRequest();
        req.setMaterialId(material.getId());
        req.setFactoryId(factory.getId());
        req.setLocationId(location.getId());
        req.setSupplierId(supplier.getId());
        req.setQuantity(quantity);
        req.setUnitCost(material.getStandardCost());
        inventoryService.receive(req);
    }

    private String toTitleCase(String enumName) {
        String[] parts = enumName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
