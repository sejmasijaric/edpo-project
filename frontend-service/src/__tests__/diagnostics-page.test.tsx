import { render, screen } from "@testing-library/react"
import { DiagnosticsPage } from "@/components/diagnostics-page"
import { ThemeProvider } from "@/components/theme-provider"
import { fetchDashboardMetrics } from "@/services/api"
import type { DashboardMetricsResponse } from "@/types/dashboard"

const mockFetch = vi.mocked(fetchDashboardMetrics)

function metrics(overrides: Partial<DashboardMetricsResponse> = {}): DashboardMetricsResponse {
  return {
    from: "2026-05-13T11:34:56Z",
    to: "2026-05-13T12:34:56Z",
    qcRejectedRate: { rejectedCount: 2, passedCount: 8, totalCount: 10, rejectedPercentage: 20 },
    averageManufacturingTime: { completedAttemptCount: 5, averageDurationMillis: 12_345 },
    manualInterventions: {
      openCount: 1,
      completedCount: 3,
      openInterventions: [
        { itemIdentifier: "ITEM-X", taskName: "Resolve Issue", currentStage: "QC" },
      ],
    },
    manufacturingFailureRate: {
      failedCount: 1,
      completedCount: 4,
      totalCount: 5,
      failurePercentage: 20,
      failedItemCount: 1,
    },
    averageEndToEndProductionTime: {
      completedCount: 4,
      averageDurationMillis: 60_000,
      minimumDurationMillis: 30_000,
      maximumDurationMillis: 120_000,
    },
    workInProgressByStage: { INTAKE: 1, MANUFACTURING: 2, QC: 1, MANUAL_INTERVENTION: 0 },
    retryRate: {
      completedItemCount: 4,
      totalRetries: 3,
      averageRetriesPerCompletedItem: 0.75,
      retriesPerItem: { "ITEM-A": 2, "ITEM-B": 1 },
    },
    ...overrides,
  }
}

beforeEach(() => {
  mockFetch.mockReset()
})

function renderPage() {
  return render(
    <ThemeProvider>
      <DiagnosticsPage />
    </ThemeProvider>
  )
}

describe("DiagnosticsPage", () => {
  it("renders metric cards from API response", async () => {
    mockFetch.mockResolvedValue(metrics())
    renderPage()

    expect(await screen.findByText("QC Rejected Rate")).toBeInTheDocument()
    expect(screen.getByText(/2 of 10 rejected/)).toBeInTheDocument()
    expect(screen.getByText("Manufacturing Failure Rate")).toBeInTheDocument()
    expect(screen.getByText(/1 failed \/ 5 attempts/)).toBeInTheDocument()
    expect(screen.getByText("Avg Manufacturing Time")).toBeInTheDocument()
    expect(screen.getByText("Work in Progress")).toBeInTheDocument()
    expect(screen.getByText("Manual Interventions")).toBeInTheDocument()
    expect(screen.getByText("Resolve Issue")).toBeInTheDocument()
    expect(screen.getByText("ITEM-X")).toBeInTheDocument()
    expect(screen.getByText("0.75")).toBeInTheDocument()
  })

  it("shows an error message when API call fails", async () => {
    mockFetch.mockRejectedValueOnce(new Error("Dashboard down"))
    renderPage()

    expect(await screen.findByText("Dashboard down")).toBeInTheDocument()
  })

  it("does not render dashboard when work-in-progress is empty", async () => {
    mockFetch.mockResolvedValueOnce(
      metrics({ workInProgressByStage: { INTAKE: 0, MANUFACTURING: 0, QC: 0 } })
    )
    renderPage()

    expect(await screen.findByText("No items in progress.")).toBeInTheDocument()
  })
})
