// Mirrors the backend's enums and response DTOs. Kept intentionally plain (no class
// instances) since everything here is just JSON coming back from the API.

export type Role =
  | 'ADMIN'
  | 'PRODUCTION_MANAGER'
  | 'SUPERVISOR'
  | 'QUALITY_INSPECTOR'
  | 'INVENTORY_MANAGER'
  | 'WORKER'

export interface CurrentUser {
  id: number
  username: string
  email: string
  fullName: string
  roles: Role[]
}

export type OrderStatus =
  | 'DRAFT' | 'PLANNED' | 'PENDING_APPROVAL' | 'APPROVED' | 'MATERIALS_RESERVED'
  | 'READY' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED' | 'CANCELLED' | 'ON_HOLD'

export type BomStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'ACTIVE' | 'ARCHIVED'

export type InventoryCategory = 'RAW_MATERIAL' | 'WORK_IN_PROGRESS' | 'FINISHED_GOOD' | 'DAMAGED' | 'QUARANTINED'

export type MachineStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'BROKEN' | 'INACTIVE'
export type TaskStatus = 'SCHEDULED' | 'READY' | 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
export type MaintenanceStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type MaintenanceType = 'PREVENTIVE' | 'CORRECTIVE' | 'EMERGENCY' | 'INSPECTION'
export type AssignmentStatus = 'ACTIVE' | 'REASSIGNED' | 'CANCELLED' | 'COMPLETED'

export type InspectionResult = 'PENDING' | 'PASSED' | 'FAILED' | 'PARTIALLY_PASSED' | 'QUARANTINED'
export type DefectStatus = 'OPEN' | 'UNDER_REVIEW' | 'CORRECTIVE_ACTION' | 'RESOLVED' | 'CLOSED'
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type ReworkStatus = 'REQUESTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type OutputStatus = 'PENDING_INSPECTION' | 'ACCEPTED' | 'REJECTED' | 'QUARANTINED' | 'RELEASED'

export type PurchaseOrderStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED'

export type NotificationType =
  | 'LOW_STOCK' | 'MATERIAL_SHORTAGE' | 'PRODUCTION_DELAY' | 'MAINTENANCE_DUE' | 'MACHINE_FAILURE'
  | 'INSPECTION_PENDING' | 'BATCH_FAILED' | 'RECEIVING_EVENT' | 'ORDER_APPROVAL' | 'GENERAL'

export interface ProductionOrderItem {
  id: number
  productionOrderId: number
  materialId: number
  requiredQuantity: number
  reservedQuantity: number
  consumedQuantity: number
}

export interface ProductionOrder {
  id: number
  orderNumber: string
  productId: number
  bomId: number
  plannedQuantity: number
  completedQuantity: number
  rejectedQuantity: number
  factoryId: number
  lineId: number | null
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  plannedStart: string | null
  plannedEnd: string | null
  actualStart: string | null
  actualEnd: string | null
  status: OrderStatus
  allowPartialMaterials: boolean | null
  statusReason: string | null
  createdBy: number | null
  approvedBy: number | null
  createdAt: string
  version: number
  items: ProductionOrderItem[]
}

export interface MaterialRequirementView {
  materialId: number
  requiredQuantity: number
  availableQuantity: number
  shortageQuantity: number
}

export interface InventoryView {
  id: number
  factoryId: number
  locationId: number
  batchId: number
  batchNumber: string | null
  materialId: number | null
  productId: number | null
  category: InventoryCategory
  quantityOnHand: number
  reservedQuantity: number
  availableQuantity: number
  unitCost: number
  expirationDate: string | null
  updatedAt: string | null
}

export interface InventoryTransaction {
  id: number
  transactionNumber: string
  inventoryId: number
  batchId: number
  materialId: number | null
  productId: number | null
  factoryId: number
  locationId: number
  type: string
  quantity: number
  referenceType: string | null
  referenceId: number | null
  previousQuantity: number
  newQuantity: number
  notes: string | null
  createdAt: string
}

