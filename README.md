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