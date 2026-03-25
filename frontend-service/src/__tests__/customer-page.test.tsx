import { useState } from "react"
import { render, screen, within } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { CustomerPage } from "@/components/customer-page"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"
import type { Order } from "@/types/order"

function Wrapper() {
  const [orders, setOrders] = useState<Order[]>([])
  const submitOrder = async (order: Order) => {
    setOrders((prev) => [order, ...prev])
  }
  return (
    <ThemeProvider>
      <CustomerPage orders={orders} submitOrder={submitOrder} />
      <Toaster />
    </ThemeProvider>
  )
}

function renderCustomerPage() {
  return render(<Wrapper />)
}

describe("CustomerPage", () => {
  it("renders the order form with all elements", () => {
    renderCustomerPage()

    expect(screen.getByText("Order Laser Engraved Air Tag")).toBeInTheDocument()
    expect(screen.getByLabelText("Red")).toBeInTheDocument()
    expect(screen.getByLabelText("White")).toBeInTheDocument()
    expect(screen.getByLabelText("Blue")).toBeInTheDocument()
    expect(screen.getByLabelText(/engraved text/i)).toBeInTheDocument()
    expect(screen.getByRole("button", { name: /submit order/i })).toBeInTheDocument()
  })

  it("shows character count for engraved text", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    expect(screen.getByText("0/20")).toBeInTheDocument()

    await user.type(screen.getByLabelText(/engraved text/i), "Hello")

    expect(screen.getByText("5/20")).toBeInTheDocument()
  })

  it("enforces 20 character max on engraved text", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    const input = screen.getByLabelText(/engraved text/i)
    await user.type(input, "123456789012345678901234")

    expect(input).toHaveValue("12345678901234567890")
    expect(screen.getByText("20/20")).toBeInTheDocument()
  })

  it("shows error toast when submitting without color", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(await screen.findByText("Please select a color")).toBeInTheDocument()
  })

  it("submits order successfully with color only", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(await screen.findByText("Order submitted successfully!")).toBeInTheDocument()
  })

  it("submits order with color and engraved text", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Blue"))
    await user.type(screen.getByLabelText(/engraved text/i), "My Tag")
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(await screen.findByText("Order submitted successfully!")).toBeInTheDocument()
    expect(screen.getByText("Blue Air Tag")).toBeInTheDocument()
    expect(screen.getByText(/My Tag/)).toBeInTheDocument()
  })

  it("resets form after successful submission", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.type(screen.getByLabelText(/engraved text/i), "Test")
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(screen.getByLabelText(/engraved text/i)).toHaveValue("")
    expect(screen.getByText("0/20")).toBeInTheDocument()
  })
})

describe("Order History", () => {
  it("shows empty state when no orders submitted", () => {
    renderCustomerPage()

    expect(screen.getByText("Order History")).toBeInTheDocument()
    expect(screen.getByText(/no orders yet/i)).toBeInTheDocument()
  })

  it("displays order in history after submission", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(screen.getByText("Red Air Tag")).toBeInTheDocument()
    expect(screen.getByText("To Do")).toBeInTheDocument()
    expect(screen.queryByText(/no orders yet/i)).not.toBeInTheDocument()
  })

  it("displays engraved text in order history", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Blue"))
    await user.type(screen.getByLabelText(/engraved text/i), "Hello")
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(screen.getByText("Blue Air Tag")).toBeInTheDocument()
    expect(screen.getByText(/Hello/)).toBeInTheDocument()
  })

  it("shows multiple orders with newest first", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    // Submit first order
    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    // Submit second order
    await user.click(screen.getByLabelText("Blue"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    const items = screen.getAllByText(/^\w+ Air Tag$/)
    expect(items).toHaveLength(2)
    expect(items[0]).toHaveTextContent("Blue Air Tag")
    expect(items[1]).toHaveTextContent("Red Air Tag")
  })

  it("shows order count in header", async () => {
    const user = userEvent.setup()
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    const heading = screen.getByText("Order History").closest("div")!
    expect(within(heading).getByText("(1)")).toBeInTheDocument()

    await user.click(screen.getByLabelText("Blue"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(within(heading).getByText("(2)")).toBeInTheDocument()
  })
})
