# ADR 0004-01: Business Process Decomposition

**Date:** 17.03.2026
**Status:** Accepted

## Context
The system contains multiple workflows related to order handling, manufacturing execution, quality control, and dispatch within a smart factory environment (the MVP focuses on just the QC). These workflows interact with asynchronous machine events and services through the event backbone. Modeling all logic in a single BPMN process would create a large and difficult-to-maintain workflow model. At the same time, overly fragmenting workflows into many small processes would challenge coordination and reasoning about system behavior.

## Decision
Workflow models will be decomposed into multiple BPMN according to domain responsibilities and operational autonomy. Meanwhile, a top-level process will represent the overall business flow and coordinate major stages. Communication between processes may occur through Camundas native mechanisms or through the event backbone when crossing system boundaries.

### Guidelines:
- Top-level BPMN models represent business-level flows and major lifecycle stages.
- Domain-specific workflows may be separated when they represent distinct operational responsibilities or machine domains.

## Consequences

### Positive:
Separating BPMN models by domain responsibility improves readability, maintainability, and reuse of workflow logic. It allows high-level orchestration to remain simple while encapsulating technical control logic closer to the relevant system components. This approach also prevents workflow models from being tightly coupled to low-level machine interactions.

### Negative:
Splitting the workflow into multiple BPMN models introduces additional communication overhead and increases overall system complexity. Process traceability may be reduced, as the end-to-end workflow can span several process instances rather than being visible in a single model. Determining appropriate process granularity is challenging and can lead to over-segmentation.
