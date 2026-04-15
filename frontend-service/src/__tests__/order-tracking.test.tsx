import { render, screen } from "@testing-library/react"
import { OrderList } from "@/components/order-list"
import { ThemeProvider } from "@/components/theme-provider"
import type { Order } from "@/types/order"
import type { MachineOrchestrationEvent } from "@/types/machine-event"

const sampleOrder: Order = {
  id: "abc-123",
  color: "red",
  status: "To Do",
  createdAt: new Date("2026-01-01T12:00:00Z"),
}

function renderOrderList(
  orders: Order[] = [],
  events: MachineOrchestrationEvent[] = [],
  connected?: boolean
) {
  return render(
    <ThemeProvider>
      <OrderList orders={orders} events={events} connected={connected} />
    </ThemeProvider>
  )
}

describe("OrderList with live events", () => {
  it("shows connected indicator when connected", () => {
    renderOrderList([sampleOrder], [], true)

    const indicator = screen.getByTestId("ws-status")
    expect(indicator.className).toContain("bg-green-500")
  })

  it("shows disconnected indicator when not connected", () => {
    renderOrderList([sampleOrder], [], false)

    const indicator = screen.getByTestId("ws-status")
    expect(indicator.className).toContain("bg-red-500")
  })

  it("does not show connection indicator when connected prop is undefined", () => {
    renderOrderList([sampleOrder])

    expect(screen.queryByTestId("ws-status")).not.toBeInTheDocument()
  })

  it("shows live event badge on matching order", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "intake-completed" },
    ]
    renderOrderList([sampleOrder], events)

    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
  })

  it("shows multiple event badges on same order", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "intake-completed" },
      { itemIdentifier: "abc-123", outcomeType: "manufacturing-completed" },
    ]
    renderOrderList([sampleOrder], events)

    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Completed")).toBeInTheDocument()
  })

  it("does not show events for non-matching orders", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "other-id", outcomeType: "intake-completed" },
    ]
    renderOrderList([sampleOrder], events)

    expect(screen.queryByText("Intake Completed")).not.toBeInTheDocument()
  })

  it("shows all outcome types with correct labels", () => {
    const orders: Order[] = [
      { ...sampleOrder, id: "o1" },
      { ...sampleOrder, id: "o2" },
      { ...sampleOrder, id: "o3" },
      { ...sampleOrder, id: "o4" },
      { ...sampleOrder, id: "o5" },
    ]
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "o1", outcomeType: "intake-completed" },
      { itemIdentifier: "o2", outcomeType: "manufacturing-completed" },
      { itemIdentifier: "o3", outcomeType: "manufacturing-failed" },
      { itemIdentifier: "o4", outcomeType: "qc-shipping" },
      { itemIdentifier: "o5", outcomeType: "qc-rejection" },
    ]
    renderOrderList(orders, events)

    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Failed")).toBeInTheDocument()
    expect(screen.getByText("QC Approved for Shipping")).toBeInTheDocument()
    expect(screen.getByText("QC Rejected")).toBeInTheDocument()
  })

  it("falls back to raw outcomeType for unknown types", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "unknown-stage" },
    ]
    renderOrderList([sampleOrder], events)

    expect(screen.getByText("unknown-stage")).toBeInTheDocument()
  })

  it("still shows order details alongside events", () => {
    const order: Order = { ...sampleOrder, engravedText: "Hello" }
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "intake-completed" },
    ]
    renderOrderList([order], events)

    expect(screen.getByText("Red Air Tag")).toBeInTheDocument()
    expect(screen.getByText(/Hello/)).toBeInTheDocument()
    expect(screen.getByText("To Do")).toBeInTheDocument()
    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
  })
})
