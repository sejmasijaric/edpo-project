# edpo-project
Event-driven factory system for engraving, quality control, and automated sorting.

## Modules

- `demo-service`: Camunda-based orchestration demo service
- `factory-simulator`: UI-driven simulator for local testing
- `mqtt-kafka-bridge`: reusable integration library for translating MQTT events into Kafka events

Start:
```bash
docker compose -f docker/docker-compose.yml up
```

## MQTT credentials

Docker Compose loads MQTT broker credentials for `sorter-integration-service` from `docker/.env.mqtt.local`.
This file is intended for local secrets and is excluded from Git.

To set up a new environment, copy `docker/.env.mqtt.local.example` to `docker/.env.mqtt.local` and fill in the broker URL, username, and password for the target MQTT broker.
