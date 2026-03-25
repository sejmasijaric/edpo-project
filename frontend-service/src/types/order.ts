export const COLORS = [
  { value: "red", label: "Red", className: "bg-red-500" },
  { value: "white", label: "White", className: "bg-white border border-input" },
  { value: "blue", label: "Blue", className: "bg-blue-500" },
] as const

export const MAX_TEXT_LENGTH = 20

export type ColorValue = (typeof COLORS)[number]["value"]

export type OrderStatus = "To Do" | "In Progress" | "Done" | "Error"

export interface Order {
  id: string
  color: ColorValue
  engravedText?: string
  status: OrderStatus
  createdAt: Date
}

export function getColorConfig(color: string) {
  return COLORS.find((c) => c.value === color)
}
