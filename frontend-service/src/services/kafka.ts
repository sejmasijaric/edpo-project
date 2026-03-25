import type { Order, OrderStatus, ColorValue } from "@/types/order"

const BASE = "/api/kafka"
const TOPIC = "air-tag-orders"
const CONSUMER_GROUP = "frontend-orders"

interface KafkaOrderMessage {
  id: string
  color: ColorValue
  engravedText?: string
  status: OrderStatus
  createdAt: string // ISO 8601
}

function orderToMessage(order: Order): KafkaOrderMessage {
  return {
    id: order.id,
    color: order.color,
    status: order.status,
    createdAt: order.createdAt.toISOString(),
    ...(order.engravedText ? { engravedText: order.engravedText } : {}),
  }
}

function messageToOrder(msg: KafkaOrderMessage): Order {
  return {
    id: msg.id,
    color: msg.color,
    status: msg.status,
    createdAt: new Date(msg.createdAt),
    ...(msg.engravedText ? { engravedText: msg.engravedText } : {}),
  }
}

/**
 * REST Proxy returns base_uri pointing to internal Docker hostname (kafka-rest:8082).
 * Rewrite it to use our nginx proxy path so the browser can reach it.
 */
function rewriteBaseUri(baseUri: string): string {
  try {
    const url = new URL(baseUri)
    return `${BASE}${url.pathname}`
  } catch {
    // If it's already a relative path, just prepend BASE
    return baseUri.startsWith(BASE) ? baseUri : `${BASE}${baseUri}`
  }
}

export async function produceOrder(order: Order): Promise<void> {
  const response = await fetch(`${BASE}/topics/${TOPIC}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/vnd.kafka.json.v2+json",
      Accept: "application/vnd.kafka.v2+json",
    },
    body: JSON.stringify({
      records: [{ key: order.id, value: orderToMessage(order) }],
    }),
  })

  if (!response.ok) {
    throw new Error(`Failed to produce order: ${response.status}`)
  }
}

export async function createConsumer(): Promise<string> {
  const instanceName = `frontend-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

  const response = await fetch(`${BASE}/consumers/${CONSUMER_GROUP}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/vnd.kafka.v2+json",
    },
    body: JSON.stringify({
      name: instanceName,
      format: "json",
      "auto.offset.reset": "earliest",
    }),
  })

  if (!response.ok) {
    throw new Error(`Failed to create consumer: ${response.status}`)
  }

  const data = await response.json()
  return rewriteBaseUri(data.base_uri)
}

export async function subscribe(baseUri: string): Promise<void> {
  const response = await fetch(`${baseUri}/subscription`, {
    method: "POST",
    headers: {
      "Content-Type": "application/vnd.kafka.v2+json",
    },
    body: JSON.stringify({ topics: [TOPIC] }),
  })

  if (!response.ok) {
    throw new Error(`Failed to subscribe: ${response.status}`)
  }
}

export async function pollRecords(baseUri: string): Promise<Order[]> {
  const response = await fetch(`${baseUri}/records`, {
    headers: {
      Accept: "application/vnd.kafka.json.v2+json",
    },
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new ConsumerNotFoundError()
    }
    throw new Error(`Failed to poll records: ${response.status}`)
  }

  const records = await response.json()
  return records.map((r: { key: string; value: KafkaOrderMessage }) =>
    messageToOrder(r.value),
  )
}

export async function destroyConsumer(baseUri: string): Promise<void> {
  try {
    await fetch(baseUri, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/vnd.kafka.v2+json",
      },
    })
  } catch {
    // Best-effort cleanup — ignore errors
  }
}

export class ConsumerNotFoundError extends Error {
  constructor() {
    super("Consumer instance not found (expired or deleted)")
    this.name = "ConsumerNotFoundError"
  }
}
