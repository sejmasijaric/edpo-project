import type { Order, OrderStatus } from "@/types/order"
import { getColorConfig } from "@/types/order"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"

const statusVariant: Record<
  OrderStatus,
  "secondary" | "default" | "outline" | "destructive"
> = {
  "To Do": "secondary",
  "In Progress": "default",
  Done: "outline",
  Error: "destructive",
}

interface OrderListProps {
  orders: Order[]
  title?: string
  description?: string
  emptyMessage?: string
  renderActions?: (order: Order) => React.ReactNode
}

export function OrderList({
  orders,
  title = "Order History",
  description = "Track your submitted orders.",
  emptyMessage = "No orders yet. Submit your first order!",
  renderActions,
}: OrderListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>
          {title}
          {orders.length > 0 && (
            <span className="text-muted-foreground ml-2 text-sm font-normal">
              ({orders.length})
            </span>
          )}
        </CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {orders.length === 0 ? (
          <p className="text-muted-foreground py-8 text-center text-sm">
            {emptyMessage}
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
                  <div className="flex flex-col items-end gap-1">
                    <Badge variant={statusVariant[order.status]}>
                      {order.status}
                    </Badge>
                    {renderActions?.(order)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
