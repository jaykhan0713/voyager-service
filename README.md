# platform-service

A production-grade Spring Boot 4 service platform baseline designed to run as a real service and serve as a repeatable foundation for additional services on AWS architecture.

This is **not** a simple boilerplate or starter template. It is an opinionated, fully runnable service built around **Project Loom virtual threads**, **Spring Boot 4**, **Java 25**, and modern production concerns such as explicit backpressure, structured logging, distributed tracing readiness, and resilience-by-default.

The repository demonstrates how a real-world microservice should be built when **implicit thread-based safety nets no longer exist**.

---
## About this project

This project exists as a concrete demonstration of how I approach building modern backend services in today’s Java and Spring ecosystem.

- After spending 7 years at Expedia working on distributed systems and microservices, I accumulated deep, hands-on experience with resiliency, scalability, and operational reliability. This repository distills that experience into a tangible, runnable service rather than abstract examples.

- The goal was to design and build an end-to-end, production-oriented microservice platform that reflects real-world constraints. This includes explicit backpressure, disciplined layering, resilience-by-default, and observability as a first-class concern rather than an afterthought.

- A key intention of this platform is to provide a strong baseline for resiliency, with circuit breakers and bulkheads treated as mandatory primitives. Through industry experience, I have seen how essential these patterns are in large microservice ecosystems. With Project Loom removing implicit thread-based backpressure, bulkheads in particular become a required design choice rather than an optional optimization.

- This is my first opportunity to build an opinionated service platform from the ground up, with the explicit goal of future-proofing microservices. Infrastructure concerns, concurrency models, and resiliency mechanics are solved once so that future services can focus primarily on business orchestration.

- In environments where many engineers collaborate on a single service, architectural layering tends to erode over time unless it is explicitly enforced. Short-term optimizations often win, while structure degrades silently. This project demonstrates that clean layering, performance, and operational safety can coexist when the architecture is intentional.

- The platform reflects where the Java and Spring ecosystem is today and where it is heading. It showcases a virtual thread concurrency model using Project Loom, Spring Boot 4, and Java 25, all of which are now stable and production-ready. The project originally started on Spring Boot 3 and Java 21 and was intentionally migrated forward to mirror real-world evolution.

- Modern observability standards are adopted throughout the platform, moving away from legacy tracing approaches toward OpenTelemetry with Micrometer, and providing a foundation suitable for real production diagnostics.

- The project also demonstrates an end-to-end AWS deployment architecture, from API Gateway with Cognito-based JWT authentication, through VPC Link into a private VPC, terminating at an Application Load Balancer fronting ECS services. CI/CD practices are incorporated to reflect how services are built, deployed, and evolved in production environments.

- The long-term vision is simple: spin up new microservices by plugging in business logic, without repeatedly re-solving infrastructure, concurrency, resiliency, or deployment concerns.

---
## Running locally 

### Docker (recommended)

```
docker compose up --build
```

### Gradle

```
./gradlew bootRun --args="--spring.profiles.active=dev"
```
---

## Why this exists

Project Loom makes blocking I/O cheap and abundant by removing the traditional cost of threads. While this simplifies many programming models, it also removes implicit safety mechanisms teams have relied on for years, especially bounded thread pools acting as accidental backpressure.

This platform demonstrates how to build a microservice **from the ground up** when:

- Concurrency is effectively unbounded due to virtual threads
- Resilience must be intentional. CircuitBreaker and Bulkhead decorations at the outbound protocol request boundary
- Backpressure must be explicit
- Observability must be first-class

The goal is **clarity, safety, and realism**.

---

## What this repository demonstrates

- Spring Boot 4 with Java 25 and Project Loom
- A hexagonal (ports and adapters) layering architecture
- Explicit separation between business logic and infrastructure
- A dedicated composition root for Spring wiring
- Versioned API contracts
- Production-oriented HTTP boundary patterns
- Outbound client foundations with centralized resiliency
- Deployment models aligned with AWS ECS and Fargate
- Local observability tooling via Prometheus and Grafana
- Functional tests and baselines
- Jacoco test coverage
- Sonar gates for pmd and checkstyle

---

## Architecture overview

This service follows a **hexagonal-inspired architecture** adapted pragmatically for Spring Boot.

Business logic lives at the center. Frameworks, delivery mechanisms, and infrastructure adapt to it. Dependencies always point inward.

```
           web
            |
            v
           app
            |
            v
          core
            ^
            |
          infra

bootstrap -----> (web, app, core, infra)
```

```
api        external contract models (versioned)
web        inbound adapter (Spring MVC, filters, error mapping)
app        application orchestration
core       domain models, policies, ports
infra      outbound adapters (HTTP clients, propagation, resiliency), concurrency structures and context propagation
bootstrap  composition root (Spring wiring, config binding)
```

Spring wiring and configuration binding live exclusively in the `bootstrap` layer, which acts as the **composition root**.

