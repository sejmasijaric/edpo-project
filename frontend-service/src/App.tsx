import { useState } from "react"
import { AppSidebar, type Page } from "@/components/app-sidebar"
import { CustomerPage } from "@/components/customer-page"
import { WorkerPage } from "@/components/worker-page"
import { useKafkaOrders } from "@/hooks/useKafkaOrders"

export function App() {
  const [activePage, setActivePage] = useState<Page>("customer")
  const { orders, submitOrder, updateOrderStatus } = useKafkaOrders()

  return (
    <div className="flex h-screen">
      <AppSidebar activePage={activePage} onNavigate={setActivePage} />
      <main className="flex-1 overflow-auto p-6">
        {activePage === "customer" && (
          <CustomerPage orders={orders} submitOrder={submitOrder} />
        )}
        {activePage === "worker" && (
          <WorkerPage orders={orders} updateOrderStatus={updateOrderStatus} />
        )}
      </main>
    </div>
  )
}

export default App
