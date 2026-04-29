# ADR 11: Refactor MQTT-Kafka Bridge into Raw Event Ingestion and Kafka Streams Processing

## Status

Proposed

Date: 29.04.2026

## Context

FT-Engrave currently uses a shared MQTT-Kafka bridge in the machine integration layer. The bridge subscribes to MQTT topics emitted by the factory, filters low-level machine telemetry inside the respective integration services, translates relevant signals into business events, and publishes those events to dedicated Kafka topics.

This design successfully decouples the physical factory from the workflow layer. However, most event filtering, routing, and translation logic currently happens in the integration services before events enter Kafka. This means that the system doesn’t leverage Kafka Streams for processing the events.

## Decision

We will refactor the MQTT-Kafka bridge so that it no longer performs business-level filtering and translation.

The bridge will become a raw ingestion adapter with the following responsibilities:

- subscribe to configured MQTT topics,
- perform minimal technical filtering, such as duplicate removal,
- normalize timestamps and metadata,
- wrap incoming MQTT messages in a common event envelope,
- publish all normalized raw machine telemetry to a shared Kafka topic named `raw-machine-events`.

Business-level processing will be moved to a Kafka Streams. This Kafka Streams application will consume from `raw-machine-events` and apply the following topology:

1. Content Filter  
   Reduce raw machine telemetry to the fields required for further processing.

2. Event Router  
   Route events based on machine type, station, or MQTT topic metadata.

3. Event Translator  
   Convert raw machine events into domain-aligned business events.

The final translated events will be published to dedicated machine event topics:

- `vacuum-gripper-events`
- `engraver-events`
- `workstation-transport-events`
- `polishing-machine-events`
- `sorting-machine-events`

The domain orchestration services will continue consuming these dedicated business event topics. Therefore, the workflow layer remains decoupled from raw factory telemetry.