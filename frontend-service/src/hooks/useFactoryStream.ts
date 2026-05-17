import { useEffect, useRef, useState } from "react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import type { MachineOrchestrationEvent, LatestItemStatus } from "@/types/machine-event"
import type { UserTaskEvent } from "@/types/user-task"

const MAX_EVENTS = 200
const MAX_USER_TASKS = 100
const MAX_ITEM_STATUSES = 200

export function useFactoryStream() {
  const [orderEvents, setOrderEvents] = useState<MachineOrchestrationEvent[]>([])
  const [userTasks, setUserTasks] = useState<UserTaskEvent[]>([])
  const [itemStatuses, setItemStatuses] = useState<LatestItemStatus[]>([])
  const [latestStatusByItem, setLatestStatusByItem] = useState<
    Record<string, LatestItemStatus>
  >({})
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe("/topic/order-updates", (message) => {
          const event: MachineOrchestrationEvent = JSON.parse(message.body)
          setOrderEvents((prev) => [event, ...prev].slice(0, MAX_EVENTS))
        })
        client.subscribe("/topic/user-tasks", (message) => {
          const event: UserTaskEvent = JSON.parse(message.body)
          setUserTasks((prev) => [event, ...prev].slice(0, MAX_USER_TASKS))
        })
        client.subscribe("/topic/item-status", (message) => {
          const status: LatestItemStatus = JSON.parse(message.body)
          setItemStatuses((prev) => [status, ...prev].slice(0, MAX_ITEM_STATUSES))
          if (status.itemIdentifier) {
            setLatestStatusByItem((prev) => ({
              ...prev,
              [status.itemIdentifier]: status,
            }))
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"])
        setConnected(false)
      },
      onWebSocketClose: () => setConnected(false),
    })

    clientRef.current = client
    client.activate()

    return () => {
      void client.deactivate()
    }
  }, [])

  return {
    orderEvents,
    userTasks,
    itemStatuses,
    latestStatusByItem,
    connected,
  }
}
