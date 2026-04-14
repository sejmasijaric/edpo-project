import { render, screen } from "@testing-library/react"
import { OrderTracking } from "@/components/order-tracking"
import { ThemeProvider } from "@/components/theme-provider"
import type { MachineOrchestrationEvent } from "@/types/machine-event"

function renderTracking(
  events: MachineOrchestrationEvent[] = [],
  connected = false
) {
  return render(
    <ThemeProvider>
      <OrderTracking events={events} connected={connected} />
    </ThemeProvider>
  )
}

describe("OrderTracking", () => {
  it("renders the tracking card with title", () => {
    renderTracking()

    expect(screen.getByText("Order Tracking")).toBeInTheDocument()
  })

  it("shows empty state when no events", () => {
    renderTracking()

    expect(
      screen.getByText(/no events yet/i)
    ).toBeInTheDocument()
  })

  it("shows connected status when connected", () => {
    renderTracking([], true)

    expect(screen.getByText(/connected\./i)).toBeInTheDocument()
    const indicator = screen.getByTestId("ws-status")
    expect(indicator.className).toContain("bg-green-500")
  })

  it("shows disconnected status when not connected", () => {
    renderTracking([], false)

    expect(screen.getByText(/disconnected\./i)).toBeInTheDocument()
    const indicator = screen.getByTestId("ws-status")
    expect(indicator.className).toContain("bg-red-500")
  })

  it("displays a single event", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "intake-completed" },
    ]
    renderTracking(events)

    expect(screen.getByText("abc-123")).toBeInTheDocument()
    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
  })

  it("displays multiple events for the same order grouped", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc-123", outcomeType: "intake-completed" },
      { itemIdentifier: "abc-123", outcomeType: "manufacturing-completed" },
    ]
    renderTracking(events)

    const orderLabels = screen.getAllByText("abc-123")
    expect(orderLabels).toHaveLength(1)
    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Completed")).toBeInTheDocument()
  })

  it("displays events for different orders separately", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "order-1", outcomeType: "intake-completed" },
      { itemIdentifier: "order-2", outcomeType: "manufacturing-failed" },
    ]
    renderTracking(events)

    expect(screen.getByText("order-1")).toBeInTheDocument()
    expect(screen.getByText("order-2")).toBeInTheDocument()
    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Failed")).toBeInTheDocument()
  })

  it("shows human-readable labels for all outcome types", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "o1", outcomeType: "intake-completed" },
      { itemIdentifier: "o2", outcomeType: "manufacturing-completed" },
      { itemIdentifier: "o3", outcomeType: "manufacturing-failed" },
      { itemIdentifier: "o4", outcomeType: "qc-shipping" },
      { itemIdentifier: "o5", outcomeType: "qc-rejection" },
    ]
    renderTracking(events)

    expect(screen.getByText("Intake Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Completed")).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Failed")).toBeInTheDocument()
    expect(screen.getByText("QC Approved for Shipping")).toBeInTheDocument()
    expect(screen.getByText("QC Rejected")).toBeInTheDocument()
  })

  it("falls back to raw outcomeType for unknown types", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "o1", outcomeType: "unknown-stage" },
    ]
    renderTracking(events)

    expect(screen.getByText("unknown-stage")).toBeInTheDocument()
  })

  it("does not show empty state when events exist", () => {
    const events: MachineOrchestrationEvent[] = [
      { itemIdentifier: "abc", outcomeType: "intake-completed" },
    ]
    renderTracking(events)

    expect(screen.queryByText(/no events yet/i)).not.toBeInTheDocument()
  })
})
