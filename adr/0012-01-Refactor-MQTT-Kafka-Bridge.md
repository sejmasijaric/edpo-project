# ADR 11: Refactor MQTT-Kafka Bridge into Raw Event Ingestion and Kafka Streams Processing

## Status

Accepted, revised by ADR 0013

Date: 29.04.2026

## Context

FT-Engrave currently uses a shared MQTT-Kafka bridge in the machine integration layer. The bridge subscribes to MQTT topics emitted by the factory, filters low-level machine telemetry inside the respective integration services, translates relevant signals into business events, and publishes those events to dedicated Kafka topics.

This design successfully decouples the physical factory from the workflow layer. However, most event filtering, routing, and translation logic currently happens in the integration services before events enter Kafka. This means that the system doesn’t leverage Kafka Streams for processing the events.

## Decision

We will refactor the MQTT-Kafka bridge so that it no longer performs business-level filtering and translation.

The bridge will become a raw ingestion adapter with the following responsibilities:

- subscribe to configured MQTT topics,
- publish incoming MQTT payloads unchanged to a shared Kafka topic named `factory.raw-events`.

Business-level processing will be moved to Kafka Streams in a dedicated
`factory-event-streams-service`. This Kafka Streams application consumes from
`factory.raw-events` and applies the following topology in memory:

1. Timestamp handling and raw event normalization  
   Use the event timestamp from the raw payload where possible.

2. Splitter  
   Split raw machine telemetry into sensor-level events.

3. Duplicate filter  
   Remove consecutive duplicate sensor-level events by station, sensor name, and value.

4. Event Router  
   Route events through a shared translator registry based on machine type, station, or MQTT topic
   metadata.

5. Event Translator  
   Convert station-specific sensor events into the existing machine business event contracts.

The final translated events are published to the existing dedicated machine event topics:

- `sorting-machine-events`
- `vacuum-gripper-events`
- `engraver-events`
- `polishing-machine-events`
- `workstation-transport-events`

Existing downstream services continue consuming their previous machine event topics. This ADR
originally kept raw MQTT adapters embedded in the machine integration services. ADR 0013 revises
that deployment choice: `mqtt-kafka-bridge` now runs as a standalone raw ingestion service that
forwards factory payloads to `factory.raw-events`. Legacy service-local MQTT filters have been
removed; station translation is handled only by `factory-event-streams-service`.
