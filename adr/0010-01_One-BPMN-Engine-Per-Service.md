# ADR 0010-01: One BPMN Engine Per Service

**Date:** 10.04.2026
**Status:** Accepted

## Context
The system uses Camunda 7 BPMN orchestration in an event-driven architecture with independently deployable services and a Kafka-based messaging backbone. Existing decisions already establish that:
- BPMN is used to orchestrate long-running workflows with durable state and recoverability requirements. (ADR-0001)
- Workflow models are decomposed by domain responsibility instead of being modeled as one large end-to-end process. (ADR-0004)
- Domain-specific orchestrators own local process logic, while the main order orchestrator owns cross-service business progression. (ADR-0006)

This ADR concerns the topology for the deployment of these BPMNs. 

The key drivers contributing to this decision are:
- Alignment of workflow ownership with bounded contexts and service hexagons
- Independent deployability of services
- Fault isolation and operational resilience
- Maintainability and local evolution of workflows

### Decision Scope
This decision applies to all services that own BPMN workflows, including the main order-orchestrator and factory-specific services.




## Options Considered

### Option 1: Individual BPMN engine per service
Each service that owns workflow logic runs its own BPMN engine.

**Pros:**
- Alignment of orchestration engines with service boundaries and bounded contexts
- Workflow state is owned where the corresponding domain logic is implemented
- Services remain independently deployable
- Failures or maintenance on one engine are isolated to the owning service
- Domain-specific delegates can remain local to the service instead of creating tight coupling through a central engine

**Cons:**
- More BPMN engine instances must be configured, deployed, monitored, and consume resources
- End-to-end tracing spans multiple engines

### Option 2: One shared BPMN engine
All workflows run on a central BPMN engine. Services expose operations through adapters, and the shared engine coordinates each workflow by invoking service ports or through events.

**Pros:**
- Centralized operational view of BPMN engine
- Less duplicated engine infrastructure

**Cons:**
- Workflow ownership is separated from the services that own the business and machine logic
- A central engine becomes a cross-cutting dependency
- Delegates, message correlation, and technical adapters risk coupling the central engine to multiple service internals

## Decision
We use an individual orchestration engine per service that owns workflow logic.

Each service is responsible for its own BPMN models, delegates, and workflow state. The BPMN engine is treated as part of service implementation and sits within the service boundary rather than above multiple service hexagons.

## Consequences

### Positive:
- Workflow ownership remains consistent with bounded-contexts
- Services can change, deploy, test, and scale their workflows independently
- Local workflow failures are constrained to each service boundary
- Machine-specific orchestration stays close to the adapters and domain logic that understand the hardware context

### Negative:
- Operating multiple engines increases platform complexity
- End-to-end operational visibility requires cross-engine tracing through business keys and correlation identifiers
- Common configuration standards for engine setup, monitoring, incident handling, and naming conventions must be maintained deliberately across services