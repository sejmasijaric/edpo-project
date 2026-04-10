# ADR 0006-01: Separation of Concerns Between Main Orchestrator and Domain Specific Orchestrators

**Date:** 17.03.2026
**Status:** Accepted

## Context
The system controls a multi-stage factory automation process (intake, manufacturing, quality control) using a kafka-based event-driven architecture with Camunda 7 for workflow orchestration. Each factory domains is implemented as an independent service with its own workflows orchestration.

Currently, orchestration responsibilities are not strictly defined. This leads to risks of:
- Leakage of domain logic into global workflows
- Inconsistent process decomposition
- Tight coupling between services
- Reduced traceability and maintainability

The key drivers contributing to this decision are:
- End-to-End Workflow Traceability
- Temporal Decoupling
- Fault Tolerance & Recoverability
- Maintainability

**Decision Scope**
This decision applies to all workflow orchestration across services using Camunda BPMN engines and Kafka-based messaging.


## Decision
We implement the following ownership model:

### Main Order Orchestrator
- Owns cross-service business workflow
- Maintains global order state
- Coordinates domain interactions via events/commands
- Is decoupled from factory/machine specific logic and delegates coarse grained tasks to factory domain specific services 

### Domain Orchestrators
- Own domain-specific workflows and machine interactions
- Handle machine specific stages that may depend on the factory hardware
- Manage local state
- Are invoked by messages from the main orchestrator
- Report workflow outcomes to the main orchestrator


## Consequences

### Positive:
Clear separation of concerns
Global business progression is decoupled from domain-specific execution
Improved maintainability
Changes in domain workflows do not affect orchestration logic (and vice versa)
Alignment with DDD
Each service acts as a bounded context with full ownership of its process logic
Better observability
The orchestrator provides a clear, high-level view of order progression
Resilience
Failures can be handled locally where context is richest, reducing global complexity

### Negative:
Increased modeling effort
Requires designing and maintaining two layers of BPMN processes
Potential duplication of concepts
Some state may exist both locally and globally (at different abstraction levels)
Coordination overhead
Requires well-defined event contracts between orchestrator and services
Debugging complexity
Tracing issues may require navigating both orchestration and local processes






# ADR 0006-01: Separation of Concerns Between Main Orchestrator and Domain Specific Orchestrators

## Consequences

### Positive
This decision introduces a clear separation of concerns between global business workflow management and machine-specific execution. This significantly improves end-to-end workflow traceability. Maintainability is enhanced as domain logic remains encapsulated within bounded contexts, allowing individual services to evolve independently without impacting the global orchestration layer. Furthermore, the system benefits from localized failure handling, where domain orchestrators manage machine-specific issues in context.

### Negative
Decoupling factory-specific orchestration with from the main order orchestration leads to additional complexity to communicate factory state with the order orchestrator and limits the availability of detailed state information on the order orchestration level.