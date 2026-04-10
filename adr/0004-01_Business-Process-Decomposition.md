# ADR 0004-01: Business Process Decomposition

**Date:** 17.03.2026
**Status:** Accepted

## Context
The system orchestrates workflows for order handling, manufacturing,
and quality control in a smart factory. Workflows interact with asynchronous machine-reliant events, user tasks, and other services via an event-driven Kafka backbone, with orchestration implemented using Camunda 7 BPMN engines deployed per service. Workflows can be long-running and cross multiple integration boundaries regarding factory hardware (different machines) and control infrastructure (user and factory worker interactions). This decision concerns the structural separation of BPMN workflows across services and orchestration layers.

The key drivers contributing to this decision are:
- Maintainability regarding workflow complexity
- End-to-end process traceability
- Reliability and recoverability

## Options Considered

### Option 1: One single BPMN per business workflow
**Pros:**
- Full end-to-end visibility in one model
- Simplified traceability

**Cons:**
- Poor maintainability due to model size
- No separation of order workflow and machine-level process handling

### Option 2: Distributed BPMN (per service/domain only)
**Pros:**
- High modularity and separation of concerns
- Improved maintainability
- Aligns with service boundaries
- Possibility to separate workflows driven by ordering domain from those driven by machine (hardware) constraints 

**Cons:**
- Loss of end-to-end visibility
- Additional event-driven workflow coordination
- More challenging to debug and trace

### Option 3: Hybrid decomposition
Combine a top-level orchestration BPMN with domain-specific BPMN processes.

**Pros:**
- Balances maintainability and traceability
- Preserves high-level visibility
- Enables modular domain workflows
- Possibility to separate workflows driven by ordering domain from those driven by machine (hardware) constraints

**Cons:**
- Increased system complexity due to coordination overhead
- Weaker end-to-end traceability
- More challenging to debug

## Decision
Workflow models will be decomposed into multiple BPMN processes aligned with
domain responsibilities and operational autonomy, while maintaining a top-level
BPMN process for business-level orchestration.

### Communication between processes:
- Within services: Camunda-native mechanisms
- Across services: Kafka-based event backbone

### Guidelines:
- Top-level BPMN models represent business lifecycle stages
- Domain workflows encapsulate machine-specific or service-specific logic
- Stable identifiers (business keys) must be used for correlation

## Consequences

### Positive:
Separating BPMN models by domain responsibility improves readability, and maintainability. It allows high-level orchestration to remain simple while encapsulating technical control logic closer to the relevant system components. This approach also prevents workflow models from being tightly coupled to low-level machine interactions.

### Negative:
Splitting the workflow into multiple BPMN models introduces additional communication overhead and increases overall system complexity. Process traceability is weakened, as the end-to-end workflow can span several process instances rather than being visible in a single model. Determining appropriate process granularity is challenging and can lead to over-segmentation.