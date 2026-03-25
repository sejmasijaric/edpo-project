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

const COLORS = [
  { value: "red", label: "Red", className: "bg-red-500" },
  { value: "white", label: "White", className: "bg-white border border-input" },
  { value: "blue", label: "Blue", className: "bg-blue-500" },
] as const

const MAX_TEXT_LENGTH = 20

export function CustomerPage() {
  const [color, setColor] = useState("")
  const [engravedText, setEngravedText] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!color) {
      toast.error("Please select a color")
      return
    }

    const order = {
      color,
      ...(engravedText ? { engravedText } : {}),
    }

    console.log("Order submitted:", order)
    toast.success("Order submitted successfully!")

    setColor("")
    setEngravedText("")
  }

  return (
    <Card className="mx-auto max-w-lg">
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
  )
}
