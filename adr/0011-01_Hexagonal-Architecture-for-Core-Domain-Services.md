# ADR 0011-01: Hexagonal Architecture for Core Domain Services

**Date:** 03.04.2026
**Status:** Accepted

## Context
The system is organized as independently deployable services in an event-driven architecture. Core domain services must coordinate long-running workflows, consume and publish Kafka messages, and interact with technical concerns such as BPMN delegates and machine-facing integration components.

Without a clear architectural boundary inside these services, workflow logic, domain rules, transport formats, and framework-specific code can become intertwined. If technical integration details leak into the core logic, these services become harder to test, harder to evolve, and more tightly coupled to messaging, BPMN, and infrastructure choices.

The key drivers contributing to this decision are:
- Isolation of domain logic from transport and framework concerns
- Testability of business rules, application use cases, and integration components
- Modularity of adapters for Kafka, BPMN, persistence, and external integrations
- Clear ownership of service boundaries in an event-driven system

### Decision Scope
This decision applies to core domain services, especially services that own factory-stage behavior or domain-specific orchestration. Lightweight protocol translation or infrastructure-only integration services may use simpler structures when they do not own domain logic or workflow decisions.

## Options Considered

### Option 1: Hexagonal architecture in core domain services
Each core domain service follows the hexagonal architecture with a domain core that does not have outwards-facing dependencies.Inbound and outbound ports connect application services to adapters which connect the logic to external dependencies, such as the orchestration engine or kafka. 

**Pros:**
- Keeps domain and application logic independent from infrastructure details
- Makes business use cases testable without bootstrapping Kafka, BPMN, or other technical runtimes
- Supports replacing or evolving adapters without changing domain or application logic
- Reduces the risk of protocol-specific details leaking into core domain

**Cons:**
- Introduces infrastructure overhead
- Increases implementation complexity

### Option 2: Direct layered or framework-centric service structure
Core services organize code primarily around framework entry points and technical components such as controllers, message listeners, delegates, repositories, and service classes, with domain logic invoked directly from these components.

**Pros:**
- Lower upfront structure and fewer abstractions
- Faster to implement for small services

**Cons:**
- Risk of coupling domain logic to external dependencies
- More complex to testability individual services
- Modifying transport or workflow infrastructure concerns may require application or domain specific changes
- Less explicit service boundaries risk leaking technical concerns into domain

## Decision
We use the hexagonal architecture for core domain services that represent factory stages or own domain-specific workflow behavior.

These services must structure their code so that:
- Domain and application logic remain inside the core service boundary
- Inbound interactions enter through defined input ports or application use cases
- Outbound dependencies are addressed through output ports
- Kafka consumers/producers, BPMN delegates, and other framework integrations are implemented as adapters

Hexagonal architecture is not required as a rigid template for every component in the repository. It is the required architecture for services that own business decisions, workflow progression, or factory-stage behavior. Simpler infrastructure-oriented services may use more lightweight architectures when they are intentionally stateless or limited to protocol translation.

## Consequences

### Positive:
- Core domain logic and application services are not tightly coupled to infrastructure such as Kafka or Orchestration Engines
- Testability is improved
- Service boundaries become clearer, which supports maintainability and more consistent ownership across the system

### Negative:
- More boilerplate is required for ports, adapters, and DTO-to-domain mapping
- Some small services may appear over-engineered if they do not actually contain meaningful domain behavior
- Introduction of additional implementation complexity