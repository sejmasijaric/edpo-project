# ADR 0011-01: MQTT-Kafka Bridge for Machine Event Ingestion

**Date:** 10.04.2026
**Status:** Accepted

## Context

The physical factory boundary exposes machine state and execution results over MQTT topics. The rest of the application landscape uses Kafka for domain-level coordination, workflow progress, and service-to-service messaging as defined in ADR 0002.

If domain services consumed MQTT directly, machine protocol concerns would leak into each service, Kafka replayability would be bypassed, and every machine integration would need to duplicate the same subscription and forwarding infrastructure. We therefore need a reusable way to ingest MQTT-originated machine events into Kafka while allowing each integration service to keep control over its own event interpretation.

## Decision

We use a shared Maven module named `mqtt-kafka-bridge` as the standard bridge from machine-level MQTT topics into Kafka topics.

The bridge is embedded into the machine integration services rather than deployed as a standalone central service.

### Bridge responsibilities

- Open an MQTT subscription to exactly one configured MQTT topic per integration service.
- Reconnect automatically to the MQTT broker.
- Optionally authenticate with MQTT username and password.
- Pass each raw MQTT payload through a service-local `MqttEventFilter`.
- Publish only accepted and transformed events to Kafka.
- Publish to the configured Kafka bridge target topic, defaulting to the service's machine topic.

### Current usage

The bridge is currently consumed by:

- `sorter-integration-service`
- `vacuum-gripper-integration-service`
- `engraver-integration-service`
- `polishing-machine-integration-service`
- `workstation-transport-integration-service`

Each of these services:

- includes the shared `mqtt-kafka-bridge` dependency
- exposes its own `MqttEventFilter` implementation
- enables bridge scanning through `org.unisg.mqttkafkabridge`
- binds one MQTT topic to one Kafka target topic through application properties

### Configuration model

The bridge is controlled with the following properties:

- `mqtt.bridge.enabled`, default `true`
- `mqtt.broker.url`
- `mqtt.topic`
- `mqtt.client-id`
- `mqtt.username`
- `mqtt.password`
- `kafka.bootstrap-servers`
- `kafka.topic.bridge-target`

In the current stack, the MQTT to Kafka mapping is:

| Integration service | MQTT topic | Kafka topic |
| --- | --- | --- |
| `sorter-integration-service` | `FTFactory/SM_1` | `sorting-machine` |
| `vacuum-gripper-integration-service` | `FTFactory/VGR_1` | `vacuum-gripper` |
| `engraver-integration-service` | `FTFactory/OV_1` | `engraver` |
| `polishing-machine-integration-service` | `FTFactory/MM_1` | `polishing-machine` |
| `workstation-transport-integration-service` | `FTFactory/WT_1` | `workstation-transport` |

## Consequences

### Positive

- Machine protocol handling stays localized at the integration boundary instead of spreading across orchestration services.
- Kafka remains the single durable and replayable domain backbone after ingestion.
- New machine integrations can reuse the same subscription and publishing infrastructure with only a service-local filter and topic mapping.
- The bridge can be disabled in tests or deployments that do not require MQTT ingestion.

### Negative

- The bridge is not a fully generic standalone product; each integration service still owns its event filter and payload mapping.
- MQTT messages are forwarded only after successful filter acceptance, so bad filters can silently suppress expected events.
- The bridge currently handles MQTT to Kafka only; command traffic in the opposite direction still belongs to the integration services' own protocol adapters.
