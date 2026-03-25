import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { OrderList } from "@/components/order-list"
import type { Order, OrderStatus } from "@/types/order"

interface WorkerPageProps {
  orders: Order[]
  setOrders: React.Dispatch<React.SetStateAction<Order[]>>
}

export function WorkerPage({ orders, setOrders }: WorkerPageProps) {
  const handleStatusUpdate = (orderId: string, newStatus: OrderStatus) => {
    setOrders((prev) =>
      prev.map((order) =>
        order.id === orderId ? { ...order, status: newStatus } : order
      )
    )
    toast.success(`Order status updated to "${newStatus}"`)
  }

  return (
    <OrderList
      orders={orders}
      title="Production Queue"
      description="Monitor and manage all orders."
      emptyMessage="No orders in the queue."
      renderActions={(order) => {
        switch (order.status) {
          case "To Do":
            return (
              <Button
                size="sm"
                onClick={() => handleStatusUpdate(order.id, "In Progress")}
              >
                Start
              </Button>
            )
          case "In Progress":
            return (
              <div className="flex gap-1">
                <Button
                  size="sm"
                  onClick={() => handleStatusUpdate(order.id, "Done")}
                >
                  Done
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => handleStatusUpdate(order.id, "Error")}
                >
                  Error
                </Button>
              </div>
            )
          case "Error":
            return (
              <Button
                size="sm"
                onClick={() => handleStatusUpdate(order.id, "In Progress")}
              >
                Retry
              </Button>
            )
          case "Done":
            return null
        }
      }}
    />
  )
}
