# Port Mapping

This file documents the ports currently used or reserved by services in this repository.

## Active assignments

| Service | Scope | Host port(s) | Container/Application port(s) | Notes |
| --- | --- | --- | --- | --- |
| `factory-simulator` | Docker Compose simulation override | `8081` | `8081` | Included only with `docker/docker-compose.simulation.yml` |
| `qc-service` | Docker Compose | `8100` | `8100` | Embedded Camunda engine for quality-control BPMN models |
| `order-orchestrator` | Docker Compose | `8101` | `8101` | Embedded Camunda engine for global order orchestration BPMN models |
| `kafka-inspector-service` | Docker Compose | `8102` | `8102` | Lightweight Kafka listener and JSON publisher endpoint |
| `intake-service` | Docker Compose | `8103` | `8103` | Embedded Camunda engine for intake BPMN models |
| `manufacturing-service` | Docker Compose | `8104` | `8104` | Embedded Camunda engine for manufacturing BPMN models |
| `sorter-integration-service` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `vacuum-gripper-integration-service` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `engraver-integration-service` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `polishing-machine-integration-service` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `workstation-transport-integration-service` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `kafka` | Docker Compose | `9092`, `29092` | `9092`, `29092` | Broker ports exposed by the compose stack |
| `frontend-springboot-service` | Docker Compose | `8082` | `8081` | Spring Boot backend for the frontend application |
| `frontend-service` | Docker Compose | `3000` | `80` | Static frontend served by the compose stack |
| `mqtt-broker` | Docker Compose simulation override | `1883` | `1883` | Included only with `docker/docker-compose.simulation.yml` |

## Reserved contiguous block

The host port range `8100-8109` is intentionally reserved for service applications so additional services can be added without reshuffling ports later.

| Host port | Assignment |
| --- | --- |
| `8100` | `qc-service` |
| `8101` | `order-orchestrator` |
| `8102` | `kafka-inspector-service` |
| `8103` | `intake-service` |
| `8104` | `manufacturing-service` |
| `8105` | Reserved for future service |
| `8106` | Reserved for future service |
| `8107` | Reserved for future service |
| `8108` | Reserved for future service |
| `8109` | Reserved for future service |
