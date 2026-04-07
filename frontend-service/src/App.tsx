import { useEffect, useState } from "react"
import { AppSidebar, type Page } from "@/components/app-sidebar"
import { CustomerPage } from "@/components/customer-page"
import { WorkerPage } from "@/components/worker-page"
import type { Order } from "@/types/order"
import { fetchOrders } from "@/services/api"

export function App() {
  const [activePage, setActivePage] = useState<Page>("customer")
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => {
    fetchOrders()
      .then((data) => setOrders(data))
      .catch(console.error)
  }, [activePage])

  return (
    <div className="flex h-screen">
      <AppSidebar activePage={activePage} onNavigate={setActivePage} />
      <main className="flex-1 overflow-auto p-6">
        {activePage === "customer" && (
          <CustomerPage orders={orders} setOrders={setOrders} />
        )}
        {activePage === "worker" && (
          <WorkerPage orders={orders} setOrders={setOrders} />
        )}
      </main>
    </div>
  )
}

export default App
