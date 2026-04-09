# ADR 0002-01: Use Kafka as Central Asynchronous Messaging Backbone

**Date:** 17.03.2026
**Status:** Accepted

## Context
The system controls a fischertechnik smart factory with stations for item-transportation, engraving, polishing, quality control, and sorting. The physical environment produces asynchronous sensor updates and long-running execution results. The system consists of multiple services in an event-driven architecture which internally use Camunda engines for workflow orchestration. We need to realize asynchronous communication between services, interfaces, and hardware components in a way that supports long-running workflows and unreliable physical execution without creating tight runtime coupling between participants.

The key drivers contributing to this decision are:
- temporal decoupling between producers and consumers
- end-to-end workflow traceability across long-running processes
- fault tolerance and recoverability under partial physical failure
- reliability and data integrity for inter-service communication

### Decision Scope
This decision concerns the transport of domain events and commands between system components related to orchestration, hardware control, event processing, user interfaces, and data processing. This decision does not concern immediate communication on the hardware level. 

## Options Considered
### Option 1: Synchronous inter service communication
- Pros: Simplicity; lightweight messaging infrastructure; simple debugging
- Cons: Strong temporal service coupling, challenges asynchronous machine behavior and long-running workflows; limited recoverability

### Option 2: Asynchronous MQTT based messaging backbone
- Pros: Re-usable infrastructure which is present at machine boundary; reduced temporal decoupling through asynchronous communication
- Cons: Weak retention, replay, and traceability support; re-use of infrastructure risks mixing machine/protocol concerns with domain

### Option 3: Asynchronous Kafka based messaging backbone
- Pros: Strong temporal decoupling with replay and recovery mechanisms; durable and traceable; scalable; separate from machine protocols
- Cons: Additional infrastructure complexity; requires topic and schema design; event ordering must be handled


## Decision
Kafka will be used as the central event bus for asynchronous domain-level communication between services. Kafka will transport both domain events and commands. Messages must carry stable correlation identifiers so that workflow execution and physical process state can be traced end-to-end across services and BPMN processes. Kafka does not replace workflow state management or orchestration logic. Camunda remains responsible for workflow execution state and BPMN coordination semantics. 

## Consequences

### Positive:
By using Kafka as the central messaging backbone, the architecture gains temporal decoupling between producers and consumers, which improves resilience to component unavailability and partial failure. Services and orchestration-related components can evolve more independently because they no longer require direct point-to-point runtime knowledge of each other.
Kafka's durable retention model supports end-to-end workflow traceability, replay, and recovery, which is especially valuable in a physical factory environment with unreliable sensor input, asynchronous machine behavior, and long-running executions. 

The decision also reinforces architectural separation of concerns: MQTT and REST stay machine level protocols, while Kafka becomes the domain-level backbone for inter-service coordination. This reduces protocol leakage from the physical environment into domain services.


### Negative:
Kafka introduces additional infrastructure that must be operated, monitored, and maintained. Development teams must design events, topics, and clear semantics for commands versus events. Consumers must be designed for idempotency and failure handling, and the architecture must define retry and poison-message strategies.
Testing and debugging become more complex because relevant behavior is distributed across asynchronous producers, consumers, and workflow engines. Kafka alone does not provide workflow state management or business process coordination. Teams must avoid shifting orchestration logic into the event backbone and must keep the boundary to Camunda explicit.