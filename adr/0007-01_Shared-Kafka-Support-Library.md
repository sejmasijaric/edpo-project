# 0007-01 Shared Kafka Support Library
**Date:** 03.04.2026
**Status:** Accepted

## Context

Multiple services in the solution implement the same Spring Kafka infrastructure repeatedly: producer factory setup, consumer factory setup, listener container setup, and transaction-aware publishing helpers. The duplication currently exists in `order-orchestrator`, `qc-service`, and `sorter-integration-service`.

## Decision

Introduce a small shared Maven module named `shared-kafka-support` that contains only reusable Kafka infrastructure:

- typed Spring Kafka producer configuration support
- typed Spring Kafka consumer configuration support
- a transaction-aware publisher helper used by service-local publisher adapters

Service-specific topics, DTOs, listeners, and workflow logic remain inside their owning services. The MQTT bridge keeps its existing Kafka publishing implementation unchanged.

## Consequences

The services keep their current Kafka behavior while reusing a single implementation for common Kafka infrastructure. Docker builds must install the shared library before packaging dependent services because the repository does not use a root Maven reactor build.
