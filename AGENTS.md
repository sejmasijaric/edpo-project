# Coding Agent Guidelines

## Code Style
- Perform small, focused changes with thorough explanations in commit messages.
- Match existing coding conventions. When in doubt, reference similar files in the repository.
- Avoid introducing unused dependencies.

## Architecture
The project follows an event driven architecture following a predefined set of layers. Please refer to ADRs when making architectural decisions. The following guidelines apply:
- Whenever a new service or sub application is created, it should be an independent maven project which sits in a subdirectory of the project root. These should be independently deployable and (unless specified) be part of some docker compose.
- docker compose files should be located in [`docker/`](docker/), dockerfiles directly in the project subdirectory.

## Testing
- Run all relevant automated tests or linters for components that were modified.
- Ensure that new modules have appropriate test coverage.

## Documentation
- Keep README files, diagrams, or inline comments up to date when behavior changes.
- Explore the [`docs/`](docs/) and [`adr/`](adr/) folder for additional domain background such as ubiquitous language. Keep these resources updated when requirements evolve.
- [`docker/PORTS.md`](docker/PORTS.md) contains the port mapping of the entire project. Please refer to it when specifying new ports and update it as new ports mappings are added. 

## Architecture Decision Records (ADRs)
- ADRs live in the [`adr/`](adr/) directory. Add a new ADR whenever a significant
  architectural choice is made or revised.
- Follow the existing numbering convention (`NNNN-SS-title.md`) and document the
  context, decision, and consequences clearly so future contributors understand
  the rationale.
- Reference relevant ADRs in commit messages or PR descriptions when changes are
  directly related.
