import { useState } from "react"
import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { WorkerPage } from "@/components/worker-page"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"
import type { Order, OrderStatus } from "@/types/order"

function makeOrder(overrides: Partial<Order> = {}): Order {
  return {
    id: crypto.randomUUID(),
    color: "red",
    status: "To Do",
    createdAt: new Date(),
    ...overrides,
  }
}

function WorkerWrapper({ initialOrders = [] }: { initialOrders?: Order[] }) {
  const [orders, setOrders] = useState<Order[]>(initialOrders)
  const updateOrderStatus = async (orderId: string, newStatus: OrderStatus) => {
    setOrders((prev) =>
      prev.map((order) =>
        order.id === orderId ? { ...order, status: newStatus } : order,
      ),
    )
  }
  return (
    <ThemeProvider>
      <WorkerPage orders={orders} updateOrderStatus={updateOrderStatus} />
      <Toaster />
    </ThemeProvider>
  )
}

function renderWorkerPage(initialOrders: Order[] = []) {
  return render(<WorkerWrapper initialOrders={initialOrders} />)
}

describe("WorkerPage", () => {
  it("shows empty production queue message", () => {
    renderWorkerPage()

    expect(screen.getByText("Production Queue")).toBeInTheDocument()
    expect(screen.getByText("No orders in the queue.")).toBeInTheDocument()
  })

  it("displays orders with correct info", () => {
    const orders = [makeOrder({ color: "blue", engravedText: "Test" })]
    renderWorkerPage(orders)

    expect(screen.getByText("Blue Air Tag")).toBeInTheDocument()
    expect(screen.getByText(/Test/)).toBeInTheDocument()
  })

  it("shows correct status badges", () => {
    const orders = [
      makeOrder({ status: "To Do" }),
      makeOrder({ status: "In Progress" }),
      makeOrder({ status: "Done" }),
      makeOrder({ status: "Error" }),
    ]
    renderWorkerPage(orders)

    const badges = screen.getAllByText(/To Do|In Progress|Done|Error/, {
      selector: "[data-slot='badge']",
    })
    expect(badges).toHaveLength(4)
    expect(badges[0]).toHaveTextContent("To Do")
    expect(badges[1]).toHaveTextContent("In Progress")
    expect(badges[2]).toHaveTextContent("Done")
    expect(badges[3]).toHaveTextContent("Error")
  })

  it("Start button moves To Do to In Progress", async () => {
    const user = userEvent.setup()
    renderWorkerPage([makeOrder({ status: "To Do" })])

    await user.click(screen.getByRole("button", { name: "Start" }))

    expect(screen.getByText("In Progress")).toBeInTheDocument()
    expect(screen.queryByText("To Do")).not.toBeInTheDocument()
  })

  it("Done button moves In Progress to Done", async () => {
    const user = userEvent.setup()
    renderWorkerPage([makeOrder({ status: "In Progress" })])

    await user.click(screen.getByRole("button", { name: "Done" }))

    expect(screen.getByText("Done")).toBeInTheDocument()
    expect(screen.queryByText("In Progress")).not.toBeInTheDocument()
  })

  it("Error button moves In Progress to Error", async () => {
    const user = userEvent.setup()
    renderWorkerPage([makeOrder({ status: "In Progress" })])

    await user.click(screen.getByRole("button", { name: "Error" }))

    expect(screen.getByText("Error")).toBeInTheDocument()
    expect(screen.queryByText("In Progress")).not.toBeInTheDocument()
  })

  it("Retry button moves Error to In Progress", async () => {
    const user = userEvent.setup()
    renderWorkerPage([makeOrder({ status: "Error" })])

    await user.click(screen.getByRole("button", { name: "Retry" }))

    const badge = screen.getByText("In Progress", {
      selector: "[data-slot='badge']",
    })
    expect(badge).toBeInTheDocument()
    expect(screen.queryByRole("button", { name: "Retry" })).not.toBeInTheDocument()
  })

  it("shows no action buttons for Done orders", () => {
    renderWorkerPage([makeOrder({ status: "Done" })])

    expect(screen.queryByRole("button", { name: "Start" })).not.toBeInTheDocument()
    expect(screen.queryByRole("button", { name: "Done" })).not.toBeInTheDocument()
    expect(screen.queryByRole("button", { name: "Error" })).not.toBeInTheDocument()
    expect(screen.queryByRole("button", { name: "Retry" })).not.toBeInTheDocument()
  })
})
