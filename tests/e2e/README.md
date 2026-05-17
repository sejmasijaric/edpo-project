# E2E tests

End-to-end test for the FT-Engrave factory stack. Drives a real order
through the full pipeline and verifies both QC outcomes.

## What it covers

| Path | Steps |
| --- | --- |
| **QC accept** | Create order → `POST /api/simulator/items` → wait for `Check Quality` → `POST /api/qc/check-quality/complete?passed=true` → assert item lands in a `SINK-S*` shipping sink and no QC tasks remain. |
| **QC reject** | Create order → insert → reject QC → wait for `Remove Item From Factory` → `DELETE /api/simulator/items/{id}` + `POST /api/manual-task/complete` → assert item gone and no order-orchestrator tasks remain. |
| **Dashboard** | Reads `/api/dashboard/metrics` and asserts the counters reflect at least the two items processed. |

Only the proxies exposed by `frontend-springboot-service` are exercised
end-to-end. The factory simulator and the Camunda REST APIs are queried
read-only for assertions.

## Local run

```bash
# Bring the stack up (see top-level README)
docker compose --env-file docker/.env.simulation \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.simulation.yml up -d

bash tests/e2e/run.sh
```

Override URLs / timeouts via env vars:

| Variable | Default |
| --- | --- |
| `FRONTEND_URL` | `http://localhost:3000` |
| `SIMULATOR_URL` | `http://localhost:8081` |
| `QC_ENGINE_URL` | `http://localhost:8100` |
| `ORDER_ENGINE_URL` | `http://localhost:8101` |
| `STACK_READY_TIMEOUT` | `180` (seconds) |
| `PIPELINE_TIMEOUT` | `180` (seconds spent waiting for `Check Quality`) |
| `SORTER_TIMEOUT` | `30` (seconds spent waiting for the sorter to route) |

Requires `bash`, `curl`, `python3`. No other deps.

## CI

`.github/workflows/e2e.yml` runs this script on every PR to `main`.
Service logs are uploaded as an artifact when the script fails.
