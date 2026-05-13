# 0014-01 Latest Item Status Query

## Context

The factory event stream processor is a headless Kafka Streams process. It already normalizes raw factory telemetry into machine-specific events, but the frontend needs a point lookup that answers: given an `itemIdentifier`, what is the latest station/outcome/timestamp for that item?

## Decision

The `factory-event-streams-service` maintains a versioned Kafka Streams table named `factory-latest-by-item-v1-store`. The table is keyed by `itemIdentifier`, keeps the latest translated event by event-time, and falls back to processing-time when an event does not contain a parseable timestamp.

The table is also published to the compacted topic `factory.latest-status`, keyed by `itemIdentifier`. Compaction preserves the latest known status per item and keeps partitioning aligned with point lookups.

The headless stream processor does not expose HTTP. The existing frontend Spring backend consumes `factory.latest-status` from the beginning with a dedicated Kafka consumer group and keeps a local lookup store named `frontend-latest-status-by-item-v1-store`. It exposes:

`GET /api/orders/{itemIdentifier}/latest-status`

Unknown items return HTTP `404` with an error body. The API does not return synthetic `"unknown"` statuses because callers can distinguish missing data from a real latest event through normal HTTP semantics.

## Consequences

Reads are local key-value lookups in the query-capable backend and are suitable for expected frontend query volume. The compacted topic also allows additional query services to be added later without coupling them to the internal state directory of the headless stream processor.

Store names include `v1` so future incompatible value-shape or ordering changes can be introduced with a new store/topic migration path.
