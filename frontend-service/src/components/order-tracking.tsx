import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import type { MachineOrchestrationEvent } from "@/types/machine-event"
import { getOutcomeLabel, getOutcomeStatus } from "@/types/machine-event"

const statusVariant: Record<string, "default" | "destructive" | "outline"> = {
  success: "outline",
  error: "destructive",
  info: "default",
}

interface OrderTrackingProps {
  events: MachineOrchestrationEvent[]
  connected: boolean
}

export function OrderTracking({ events, connected }: OrderTrackingProps) {
  const grouped = groupByItem(events)

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Order Tracking
            <span
              data-testid="ws-status"
              className={`inline-block size-2 rounded-full ${connected ? "bg-green-500" : "bg-red-500"}`}
            />
          </CardTitle>
          <CardDescription>
            Real-time updates from the production line.
            {connected ? " Connected." : " Disconnected."}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {events.length === 0 ? (
            <p className="text-muted-foreground py-8 text-center text-sm">
              No events yet. Updates will appear here in real-time.
            </p>
          ) : (
            <div className="space-y-4">
              {Object.entries(grouped).map(([itemId, itemEvents], index) => (
                <div key={itemId}>
                  {index > 0 && <Separator className="mb-4" />}
                  <div className="space-y-2">
                    <p className="text-sm font-medium">
                      Order:{" "}
                      <span className="text-muted-foreground font-mono text-xs">
                        {itemId}
                      </span>
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {itemEvents.map((event, i) => {
                        const status = getOutcomeStatus(event.outcomeType)
                        return (
                          <Badge
                            key={`${event.itemIdentifier}-${event.outcomeType}-${i}`}
                            variant={statusVariant[status]}
                          >
                            {getOutcomeLabel(event.outcomeType)}
                          </Badge>
                        )
                      })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function groupByItem(
  events: MachineOrchestrationEvent[]
): Record<string, MachineOrchestrationEvent[]> {
  const grouped: Record<string, MachineOrchestrationEvent[]> = {}
  for (const event of events) {
    if (!grouped[event.itemIdentifier]) {
      grouped[event.itemIdentifier] = []
    }
    grouped[event.itemIdentifier].push(event)
  }
  return grouped
}
