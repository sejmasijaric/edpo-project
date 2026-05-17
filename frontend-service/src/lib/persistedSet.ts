import { useCallback, useEffect, useState } from "react"

interface Entry {
  values: Record<string, number>
}

const DEFAULT_TTL_MS = 24 * 60 * 60 * 1000

function read(key: string, ttlMs: number): Set<string> {
  if (typeof window === "undefined") return new Set()
  try {
    const raw = window.localStorage.getItem(key)
    if (!raw) return new Set()
    const parsed = JSON.parse(raw) as Entry
    const cutoff = Date.now() - ttlMs
    const live = Object.entries(parsed.values ?? {}).filter(
      ([, ts]) => ts >= cutoff
    )
    return new Set(live.map(([id]) => id))
  } catch {
    return new Set()
  }
}

function write(key: string, set: Set<string>): void {
  if (typeof window === "undefined") return
  try {
    const now = Date.now()
    const values: Record<string, number> = {}
    for (const v of set) values[v] = now
    window.localStorage.setItem(key, JSON.stringify({ values }))
  } catch {
    // Ignore quota errors etc.
  }
}

export function usePersistedSet(
  key: string,
  ttlMs: number = DEFAULT_TTL_MS
): [Set<string>, (id: string) => void, () => void] {
  const [set, setSet] = useState<Set<string>>(() => read(key, ttlMs))

  useEffect(() => {
    setSet(read(key, ttlMs))
  }, [key, ttlMs])

  const add = useCallback(
    (id: string) => {
      setSet((prev) => {
        if (prev.has(id)) return prev
        const next = new Set(prev)
        next.add(id)
        write(key, next)
        return next
      })
    },
    [key]
  )

  const clear = useCallback(() => {
    setSet(new Set())
    if (typeof window !== "undefined") {
      window.localStorage.removeItem(key)
    }
  }, [key])

  return [set, add, clear]
}
