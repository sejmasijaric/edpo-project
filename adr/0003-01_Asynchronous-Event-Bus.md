# ADR 0003-01: Event-Driven Backbone

**Date:** 17.03.2026
**Status:** Accepted

## Context
The system must integrate multiple software components with a physical smart factory that produces asynchronous sensor updates and long-running execution results. Components need to exchange commands and events without becoming tightly coupled to each other’s implementations or availability. The architecture therefore requires a durable and decoupled messaging backbone.

## Decision
Kafka will be used as the central event bus for asynchronous system communication. It will transport domain events and command-style messages between services and orchestration-related components. Kafka will not replace workflow state management, which remains the responsibility of Camunda. Raw machine protocols such as MQTT and REST will remain confined to the integration layer.

## Consequences

### Positive:
By using Kafka for asynchronous communication, we can keep services, integration, and orchestration loosely coupled, leading to improved fault isolation and independent service evolvability. Furthermore, Kafka provides a durable message model for this type of inter service communication which is both scaleable and resilient. Point-to-point knowledge among participating publishers and subscribers is no longer needed.

### Negative:
Kafka introduces additional messaging infrastructure which introduces additional implementation and maintenance overhead. Event design becomes a key consideration in the system design, with a careful distinction between commands and events needing to be made.