---

## Layer responsibilities

### `core/`
Framework-agnostic business layer.

- Domain models and business outputs
- Stable error contracts
- infra port interfaces (to bridge infra to the rest of the application)
- Behavioral models such as policies and settings

This layer contains **no Spring**, **no HTTP**, and **no infrastructure dependencies**.

### `app/`
Application orchestration layer. (or business orchestration)

- Coordinates use cases and business workflows.
- Calls outbound ports defined in `core`
- Translates dependency failures into application-level semantics

### `web/`
Inbound HTTP adapter. Spring web.

- Spring MVC layer (controllers, post Dispatcher Servlet handling)
- Servlet filters
- HTTP-to-domain mapping
- Standardized error translation
- Inbound resiliency

### `infra/`
Outbound adapter implementations and concurrency. (Framework-agnostic)

- HTTP clients and registries
- Interceptors and request propagation
- Resiliency mechanisms (bulkheads, circuit breakers)
- Error mapping for external dependencies (i.e outbound exceptions to core DependencyExceptions)
- Concurrency context propagation (identity, MDC)

### `bootstrap/`
Composition root and wiring layer.

- Spring `@Configuration` classes
- ConfigurationProperties binding
- Mapping configuration records into core models
- Adapter assembly and dependency wiring
- Servlet filter registration

Notes:

Naming convention: configuration classes use `*Configuration`, not `*Config`, to avoid conflicts with third-party conventions.

The term "Dependency" is meant to be a bridging convention between outbound infra and business layer

For detailed layering and dependency rules, see  
[Layering Index page](docs/layering/INDEX.md)

---

## Concurrency model

The platform is built with **Project Loom virtual threads** as a first-class assumption.

Because virtual threads remove implicit backpressure from bounded thread pools, this service enforces:

- Explicit concurrency limits via inbound bulkhead semaphores
- Explicit backpressure handling via outbound bulkhead semaphores, timeout configurations
- Predictable resource usage under load

---

## Observability

The platform is observable by default:

- Structured logging
- Correlation IDs
- Identity context propagation
- Distributed tracing readiness
- Standardized error responses
- Health and readiness endpoints
- Micrometer, OpenTelemetry, MDC, and Logback are used as a part of this.

On Docker (development only), Jaeger is used to inspect distributed traces.
- The service exports spans via the OTLP exporter to the OpenTelemetry Collector container.
- The OpenTelemetry Collector then forwards traces to Jaeger for visualization.
---

## Security

This service does **NOT** use Spring Security

- It is designed under the assumption of an API Gateway + Cognito entry
- This starts at Http API gateway, with cognitio authorization attachment, JWT issuing. API gateway is the source of truth.
- The sub extracted from the JWT is propagated to the service, effectively keeping microservice security "dumb" and reading that authenticated user sub from http headers
- All services on the VPC are http, exist on private subnets. TLS is terminated at the edge by ALB.

---

## Git CI + SONAR PMD/Checkstyle/Code smells + Jacoco Coverage gates

This service is wired via git workflows [ci.yml](.github/workflows/ci.yml) 

- Pull requests are gated on CI passing.
- ./gradlew clean check covers tests, functional tests, jacoco, dependencies, etc
- Jacoco code line coverage to ensure code paths are tested.
- Sonar qube (using sonar cloud free-tier) gates for code smells.

---

## Functional tests

A functional test path is created to teach implementing microservices how to model and layer correctly.

- A SmokeController endpoint path is enabled for smoke profile
- This path demonstrates a business usecase workflow called "ping"
- an outbound dependency (infra sees it as RestClient build ontop of jdk http client) PingDependency is used app orchestration layer
- Downstream contract model is mapped to an in-service core domain model for use with orchestration
- this core domain model is mapped to inbound contract model to hand off to caller.

---

## HTTP endpoints

```
service-url: http://localhost:8080

/actuator/              Health, metrics, diagnostics
/v3/api-docs            OpenAPI specification
/swagger-ui.html        Swagger UI
/api/v1/sample          Sample endpoint to showcase OpenAPI and service workflow
```

```
prometheus-url: http://localhost:9090
```

```
grafana-url: http://localhost:3000
```

```
jaeger-url: http://localhost:16686
```

---

## AWS notes
- ECS Fargate
- edge service behind ALB
- Internal service-to-service communication via ECS Service Connect
- Rolling deployments by default
- Blue-green or canary deployments at the ALB layer when service is behind ALB (TO-DO)
- Starting at Http API Gateway via VPC link -> ALB (private subnet) at the edge
---

## Status

1. Actively evolving as a platform foundation. MVP baseline complete. AWS architecture + ECS microservices that template this service are in progress
2. Documentation for platform-service in progress
3. Separate AWS platform architecture documentation and decions will be in a separate repo

---
## Contact

Please feel free to reach out and discuss anything at jaykhan0713@gmail.com

---
