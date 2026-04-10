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
This decision introduces a clear separation of concerns between global business workflow management and machine-specific execution. This improves end-to-end workflow traceability. Maintainability is enhanced as domain logic remains encapsulated within bounded contexts, allowing individual services to evolve independently. Furthermore, the system benefits from localized failure handling, where domain orchestrators manage machine-specific issues in context.

### Negative:
Decoupling factory-specific orchestration with from the main order orchestration leads to additional complexity to communicate factory state with the order orchestrator and limits the availability of detailed state information on the order orchestration level.