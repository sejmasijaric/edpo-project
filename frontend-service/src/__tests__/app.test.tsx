import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { App } from "@/App"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"

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

    expect(screen.getByText("Worker Dashboard")).toBeInTheDocument()
    expect(screen.queryByText("Order Laser Engraved Air Tag")).not.toBeInTheDocument()
  })

  it("navigates back to Customer page", async () => {
    const user = userEvent.setup()
    renderApp()

    await user.click(screen.getByRole("button", { name: /worker/i }))
    await user.click(screen.getByRole("button", { name: /customer/i }))

    expect(screen.getByText("Order Laser Engraved Air Tag")).toBeInTheDocument()
    expect(screen.queryByText("Worker Dashboard")).not.toBeInTheDocument()
  })

  it("renders theme toggle button", () => {
    renderApp()

    expect(screen.getByRole("button", { name: /toggle theme/i })).toBeInTheDocument()
  })
})
