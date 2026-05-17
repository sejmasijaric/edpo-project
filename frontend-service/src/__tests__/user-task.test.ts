import {
  colorClassName,
  isCompletedTask,
  isErrorTask,
  normalizeColor,
} from "@/types/user-task"

describe("user-task helpers", () => {
  it("detects error tasks by category", () => {
    expect(isErrorTask({ taskCategory: "error" })).toBe(true)
    expect(isErrorTask({ taskCategory: "Error" })).toBe(true)
    expect(isErrorTask({ taskCategory: "normal" })).toBe(false)
  })

  it("detects error tasks via errorMessage", () => {
    expect(isErrorTask({ taskCategory: "normal", errorMessage: "x" })).toBe(true)
  })

  it("detects completed tasks from status", () => {
    expect(isCompletedTask({ taskStatus: "completed" })).toBe(true)
    expect(isCompletedTask({ taskStatus: "resolved" })).toBe(true)
    expect(isCompletedTask({ taskStatus: "Closed" })).toBe(true)
    expect(isCompletedTask({ taskStatus: "open" })).toBe(false)
    expect(isCompletedTask({})).toBe(false)
  })

  it("normalizes colors to lowercase", () => {
    expect(normalizeColor("RED")).toBe("red")
    expect(normalizeColor(null)).toBeNull()
    expect(normalizeColor(undefined)).toBeNull()
  })

  it("maps colors to tailwind classes", () => {
    expect(colorClassName("red")).toContain("red")
    expect(colorClassName("BLUE")).toContain("blue")
    expect(colorClassName("white")).toContain("white")
    expect(colorClassName("unknown")).toContain("muted")
  })
})
