# ADR 0002-01: Domain Integration Layer

**Date:** 17.03.2026
**Status:** Accepted

## Context
The Fischertechnik smart factory produces raw sensor events which are published through MQTT. Our application uses Kafka for event processing. The implementation should separate low-level hardware integration from domain-specific workflow logic.

## Decision
All Kafka topics should remain free from low-level hardware events specific to the factory’s sensors and actuators. Instead, only domain events should be published to Kafka. The system will achieve this by incorporating a lightweight integration layer which translates raw MQTT sensor events to domain-aligned Kafka events. While this layer will contain hardware-specific logic required to translate hardware-specific with domain-aligned events, it should not be responsible for domain-specific factory operations and not contain any internal states.

## Consequences

### Positive:
This decision keeps the Kafka topics clean and domain-aligned. Low-level hardware noise remains isolated to the layers immediately connected to the factory. The stateless property of the integration layer guarantees that the non-hardware-specific layers keep full control over the factory.

### Negative:
Additional abstraction over sensor data may be more restrictive regarding what data is available to business services. Future requirements may require adding new domain events to the factory integration layer.
