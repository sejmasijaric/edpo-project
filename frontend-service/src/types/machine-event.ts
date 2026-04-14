export interface MachineOrchestrationEvent {
  itemIdentifier: string
  outcomeType: string
}

export const OUTCOME_LABELS: Record<string, string> = {
  "intake-completed": "Intake Completed",
  "manufacturing-completed": "Manufacturing Completed",
  "manufacturing-failed": "Manufacturing Failed",
  "qc-shipping": "QC Approved for Shipping",
  "qc-rejection": "QC Rejected",
}

export function getOutcomeLabel(outcomeType: string): string {
  return OUTCOME_LABELS[outcomeType] ?? outcomeType
}

export type OutcomeStatus = "success" | "error" | "info"

export function getOutcomeStatus(outcomeType: string): OutcomeStatus {
  if (outcomeType === "manufacturing-failed" || outcomeType === "qc-rejection") {
    return "error"
  }
  if (outcomeType === "qc-shipping") {
    return "success"
  }
  return "info"
}
