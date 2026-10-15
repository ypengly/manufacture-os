-- ManufactureOS schema. Every table has PK/FK/NOT NULL/CHECK integrity enforced by the database itself.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ===== Security =====
CREATE TABLE roles (name VARCHAR(30) PRIMARY KEY);
INSERT INTO roles(name) VALUES ('ADMIN'),('PRODUCTION_MANAGER'),('SUPERVISOR'),('QUALITY_INSPECTOR'),('INVENTORY_MANAGER'),('WORKER');

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(60) NOT NULL UNIQUE,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  reset_token_hash VARCHAR(64),
  reset_token_expires_at TIMESTAMPTZ,
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_name VARCHAR(30) NOT NULL REFERENCES roles(name),
  PRIMARY KEY (user_id, role_name)
);
CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);

-- ===== Facilities =====
CREATE TABLE factories (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  address VARCHAR(255),
  contact_details VARCHAR(255),
  manager_name VARCHAR(120),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  time_zone VARCHAR(60) NOT NULL DEFAULT 'UTC',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE departments (
  id BIGSERIAL PRIMARY KEY,
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  name VARCHAR(120) NOT NULL,
  UNIQUE (factory_id, name)
);
CREATE TABLE production_lines (
  id BIGSERIAL PRIMARY KEY,
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  department_id BIGINT REFERENCES departments(id),
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE workstations (
  id BIGSERIAL PRIMARY KEY,
  line_id BIGINT NOT NULL REFERENCES production_lines(id),
  name VARCHAR(120) NOT NULL,
  UNIQUE (line_id, name)
);
CREATE TABLE storage_locations (
  id BIGSERIAL PRIMARY KEY,
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  code VARCHAR(30) NOT NULL,
  name VARCHAR(120) NOT NULL,
  type VARCHAR(30) NOT NULL DEFAULT 'WAREHOUSE',
  UNIQUE (factory_id, code)
);

-- ===== Master data =====
CREATE TABLE suppliers (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  company_name VARCHAR(160) NOT NULL,
  contact_person VARCHAR(120),
  email VARCHAR(160),
  phone VARCHAR(40),
  address VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  sku VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL,
  description TEXT,
  category VARCHAR(80),
  version INT NOT NULL DEFAULT 1,
  unit_of_measure VARCHAR(20) NOT NULL DEFAULT 'EA',
  standard_minutes NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (standard_minutes >= 0),
  standard_cost NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (standard_cost >= 0),
  selling_price NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (selling_price >= 0),
  target_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (target_quantity >= 0),
  active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_products_category ON products(category);
CREATE TABLE materials (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL,
  description TEXT,
  category VARCHAR(80),
  unit_of_measure VARCHAR(20) NOT NULL DEFAULT 'EA',
  min_stock_level NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (min_stock_level >= 0),
  reorder_level NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (reorder_level >= 0),
  standard_cost NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (standard_cost >= 0),
  supplier_id BIGINT REFERENCES suppliers(id),
  active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_materials_category ON materials(category);

-- ===== BOM (each row is one version of a product's BOM) =====
CREATE TABLE boms (
  id BIGSERIAL PRIMARY KEY,
  bom_number VARCHAR(40) NOT NULL UNIQUE,
  product_id BIGINT NOT NULL REFERENCES products(id),
  version INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  effective_from DATE,
  effective_to DATE,
  created_by BIGINT REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  approved_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (product_id, version),
  CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);
-- only one ACTIVE BOM per product for any effective period
ALTER TABLE boms ADD CONSTRAINT boms_no_overlap EXCLUDE USING gist (
  product_id WITH =,
  daterange(effective_from, effective_to, '[]') WITH &&
) WHERE (status = 'ACTIVE');
CREATE TABLE bom_components (
  id BIGSERIAL PRIMARY KEY,
  bom_id BIGINT NOT NULL REFERENCES boms(id) ON DELETE CASCADE,
  material_id BIGINT NOT NULL REFERENCES materials(id),
  quantity NUMERIC(14,4) NOT NULL CHECK (quantity > 0),
  waste_percent NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (waste_percent >= 0 AND waste_percent < 100),
  UNIQUE (bom_id, material_id)
);

-- ===== Purchasing =====
CREATE TABLE purchase_orders (
  id BIGSERIAL PRIMARY KEY,
  po_number VARCHAR(40) NOT NULL UNIQUE,
  supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  expected_date DATE,
  created_by BIGINT REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE purchase_order_items (
  id BIGSERIAL PRIMARY KEY,
  purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
  material_id BIGINT NOT NULL REFERENCES materials(id),
  quantity NUMERIC(14,4) NOT NULL CHECK (quantity > 0),
  received_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (received_quantity >= 0),
  unit_cost NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (unit_cost >= 0),
  CHECK (received_quantity <= quantity)
);

-- ===== Production orders =====
CREATE TABLE production_orders (
  id BIGSERIAL PRIMARY KEY,
  order_number VARCHAR(40) NOT NULL UNIQUE,
  product_id BIGINT NOT NULL REFERENCES products(id),
  bom_id BIGINT NOT NULL REFERENCES boms(id),
  planned_quantity NUMERIC(14,4) NOT NULL CHECK (planned_quantity > 0),
  completed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (completed_quantity >= 0),
  rejected_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (rejected_quantity >= 0),
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  line_id BIGINT REFERENCES production_lines(id),
  priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
  planned_start TIMESTAMPTZ NOT NULL,
  planned_end TIMESTAMPTZ NOT NULL,
  actual_start TIMESTAMPTZ,
  actual_end TIMESTAMPTZ,
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  status_before_hold VARCHAR(30),
  allow_partial_materials BOOLEAN NOT NULL DEFAULT FALSE,
  schedule_override_reason VARCHAR(255),
  status_reason VARCHAR(255),
  created_by BIGINT REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0,
  CHECK (planned_end > planned_start),
  CHECK (completed_quantity + rejected_quantity <= planned_quantity)
);
CREATE INDEX idx_po_status ON production_orders(status);
CREATE INDEX idx_po_line ON production_orders(line_id, planned_start, planned_end);
CREATE TABLE production_order_items (
  id BIGSERIAL PRIMARY KEY,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
  material_id BIGINT NOT NULL REFERENCES materials(id),
  required_quantity NUMERIC(14,4) NOT NULL CHECK (required_quantity > 0),
  reserved_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
  consumed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (consumed_quantity >= 0),
  UNIQUE (production_order_id, material_id)
);
CREATE TABLE production_reports (
  id BIGSERIAL PRIMARY KEY,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  completed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (completed_quantity >= 0),
  rejected_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (rejected_quantity >= 0),
  downtime_minutes INT NOT NULL DEFAULT 0 CHECK (downtime_minutes >= 0),
  notes VARCHAR(500),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reports_order ON production_reports(production_order_id);

-- ===== Inventory =====
CREATE TABLE inventory_batches (
  id BIGSERIAL PRIMARY KEY,
  batch_number VARCHAR(60) NOT NULL UNIQUE,
  material_id BIGINT REFERENCES materials(id),
  product_id BIGINT REFERENCES products(id),
  supplier_id BIGINT REFERENCES suppliers(id),
  purchase_order_id BIGINT REFERENCES purchase_orders(id),
  production_order_id BIGINT REFERENCES production_orders(id),
  manufacturing_date DATE,
  expiration_date DATE,
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((material_id IS NOT NULL AND product_id IS NULL) OR (material_id IS NULL AND product_id IS NOT NULL))
);
CREATE INDEX idx_batch_material ON inventory_batches(material_id);
CREATE INDEX idx_batch_product ON inventory_batches(product_id);
CREATE TABLE inventory (
  id BIGSERIAL PRIMARY KEY,
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  location_id BIGINT NOT NULL REFERENCES storage_locations(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  category VARCHAR(20) NOT NULL,
  quantity_on_hand NUMERIC(18,4) NOT NULL DEFAULT 0 CHECK (quantity_on_hand >= 0),
  reserved_quantity NUMERIC(18,4) NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
  damaged_quantity NUMERIC(18,4) NOT NULL DEFAULT 0 CHECK (damaged_quantity >= 0),
  unit_cost NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (unit_cost >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (factory_id, location_id, batch_id, category),
  CHECK (reserved_quantity <= quantity_on_hand)
);
CREATE INDEX idx_inventory_batch ON inventory(batch_id);
CREATE TABLE material_reservations (
  id BIGSERIAL PRIMARY KEY,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  inventory_id BIGINT NOT NULL REFERENCES inventory(id),
  material_id BIGINT NOT NULL REFERENCES materials(id),
  quantity NUMERIC(18,4) NOT NULL CHECK (quantity > 0),
  consumed_quantity NUMERIC(18,4) NOT NULL DEFAULT 0 CHECK (consumed_quantity >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (consumed_quantity <= quantity)
);
CREATE INDEX idx_res_order ON material_reservations(production_order_id);
CREATE TABLE inventory_transactions (
  id BIGSERIAL PRIMARY KEY,
  transaction_number VARCHAR(40) NOT NULL UNIQUE,
  inventory_id BIGINT NOT NULL REFERENCES inventory(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  material_id BIGINT REFERENCES materials(id),
  product_id BIGINT REFERENCES products(id),
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  location_id BIGINT NOT NULL REFERENCES storage_locations(id),
  type VARCHAR(30) NOT NULL,
  quantity NUMERIC(18,4) NOT NULL,
  reference_type VARCHAR(40),
  reference_id BIGINT,
  previous_quantity NUMERIC(18,4) NOT NULL,
  new_quantity NUMERIC(18,4) NOT NULL,
  user_id BIGINT REFERENCES users(id),
  notes VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tx_batch ON inventory_transactions(batch_id);
CREATE INDEX idx_tx_ref ON inventory_transactions(reference_type, reference_id);
CREATE INDEX idx_tx_created ON inventory_transactions(created_at);
CREATE TABLE material_consumptions (
  id BIGSERIAL PRIMARY KEY,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  material_id BIGINT NOT NULL REFERENCES materials(id),
  reservation_id BIGINT NOT NULL REFERENCES material_reservations(id),
  inventory_id BIGINT NOT NULL REFERENCES inventory(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  quantity NUMERIC(18,4) NOT NULL CHECK (quantity > 0),
  idempotency_key VARCHAR(80) NOT NULL,
  user_id BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (idempotency_key, reservation_id)
);
CREATE INDEX idx_cons_order ON material_consumptions(production_order_id);
CREATE INDEX idx_cons_batch ON material_consumptions(batch_id);
CREATE TABLE production_outputs (
  id BIGSERIAL PRIMARY KEY,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  product_id BIGINT NOT NULL REFERENCES products(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  report_id BIGINT REFERENCES production_reports(id),
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  location_id BIGINT NOT NULL REFERENCES storage_locations(id),
  quantity NUMERIC(14,4) NOT NULL CHECK (quantity > 0),
  released_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (released_quantity >= 0),
  rejected_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (rejected_quantity >= 0),
  pending_disposition_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (pending_disposition_quantity >= 0),
  status VARCHAR(25) NOT NULL DEFAULT 'PENDING_INSPECTION',
  produced_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT NOT NULL DEFAULT 0,
  CHECK (released_quantity + rejected_quantity <= quantity)
);
CREATE INDEX idx_output_order ON production_outputs(production_order_id);

-- ===== Machines and scheduling =====
CREATE TABLE machines (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL,
  machine_type VARCHAR(60),
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  line_id BIGINT REFERENCES production_lines(id),
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  capacity_per_hour NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (capacity_per_hour >= 0),
  crew_size INT NOT NULL DEFAULT 1 CHECK (crew_size >= 1),
  required_skill VARCHAR(60),
  operating_hours NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (operating_hours >= 0),
  maintenance_interval_days INT NOT NULL DEFAULT 90 CHECK (maintenance_interval_days > 0),
  last_maintenance_date DATE,
  next_maintenance_date DATE,
  version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE machine_tasks (
  id BIGSERIAL PRIMARY KEY,
  machine_id BIGINT NOT NULL REFERENCES machines(id),
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
  actual_start TIMESTAMPTZ,
  actual_end TIMESTAMPTZ,
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (end_time > start_time)
);
-- DB-level guarantee: a machine never has two live tasks overlapping in time
ALTER TABLE machine_tasks ADD CONSTRAINT machine_tasks_no_overlap EXCLUDE USING gist (
  machine_id WITH =,
  tstzrange(start_time, end_time) WITH &&
) WHERE (status IN ('SCHEDULED','READY','RUNNING','PAUSED'));
CREATE INDEX idx_tasks_order ON machine_tasks(production_order_id);
CREATE TABLE machine_maintenance (
  id BIGSERIAL PRIMARY KEY,
  machine_id BIGINT NOT NULL REFERENCES machines(id),
  maintenance_type VARCHAR(20) NOT NULL,
  description VARCHAR(500),
  scheduled_start TIMESTAMPTZ NOT NULL,
  scheduled_end TIMESTAMPTZ NOT NULL,
  started_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  technician VARCHAR(120),
  cost NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (cost >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
  notes VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (scheduled_end > scheduled_start)
);
CREATE INDEX idx_maint_machine ON machine_maintenance(machine_id);

-- ===== Workers =====
CREATE TABLE shifts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  factory_id BIGINT NOT NULL REFERENCES factories(id),
  department VARCHAR(80),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  UNIQUE (factory_id, name)
);
CREATE TABLE workers (
  id BIGSERIAL PRIMARY KEY,
  employee_id VARCHAR(30) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160),
  phone VARCHAR(40),
  department VARCHAR(80),
  job_title VARCHAR(80),
  skill_level INT NOT NULL DEFAULT 1 CHECK (skill_level BETWEEN 1 AND 5),
  shift_id BIGINT REFERENCES shifts(id),
  user_id BIGINT UNIQUE REFERENCES users(id),
  employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  hire_date DATE
);
CREATE TABLE worker_skills (
  id BIGSERIAL PRIMARY KEY,
  worker_id BIGINT NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
  skill VARCHAR(60) NOT NULL,
  UNIQUE (worker_id, skill)
);
CREATE TABLE worker_assignments (
  id BIGSERIAL PRIMARY KEY,
  worker_id BIGINT NOT NULL REFERENCES workers(id),
  machine_task_id BIGINT NOT NULL REFERENCES machine_tasks(id),
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  reason VARCHAR(255),
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (end_time > start_time)
);
ALTER TABLE worker_assignments ADD CONSTRAINT worker_assignments_no_overlap EXCLUDE USING gist (
  worker_id WITH =,
  tstzrange(start_time, end_time) WITH &&
) WHERE (status = 'ACTIVE');
CREATE INDEX idx_wa_task ON worker_assignments(machine_task_id);

-- ===== Quality =====
CREATE TABLE quality_criteria (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id),
  name VARCHAR(120) NOT NULL,
  min_value NUMERIC(14,4),
  max_value NUMERIC(14,4),
  unit VARCHAR(20),
  mandatory BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (product_id, name)
);
CREATE TABLE quality_inspections (
  id BIGSERIAL PRIMARY KEY,
  inspection_number VARCHAR(40) NOT NULL UNIQUE,
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  product_id BIGINT NOT NULL REFERENCES products(id),
  output_id BIGINT NOT NULL REFERENCES production_outputs(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  inspector_id BIGINT REFERENCES users(id),
  inspected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  sample_size INT NOT NULL DEFAULT 0 CHECK (sample_size >= 0),
  passed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (passed_quantity >= 0),
  failed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (failed_quantity >= 0),
  result VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  measurements TEXT,
  notes VARCHAR(500),
  finalized BOOLEAN NOT NULL DEFAULT FALSE,
  rework BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_insp_output ON quality_inspections(output_id);
CREATE TABLE quality_defects (
  id BIGSERIAL PRIMARY KEY,
  defect_code VARCHAR(40) NOT NULL UNIQUE,
  defect_type VARCHAR(80) NOT NULL,
  description VARCHAR(500),
  severity VARCHAR(10) NOT NULL DEFAULT 'LOW',
  production_order_id BIGINT REFERENCES production_orders(id),
  product_id BIGINT REFERENCES products(id),
  batch_id BIGINT REFERENCES inventory_batches(id),
  reported_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  corrective_action VARCHAR(500),
  status VARCHAR(25) NOT NULL DEFAULT 'OPEN'
);
CREATE TABLE rework_orders (
  id BIGSERIAL PRIMARY KEY,
  rework_number VARCHAR(40) NOT NULL UNIQUE,
  output_id BIGINT NOT NULL REFERENCES production_outputs(id),
  production_order_id BIGINT NOT NULL REFERENCES production_orders(id),
  quantity NUMERIC(14,4) NOT NULL CHECK (quantity > 0),
  passed_quantity NUMERIC(14,4) NOT NULL DEFAULT 0 CHECK (passed_quantity >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
  assigned_worker_id BIGINT REFERENCES workers(id),
  machine_id BIGINT REFERENCES machines(id),
  notes VARCHAR(500),
  outcome VARCHAR(500),
  additional_materials VARCHAR(500),
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE scrap_records (
  id BIGSERIAL PRIMARY KEY,
  scrap_number VARCHAR(40) NOT NULL UNIQUE,
  production_order_id BIGINT REFERENCES production_orders(id),
  inventory_id BIGINT NOT NULL REFERENCES inventory(id),
  batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
  output_id BIGINT REFERENCES production_outputs(id),
  quantity NUMERIC(14,4) NOT NULL CHECK (quantity > 0),
  reason VARCHAR(255) NOT NULL,
  approved_by BIGINT REFERENCES users(id),
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ===== Notifications and audit =====
CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  recipient_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
  recipient_role VARCHAR(30) REFERENCES roles(name),
  title VARCHAR(160) NOT NULL,
  message VARCHAR(500) NOT NULL,
  type VARCHAR(40) NOT NULL,
  dedupe_key VARCHAR(120),
  read_status BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (recipient_user_id IS NOT NULL OR recipient_role IS NOT NULL)
);
CREATE INDEX idx_notif_user ON notifications(recipient_user_id, read_status);
CREATE INDEX idx_notif_role ON notifications(recipient_role, read_status);
CREATE UNIQUE INDEX idx_notif_dedupe ON notifications(dedupe_key) WHERE dedupe_key IS NOT NULL;
CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(60),
  action VARCHAR(60) NOT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id VARCHAR(40),
  description VARCHAR(500),
  ip_address VARCHAR(45),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);

-- ===== Immutability: audit records and inventory transactions are append-only =====
CREATE FUNCTION forbid_mutation() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION '% on % is not allowed: records are immutable', TG_OP, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER audit_logs_immutable BEFORE UPDATE OR DELETE ON audit_logs FOR EACH ROW EXECUTE FUNCTION forbid_mutation();
CREATE TRIGGER inventory_tx_immutable BEFORE UPDATE OR DELETE ON inventory_transactions FOR EACH ROW EXECUTE FUNCTION forbid_mutation();
