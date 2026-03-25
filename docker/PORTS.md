# Port Mapping

This file documents the ports currently used or reserved by services in this repository.

## Active assignments

| Service | Scope | Host port(s) | Container/Application port(s) | Notes |
| --- | --- | --- | --- | --- |
| `demo-service` | Docker Compose | `8080` | `8080` | Camunda demo service |
| `factory-simulator` | Local application | `8081` | `8081` | Configured in `factory-simulator/src/main/resources/application.yaml` |
| `qc-service` | Docker Compose | `8100` | `8100` | Embedded Camunda engine for quality-control BPMN models |
| `sorter-integration-setrvice` | Docker Compose | None | None | Internal Kafka/HTTP integration service with no exposed API port |
| `kafka` | Docker Compose | `9092`, `29092` | `9092`, `29092` | Broker ports exposed by the compose stack |

## Reserved contiguous block

The host port range `8100-8109` is intentionally reserved for service applications so additional services can be added without reshuffling ports later.

| Host port | Assignment |
| --- | --- |
| `8100` | `qc-service` |
| `8101` | Reserved for future service |
| `8102` | Reserved for future service |
| `8103` | Reserved for future service |
| `8104` | Reserved for future service |
| `8105` | Reserved for future service |
| `8106` | Reserved for future service |
| `8107` | Reserved for future service |
| `8108` | Reserved for future service |
| `8109` | Reserved for future service |
