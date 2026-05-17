import type { Order, OrderStatus } from "@/types/order"
import type { LatestItemStatus } from "@/types/machine-event"
import type { UserTaskEvent } from "@/types/user-task"
import type { DashboardMetricsResponse } from "@/types/dashboard"

const API_BASE = "/api"

interface CreateOrderPayload {
  id: string
  color: string
  engravedText?: string
  createdAt: string
}

export async function createOrder(order: {
  id: string
  color: string
  engravedText?: string
  createdAt: Date
}): Promise<Order> {
  const payload: CreateOrderPayload = {
    id: order.id,
    color: order.color,
    ...(order.engravedText ? { engravedText: order.engravedText } : {}),
    createdAt: order.createdAt.toISOString(),
  }

  const res = await fetch(`${API_BASE}/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  })

  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Failed to create order" }))
    throw new Error(err.error)
  }

  return parseOrder(await res.json())
}

export async function fetchOrders(): Promise<Order[]> {
  const res = await fetch(`${API_BASE}/orders`)

  if (!res.ok) {
    throw new Error("Failed to fetch orders")
  }

  const data = await res.json()
  return data.map(parseOrder)
}

export async function updateOrderStatus(
  orderId: string,
  status: OrderStatus
): Promise<Order> {
  const res = await fetch(`${API_BASE}/orders/${orderId}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  })

  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Failed to update status" }))
    throw new Error(err.error)
  }

  return parseOrder(await res.json())
}

export async function fetchLatestItemStatus(
  itemIdentifier: string
): Promise<LatestItemStatus> {
  const res = await fetch(
    `${API_BASE}/orders/${encodeURIComponent(itemIdentifier)}/latest-status`
  )

  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Latest status not found" }))
    throw new Error(err.error)
  }

  return res.json()
}

export async function fetchOpenUserTasks(): Promise<UserTaskEvent[]> {
  const res = await fetch(`${API_BASE}/user-tasks`)
  if (!res.ok) {
    throw new Error("Failed to fetch open user tasks")
  }
  return res.json()
}

export async function fetchRecentUserTasks(): Promise<UserTaskEvent[]> {
  const res = await fetch(`${API_BASE}/user-tasks/recent`)
  if (!res.ok) {
    throw new Error("Failed to fetch recent user tasks")
  }
  return res.json()
}

export async function fetchDashboardMetrics(params?: {
  from?: string
  to?: string
}): Promise<DashboardMetricsResponse> {
  const query = new URLSearchParams()
  if (params?.from) query.set("from", params.from)
  if (params?.to) query.set("to", params.to)
  const suffix = query.toString() ? `?${query.toString()}` : ""
  const res = await fetch(`${API_BASE}/dashboard/metrics${suffix}`)
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Failed to fetch dashboard metrics" }))
    throw new Error(err.error ?? "Failed to fetch dashboard metrics")
  }
  return res.json()
}

export async function completeCheckQualityTask(params: {
  itemId: string
  passed: boolean
}): Promise<void> {
  const query = new URLSearchParams({
    itemId: params.itemId,
    passed: String(params.passed),
  })
  const res = await fetch(`${API_BASE}/qc/check-quality/complete?${query.toString()}`, {
    method: "POST",
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Failed to complete QC task" }))
    throw new Error(err.error ?? "Failed to complete QC task")
  }
}

export async function insertItemIntoSimulator(params: {
  itemId: string
  color: string
  sinkId?: string
}): Promise<void> {
  const query = new URLSearchParams({
    itemId: params.itemId,
    color: params.color,
    ...(params.sinkId ? { sinkId: params.sinkId } : {}),
  })
  const res = await fetch(`${API_BASE}/simulator/items?${query.toString()}`, {
    method: "POST",
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Failed to insert item" }))
    throw new Error(err.error ?? "Failed to insert item")
  }
}

function parseOrder(data: Record<string, unknown>): Order {
  return {
    ...data,
    createdAt: new Date(data.createdAt as string),
  } as Order
}
