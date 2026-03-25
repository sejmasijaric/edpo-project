import { Wrench } from "lucide-react"
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"

export function WorkerPage() {
  return (
    <Card className="mx-auto max-w-lg">
      <CardHeader className="items-center text-center">
        <Wrench className="text-muted-foreground mb-2 size-10" />
        <CardTitle>Worker Dashboard</CardTitle>
        <CardDescription>Coming soon...</CardDescription>
      </CardHeader>
    </Card>
  )
}
