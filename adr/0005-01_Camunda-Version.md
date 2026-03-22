# ADR 0005-01: Camunda 7 vs Camunda 8

**Date:** 17.03.2026
**Status:** Accepted

## Context
Our IoT Fischertechnik factory project requires a lightweight, self-hosted workflow engine to orchestrate BPMN processes such as quality control and sorting. We evaluated Camunda 7 Community Edition and Camunda 8 for this purpose.

## Decision
We chose **Camunda 7 Community Edition** for the following reasons:

- **Open-Source Licensing:**
Camunda 7 CE is licensed under the Apache License 2.0 (https://github.com/camunda/camunda-bpm-platform), allowing unrestricted production use at zero cost. Camunda 8 moved to the proprietary *Camunda License v1* starting with version 8.6 (October 2024). Core components such as Zeebe, Operate, and Tasklist require an Enterprise license for production use (https://docs.camunda.io/docs/reference/licenses/}}\footnote{\url{https://camunda.com/blog/2024/04/licensing-update-camunda-8-self-managed/).

- **Lightweight Self-Hosting:**
    Camunda 7 can be embedded directly as a library inside a Spring Boot application, running in a single JVM with an in-memory or file-based H2 database. Deployment is a single .jar file. Camunda 8 is built on a distributed, cloud-native architecture. A minimal self-managed deployment requires a Kubernetes cluster running Zeebe brokers, Elasticsearch, a Zeebe Gateway, and optionally Operate, Tasklist, and Identity as separate services (https://docs.camunda.io/docs/components/best-practices/architecture/sizing-your-environment/). This is excessive for our constrained IoT environment.

- **Community Tooling Included:**
    Camunda 7 CE ships with Cockpit (process monitoring) and Tasklist (user task management) ready for production.
    In Camunda 8, Operate and Tasklist are only free for non-production (development/testing) use. Running them in production requires a commercial Enterprise license. Without it, users must build their own monitoring and task management tools from scratch using low-level Zeebe APIs (https://camunda.com/blog/2022/05/how-open-is-camunda-platform-8/).

- **Simplicity for Our Scale:**
    Camunda 8 is designed for high-throughput, horizontally scalable microservice architectures. Our Fischertechnik factory processes a low volume of items and does not require distributed partitioning, Elasticsearch-backed persistence, or Kubernetes orchestration.

## Consequences

### Positive:
- Zero licensing cost for production deployment.
- Single-JAR deployment with embedded engine no Kubernetes or Elasticsearch required.
- Cockpit and Tasklist available out of the box for process monitoring and user tasks.

### Negative:
- Camunda 7 CE reached end-of-life in October 2025 (version 7.24) (https://www.mesoneer.io/blog/camunda); no further community updates. The Enterprise Edition receives only maintenance fixes until April 2027.
- Limited horizontal scalability acceptable given our low-throughput IoT use case.

### Alternatives Considered:
- **Camunda 8 Self-Managed:** Rejected due to commercial licensing for production, heavyweight Kubernetes infrastructure, and no embedded engine support.
- **CIB seven (Camunda 7 fork):** A maintained open-source fork of Camunda 7 under Apache 2.0 (https://www.cib.de/en/open-source-bpm-cib-seven-best-alternative-to-camunda-7/). Viable long-term fallback if Camunda 7 CE becomes unmaintainable.
- **Flowable:** Open-source BPM engine with similar concepts, but would require migration of existing BPMN models and worker implementations.