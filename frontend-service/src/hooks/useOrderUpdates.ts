import { useEffect, useRef, useState, useCallback } from "react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import type { MachineOrchestrationEvent } from "@/types/machine-event"

export function useOrderUpdates() {
  const [events, setEvents] = useState<MachineOrchestrationEvent[]>([])
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)

  const clearEvents = useCallback(() => setEvents([]), [])

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe("/topic/order-updates", (message) => {
          const event: MachineOrchestrationEvent = JSON.parse(message.body)
          setEvents((prev) => [event, ...prev])
        })
      },
      onDisconnect: () => {
        setConnected(false)
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"])
        setConnected(false)
      },
    })

    clientRef.current = client
    client.activate()

    return () => {
      client.deactivate()
    }
  }, [])

  return { events, connected, clearEvents }
}
