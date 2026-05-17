export interface UserTaskEvent {
  itemIdentifier?: string | null
  commandType?: string | null
  taskName?: string | null
  taskCategory?: string | null
  stationName?: string | null
  targetColor?: string | null
  taskStatus?: string | null
  errorMessage?: string | null
  eventTimestampEpochMillis?: number | null
}

export function isErrorTask(task: UserTaskEvent): boolean {
  if (task.errorMessage) {
    return true
  }
  if (!task.taskCategory) {
    return false
  }
  return task.taskCategory.toLowerCase() === "error"
}

export function isCompletedTask(task: UserTaskEvent): boolean {
  const status = task.taskStatus?.toLowerCase() ?? ""
  return (
    status.includes("complete") ||
    status.includes("resolved") ||
    status.includes("closed")
  )
}

export function normalizeColor(color: string | null | undefined): string | null {
  if (!color) return null
  return color.toLowerCase()
}

export function colorClassName(color: string | null | undefined): string {
  switch (normalizeColor(color)) {
    case "red":
      return "bg-red-500"
    case "blue":
      return "bg-blue-500"
    case "white":
      return "bg-white border border-input"
    default:
      return "bg-muted"
  }
}
