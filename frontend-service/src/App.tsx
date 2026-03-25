import { useState } from "react"
import { AppSidebar, type Page } from "@/components/app-sidebar"
import { CustomerPage } from "@/components/customer-page"
import { WorkerPage } from "@/components/worker-page"

export function App() {
  const [activePage, setActivePage] = useState<Page>("customer")

  return (
    <div className="flex h-screen">
      <AppSidebar activePage={activePage} onNavigate={setActivePage} />
      <main className="flex-1 overflow-auto p-6">
        {activePage === "customer" && <CustomerPage />}
        {activePage === "worker" && <WorkerPage />}
      </main>
    </div>
  )
}

export default App
