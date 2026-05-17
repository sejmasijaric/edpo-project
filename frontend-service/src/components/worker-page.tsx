import { useEffect, useMemo, useState } from "react"
import { AlertTriangle, Box, CheckCircle2, Wrench } from "lucide-react"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Button } from "@/components/ui/button"
import {
  colorClassName,
  isCompletedTask,
  isErrorTask,
  type UserTaskEvent,
} from "@/types/user-task"
import { fetchOpenUserTasks, fetchRecentUserTasks } from "@/services/api"

interface WorkerPageProps {
  liveTasks?: UserTaskEvent[]
  connected?: boolean
}

const INTAKE_COMMAND = "insert-item-into-intake-command"

function taskColorLabel(color?: string | null): string {
  if (!color) return ""
  const upper = color.toUpperCase()
  return upper.charAt(0) + upper.slice(1).toLowerCase()
}

function formatTimestamp(ts?: number | null): string {
  if (!ts) return ""
  return new Date(ts).toLocaleTimeString()
}

function mergeTasks(open: UserTaskEvent[], live: UserTaskEvent[]): UserTaskEvent[] {
  const byKey = new Map<string, UserTaskEvent>()
  const keyFor = (t: UserTaskEvent) =>
    `${t.itemIdentifier ?? "?"}::${t.commandType ?? t.taskName ?? "?"}`

  for (const task of open) {
    byKey.set(keyFor(task), task)
  }
  for (const task of live) {
    const key = keyFor(task)
    if (isCompletedTask(task)) {
      byKey.delete(key)
    } else {
      byKey.set(key, task)
    }
  }
  return Array.from(byKey.values()).sort(
    (a, b) =>
      (b.eventTimestampEpochMillis ?? 0) - (a.eventTimestampEpochMillis ?? 0)
  )
}

export function WorkerPage({ liveTasks = [], connected }: WorkerPageProps) {
  const [initialOpen, setInitialOpen] = useState<UserTaskEvent[]>([])
  const [initialRecent, setInitialRecent] = useState<UserTaskEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refresh = async () => {
    setLoading(true)
    setError(null)
    try {
      const [open, recent] = await Promise.all([
        fetchOpenUserTasks(),
        fetchRecentUserTasks(),
      ])
      setInitialOpen(open)
      setInitialRecent(recent)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load user tasks")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void refresh()
  }, [])

  const openTasks = useMemo(
    () => mergeTasks(initialOpen, liveTasks),
    [initialOpen, liveTasks]
  )

  const intakeTasks = openTasks.filter((t) => t.commandType === INTAKE_COMMAND)
  const otherTasks = openTasks.filter((t) => t.commandType !== INTAKE_COMMAND)

  const errorEvents = useMemo(() => {
    const seen = new Set<string>()
    const combined = [...liveTasks, ...initialRecent]
    return combined
      .filter((task) => {
        if (!isErrorTask(task)) return false
        const key = `${task.itemIdentifier}-${task.taskName}-${task.eventTimestampEpochMillis}`
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      .slice(0, 20)
  }, [liveTasks, initialRecent])

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-4">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Box className="size-5" /> Items to Insert
                {connected !== undefined && (
                  <span
                    data-testid="ws-status"
                    className={`inline-block size-2 rounded-full ${connected ? "bg-green-500" : "bg-red-500"}`}
                    title={connected ? "Live updates connected" : "Live updates disconnected"}
                  />
                )}
              </CardTitle>
              <CardDescription>
                Insert the requested color into the intake sink.
              </CardDescription>
            </div>
            <Button size="sm" variant="outline" onClick={refresh} disabled={loading}>
              {loading ? "Refreshing..." : "Refresh"}
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {error && <p className="text-destructive text-sm">{error}</p>}
          {!error && intakeTasks.length === 0 ? (
            <p className="text-muted-foreground py-6 text-center text-sm">
              No items waiting for intake.
            </p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2">
              {intakeTasks.map((task) => (
                <IntakeTaskCard
                  key={`${task.itemIdentifier}-${task.eventTimestampEpochMillis}`}
                  task={task}
                />
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Wrench className="size-5" /> Open Manual Tasks
            </CardTitle>
            <CardDescription>
              Worker actions required at the manufacturing or QC stations.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {otherTasks.length === 0 ? (
              <p className="text-muted-foreground py-6 text-center text-sm">
                No open manual tasks.
              </p>
            ) : (
              <div className="space-y-3">
                {otherTasks.map((task, idx) => (
                  <div key={`${task.itemIdentifier}-${task.taskName}-${idx}`}>
                    {idx > 0 && <Separator className="mb-3" />}
                    <UserTaskRow task={task} />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <AlertTriangle className="text-destructive size-5" /> Recent Errors
            </CardTitle>
            <CardDescription>
              Errors and recoverable issues reported by the production line.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {errorEvents.length === 0 ? (
              <p className="text-muted-foreground py-6 text-center text-sm">
                No recent errors. <CheckCircle2 className="ml-1 inline size-4" />
              </p>
            ) : (
              <div className="space-y-3">
                {errorEvents.map((task, idx) => (
                  <div
                    key={`${task.itemIdentifier}-${task.eventTimestampEpochMillis}-${idx}`}
                  >
                    {idx > 0 && <Separator className="mb-3" />}
                    <UserTaskRow task={task} highlightError />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

function IntakeTaskCard({ task }: { task: UserTaskEvent }) {
  const color = task.targetColor ?? null
  return (
    <div className="bg-card flex items-start gap-3 rounded-md border p-4">
      <span
        className={`mt-1 inline-block size-8 rounded-full ${colorClassName(color)}`}
        aria-label={`${taskColorLabel(color)} airtag`}
      />
      <div className="flex-1 space-y-1">
        <div className="flex items-center justify-between gap-2">
          <p className="text-sm font-semibold">
            Insert {taskColorLabel(color) || "an"} AirTag
          </p>
          <Badge variant="secondary">{task.stationName ?? "intake"}</Badge>
        </div>
        <p className="text-muted-foreground text-xs">
          Item: <span className="font-mono">{task.itemIdentifier}</span>
        </p>
        {task.eventTimestampEpochMillis && (
          <p className="text-muted-foreground text-xs">
            Issued at {formatTimestamp(task.eventTimestampEpochMillis)}
          </p>
        )}
      </div>
    </div>
  )
}

function UserTaskRow({
  task,
  highlightError = false,
}: {
  task: UserTaskEvent
  highlightError?: boolean
}) {
  const error = highlightError || isErrorTask(task)
  return (
    <div
      className={`flex items-start justify-between gap-3 rounded-md p-2 ${error ? "bg-destructive/10" : ""}`}
    >
      <div className="space-y-1">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium">{task.taskName ?? "Task"}</span>
          {task.stationName && (
            <Badge variant="outline" className="text-xs">
              {task.stationName}
            </Badge>
          )}
          {task.taskCategory && (
            <Badge
              variant={
                task.taskCategory.toLowerCase() === "error"
                  ? "destructive"
                  : "secondary"
              }
              className="text-xs"
            >
              {task.taskCategory}
            </Badge>
          )}
        </div>
        <p className="text-muted-foreground text-xs">
          Item: <span className="font-mono">{task.itemIdentifier}</span>
        </p>
        {task.errorMessage && (
          <p className="text-destructive text-xs">{task.errorMessage}</p>
        )}
        {task.eventTimestampEpochMillis && (
          <p className="text-muted-foreground text-xs">
            {formatTimestamp(task.eventTimestampEpochMillis)}
          </p>
        )}
      </div>
    </div>
  )
}
