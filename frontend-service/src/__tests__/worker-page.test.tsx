import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { WorkerPage } from "@/components/worker-page"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "@/components/ui/sonner"
import type { UserTaskEvent } from "@/types/user-task"
import {
  completeCheckQualityTask,
  fetchOpenUserTasks,
  fetchRecentUserTasks,
  insertItemIntoSimulator,
} from "@/services/api"

const mockFetchOpen = vi.mocked(fetchOpenUserTasks)
const mockFetchRecent = vi.mocked(fetchRecentUserTasks)
const mockInsert = vi.mocked(insertItemIntoSimulator)
const mockCompleteQc = vi.mocked(completeCheckQualityTask)

function renderPage(liveTasks: UserTaskEvent[] = [], connected = true) {
  return render(
    <ThemeProvider>
      <WorkerPage liveTasks={liveTasks} connected={connected} />
      <Toaster />
    </ThemeProvider>
  )
}

beforeEach(() => {
  window.localStorage.clear()
  mockFetchOpen.mockReset()
  mockFetchRecent.mockReset()
  mockInsert.mockReset()
  mockCompleteQc.mockReset()
  mockFetchOpen.mockResolvedValue([])
  mockFetchRecent.mockResolvedValue([])
  mockInsert.mockResolvedValue(undefined)
  mockCompleteQc.mockResolvedValue(undefined)
})

