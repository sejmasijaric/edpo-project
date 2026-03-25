import { useCallback, useEffect, useRef, useState } from "react"
import type { Order, OrderStatus } from "@/types/order"
import {
  produceOrder,
  createConsumer,
  subscribe,
  pollRecords,
  destroyConsumer,
  ConsumerNotFoundError,
} from "@/services/kafka"

const POLL_INTERVAL_MS = 2000

export interface UseKafkaOrdersReturn {
  orders: Order[]
  submitOrder: (order: Order) => Promise<void>
  updateOrderStatus: (orderId: string, newStatus: OrderStatus) => Promise<void>
  isConnected: boolean
}

export function useKafkaOrders(): UseKafkaOrdersReturn {
  const [ordersMap, setOrdersMap] = useState<Map<string, Order>>(new Map())
  const [isConnected, setIsConnected] = useState(false)

  const consumerUri = useRef<string | null>(null)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const initialized = useRef(false)

  const orders = Array.from(ordersMap.values()).sort(
    (a, b) => b.createdAt.getTime() - a.createdAt.getTime(),
  )

  const startConsumer = useCallback(async () => {
    try {
      const uri = await createConsumer()
      consumerUri.current = uri
      await subscribe(uri)
      setIsConnected(true)

      // First poll primes the consumer (REST Proxy quirk — often returns empty)
      await pollRecords(uri).catch(() => {})

      intervalRef.current = setInterval(async () => {
        if (!consumerUri.current) return
        try {
          const newOrders = await pollRecords(consumerUri.current)
          if (newOrders.length > 0) {
            setOrdersMap((prev) => {
              const next = new Map(prev)
              for (const order of newOrders) {
                next.set(order.id, order)
              }
              return next
            })
          }
        } catch (err) {
          if (err instanceof ConsumerNotFoundError) {
            // Consumer expired — recreate
            clearInterval(intervalRef.current!)
            intervalRef.current = null
            consumerUri.current = null
            setIsConnected(false)
            startConsumer()
          }
        }
      }, POLL_INTERVAL_MS)
    } catch {
      setIsConnected(false)
      // Retry after a delay if REST Proxy isn't ready
      setTimeout(startConsumer, 3000)
    }
  }, [])

  useEffect(() => {
    if (initialized.current) return
    initialized.current = true

    startConsumer()

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
      if (consumerUri.current) {
        destroyConsumer(consumerUri.current)
        consumerUri.current = null
      }
      initialized.current = false
    }
  }, [startConsumer])

  const submitOrder = useCallback(
    async (order: Order) => {
      await produceOrder(order)
      // Optimistic update
      setOrdersMap((prev) => new Map(prev).set(order.id, order))
    },
    [],
  )

  const updateOrderStatus = useCallback(
    async (orderId: string, newStatus: OrderStatus) => {
      const existing = ordersMap.get(orderId)
      if (!existing) return

      const updated: Order = { ...existing, status: newStatus }
      await produceOrder(updated)
      // Optimistic update
      setOrdersMap((prev) => new Map(prev).set(orderId, updated))
    },
    [ordersMap],
  )

  return { orders, submitOrder, updateOrderStatus, isConnected }
}
