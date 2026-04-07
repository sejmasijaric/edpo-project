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

global.ResizeObserver = ResizeObserverMock as unknown as typeof ResizeObserver
