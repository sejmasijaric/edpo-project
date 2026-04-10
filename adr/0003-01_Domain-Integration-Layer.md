# ADR 0003-01: Domain Integration Layer

**Date:** 17.03.2026
**Status:** Accepted

## Context
The Fischertechnik smart factory exposes actuators through a REST API and publishes real-time raw
sensor events through MQTT. Internally, our application uses Kafka as the asynchronous
messaging backbone of our event-driven application.
This ADR concerns the architectural boundary between machine-specific interfaces (MQTT/REST) and
the internal domain-oriented services. Without an explicit integration boundary, internal services and workflows would be coupled to machine-specific features and formats. This weakens maintainability, fault isolation, and complicate testing.

## Options Considered:
### Option 1: Raw Handling
Consume and process raw MQTT events directly in the respective services and send HTTP requests to the machines from the services directly. 
- Pros: minimal upfront translation logic; preserves all raw data.
- Cons: leaks hardware semantics into internal contracts; couples machine implementation details and message formats to services; duplicates parsing
and interpretation logic across services; makes testing and evolution harder.

### Option 2: Integration layer
Introduce a dedicated integration layer that translates machine protocols and raw sensor events
into domain-aligned commands and events before they enter the internal service landscape.
- Pros: isolates hardware-specific concerns; stabilizes Kafka contracts; improves maintainability,
testability, and semantic consistency.
- Cons: adds extra components; requires schema and mapping governance; may hide useful raw
detail unless explicitly preserved.

### Option 3: Hybrid Approach
Hybrid approach for MQTT and HTTP.
- Pros: reduces component complexity; less boilerplate code required for factory integration; unrestricted access to raw factory data
- Cons: blurs boundary between service and factory implementations; reduces maintainability due to inconsistent boundaries; 

## Decision
We will use a dedicated, lightweight domain integration layer between the factory interfaces
and the internal Kafka-based service implementations.
Raw MQTT sensor events and machine-specific REST interaction details must not be exposed directly to services. Instead, the integration layer will:
- consume sensor inputs
- consume machine control commands from the internal Kafka bus
- translate sensor event payloads into domain-aligned events and commands
- emit machine control through HTTP API
- keep protocol- and hardware-specific logic confined to the integration boundary

The integration layer must remain stateless with respect to business process execution. It may
perform translation, validation, enrichment with technical metadata, and protocol adaptation, but it
must not own domain workflow state or business decisions. Domain-specific factory operations remain
the responsibility of domain services and workflow orchestration.

## Consequences

### Positive:
- This decision keeps Kafka topics domain-aligned and prevents low-level hardware semantics from
leaking into service contracts and workflow logic.
- Domain services can evolve against stable business-level events instead of machine-specific payloads,
which improves maintainability, testability, and separation of concerns.
- Semantic translation is implemented once at the integration boundary instead of being duplicated
across services, which reduces inconsistency and supports clearer governance of event meaning.
- Keeping the integration layer stateless with respect to business execution supports temporal
decoupling, simpler recovery behavior, and clearer ownership of workflow state in the orchestration
and domain-service layers.
- Preserving correlation identifiers through the translation step supports end-to-end workflow
traceability across machine interaction, Kafka messaging, and BPMN processes.

### Negative:

- Some raw machine detail may be abstracted away from domain consumers. If future use cases require
  additional information, new domain events or enriched payloads will need to be introduced
  deliberately.
- Additional implementation and governance effort is required for event mapping, schema evolution,
validation rules, and observability.
- Troubleshooting may require visibility into both raw machine events and translated domain events,
which increases operational tooling needs for integration and support teams.




