# Dashboard Service

`dashboard-service` is the dashboard read-model service for factory operational metrics. It is a separate Spring Boot and Kafka Streams application rather than part of the frontend backend because the metrics need replayable, stateful processing over Kafka topics.

## Inputs

The service consumes these existing topics:

| Topic | Purpose |
| --- | --- |
| `stage-orchestration` | Stage commands such as `run-item-intake-command`, `run-production-command`, and `run-item-qc-command` |
| `machine-orchestration` | Outcomes such as `manufacturing-completed`, `manufacturing-failed`, `qc-shipping`, and `qc-rejection` |
| `user-task-management` | Manual intervention task events |
| `order-created` | Optional end-to-end production start events |

It publishes normalized dashboard events to `dashboard.metrics` and materializes local Kafka Streams state stores for API queries.

## Stream Processing Pattern

The service uses Kafka Streams for normalization, state materialization, and aggregate source events.

Average manufacturing time is implemented with a stream-stream join:

```text
stage-orchestration run-production-command
  join machine-orchestration manufacturing-completed/manufacturing-failed
  by itemIdentifier within DASHBOARD_MANUFACTURING_JOIN_WINDOW
  -> MANUFACTURING_ATTEMPT_DURATION dashboard event
```

The join preserves completed and failed attempts and stores their duration for selected time-window queries.

## Metrics

`GET /api/dashboard/metrics?from=<ISO instant>&to=<ISO instant>` returns:

- QC rejected rate: rejected count, passed count, total, and percentage.
- Average manufacturing time: count and average duration in milliseconds.
- Manual interventions: open count, completed count in the selected window, and open task details.
- Manufacturing failure rate: completed count, failed count, total, percentage, and failed item count.
- Average end-to-end production time: count, average, minimum, and maximum duration in milliseconds.
- Work in progress: current non-terminal item counts by `INTAKE`, `MANUFACTURING`, `QC`, and `MANUAL_INTERVENTION`.
- Retry rate: per-item retry counts plus average retries per completed item.

If `from` or `to` is omitted, the API defaults to the last hour.

## Deployment

The service is wired into `docker/docker-compose.yml` and exposes port `8105`.

```bash
docker compose -f docker/docker-compose.yml up --build dashboard-service
```

Relevant environment variables:

| Variable | Default |
| --- | --- |
| `KAFKA_BOOTSTRAP_ADDRESS` | `kafka:9092,kafka-2:9092,kafka-3:9092` in Docker |
| `KAFKA_TOPIC_STAGE_ORCHESTRATION` | `stage-orchestration` |
| `KAFKA_TOPIC_MACHINE_ORCHESTRATION` | `machine-orchestration` |
| `KAFKA_TOPIC_USER_TASK_MANAGEMENT` | `user-task-management` |
| `KAFKA_TOPIC_ORDER_CREATED` | `order-created` |
| `KAFKA_TOPIC_DASHBOARD_METRICS` | `dashboard.metrics` |
| `DASHBOARD_MANUFACTURING_JOIN_WINDOW` | `12h` |

## Notes

Manual intervention completion depends on an event with a task completion status on `user-task-management`. Current services mostly emit task-issued events, so open intervention counts are reliable from current events; completed intervention counts will become more useful once completion events are emitted.

Retry rate is derived from repeated command attempts seen by this service, primarily stage orchestration commands. Additional machine command or diagnostic topics can be added later without changing the API contract.
