# edpo-project
Event-driven factory system for engraving, quality control, and automated sorting.

## Modules

- `factory-simulator`: UI-driven simulator for local testing
- `kafka-inspector-service`: lightweight Kafka topic listener, JSON publisher, and sorter-command emitter for diagnostics/manual testing
- `mqtt-kafka-bridge`: reusable integration library for translating MQTT events into Kafka events
- `sorter-integration-service`: integration layer for the sorter
- `vacuum-gripper-integration-service`: integration layer for the vacuum gripper
- `engraver-integration-service`: integration layer for the engraver
- `polishing-machine-integration-service`: integration layer for the polishing machine
- `workstation-transport-integration-service`: integration layer for the workstation transport

Start the production stack (requires physical factory):
```bash
docker compose --env-file docker/.env.production -f docker/docker-compose.yml up
```

Start the simulation stack (standalone):
```bash
docker compose \
  --env-file docker/.env.simulation \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.simulation.yml \
  up
```

## MQTT credentials

Docker Compose loads MQTT broker credentials for `sorter-integration-service`, `vacuum-gripper-integration-service`, `engraver-integration-service`, `polishing-machine-integration-service`, and `workstation-transport-integration-service` from `docker/.env.mqtt.local`.
This file is intended for local secrets and is excluded from Git.

The shared non-secret connection targets for each mode live in `docker/.env.production` and `docker/.env.simulation`.

To set up a new environment, copy `docker/.env.mqtt.local.example` to `docker/.env.mqtt.local` and fill in the MQTT username and password for the target broker.
