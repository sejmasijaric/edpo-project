# ADR 0010-01: Kafka Cluster, Topic, and Client Configuration

**Date:** 10.04.2026
**Status:** Accepted

## Context

ADR 0002 established Kafka as the domain-level messaging backbone. Since then, the implementation has evolved from a single-broker local setup toward a replicated three-broker cluster with explicit topic replication, selective partition scaling, and shared producer and consumer defaults.

The repository now contains enough concrete Kafka configuration that the effective baseline should be documented in one place. Without that baseline, it is difficult to reason about delivery guarantees, topic sizing, and the intended difference between orchestration topics and machine-specific topics.

## Decision

We standardize on the following Kafka baseline for the current system.

### Cluster topology

- Local and deployment compose configurations use a three-broker Kafka cluster: `kafka`, `kafka-2`, and `kafka-3`.
- Services connect through the shared bootstrap address `kafka:9092,kafka-2:9092,kafka-3:9092` inside Docker, with host access exposed on `localhost:29092`, `localhost:29094`, and `localhost:29096`.
- Broker-level durability defaults are:
  - default replication factor `3`
  - offsets topic replication factor `3`
  - transaction state log replication factor `3`
  - minimum in-sync replicas `2`
  - transaction state log minimum ISR `2`

### Topic creation defaults

- Application-managed topics remain enabled by default through `kafka.topic.auto-create=true`.
- Topic replication factor defaults to `3`.
- Machine-specific topics default to `1` partition unless overridden.
- Orchestration topics that process multiple items in parallel are explicitly created with `3` partitions.

### Current topic sizing

The current topic baseline is:

| Topic | Partitions | Replication factor | Purpose |
| --- | --- | --- | --- |
| `order-created` | `3` | `3` | Order intake events for the orchestrator |
| `stage-orchestration` | `3` | `3` | Stage-level workflow coordination |
| `machine-orchestration` | `3` | `3` | Machine command and outcome coordination |
| `user-task-management` | `1` | `3` | User-facing intake task coordination |
| `sorting-machine` | `1` | `3` | Sorter integration events and commands |
| `vacuum-gripper` | `1` | `3` | Vacuum gripper integration events and commands |
| `engraver` | `1` | `3` | Engraver integration events and commands |
| `workstation-transport` | `1` | `3` | Workstation transport integration events and commands |
| `polishing-machine` | `1` | `3` | Polishing machine integration events and commands |

### Configuration override rule

- All broker addresses, topic names, partition counts, replication factors, and client tuning parameters remain overrideable through environment variables or application properties.
- The values in this ADR describe the current baseline expected by the repository and Docker Compose stack, not an irreversible hard-coded limit.

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
