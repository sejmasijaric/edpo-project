import { useEffect, useState } from "react"
import { AppSidebar, type Page } from "@/components/app-sidebar"
import { CustomerPage } from "@/components/customer-page"
import { WorkerPage } from "@/components/worker-page"
import { DiagnosticsPage } from "@/components/diagnostics-page"
import { useFactoryStream } from "@/hooks/useFactoryStream"
import type { Order } from "@/types/order"
import { fetchOrders } from "@/services/api"

export function App() {
  const [activePage, setActivePage] = useState<Page>("customer")
  const [orders, setOrders] = useState<Order[]>([])
  const { orderEvents, userTasks, connected } = useFactoryStream()

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
          <CustomerPage
            orders={orders}
            setOrders={setOrders}
            events={orderEvents}
            connected={connected}
          />
        )}
        {activePage === "worker" && (
          <WorkerPage liveTasks={userTasks} connected={connected} />
        )}
        {activePage === "diagnostics" && <DiagnosticsPage />}
      </main>
    </div>
  )
}

export default App