export interface Machine {
  id: number
  code: string
  name: string
  machineType: string
  factoryId: number
  lineId: number | null
  status: MachineStatus
  capacityPerHour: number
  crewSize: number
  requiredSkill: string | null
  maintenanceIntervalDays: number | null
  lastMaintenanceDate: string | null
  nextMaintenanceDate: string | null
}

export interface MachineTask {
  id: number
  machineId: number
  productionOrderId: number
  startTime: string
  endTime: string
  status: TaskStatus
  actualStart: string | null
  actualEnd: string | null
}

export interface MachineMaintenance {
  id: number
  machineId: number
  maintenanceType: MaintenanceType
  description: string | null
  scheduledStart: string
  scheduledEnd: string
  startedAt: string | null
  completedAt: string | null
  technician: string | null
  cost: number | null
  status: MaintenanceStatus
  notes: string | null
}

export interface Worker {
  id: number
  employeeId: string
  fullName: string
  email: string | null
  department: string | null
  jobTitle: string | null
  skillLevel: number | null
  shiftId: number | null
  employmentStatus: string
}

export interface WorkerAssignment {
  id: number
  workerId: number
  machineTaskId: number | null
  productionOrderId: number | null
  startTime: string
  endTime: string
  status: AssignmentStatus
  reason: string | null
}

export interface QualityInspection {
  id: number
  inspectionNumber: string
  productionOrderId: number
  productId: number
  outputId: number
  batchId: number
  inspectorId: number | null
  inspectedAt: string
  sampleSize: number | null
  passedQuantity: number
  failedQuantity: number
  result: InspectionResult
  measurements: string | null
  notes: string | null
  finalized: boolean
  rework: boolean
}

export interface QualityDefect {
  id: number
  defectCode: string
  defectType: string
  description: string | null
  severity: Severity
  productionOrderId: number | null
  productId: number | null
  batchId: number | null
  status: DefectStatus
  correctiveAction: string | null
  createdAt: string
}

export interface ReworkOrder {
  id: number
  reworkNumber: string
  outputId: number
  productionOrderId: number
  quantity: number
  passedQuantity: number
  status: ReworkStatus
  outcome: string | null
}

export interface ScrapRecord {
  id: number
  scrapNumber: string
  productionOrderId: number | null
  inventoryId: number
  outputId: number | null
  quantity: number
  reason: string
  approvedBy: number | null
  createdAt: string
}

export interface PurchaseOrderItem {
  id: number
  purchaseOrderId: number
  materialId: number
  quantity: number
  receivedQuantity: number
  unitCost: number
}

export interface PurchaseOrder {
  id: number
  poNumber: string
  supplierId: number
  factoryId: number
  status: PurchaseOrderStatus
  expectedDate: string | null
  createdBy: number | null
  approvedBy: number | null
  createdAt: string
  items: PurchaseOrderItem[]
}

export interface AppNotification {
  id: number
  recipientUserId: number | null
  recipientRole: Role | null
  title: string
  message: string
  type: NotificationType
  readStatus: boolean
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// Simple master-data shapes (generic CRUD entities) - only the fields the UI needs.
export interface Product {
  id: number
  code: string
  sku: string
  name: string
  category: string | null
  unitOfMeasure: string
  standardCost: number
  sellingPrice: number
  active: boolean
}

export interface Material {
  id: number
  code: string
  name: string
  category: string | null
  unitOfMeasure: string
  minStockLevel: number | null
  reorderLevel: number | null
  standardCost: number | null
  supplierId: number | null
  active: boolean
}

export interface Supplier {
  id: number
  code: string
  companyName: string
  contactPerson: string | null
  email: string | null
  phone: string | null
  status: string | null
}

export interface Factory {
  id: number
  code: string
  name: string
  status: string | null
}

export interface Bom {
  id: number
  bomNumber: string
  productId: number
  version: number
  status: BomStatus
  effectiveFrom: string | null
  effectiveTo: string | null
}

export interface BomComponentInput {
  materialId: number
  quantity: number
  wastePercent: number
}

export interface BomDetail extends Bom {
  components: (BomComponentInput & { id: number })[]
}
