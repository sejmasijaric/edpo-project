import {
  getOutcomeLabel,
  getOutcomeStatus,
  OUTCOME_LABELS,
} from "@/types/machine-event"

describe("getOutcomeLabel", () => {
  it("returns human-readable label for known outcome types", () => {
    expect(getOutcomeLabel("intake-completed")).toBe("Intake Completed")
    expect(getOutcomeLabel("manufacturing-completed")).toBe("Manufacturing Completed")
    expect(getOutcomeLabel("manufacturing-failed")).toBe("Manufacturing Failed")
    expect(getOutcomeLabel("qc-shipping")).toBe("QC Approved for Shipping")
    expect(getOutcomeLabel("qc-rejection")).toBe("QC Rejected")
  })

  it("returns raw outcomeType for unknown types", () => {
    expect(getOutcomeLabel("some-unknown-type")).toBe("some-unknown-type")
  })

  it("has labels for all defined outcome types", () => {
    const expectedKeys = [
      "intake-completed",
      "manufacturing-completed",
      "manufacturing-failed",
      "qc-shipping",
      "qc-rejection",
    ]
    for (const key of expectedKeys) {
      expect(OUTCOME_LABELS[key]).toBeDefined()
    }
  })
})

describe("getOutcomeStatus", () => {
  it("returns error for failure outcomes", () => {
    expect(getOutcomeStatus("manufacturing-failed")).toBe("error")
    expect(getOutcomeStatus("qc-rejection")).toBe("error")
  })

  it("returns success for shipping outcome", () => {
    expect(getOutcomeStatus("qc-shipping")).toBe("success")
  })

  it("returns info for progress outcomes", () => {
    expect(getOutcomeStatus("intake-completed")).toBe("info")
    expect(getOutcomeStatus("manufacturing-completed")).toBe("info")
  })

  it("returns info for unknown outcome types", () => {
    expect(getOutcomeStatus("some-future-stage")).toBe("info")
  })
})
