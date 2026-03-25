/**
 * Integration tests for Kafka order flow via REST Proxy.
 *
 * Prerequisites: Kafka + REST Proxy must be running.
 *   docker compose -f docker/docker-compose.yml up kafka kafka-rest -d
 *
 * Run with: npm run test:integration
 */

const REST_PROXY_URL =
  process.env.KAFKA_REST_URL ?? "http://localhost:8082"
const TOPIC = "air-tag-orders"

// Unique consumer group per test run to avoid conflicts
const GROUP_PREFIX = `test-${Date.now()}`

interface TestOrder {
  id: string
  color: "red" | "white" | "blue"
  engravedText?: string
  status: "To Do" | "In Progress" | "Done" | "Error"
  createdAt: string
}

function makeTestOrder(overrides: Partial<TestOrder> = {}): TestOrder {
  return {
    id: crypto.randomUUID(),
    color: "red",
    status: "To Do",
    createdAt: new Date().toISOString(),
    ...overrides,
  }
}

async function produce(
  records: { key: string; value: TestOrder }[],
): Promise<void> {
  const res = await fetch(`${REST_PROXY_URL}/topics/${TOPIC}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/vnd.kafka.json.v2+json",
      Accept: "application/vnd.kafka.v2+json",
    },
    body: JSON.stringify({ records }),
  })
  if (!res.ok) {
    throw new Error(`Produce failed: ${res.status} ${await res.text()}`)
  }
}

async function createConsumer(
  group: string,
): Promise<string> {
  const instanceName = `instance-${Math.random().toString(36).slice(2, 8)}`
  const res = await fetch(`${REST_PROXY_URL}/consumers/${group}`, {
    method: "POST",
    headers: { "Content-Type": "application/vnd.kafka.v2+json" },
    body: JSON.stringify({
      name: instanceName,
      format: "json",
      "auto.offset.reset": "earliest",
    }),
  })
  if (!res.ok) {
    throw new Error(`Create consumer failed: ${res.status} ${await res.text()}`)
  }
  const data = await res.json()
  return data.base_uri as string
}

async function subscribe(baseUri: string): Promise<void> {
  const res = await fetch(`${baseUri}/subscription`, {
    method: "POST",
    headers: { "Content-Type": "application/vnd.kafka.v2+json" },
    body: JSON.stringify({ topics: [TOPIC] }),
  })
  if (!res.ok) {
    throw new Error(`Subscribe failed: ${res.status} ${await res.text()}`)
  }
}

async function poll(
  baseUri: string,
): Promise<{ key: string; value: TestOrder }[]> {
  const res = await fetch(`${baseUri}/records`, {
    headers: { Accept: "application/vnd.kafka.json.v2+json" },
  })
  if (!res.ok) {
    throw new Error(`Poll failed: ${res.status} ${await res.text()}`)
  }
  return res.json()
}

async function destroyConsumer(baseUri: string): Promise<void> {
  await fetch(baseUri, {
    method: "DELETE",
    headers: { "Content-Type": "application/vnd.kafka.v2+json" },
  }).catch(() => {})
}

/**
 * Poll until we get at least `minRecords` records.
 * REST Proxy often returns empty on first poll after subscribe.
 */
async function pollUntil(
  baseUri: string,
  minRecords: number,
  maxAttempts = 10,
): Promise<{ key: string; value: TestOrder }[]> {
  const all: { key: string; value: TestOrder }[] = []
  for (let i = 0; i < maxAttempts; i++) {
    const records = await poll(baseUri)
    all.push(...records)
    if (all.length >= minRecords) return all
    await new Promise((r) => setTimeout(r, 1000))
  }
  return all
}

// --- Setup / Teardown ---

const consumersToCleanup: string[] = []

beforeAll(async () => {
  // Wait for REST Proxy to be ready
  for (let i = 0; i < 30; i++) {
    try {
      const res = await fetch(`${REST_PROXY_URL}/topics`)
      if (res.ok) return
    } catch {
      // not ready yet
    }
    await new Promise((r) => setTimeout(r, 2000))
  }
  throw new Error("REST Proxy did not become ready within 60 seconds")
})

afterAll(async () => {
  for (const uri of consumersToCleanup) {
    await destroyConsumer(uri)
  }
})

// --- Tests ---

describe("Kafka order integration", () => {
  it("can produce an order and consume it back", async () => {
    const order = makeTestOrder({ color: "blue", engravedText: "Test" })

    await produce([{ key: order.id, value: order }])

    const group = `${GROUP_PREFIX}-roundtrip`
    const baseUri = await createConsumer(group)
    consumersToCleanup.push(baseUri)
    await subscribe(baseUri)

    const records = await pollUntil(baseUri, 1)
    const found = records.find((r) => r.value.id === order.id)

    expect(found).toBeDefined()
    expect(found!.value.color).toBe("blue")
    expect(found!.value.engravedText).toBe("Test")
    expect(found!.value.status).toBe("To Do")
  })

  it("status update with same key overwrites previous state", async () => {
    const order = makeTestOrder()

    // Produce initial order
    await produce([{ key: order.id, value: order }])

    // Produce status update with same key
    const updated = { ...order, status: "In Progress" as const }
    await produce([{ key: order.id, value: updated }])

    const group = `${GROUP_PREFIX}-status`
    const baseUri = await createConsumer(group)
    consumersToCleanup.push(baseUri)
    await subscribe(baseUri)

    const records = await pollUntil(baseUri, 2)

    // Build a map (latest per key) — same logic as the frontend hook
    const orderMap = new Map<string, TestOrder>()
    for (const r of records) {
      if (r.value.id === order.id) {
        orderMap.set(r.key, r.value)
      }
    }

    // The last message for this key should have "In Progress"
    const latest = orderMap.get(order.id)
    expect(latest).toBeDefined()
    expect(latest!.status).toBe("In Progress")
  })

  it("multiple orders maintain separate state", async () => {
    const orders = [
      makeTestOrder({ color: "red" }),
      makeTestOrder({ color: "white" }),
      makeTestOrder({ color: "blue" }),
    ]

    await produce(orders.map((o) => ({ key: o.id, value: o })))

    const group = `${GROUP_PREFIX}-multi`
    const baseUri = await createConsumer(group)
    consumersToCleanup.push(baseUri)
    await subscribe(baseUri)

    const records = await pollUntil(baseUri, 3)

    const ids = new Set(records.map((r) => r.value.id))
    for (const order of orders) {
      expect(ids.has(order.id)).toBe(true)
    }
  })

  it("new consumer reads previously produced orders from earliest", async () => {
    const order = makeTestOrder({ engravedText: "earlier" })
    await produce([{ key: order.id, value: order }])

    // Create a brand new consumer after producing
    const group = `${GROUP_PREFIX}-earliest`
    const baseUri = await createConsumer(group)
    consumersToCleanup.push(baseUri)
    await subscribe(baseUri)

    const records = await pollUntil(baseUri, 1)
    const found = records.find((r) => r.value.id === order.id)

    expect(found).toBeDefined()
    expect(found!.value.engravedText).toBe("earlier")
  })
})
