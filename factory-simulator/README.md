# Factory Simulator

Standalone Spring Boot application for simulating sinks and items in a smart factory layout.

## Features

- Models sinks with fixed two-dimensional coordinates
- Models items with an ID and color
- Supports listing, deleting, and moving items between sinks through an HTTP API
- Supports a vacuum gripper endpoint that transports items between machine-specific sink aliases
- Renders a web UI with sinks positioned on top of a factory layout and an item-management control panel

## Run

```bash
mvn spring-boot:run
```

The UI is available on `http://localhost:8081`.

## Vacuum Grippers

The simulator exposes `GET /vgr/pick_up_and_transport?machine=vgr_1&start=sink_2&end=oven`.
The simulator also exposes `GET /wt/pick_up_and_transport?machine=wt_1&start=milling_machine&end=oven`.

The endpoint blocks until both modeled movement phases have finished. The delay between phases is configured via `factory.simulation.movement-delay`.

## Sorter

The simulator exposes `GET /sm/sort?machine=sm_1&start=initial&predefined_ejection_location=sink_1`.
The simulator also exposes `GET /sm/detect_color?machine=sm_1`.

The sorter uses a reusable one-way point-to-point transport model. It prefers `SM-I` as input, can pull an item forward from `MM-ejection` when `SM-I` is empty, and maps `sink_1`, `sink_2`, and `sink_3` to `SINK-S1`, `SINK-S2`, and `SINK-S3`.
The `detect_color` endpoint reports the current item color at the sorter input in lowercase, or `none` when no item was available to move or detect. Calling it will also advance an item from `MM-ejection` to `SM-I` when possible.

## Milling Machine

The simulator exposes `GET /mm/move_from_to?machine=mm_1&start=initial&end=ejection`.

The milling machine is modeled as a simple one-way transport using the same reusable transport module, with `initial -> MM-initial` and `ejection -> MM-ejection`.