describe("WorkerPage", () => {
  it("shows empty state when no tasks are open", async () => {
    renderPage()
    expect(await screen.findByText("Items to Insert")).toBeInTheDocument()
    expect(screen.getByText(/No items waiting for intake/i)).toBeInTheDocument()
    expect(screen.getByText(/No open manual tasks/i)).toBeInTheDocument()
    expect(screen.getByText(/No recent errors/i)).toBeInTheDocument()
  })

  it("shows a connection indicator", async () => {
    renderPage([], false)
    const dot = await screen.findByTestId("ws-status")
    expect(dot.className).toContain("bg-red-500")
  })

  it("renders an intake call-to-action with the requested color", async () => {
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-RED-1",
        commandType: "insert-item-into-intake-command",
        taskName: "Insert Item",
        taskCategory: "normal",
        stationName: "item-intake-station",
        targetColor: "RED",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()
    expect(await screen.findByText(/Insert Red AirTag/i)).toBeInTheDocument()
    expect(screen.getByText("ITEM-RED-1")).toBeInTheDocument()
  })

  it("uses live task events sent over WebSocket", async () => {
    const liveTask: UserTaskEvent = {
      itemIdentifier: "ITEM-BLUE-1",
      commandType: "insert-item-into-intake-command",
      taskName: "Insert Item",
      stationName: "item-intake-station",
      targetColor: "BLUE",
      eventTimestampEpochMillis: 1_700_000_000_000,
    }
    renderPage([liveTask])
    expect(await screen.findByText(/Insert Blue AirTag/i)).toBeInTheDocument()
  })

  it("groups non-intake tasks under Open Manual Tasks", async () => {
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-Q-1",
        commandType: "check-quality-user-task-issued",
        taskName: "Check Quality",
        taskCategory: "normal",
        stationName: "quality-control-station",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()
    expect(await screen.findByText("Check Quality")).toBeInTheDocument()
    expect(screen.getByText("quality-control-station")).toBeInTheDocument()
  })

  it("shows errors in the recent errors panel", async () => {
    mockFetchRecent.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-E-1",
        commandType: "resolve-issue-and-restore-item-position-user-task-issued",
        taskName: "Resolve Issue and Restore Item Position",
        taskCategory: "error",
        stationName: "quality-control-station",
        errorMessage: "Item dropped on the floor",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()
    expect(
      await screen.findByText("Resolve Issue and Restore Item Position")
    ).toBeInTheDocument()
    expect(screen.getByText("Item dropped on the floor")).toBeInTheDocument()
  })

  it("removes a task when a completed status comes in", async () => {
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-Q-2",
        commandType: "check-quality-user-task-issued",
        taskName: "Check Quality",
        taskCategory: "normal",
        stationName: "quality-control-station",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    const { rerender } = renderPage()
    expect(await screen.findByText("Check Quality")).toBeInTheDocument()

    rerender(
      <ThemeProvider>
        <WorkerPage
          liveTasks={[
            {
              itemIdentifier: "ITEM-Q-2",
              commandType: "check-quality-user-task-issued",
              taskName: "Check Quality",
              taskStatus: "completed",
              eventTimestampEpochMillis: 1_700_000_000_001,
            },
          ]}
        />
        <Toaster />
      </ThemeProvider>
    )

    expect(screen.queryByText("Check Quality")).not.toBeInTheDocument()
  })

  it("insert button calls the simulator and hides the task", async () => {
    const user = userEvent.setup()
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-INS-1",
        commandType: "insert-item-into-intake-command",
        taskName: "Insert Item",
        taskCategory: "normal",
        stationName: "item-intake-station",
        targetColor: "BLUE",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()

    const button = await screen.findByRole("button", { name: /insert into intake/i })
    await user.click(button)

    expect(mockInsert).toHaveBeenCalledWith({
      itemId: "ITEM-INS-1",
      color: "BLUE",
    })
    expect(await screen.findByText(/Inserted Blue airtag/i)).toBeInTheDocument()
    expect(
      screen.queryByRole("button", { name: /insert into intake/i })
    ).not.toBeInTheDocument()
  })

  it("shows an error toast when the insert call fails", async () => {
    const user = userEvent.setup()
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-INS-2",
        commandType: "insert-item-into-intake-command",
        taskName: "Insert Item",
        targetColor: "RED",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    mockInsert.mockRejectedValueOnce(new Error("Simulator down"))
    renderPage()

    await user.click(await screen.findByRole("button", { name: /insert into intake/i }))

    expect(await screen.findByText("Simulator down")).toBeInTheDocument()
  })

  it("Pass button completes the Check Quality task and hides it", async () => {
    const user = userEvent.setup()
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-QC-1",
        commandType: "check-quality-user-task-issued",
        taskName: "Check Quality",
        taskCategory: "normal",
        stationName: "quality-control-station",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()

    const pass = await screen.findByRole("button", { name: /pass/i })
    await user.click(pass)

    expect(mockCompleteQc).toHaveBeenCalledWith({ itemId: "ITEM-QC-1", passed: true })
    expect(await screen.findByText(/QC approved/i)).toBeInTheDocument()
    expect(screen.queryByText("Check Quality")).not.toBeInTheDocument()
  })

  it("Reject button completes the Check Quality task with qcPassed=false", async () => {
    const user = userEvent.setup()
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-QC-2",
        commandType: "check-quality-user-task-issued",
        taskName: "Check Quality",
        taskCategory: "normal",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()

    await user.click(await screen.findByRole("button", { name: /reject/i }))

    expect(mockCompleteQc).toHaveBeenCalledWith({ itemId: "ITEM-QC-2", passed: false })
    expect(await screen.findByText(/QC rejected/i)).toBeInTheDocument()
  })

  it("does not show Pass/Reject on non-Check-Quality manual tasks", async () => {
    mockFetchOpen.mockResolvedValueOnce([
      {
        itemIdentifier: "ITEM-X-1",
        commandType: "resolve-issue-and-restore-item-position-user-task-issued",
        taskName: "Resolve Issue and Restore Item Position",
        taskCategory: "error",
        eventTimestampEpochMillis: 1_700_000_000_000,
      },
    ])
    renderPage()
    await screen.findByText("Resolve Issue and Restore Item Position")
    expect(screen.queryByRole("button", { name: /^pass$/i })).not.toBeInTheDocument()
    expect(screen.queryByRole("button", { name: /^reject$/i })).not.toBeInTheDocument()
  })

  it("intake card stays hidden after a remount once it has been inserted", async () => {
    const user = userEvent.setup()
    const stickyTask = {
      itemIdentifier: "ITEM-PERSIST-1",
      commandType: "insert-item-into-intake-command",
      taskName: "Insert Item",
      taskCategory: "normal",
      targetColor: "BLUE",
      eventTimestampEpochMillis: 1_700_000_000_000,
    }
    mockFetchOpen.mockResolvedValue([stickyTask])
    const { unmount } = renderPage()

    await user.click(await screen.findByRole("button", { name: /insert into intake/i }))
    expect(
      screen.queryByRole("button", { name: /insert into intake/i })
    ).not.toBeInTheDocument()

    unmount()
    renderPage()
    await screen.findByText("Items to Insert")
    expect(
      screen.queryByRole("button", { name: /insert into intake/i })
    ).not.toBeInTheDocument()
  })

  it("refresh button reloads tasks from the API", async () => {
    const user = userEvent.setup()
    renderPage()
    await screen.findByText("Items to Insert")
    expect(mockFetchOpen).toHaveBeenCalledTimes(1)

    await user.click(screen.getByRole("button", { name: /refresh/i }))
    expect(mockFetchOpen).toHaveBeenCalledTimes(2)
    expect(mockFetchRecent).toHaveBeenCalledTimes(2)
  })
})
