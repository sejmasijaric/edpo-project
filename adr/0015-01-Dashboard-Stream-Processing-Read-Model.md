# 0015-01 Dashboard Stream Processing Read Model

## Status

Accepted

## Context

The factory dashboard needs metrics derived from events on multiple Kafka topics, including stage orchestration, machine orchestration, user task management, and order creation. Some metrics require stateful processing, selected-window aggregation, and correlation between command and outcome events.

Putting this logic in the browser would duplicate metric semantics per client and would not provide replay, deterministic state rebuilding, or consistent handling of repeated attempts. Putting it directly into the existing frontend backend would couple UI delivery to Kafka Streams state management and make the frontend backend responsible for long-running event processing.

## Decision

Add `dashboard-service` as an independent Spring Boot and Kafka Streams service. The service consumes the existing orchestration topics, materializes local dashboard state stores, publishes normalized dashboard events to `dashboard.metrics`, and exposes a REST API for dashboard queries.

Average manufacturing time is computed with a Kafka Streams stream-stream join between `run-production-command` events from `stage-orchestration` and manufacturing terminal outcomes from `machine-orchestration`.

## Consequences

- Dashboard metrics are replayable from Kafka and have one server-side definition.
- The frontend and frontend backend can query a stable API instead of consuming raw Kafka topics.
- The service owns dashboard-specific state and can evolve independently from process-owning services.
- The current implementation scans local Kafka Streams stores for selected time windows, which is suitable for the current dashboard scope. A larger deployment may need windowed stores or an analytical database for high-volume historical queries.
- Manual intervention completion counts depend on completion events being emitted to `user-task-management`; current services primarily emit task-issued events.
