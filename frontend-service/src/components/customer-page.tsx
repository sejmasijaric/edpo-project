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
import {
  COLORS,
  MAX_TEXT_LENGTH,
  type ColorValue,
  type Order,
} from "@/types/order"
import { createOrder } from "@/services/api"

interface CustomerPageProps {
  orders: Order[]
  setOrders: React.Dispatch<React.SetStateAction<Order[]>>
}

export function CustomerPage({ orders, setOrders }: CustomerPageProps) {
  const [color, setColor] = useState("")
  const [engravedText, setEngravedText] = useState("")
  const [submitting, setSubmitting] = useState(false)

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
      setColor("")
      setEngravedText("")
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to submit order")
    } finally {
      setSubmitting(false)
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
          </form>
        </CardContent>
      </Card>

      <OrderList orders={orders} />
    </div>
  )
}
