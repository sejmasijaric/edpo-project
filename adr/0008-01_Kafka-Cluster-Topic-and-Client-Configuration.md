# ADR 0008-01: Kafka Cluster, Topic, and Client Configuration

**Date:** 10.04.2026
**Status:** Accepted

## Context

ADR 0002 established Kafka as the domain-level messaging backbone. Since then, the implementation has evolved from a single-broker local setup toward a replicated three-broker cluster with explicit topic replication, selective partition scaling, and shared producer and consumer defaults.

The repository now contains enough concrete Kafka configuration that the effective baseline should be documented in one place. Without that baseline, it is difficult to reason about delivery guarantees, topic sizing, and the intended difference between orchestration topics, frontend order topics, machine command topics, and machine event topics.

## Decision

We standardize on the following Kafka baseline for the current system.

### Cluster topology

- Local and deployment compose configurations use a three-broker Kafka cluster: `kafka`, `kafka-2`, and `kafka-3`.
- Broker-level durability defaults are:
  - default replication factor `3`
  - offsets topic replication factor `3`
  - transaction state log replication factor `3`
  - minimum in-sync replicas `2`
  - transaction state log minimum ISR `2`

### Topic creation defaults

- Topic replication factor defaults to `3`.
- Machine command and event topics default to `1` partition because the order in which the events are processed is very important.
- Orchestration topics that can process multiple items in parallel are explicitly created with `3` partitions.
- Machine integrations use separate command and event topics. The integration services publish machine events to the configured `*-events` topic, while domain services publish machine commands to the corresponding `*-commands` topic.
  - These topics are splitted into commands and events due to the bidirectional communication between the domain services and their respective integration services.
### Current topic sizing

The current topic baseline is:

| Topic | Partitions | Replication factor | Purpose |
| --- | --- | --- | --- |
| `order-events` | `3` | `3` | Frontend-facing order update stream |
| `order-created` | `3` | `3` | Order intake events for the orchestrator |
| `stage-orchestration` | `3` | `3` | Stage-level workflow coordination |
| `machine-orchestration` | `3` | `3` | Machine command and outcome coordination |
| `user-task-management` | `1` | `3` | User-facing intake task coordination |
| `sorting-machine-commands` | `1` | `3` | Sorter commands, including color detection and sorting requests |
| `sorting-machine-events` | `1` | `3` | Sorter events bridged from MQTT |
| `vacuum-gripper-commands` | `1` | `3` | Vacuum gripper transport commands |
| `vacuum-gripper-events` | `1` | `3` | Vacuum gripper events bridged from MQTT |
| `engraver-commands` | `1` | `3` | Engraving commands |
| `engraver-events` | `1` | `3` | Engraver events bridged from MQTT |
| `workstation-transport-commands` | `1` | `3` | Workstation transport commands |
| `workstation-transport-events` | `1` | `3` | Workstation transport events bridged from MQTT |
| `polishing-machine-commands` | `1` | `3` | Polishing commands |
| `polishing-machine-events` | `1` | `3` | Polishing machine events bridged from MQTT |


## Consequences

### Positive

- The current baseline provides stronger durability than the earlier single-broker setup.
- Orchestration throughput scales across independent items while preserving per-item ordering assumptions.
- Producer and consumer reliability settings are consistent across services through shared defaults.
- Machine integration topics remain easy to reason about while the hardware model still centers on single machine instances.

### Negative

- Kafka configuration is now an explicit architectural concern rather than invisible infrastructure plumbing.
- Existing local Kafka volumes may need to be recreated when topic partition counts or replication assumptions change.
- Some services may create idle consumer threads when they subscribe to both three-partition and single-partition topics.
- Topic and client overrides remain flexible, so deployments can still drift from this baseline if changes are not documented.
