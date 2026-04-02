# Factory Simulator

Standalone Spring Boot application for simulating sinks and items in a smart factory layout.

## Features

- Models sinks with fixed two-dimensional coordinates
- Models items with an ID and color
- Supports listing, deleting, and moving items between sinks through an HTTP API
- Supports a vacuum gripper endpoint that transports items between machine-specific sink aliases
- Publishes sorter, vacuum gripper, oven, milling machine, and workstation transport MQTT snapshots every 2 seconds so the integration layer can react to sensor changes
- Renders a web UI with sinks positioned on top of a factory layout and an item-management control panel

## Run

```bash
mvn spring-boot:run
```

The UI is available on `http://localhost:8081`.

When the local MQTT broker is reachable, the simulator publishes sorter events to `FTFactory/SM_1`, vacuum gripper events to `FTFactory/VGR_1`, oven events to `FTFactory/OV_1`, milling machine events to `FTFactory/MM_1`, and workstation transport events to `FTFactory/WT_1` every 2 seconds. The vacuum gripper maps `i7_light_barrier` to `SINK-I1` and `i4_light_barrier` to `SINK-I2`. The oven maps `i5_light_barrier` to `VGR-oven`. The milling machine maps `i4_light_barrier` to `MM-ejection`. The workstation transport publishes only `timestamp`, `current_task`, and `current_task_duration`.

## Vacuum Grippers

The simulator exposes `GET /vgr/pick_up_and_transport?machine=vgr_1&start=sink_2&end=oven`.
The simulator also exposes `GET /wt/pick_up_and_transport?machine=wt_1&start=milling_machine&end=oven`.

The endpoint blocks until both modeled movement phases have finished. The delay between phases is configured via `factory.simulation.movement-delay`.

## Sorter

The simulator exposes `GET /sm/sort?machine=sm_1&start=initial&predefined_ejection_location=sink_1`.
The simulator also exposes `GET /sm/detect_color?machine=sm_1`.

The sorter uses a reusable one-way point-to-point transport model. It prefers `SM-I` as input, can pull an item forward from `MM-ejection` when `SM-I` is empty, and maps `sink_1`, `sink_2`, and `sink_3` to `SINK-S1`, `SINK-S2`, and `SINK-S3`.
The `detect_color` endpoint reports the current item color at the sorter input in lowercase, or `none` when no item was available to move or detect. Calling it will also advance an item from `MM-ejection` to `SM-I` when possible.

## MQTT Mocking

The simulator publishes MQTT payload shapes for the integration services:

- Sorter topic: `FTFactory/SM_1`
- Vacuum gripper topic: `FTFactory/VGR_1`
- Oven topic: `FTFactory/OV_1`
- Milling machine topic: `FTFactory/MM_1`
- Workstation transport topic: `FTFactory/WT_1`
- Interval: `2s`
- Sorter relevant field: `i3_light_barrier`
- Vacuum gripper relevant fields: `i7_light_barrier`, `i4_light_barrier`, `current_task`, `current_task_duration`
- Oven relevant fields: `i5_light_barrier`, `current_task`, `current_task_duration`
- Milling machine relevant fields: `i4_light_barrier`, `current_task`, `current_task_duration`
- Workstation transport relevant fields: `current_task`, `current_task_duration`

Configuration is available through these environment variables:

- `FACTORY_MQTT_ENABLED`
- `FACTORY_MQTT_BROKER_URL`
- `FACTORY_MQTT_SORTER_TOPIC`
- `FACTORY_MQTT_SORTER_CLIENT_ID`
- `FACTORY_MQTT_VGR_TOPIC`
- `FACTORY_MQTT_VGR_CLIENT_ID`
- `FACTORY_MQTT_OVEN_TOPIC`
- `FACTORY_MQTT_OVEN_CLIENT_ID`
- `FACTORY_MQTT_MM_TOPIC`
- `FACTORY_MQTT_MM_CLIENT_ID`
- `FACTORY_MQTT_WT_TOPIC`
- `FACTORY_MQTT_WT_CLIENT_ID`
- `FACTORY_MQTT_PUBLISH_INTERVAL`

## Milling Machine

The simulator exposes `GET /mm/move_from_to?machine=mm_1&start=initial&end=ejection`.

The milling machine is modeled as a simple one-way transport using the same reusable transport module, with `initial -> MM-initial` and `ejection -> MM-ejection`.
