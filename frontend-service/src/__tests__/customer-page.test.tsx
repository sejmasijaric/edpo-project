import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { CustomerPage } from "@/components/customer-page"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"

function renderCustomerPage() {
  return render(
    <ThemeProvider>
      <CustomerPage />
      <Toaster />
    </ThemeProvider>
  )
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
    const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {})
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(consoleSpy).toHaveBeenCalledWith("Order submitted:", { color: "red" })
    expect(await screen.findByText("Order submitted successfully!")).toBeInTheDocument()

    consoleSpy.mockRestore()
  })

  it("submits order with color and engraved text", async () => {
    const user = userEvent.setup()
    const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {})
    renderCustomerPage()

    await user.click(screen.getByLabelText("Blue"))
    await user.type(screen.getByLabelText(/engraved text/i), "My Tag")
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(consoleSpy).toHaveBeenCalledWith("Order submitted:", {
      color: "blue",
      engravedText: "My Tag",
    })

    consoleSpy.mockRestore()
  })

  it("resets form after successful submission", async () => {
    const user = userEvent.setup()
    vi.spyOn(console, "log").mockImplementation(() => {})
    renderCustomerPage()

    await user.click(screen.getByLabelText("Red"))
    await user.type(screen.getByLabelText(/engraved text/i), "Test")
    await user.click(screen.getByRole("button", { name: /submit order/i }))

    expect(screen.getByLabelText(/engraved text/i)).toHaveValue("")
    expect(screen.getByText("0/20")).toBeInTheDocument()

    vi.restoreAllMocks()
  })
})
