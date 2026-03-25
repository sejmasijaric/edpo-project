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
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"

const COLORS = [
  { value: "red", label: "Red", className: "bg-red-500" },
  { value: "white", label: "White", className: "bg-white border border-input" },
  { value: "blue", label: "Blue", className: "bg-blue-500" },
] as const

const MAX_TEXT_LENGTH = 20

type ColorValue = (typeof COLORS)[number]["value"]

interface Order {
  id: string
  color: ColorValue
  engravedText?: string
  status: "To Do"
  createdAt: Date
}

function getColorConfig(color: string) {
  return COLORS.find((c) => c.value === color)
}

export function CustomerPage() {
  const [color, setColor] = useState("")
  const [engravedText, setEngravedText] = useState("")
  const [orders, setOrders] = useState<Order[]>([])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!color) {
      toast.error("Please select a color")
      return
    }

    const order: Order = {
      id: crypto.randomUUID(),
      color: color as ColorValue,
      status: "To Do",
      createdAt: new Date(),
      ...(engravedText ? { engravedText } : {}),
    }

    console.log("Order submitted:", {
      color,
      ...(engravedText ? { engravedText } : {}),
    })
    toast.success("Order submitted successfully!")

    setOrders((prev) => [order, ...prev])
    setColor("")
    setEngravedText("")
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

            <Button type="submit" className="w-full">
              Submit Order
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>
            Order History
            {orders.length > 0 && (
              <span className="text-muted-foreground ml-2 text-sm font-normal">
                ({orders.length})
              </span>
            )}
          </CardTitle>
          <CardDescription>Track your submitted orders.</CardDescription>
        </CardHeader>
        <CardContent>
          {orders.length === 0 ? (
            <p className="text-muted-foreground py-8 text-center text-sm">
              No orders yet. Submit your first order!
            </p>
          ) : (
            <div className="space-y-3">
              {orders.map((order, index) => (
                <div key={order.id}>
                  {index > 0 && <Separator className="mb-3" />}
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span
                          className={`inline-block size-3 rounded-full ${getColorConfig(order.color)?.className}`}
                        />
                        <span className="text-sm font-medium">
                          {getColorConfig(order.color)?.label} Air Tag
                        </span>
                      </div>
                      {order.engravedText && (
                        <p className="text-muted-foreground text-sm">
                          Engraved: &ldquo;{order.engravedText}&rdquo;
                        </p>
                      )}
                      <p className="text-muted-foreground text-xs">
                        {order.createdAt.toLocaleTimeString()}
                      </p>
                    </div>
                    <Badge variant="secondary">{order.status}</Badge>
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
