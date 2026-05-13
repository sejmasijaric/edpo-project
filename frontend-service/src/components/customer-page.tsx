import { useState } from "react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { OrderList } from "@/components/order-list"
import { Separator } from "@/components/ui/separator"
import {
  COLORS,
  MAX_TEXT_LENGTH,
  type ColorValue,
  type Order,
} from "@/types/order"
import type { LatestItemStatus, MachineOrchestrationEvent } from "@/types/machine-event"
import { getOutcomeLabel } from "@/types/machine-event"
import { createOrder, fetchLatestItemStatus } from "@/services/api"

interface CustomerPageProps {
  orders: Order[]
  setOrders: React.Dispatch<React.SetStateAction<Order[]>>
  events?: MachineOrchestrationEvent[]
  connected?: boolean
}

export function CustomerPage({
  orders,
  setOrders,
  events = [],
  connected,
}: CustomerPageProps) {
  const [color, setColor] = useState("")
  const [engravedText, setEngravedText] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [trackedItemIdentifier, setTrackedItemIdentifier] = useState("")
  const [trackedStatus, setTrackedStatus] = useState<LatestItemStatus | null>(null)
  const [tracking, setTracking] = useState(false)
  const [lastOrderId, setLastOrderId] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!color) {
      toast.error("Please select a color")
      return
    }

    setSubmitting(true)
    try {
      const order = await createOrder({
        id: crypto.randomUUID(),
        color: color as ColorValue,
        createdAt: new Date(),
        ...(engravedText ? { engravedText } : {}),
      })

      toast.success("Order submitted successfully!")
      setOrders((prev) => [order, ...prev])
      setLastOrderId(order.id)
      setTrackedItemIdentifier(order.id)
      setColor("")
      setEngravedText("")
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit order")
    } finally {
      setSubmitting(false)
    }
  }

  const handleTrackOrder = async (e: React.FormEvent) => {
    e.preventDefault()

    const itemIdentifier = trackedItemIdentifier.trim()
    if (!itemIdentifier) {
      toast.error("Enter an item identifier")
      return
    }

    setTracking(true)
    try {
      const latestStatus = await fetchLatestItemStatus(itemIdentifier)
      setTrackedStatus(latestStatus)
    } catch (err) {
      setTrackedStatus(null)
      toast.error(err instanceof Error ? err.message : "Latest status not found")
    } finally {
      setTracking(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>Order Laser Engraved Air Tag</CardTitle>
          <CardDescription>
            Choose a color and optionally add engraved text.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-3">
              <Label>Color</Label>
              <RadioGroup value={color} onValueChange={setColor}>
                {COLORS.map((c) => (
                  <div key={c.value} className="flex items-center gap-3">
                    <RadioGroupItem value={c.value} id={`color-${c.value}`} />
                    <Label
                      htmlFor={`color-${c.value}`}
                      className="flex items-center gap-2 font-normal"
                    >
                      <span
                        className={`inline-block size-4 rounded-full ${c.className}`}
                      />
                      {c.label}
                    </Label>
                  </div>
                ))}
              </RadioGroup>
            </div>

            <div className="space-y-2">
              <Label htmlFor="engraved-text">Engraved Text (optional)</Label>
              <Input
                id="engraved-text"
                value={engravedText}
                onChange={(e) => setEngravedText(e.target.value)}
                maxLength={MAX_TEXT_LENGTH}
                placeholder="Enter text to engrave"
              />
              <p className="text-muted-foreground text-sm">
                {engravedText.length}/{MAX_TEXT_LENGTH}
              </p>
            </div>

            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting ? "Submitting..." : "Submit Order"}
            </Button>
            {lastOrderId && (
              <div className="rounded-md border bg-muted/40 p-3 text-sm">
                <span className="text-muted-foreground">Last order ID: </span>
                <span className="font-medium">{lastOrderId}</span>
              </div>
            )}
          </form>

          <Separator className="my-6" />

          <form onSubmit={handleTrackOrder} className="space-y-3">
            <Label htmlFor="track-order">Track order</Label>
            <div className="flex flex-col gap-2 sm:flex-row">
              <Input
                id="track-order"
                value={trackedItemIdentifier}
                onChange={(e) => setTrackedItemIdentifier(e.target.value)}
                placeholder="ITEM-2001"
              />
              <Button type="submit" disabled={tracking} className="sm:w-32">
                {tracking ? "Tracking..." : "Track"}
              </Button>
            </div>
            {trackedStatus && (
              <div className="rounded-md border border-primary/40 bg-primary/5 p-3 text-sm">
                <div className="font-medium">{trackedStatus.itemIdentifier}</div>
                <div className="text-muted-foreground">
                  {trackedStatus.station} - {getOutcomeLabel(trackedStatus.outcomeType)}
                </div>
                <div className="text-muted-foreground">
                  {new Date(trackedStatus.timestamp).toLocaleString()} - {trackedStatus.sourceTopic}
                </div>
              </div>
            )}
          </form>
        </CardContent>
      </Card>

      <OrderList
        orders={orders}
        events={events}
        connected={connected}
        highlightedItemIdentifier={trackedStatus?.itemIdentifier}
        latestStatus={trackedStatus}
      />
    </div>
  )
}
