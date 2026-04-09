# ADR 0001-01: Orchestration Engine for Factory Workflow

**Date:** 17.03.2026
**Status:** Accepted

## Context
The smart factory executes long-running workflows involving manufacturing, item transport, quality control,
and sorting. These workflows interact with asynchronous sensor events (via MQTT),
REST-based machine commands, and human user tasks. Physical processes are inherently unreliable and asynchronous, requiring robust handling of
delays, failures, and retries.

The key drivers contributing to this decision are:
- End-to-end workflow traceability
- Temporal decoupling
- Fault tolerance and recoverability
- Data integrity

### Decision Scope
This decision involves all stateful services that manage workflows of the order management, product transportation, production, or quality control processes. 

## Options Considered
### Option 1: Central orchestration using Camunda (BPMN)
- Pros: Strong workflow state visibility; robust and persistent retry, timeout, error, and human task handling; explicit process modeling improves maintainability
- Cons: Central dependency; requires BPMN expertise; additional complexity

### Option 2: Event choreography
- Pros: High decoupling; no central orchestration dependency
- Cons: Poor visibility of workflow state; complicates debugging; failure handling and recovery logic is complex and completeness hard to enforce

## Decision
We use Camunda as the central workflow orchestration engine. BPMNs will represent long-running processes and coordinate major workflow steps across services. The engine will maintain process state, await external events, and coordinate workflow steps. 

## Consequences

### Positive:
This decision enforces strict end-to-end workflow visibility. The orchestration engines built-in retry mechanisms and persistent workflow state improve fault tolerance. Furthermore, orchestration is clearly separated from business logic, improving maintainability. 

### Negative:
The orchestration engine becomes a centralized dependency for process management. While the system can still remain loosely coupled through asynchronous communication and service boundaries, the core workflow is dependent on the orchestration engine being available. The orchestration engine adds additional deployment, monitoring, and scaling complexity.

### Stakeholder Impact
- Developers: clearer process logic but need BPMN expertise
- Operators: improved observability but additional system to manage
- Business stakeholders: better transparency into production workflows