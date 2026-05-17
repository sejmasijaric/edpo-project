import { useEffect, useMemo, useState } from "react"
import { Activity, AlertTriangle, Gauge, RefreshCw, Timer, TrendingDown, Wrench } from "lucide-react"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import {
  TIME_WINDOWS,
  formatDuration,
  formatPercent,
  type DashboardMetricsResponse,
  type TimeWindow,
} from "@/types/dashboard"
import { fetchDashboardMetrics } from "@/services/api"

const REFRESH_INTERVAL_MS = 10_000

export function DiagnosticsPage() {
  const [window, setWindow] = useState<TimeWindow>("1h")
  const [metrics, setMetrics] = useState<DashboardMetricsResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastRefresh, setLastRefresh] = useState<Date | null>(null)
  const [autoRefresh, setAutoRefresh] = useState(true)

  const range = useMemo(() => {
    const config = TIME_WINDOWS.find((w) => w.value === window) ?? TIME_WINDOWS[1]
    const to = new Date()
    const from = new Date(to.getTime() - config.seconds * 1000)
    return { from: from.toISOString(), to: to.toISOString() }
  }, [window])

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchDashboardMetrics(range)
      setMetrics(data)
      setLastRefresh(new Date())
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load metrics")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [range])

  useEffect(() => {
    if (!autoRefresh) return
    const id = setInterval(() => {
      void load()
    }, REFRESH_INTERVAL_MS)
    return () => clearInterval(id)
  }, [autoRefresh, range])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">Production Diagnostics</h1>
          <p className="text-muted-foreground text-sm">
            Live operational metrics from the factory pipeline.
            {lastRefresh && (
              <span className="ml-2">
                Updated {lastRefresh.toLocaleTimeString()}
              </span>
            )}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex flex-wrap gap-1">
            {TIME_WINDOWS.map((w) => (
              <Button
                key={w.value}
                variant={window === w.value ? "default" : "outline"}
                size="sm"
                onClick={() => setWindow(w.value)}
              >
                {w.label}
              </Button>
            ))}
          </div>
          <Button
            size="sm"
            variant={autoRefresh ? "secondary" : "outline"}
            onClick={() => setAutoRefresh((v) => !v)}
          >
            <RefreshCw className={`mr-1 size-3 ${autoRefresh ? "animate-spin" : ""}`} />
            Auto
          </Button>
          <Button size="sm" variant="outline" onClick={load} disabled={loading}>
            Refresh
          </Button>
        </div>
      </div>

      {error && (
        <Card className="border-destructive">
          <CardContent className="pt-6">
            <p className="text-destructive text-sm">{error}</p>
          </CardContent>
        </Card>
      )}

      {!metrics && loading && (
        <p className="text-muted-foreground text-sm">Loading metrics...</p>
      )}

      {metrics && (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <DialCard
              title="QC Rejected Rate"
              icon={<TrendingDown className="size-4" />}
              valuePercent={metrics.qcRejectedRate.rejectedPercentage}
              subtitle={`${metrics.qcRejectedRate.rejectedCount} of ${metrics.qcRejectedRate.totalCount} rejected`}
              colorBy="lowIsGood"
            />
            <DialCard
              title="Manufacturing Failure Rate"
              icon={<AlertTriangle className="size-4" />}
              valuePercent={metrics.manufacturingFailureRate.failurePercentage}
              subtitle={`${metrics.manufacturingFailureRate.failedCount} failed / ${metrics.manufacturingFailureRate.totalCount} attempts`}
              colorBy="lowIsGood"
            />
            <StatCard
              title="Avg Manufacturing Time"
              icon={<Timer className="size-4" />}
              value={formatDuration(metrics.averageManufacturingTime.averageDurationMillis)}
              subtitle={`${metrics.averageManufacturingTime.completedAttemptCount} completed attempts`}
            />
            <StatCard
              title="Avg End-to-End"
              icon={<Activity className="size-4" />}
              value={formatDuration(metrics.averageEndToEndProductionTime.averageDurationMillis)}
              subtitle={`min ${formatDuration(metrics.averageEndToEndProductionTime.minimumDurationMillis)} · max ${formatDuration(metrics.averageEndToEndProductionTime.maximumDurationMillis)}`}
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Gauge className="size-4" /> Work in Progress
                </CardTitle>
                <CardDescription>Items active in each stage</CardDescription>
              </CardHeader>
              <CardContent>
                <WipBars wipByStage={metrics.workInProgressByStage} />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Wrench className="size-4" /> Manual Interventions
                </CardTitle>
                <CardDescription>
                  {metrics.manualInterventions.openCount} open ·{" "}
                  {metrics.manualInterventions.completedCount} completed in window
                </CardDescription>
              </CardHeader>
              <CardContent>
                {metrics.manualInterventions.openInterventions.length === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    No open interventions.
                  </p>
                ) : (
                  <div className="space-y-2">
                    {metrics.manualInterventions.openInterventions
                      .slice(0, 6)
                      .map((open, i) => (
                        <div key={`${open.itemIdentifier}-${i}`}>
                          {i > 0 && <Separator className="mb-2" />}
                          <div className="flex items-center justify-between gap-2 text-sm">
                            <div>
                              <p className="font-medium">{open.taskName}</p>
                              <p className="text-muted-foreground text-xs font-mono">
                                {open.itemIdentifier}
                              </p>
                            </div>
                            <Badge variant="outline" className="text-xs">
                              {open.currentStage}
                            </Badge>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <RefreshCw className="size-4" /> Retry Rate
                </CardTitle>
                <CardDescription>
                  {metrics.retryRate.totalRetries} retries across{" "}
                  {metrics.retryRate.completedItemCount} completed items
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="text-3xl font-bold">
                      {metrics.retryRate.averageRetriesPerCompletedItem.toFixed(2)}
                    </p>
                    <p className="text-muted-foreground text-xs">
                      avg retries / completed item
                    </p>
                  </div>
                  {Object.keys(metrics.retryRate.retriesPerItem).length > 0 && (
                    <div className="space-y-1">
                      <p className="text-xs font-medium">Top retried items</p>
                      {Object.entries(metrics.retryRate.retriesPerItem)
                        .sort(([, a], [, b]) => b - a)
                        .slice(0, 4)
                        .map(([item, count]) => (
                          <div
                            key={item}
                            className="flex items-center justify-between text-xs"
                          >
                            <span className="text-muted-foreground font-mono truncate">
                              {item}
                            </span>
                            <Badge variant="secondary" className="text-xs">
                              {count}
                            </Badge>
                          </div>
                        ))}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  )
}

interface DialCardProps {
  title: string
  icon: React.ReactNode
  valuePercent: number
  subtitle: string
  colorBy?: "lowIsGood" | "highIsGood"
}

function DialCard({ title, icon, valuePercent, subtitle, colorBy }: DialCardProps) {
  const pct = Number.isFinite(valuePercent) ? Math.max(0, Math.min(100, valuePercent)) : 0
  const stroke =
    colorBy === "lowIsGood"
      ? pct < 5
        ? "stroke-emerald-500"
        : pct < 15
          ? "stroke-amber-500"
          : "stroke-red-500"
      : "stroke-primary"

  const radius = 42
  const circumference = 2 * Math.PI * radius
  const dashOffset = circumference - (pct / 100) * circumference

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center gap-2 text-sm font-medium">
          {icon} {title}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-4">
          <svg viewBox="0 0 100 100" className="size-24">
            <circle
              cx="50"
              cy="50"
              r={radius}
              className="stroke-muted fill-none"
              strokeWidth="10"
            />
            <circle
              cx="50"
              cy="50"
              r={radius}
              className={`fill-none ${stroke}`}
              strokeWidth="10"
              strokeLinecap="round"
              strokeDasharray={circumference}
              strokeDashoffset={dashOffset}
              transform="rotate(-90 50 50)"
            />
            <text
              x="50"
              y="55"
              textAnchor="middle"
              className="fill-current text-sm font-semibold"
            >
              {formatPercent(pct)}
            </text>
          </svg>
          <p className="text-muted-foreground text-xs">{subtitle}</p>
        </div>
      </CardContent>
    </Card>
  )
}

interface StatCardProps {
  title: string
  icon: React.ReactNode
  value: string
  subtitle: string
}

function StatCard({ title, icon, value, subtitle }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center gap-2 text-sm font-medium">
          {icon} {title}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-3xl font-bold">{value}</p>
        <p className="text-muted-foreground text-xs">{subtitle}</p>
      </CardContent>
    </Card>
  )
}

const STAGE_ORDER = ["INTAKE", "MANUFACTURING", "QC", "MANUAL_INTERVENTION"]
const STAGE_LABELS: Record<string, string> = {
  INTAKE: "Intake",
  MANUFACTURING: "Manufacturing",
  QC: "Quality Control",
  MANUAL_INTERVENTION: "Manual",
}
const STAGE_COLORS: Record<string, string> = {
  INTAKE: "bg-sky-500",
  MANUFACTURING: "bg-violet-500",
  QC: "bg-emerald-500",
  MANUAL_INTERVENTION: "bg-amber-500",
}

function WipBars({ wipByStage }: { wipByStage: Record<string, number> }) {
  const total = Object.values(wipByStage).reduce((a, b) => a + b, 0)
  if (total === 0) {
    return (
      <p className="text-muted-foreground text-sm">No items in progress.</p>
    )
  }
  const max = Math.max(1, ...Object.values(wipByStage))
  return (
    <div className="space-y-3">
      {STAGE_ORDER.map((stage) => {
        const count = wipByStage[stage] ?? 0
        const pct = (count / max) * 100
        return (
          <div key={stage} className="space-y-1">
            <div className="flex items-center justify-between text-xs">
              <span>{STAGE_LABELS[stage] ?? stage}</span>
              <span className="font-medium">{count}</span>
            </div>
            <div className="bg-muted h-2 overflow-hidden rounded">
              <div
                className={`h-full ${STAGE_COLORS[stage] ?? "bg-primary"}`}
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>
        )
      })}
    </div>
  )
}
