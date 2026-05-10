# ADR 0013-01: Standalone MQTT-Kafka Bridge Service

**Date:** 10.05.2026
**Status:** Accepted

## Context

ADR 0012 moved business-level factory telemetry filtering and translation into
`factory-event-streams-service`, but the raw MQTT ingestion bridge still ran inside each machine
integration service. This left MQTT broker access, MQTT credentials, topic subscriptions, and raw
Kafka publishing coupled to services that otherwise handle machine commands and integration with
machine control APIs.

## Decision

We will deploy `mqtt-kafka-bridge` as its own independent service.

The standalone bridge service:

- subscribes to the configured MQTT topic pattern, defaulting to `FTFactory/#`,
- publishes each incoming MQTT payload unchanged to `factory.raw-events`,
- uses the MQTT source topic as the Kafka record key,
- owns MQTT broker configuration and credentials.

Machine integration services no longer embed the bridge library, no longer scan
`org.unisg.mqttkafkabridge`, and no longer carry MQTT bridge runtime configuration. They remain
responsible for consuming machine command topics and invoking machine control APIs.

`factory-event-streams-service` remains responsible for consuming `factory.raw-events`, filtering,
routing, translating, and publishing business-level machine events.

## Consequences

### Positive

- Low-level MQTT ingestion is isolated from machine command integration and workflow-facing code.
- MQTT credentials and subscription settings are configured in one deployable service.
- Machine integration service builds no longer need the bridge module.
- Kafka Streams scaling is independent from MQTT subscription ownership.

### Negative

- The compose stack gains one additional internal service.
- The bridge service becomes a shared ingestion point, so its availability affects all raw factory
  telemetry ingestion.
- Topic subscription breadth must be managed carefully to avoid ingesting unrelated MQTT traffic.
