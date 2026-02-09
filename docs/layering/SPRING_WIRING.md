## Spring framework as apart of layering

---

## Core principles

- Business logic must not depend on frameworks
- Spring component scanning is restricted by design
- Wiring and lifecycle decisions must be explicit

---

## Spring stereotype usage policy

Spring stereotypes are intentionally restricted to specific layers.

### Allowed layers

#### `bootstrap`
**Purpose:** Composition root and wiring

Allowed annotations:
- `@Configuration`
- `@Bean`
- `@ConfigurationProperties`
- `@Component` (for binding or wiring helpers when appropriate)

This is the only layer responsible for assembling the application.

---

#### `app` (application orchestration)
**Purpose:** Use-case entry points and orchestration

Allowed annotations:
- `@Service`

The app layer coordinates domain logic and outbound ports. It translates dependency failures into application-level semantics but does not contain transport or infrastructure code.

---

#### `web/mvc`
**Purpose:** Inbound HTTP adapter inside the DispatcherServlet

Allowed annotations:
- `@Controller`
- `@RestController`
- Spring MVC mapping annotations
- `@Component` for MVC-specific helpers such as:
    - request/response mappers
    - API version adapters
    - validation helpers

Controllers should remain thin and avoid servlet APIs. Typed deserialization (`@RequestBody`) and dedicated mappers are preferred.

---

### Discouraged or restricted layers

#### `web/servlet`
**Purpose:** Pre-MVC servlet infrastructure

Examples:
- servlet filters
- low-level request/response handling

Guideline:
- Avoid `@Component`
- Register explicitly via `FilterRegistrationBean` in `bootstrap`

Rationale:
- This layer runs outside the DispatcherServlet
- Explicit registration makes ordering and lifecycle obvious
- Prevents accidental coupling with MVC concerns

---

#### `web/error`
**Purpose:** Shared web error mapping utilities

Guideline:
- Prefer plain classes over Spring-managed beans
- Wire explicitly if integration is required

Rationale:
- Error handling is sensitive to ordering and coupling
- Keeping this explicit avoids hidden framework behavior

---

#### `infra`
**Purpose:** Outbound adapter implementations

Guideline:
- Avoid Spring stereotypes by default
- Wire adapters explicitly in `bootstrap`

Rationale:
- Prevents accidental bean sprawl
- Keeps infrastructure dependencies intentional
- Makes testing and replacement straightforward

---

#### `core`
**Purpose:** app models and port definitions

Rules:
- No Spring annotations
- No transport concerns
- No infrastructure dependencies

This layer defines:
- domain models
- outbound port interfaces
- policies and settings
- stable error contracts

---

## Summary

Spring is used as:
- a wiring mechanism
- an adapter framework

It is **not** the architecture.

Architecture lives in the code structure and dependency rules, not in Spring annotations.
