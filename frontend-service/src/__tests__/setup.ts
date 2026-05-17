import "@testing-library/jest-dom/vitest"

// Mock the API module so tests don't make real HTTP requests
vi.mock("@/services/api", () => ({
  createOrder: vi.fn(async (order: Record<string, unknown>) => ({
    id: order.id,
    color: order.color,
    engravedText: order.engravedText,
    status: "To Do",
    createdAt: order.createdAt,
  })),
  fetchOrders: vi.fn(async () => []),
  fetchLatestItemStatus: vi.fn(async (itemIdentifier: string) => ({
    itemIdentifier,
    station: "WT_1",
    outcomeType: "in_progress",
    timestamp: "2026-05-13T12:34:56Z",
    sourceTopic: "factory.raw-events",
  })),
  updateOrderStatus: vi.fn(
    async (orderId: string, status: string) =>
      // Return a minimal order with the updated status
      ({
        id: orderId,
        color: "red",
        status,
        createdAt: new Date(),
      })
  ),
  fetchOpenUserTasks: vi.fn(async () => []),
  fetchRecentUserTasks: vi.fn(async () => []),
  insertItemIntoSimulator: vi.fn(async () => undefined),
  removeItemFromSimulator: vi.fn(async () => undefined),
  completeCheckQualityTask: vi.fn(async () => undefined),
  completeManualTask: vi.fn(async () => undefined),
  fetchDashboardMetrics: vi.fn(async () => ({
    from: "2026-05-13T11:34:56Z",
    to: "2026-05-13T12:34:56Z",
    qcRejectedRate: {
      rejectedCount: 0,
      passedCount: 0,
      totalCount: 0,
      rejectedPercentage: 0,
    },
    averageManufacturingTime: { completedAttemptCount: 0, averageDurationMillis: 0 },
    manualInterventions: { openCount: 0, completedCount: 0, openInterventions: [] },
    manufacturingFailureRate: {
      failedCount: 0,
      completedCount: 0,
      totalCount: 0,
      failurePercentage: 0,
      failedItemCount: 0,
    },
    averageEndToEndProductionTime: {
      completedCount: 0,
      averageDurationMillis: 0,
      minimumDurationMillis: 0,
      maximumDurationMillis: 0,
    },
    workInProgressByStage: {},
    retryRate: {
      completedItemCount: 0,
      totalRetries: 0,
      averageRetriesPerCompletedItem: 0,
      retriesPerItem: {},
    },
  })),
}))

// Stub the WebSocket-based stream hook used by App.
vi.mock("@/hooks/useFactoryStream", () => ({
  useFactoryStream: () => ({
    orderEvents: [],
    userTasks: [],
    itemStatuses: [],
    latestStatusByItem: {},
    connected: false,
  }),
}))

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

class ResizeObserverMock {
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()
}

(globalThis as any).ResizeObserver = ResizeObserverMock as unknown as typeof ResizeObserver
