# EDPO – FT-Engrave: AirTag Engraving Factory

FT Engrave is an event-driven factory control system for engraving, polishing, and quality control of customized AirTags. Customers order an AirTag with color and engraving text of their choice. The factory refines a pre-colored AirTag by engraving the user-defined text, polishing it, and running it through an automated and a human operated quality control stage. The products are sorted based on the quality control outcome. The system is designed to handle machine and service failures gracefully, escalate cases where the item needs to be moved manually, or automatically trigger a re-run of the production of the product is damaged or fails quality control.

![Figure 1: Top down view of the factory](img/Factory.jpeg)

## Production Flow

The production flow, workflow stages and machines are visualized in Figure 2

![Figure 2: Diagram of machines, workflow stages, and flow through the factory](img/ManufacturingProcess.jpg)

The factory consists of five machines:
- 1.1 Vacuum Gripper (VGR)
- 2.1 Engraver (EGR)
- 2.2 Workstation Transport (WT)
- 2.3 Polishing Machine (PM)
- 3.1 Sorting Machine (SM)

The production is divided into three stages, each involving different machines:
1. **Intake** is responsible for taking a raw colored airtag and placing it in the production machinery. This stage involves the vacuum gripper. 
2. **Manufacturing** is responsible for fabricating the engraved AirTag. This stage involves the engraver and polishing machine to perform the manufacturing stages and the workstation transport to move the item between stations. 
3. **Quality Control** is responsible for performing a two-stage quality control process. First, the item runs through an automated quality control. If it passes, the item is then passed to the human quality control for final inspection. Based on the result, the items are sorted. This all takes place on the sorting machine.

## Deployment

### Physical Factory

Production deployment uses [`docker/docker-compose.yml`](docker/docker-compose.yml) with [`docker/.env.production`](docker/.env.production). This points the integration services at the physical factory IPs and the production MQTT broker.

Before starting the stack, create the local MQTT secrets file:

```bash
cp docker/.env.mqtt.local.example docker/.env.mqtt.local
```

Edit `docker/.env.mqtt.local` with the broker credentials for the target factory:

```env
MQTT_USERNAME=...
MQTT_PASSWORD=...
```

Then start the production stack:

```bash
docker compose \
  --env-file docker/.env.production \
  -f docker/docker-compose.yml \
  up --build
```

### Simulator

The simulator environment replaces the physical factory and factory MQTT broker with a web-based simulation environment. This environment is designed to simulate the behavior of the physical factory as closely as possible and includes features to manipulate items and simulate failures at runtime for extensive testing. 

The simulator deployment adds [`docker/docker-compose.simulation.yml`](docker/docker-compose.simulation.yml), which starts the factory simulator and a local Mosquitto broker.

```bash
docker compose \
  --env-file docker/.env.simulation \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.simulation.yml \
  up --build
```

Useful local links:

- Frontend: <http://localhost:3000>
- Factory simulator UI: <http://localhost:8081>
- Kafka UI: <http://localhost:8090>
- Port map: [`docker/PORTS.md`](docker/PORTS.md)

## Usage Example

The system behaves the same in production and simulation mode. For demonstration purposes, this example uses the simulated factory. For local testing, use the simulator stack and create an AirTag order through the backend API:

```bash
curl -X POST http://localhost:8082/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "id": "order-demo-001",
    "color": "blue",
    "engravedText": "EDA 2026"
  }'
```

Open the simulator at <http://localhost:8081> to add items and interact with the factory. When the order was successfully created, the system will publish a command that an item of specified color should be added to the system. As the item is added to the intake, the production will start.

During the flow, user tasks will be started. These are handled through the respective Camunda engine's task list. 

## Architecture

The architecture follows the ADRs in [`adr/`](adr/).

There are three service types in this system:

| Service type            | Responsibility                                      | Typical structure                                                              |
|-------------------------|-----------------------------------------------------|--------------------------------------------------------------------------------|
| Frontend Service        | displays and records user interactions              | Spring Boot, Kafka consumers/producers                                         |
| Workflow/Domain Service | Owns business process state and orchestration logic | Spring Boot, Camunda 7, Kafka consumers/producers                              |
| Integration Service     | Owns one machine boundary                           | Kafka consumers/producers, HTTP machine client, MQTT consumer and event filter |

![Figure 3: Architecture Diagram](img/ArchitectureDiagram.png)

## Key Configuration

Factory connection targets are configured in the Docker environment files:

| Concern | Real factory | Simulator |
| --- | --- | --- |
| Shared Kafka bootstrap | `docker/.env.production` | `docker/.env.simulation` |
| MQTT broker URL | `MQTT_BROKER_URL` | `MQTT_BROKER_URL` |
| Sorter HTTP target | `SORTER_HOST`, `SORTER_PORT` | `SORTER_HOST`, `SORTER_PORT` |
| Other machine HTTP targets | `VACUUM_GRIPPER_*`, `ENGRAVER_*`, `POLISHING_MACHINE_*`, `WORKSTATION_TRANSPORT_*` | Same variables |
| MQTT credentials | `docker/.env.mqtt.local` | Usually empty/demo for local Mosquitto |
