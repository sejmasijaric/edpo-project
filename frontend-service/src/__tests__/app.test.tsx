import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { App } from "@/App"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"
import { fetchOrders, createOrder } from "@/services/api"

const mockFetchOrders = vi.mocked(fetchOrders)
const mockCreateOrder = vi.mocked(createOrder)

function renderApp() {
  return render(
    <ThemeProvider>
      <App />
      <Toaster />
    </ThemeProvider>
  )
}

describe("App", () => {
  it("renders sidebar with Customer and Worker navigation buttons", () => {
    renderApp()

    expect(screen.getByRole("button", { name: /customer/i })).toBeInTheDocument()
    expect(screen.getByRole("button", { name: /worker/i })).toBeInTheDocument()
  })

  it("shows Customer page by default", () => {
    renderApp()

    expect(screen.getByText("Order Laser Engraved Air Tag")).toBeInTheDocument()
  })

  it("navigates to Worker page when Worker button is clicked", async () => {
    const user = userEvent.setup()
    renderApp()

    await user.click(screen.getByRole("button", { name: /worker/i }))

    expect(screen.getByText("Production Queue")).toBeInTheDocument()
    expect(screen.queryByText("Order Laser Engraved Air Tag")).not.toBeInTheDocument()
  })

  it("navigates back to Customer page", async () => {
    const user = userEvent.setup()
    renderApp()

    await user.click(screen.getByRole("button", { name: /worker/i }))
    await user.click(screen.getByRole("button", { name: /customer/i }))

    expect(screen.getByText("Order Laser Engraved Air Tag")).toBeInTheDocument()
    expect(screen.queryByText("Production Queue")).not.toBeInTheDocument()
  })

  it("renders theme toggle button", () => {
    renderApp()

    expect(screen.getByRole("button", { name: /toggle theme/i })).toBeInTheDocument()
  })

  it("shares orders between customer and worker pages", async () => {
    const user = userEvent.setup()
    const createdOrder = {
      id: "shared-test",
      color: "red" as const,
      status: "To Do" as const,
      createdAt: new Date(),
    }
    mockCreateOrder.mockResolvedValueOnce(createdOrder)
    // Initial mount fetch returns empty, navigation fetch returns the created order
    mockFetchOrders.mockResolvedValueOnce([])
    mockFetchOrders.mockResolvedValueOnce([createdOrder])
    renderApp()

    // Submit order on customer page
    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))
    await screen.findByText("Red Air Tag")

    // Navigate to worker page
    await user.click(screen.getByRole("button", { name: /worker/i }))

    // Verify order appears in production queue
    expect(await screen.findByText("Red Air Tag")).toBeInTheDocument()
    expect(screen.getByText("To Do")).toBeInTheDocument()
  })
})
