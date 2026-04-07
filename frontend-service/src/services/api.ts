import type { Order, OrderStatus } from "@/types/order"

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

function parseOrder(data: Record<string, unknown>): Order {
  return {
    ...data,
    createdAt: new Date(data.createdAt as string),
  } as Order
}
