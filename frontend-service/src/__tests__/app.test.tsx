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

    expect(await screen.findByText("Items to Insert")).toBeInTheDocument()
    expect(screen.queryByText("Order Laser Engraved Air Tag")).not.toBeInTheDocument()
  })

  it("navigates back to Customer page", async () => {
    const user = userEvent.setup()
    renderApp()

    await user.click(screen.getByRole("button", { name: /worker/i }))
    await user.click(screen.getByRole("button", { name: /customer/i }))

    expect(screen.getByText("Order Laser Engraved Air Tag")).toBeInTheDocument()
    expect(screen.queryByText("Items to Insert")).not.toBeInTheDocument()
  })

  it("navigates to Diagnostics page", async () => {
    const user = userEvent.setup()
    renderApp()

    await user.click(screen.getByRole("button", { name: /diagnostics/i }))

    expect(await screen.findByText(/Production Diagnostics/i)).toBeInTheDocument()
  })

  it("renders theme toggle button", () => {
    renderApp()

    expect(screen.getByRole("button", { name: /toggle theme/i })).toBeInTheDocument()
  })

  it("refetches orders when returning to the Customer page", async () => {
    const user = userEvent.setup()
    const createdOrder = {
      id: "shared-test",
      color: "red" as const,
      status: "To Do" as const,
      createdAt: new Date(),
    }
    mockCreateOrder.mockResolvedValueOnce(createdOrder)
    mockFetchOrders.mockResolvedValueOnce([])
    mockFetchOrders.mockResolvedValueOnce([])
    mockFetchOrders.mockResolvedValueOnce([createdOrder])
    renderApp()

    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))
    await screen.findByText("Red Air Tag")

    await user.click(screen.getByRole("button", { name: /worker/i }))
    await user.click(screen.getByRole("button", { name: /customer/i }))

    expect(await screen.findByText("Red Air Tag")).toBeInTheDocument()
  })
})
