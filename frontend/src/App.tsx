import { Navigate, Route, Routes } from 'react-router-dom'
import { RequireAuth } from './auth/RequireAuth'
import { AppLayout } from './layout/AppLayout'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { InventoryPage } from './pages/inventory/InventoryPage'
import { MachinesPage } from './pages/machines/MachinesPage'
import { BomsPage } from './pages/production/BomsPage'
import { ProductionOrderDetailPage } from './pages/production/ProductionOrderDetailPage'
import { ProductionOrdersPage } from './pages/production/ProductionOrdersPage'
import { PurchaseOrdersPage } from './pages/purchasing/PurchaseOrdersPage'
import { QualityPage } from './pages/quality/QualityPage'
import { ReportsPage } from './pages/reports/ReportsPage'
import { WorkersPage } from './pages/workers/WorkersPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="production-orders" element={<ProductionOrdersPage />} />
          <Route path="production-orders/:id" element={<ProductionOrderDetailPage />} />
          <Route path="boms" element={<BomsPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="purchase-orders" element={<PurchaseOrdersPage />} />
          <Route path="machines" element={<MachinesPage />} />
          <Route path="workers" element={<WorkersPage />} />
          <Route path="quality" element={<QualityPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
