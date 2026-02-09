## Hexagonal interpretation

This project uses a pragmatic hexagonal architecture:

- Outbound ports are defined explicitly in `core/port`
- Infrastructure implements those ports in `infra`
- Inbound HTTP delivery is implemented directly in `web`
- Controllers call application services rather than inbound ports

The emphasis is on dependency direction and boundary clarity rather than enforcing indirection everywhere.

---

## Error translation boundaries

Errors are translated at explicit boundaries:

- **infra → app**  
  Outbound Infrastructure failures are normalized into `DependencyCallException`

- **app → web**  
  Application-level failures are represented as `ApiException`

- **web → protocol**  
  HTTP status codes and error payloads are derived at the web boundary

Each layer owns translation at its boundary and does not leak internal exception types upward.

---

## Why this matters

This structure:
- Keeps business logic portable and testable
- Prevents framework creep into core logic
- Makes concurrency and resiliency behavior explicit
- Scales cleanly as services grow
- Reduces surprise during debugging and operations