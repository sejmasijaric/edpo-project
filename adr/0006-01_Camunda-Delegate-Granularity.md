# ADR 0001-01: Camunda Delegate Granularity

**Date:** 25.03.2026
**Status:** Accepted

## Context
The connection between tasks in the BPMN engine and services is realized using JavaDelegates. These delegates can be mapped one-to-one with BPMN tasks or could use variables to allow one delegate to distribute to multiple service methods from multiple tasks calling the same delegate. Further, hybrid approaches are possible where tasks with different parameters can use a many-to-one mapping from task to delegate while other tasks use one-to-one. 

## Decision
We decide to use a strict one-to-one mapping for all delegates. We do not allow selecting implementation branches from the BPMN using variables.

## Consequences

### Positive:
This decision enforces strict existential guarantees for delegates. If implementation or BPMN incorrectly reflect each other, the application will fail fast through existential test cases. With variable-based branches, incorrect mappings may only appear at runtime. Responsibilities are clear and the code cleanly reflects the business process. Finally, simple delegates reduce the number of failure points between orchestration engine and service implementation.

### Negative:
As each delegate is implemented in its own java class, large services may be bloated with many small delegates making the codebase harder to navigate. Processes with similar tasks (such as sorting to different sinks) will require near duplicate delegates with different configurations causing additional boilerplate. 