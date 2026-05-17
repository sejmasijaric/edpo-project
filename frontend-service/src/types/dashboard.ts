export interface QcRejectedRate {
  rejectedCount: number
  passedCount: number
  totalCount: number
  rejectedPercentage: number
}

export interface AverageDuration {
  completedAttemptCount: number
  averageDurationMillis: number
}

export interface OpenIntervention {
  itemIdentifier: string
  taskName: string
  currentStage: string
}

export interface ManualInterventions {
  openCount: number
  completedCount: number
  openInterventions: OpenIntervention[]
}

export interface ManufacturingFailureRate {
  failedCount: number
  completedCount: number
  totalCount: number
  failurePercentage: number
  failedItemCount: number
}

export interface DurationStats {
  completedCount: number
  averageDurationMillis: number
  minimumDurationMillis: number
  maximumDurationMillis: number
}

export interface RetryRate {
  completedItemCount: number
  totalRetries: number
  averageRetriesPerCompletedItem: number
  retriesPerItem: Record<string, number>
}

export interface DashboardMetricsResponse {
  from: string
  to: string
  qcRejectedRate: QcRejectedRate
  averageManufacturingTime: AverageDuration
  manualInterventions: ManualInterventions
  manufacturingFailureRate: ManufacturingFailureRate
  averageEndToEndProductionTime: DurationStats
  workInProgressByStage: Record<string, number>
  retryRate: RetryRate
}

export type TimeWindow = "15m" | "1h" | "6h" | "24h"

export const TIME_WINDOWS: { value: TimeWindow; label: string; seconds: number }[] = [
  { value: "15m", label: "Last 15 min", seconds: 15 * 60 },
  { value: "1h", label: "Last hour", seconds: 60 * 60 },
  { value: "6h", label: "Last 6 hours", seconds: 6 * 60 * 60 },
  { value: "24h", label: "Last 24 hours", seconds: 24 * 60 * 60 },
]

export function formatDuration(ms: number): string {
  if (!Number.isFinite(ms) || ms <= 0) return "—"
  const seconds = ms / 1000
  if (seconds < 60) return `${seconds.toFixed(1)}s`
  const minutes = seconds / 60
  if (minutes < 60) return `${minutes.toFixed(1)}m`
  const hours = minutes / 60
  return `${hours.toFixed(1)}h`
}

export function formatPercent(value: number): string {
  if (!Number.isFinite(value)) return "—"
  return `${value.toFixed(1)}%`
}
