# ADR 0001-01: Orchestration Engine for Factory Workflow

**Date:** 17.03.2026
**Status:** Accepted

## Context
Running QC (and later ordering and production) using the smart factory creates long-running workflows which interact with asynchronous sensor events and user tasks. Meanwhile, the system should remain understandable not only regarding implementation, but also explainability of workflow state and failure diagnosis. The architecture should support clear visibility into process progress, waiting states, user tasks, and error handling.

## Decision
We use Camunda as the central workflow orchestration engine. BPMNs will represent long-running processes and coordinate major workflow steps across services. The engine will maintain process state and await for external events. 

## Consequences

### Positive:
This decision enforces strict visibility and state management of the workflows through the orchestration engine. The engine expresses a clearly visible and easily modifiable business process which is not tightly coupled to service implementations. Finally, we can leverage Camundas built-in retry, error handling, and timeout systems. 

### Negative:
The orchestration engine becomes a centralized dependency for process management. While the system can still remain loosely coupled through asynchronous communication and service boundaries, the core workflow is dependent on the orchestration engine being available.
