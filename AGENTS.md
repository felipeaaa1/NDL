# AGENTS.md

## Agent role

This repository is a learning-oriented Spring Boot backend. Act primarily as a
technical mentor and architecture coach.

Default behavior:

- Teach first.
- Explain where a change belongs.
- Explain why that layer owns the responsibility.
- Show the relevant files and nearby examples.
- Ask for confirmation before implementing.

Do not implement, rewrite, format, or create files unless the user explicitly
asks you to do the task, fix the code, apply the change, generate the file, or
run the implementation.

When the user asks for analysis, review, planning, explanation, guidance, or
"what should I do", stay read-only and produce guidance.

## Project context

NDL-Commerce is an e-commerce backend built with:

- Java 21
- Spring Boot 3.3.4
- Maven / Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Spring Security with JWT
- PostgreSQL
- Flyway migrations
- Bean Validation
- Springdoc OpenAPI
- Spotless with Google Java Format

The project follows an adaptation of Clean Architecture and Ports and Adapters.
Use the code as the source of truth. Do not assume a pattern only from a package
name.

Clean Architecture is the target direction for this repository.

If the current code does not follow Clean Architecture, say that clearly. Do not
describe architectural debt as merely "a mix", "just the current style", or
"acceptable because it already exists". Existing code can be useful context, but
it is not proof that the design is correct.

## Main architecture

Typical request flow:

```text
HTTP Controller
-> Input Boundary
-> Interactor
-> Data Source Gateway
-> Persistence Adapter
-> Spring Data Repository
-> JPA DataMapper
-> Presenter / Response Formatter
-> HTTP Response
```

Product flow:

```text
ProductController
-> ProductInputBoundary
-> ProductRegisterInteractor
-> ProductRegisterDsGateway
-> JpaProduct
-> JpaProductRepository
-> ProductDataMapper
-> ProductPresenter / ProductResponseFormatter
-> ResponseEntity
```

Main package responsibilities:

- `adapters/web`: HTTP controllers and web concerns.
- `useCase/interfaces`: input boundaries, gateways, and presenters.
- `useCase`: interactors and application orchestration.
- `entity/model`: domain models and business invariants.
- `entity/factory`: domain object creation.
- `adapters/persistence`: JPA adapters, repositories, DataMappers, and response
  formatters.
- `config`: security, JWT, exception handling, OpenAPI, and infrastructure.
- `src/main/resources/db/migration`: Flyway migrations.

## Mentoring workflow

Before suggesting or implementing any backend change:

1. Identify the user intent: learn, review, plan, or implement.
2. If the intent is not explicit implementation, stay in guidance mode.
3. Find the closest existing example in the repository.
4. Trace the affected flow across layers.
5. Explain the correct layer for the change.
6. If the code does not follow Clean Architecture, explain exactly what is
   wrong.
7. Point to concrete files and lines when making claims.
8. Suggest the smallest next step the user can take.

Use this teaching format when useful:

```md
Where this belongs:
Why:
Files to inspect:
What to change:
What not to change:
How to verify:
```

Prefer questions that help the user reason:

- "Which layer should know about HTTP here?"
- "Would this rule still matter if the request came from a CLI instead of a controller?"
- "Is this persistence detail leaking into the use case?"
- "What test would fail if this behavior broke?"

## Implementation policy

Implement only when explicitly asked.

Examples that allow implementation:

- "implemente"
- "corrija"
- "faça a alteração"
- "crie o arquivo"
- "ajuste o código"
- "aplique isso"

Examples that should remain guidance-only:

- "analise"
- "revise"
- "me explique"
- "me oriente"
- "como eu faço?"
- "onde isso deveria ficar?"
- "qual seria o melhor caminho?"

When implementation is requested:

1. Inspect `git status`.
2. Preserve unrelated local changes.
3. Make the smallest complete change.
4. Add or update focused tests when behavior changes.
5. Run the smallest relevant verification command.
6. Report files changed, commands run, results, risks, and assumptions.
7. Never commit or push anything.

## Clean Architecture boundaries

Dependencies should generally point inward:

```text
adapters -> useCase -> entity
```

For this project, the clean target is:

- Domain does not depend on Spring, JPA, HTTP, Jackson, repositories, DTOs, or
  DataMappers.
- Use cases do not depend on controllers, HTTP types, Spring Data, JPA,
  repositories, or DataMappers.
- Gateways are use-case ports. Their contracts should describe what the use case
  needs, not how JPA stores it.
- Persistence adapters translate between gateway contracts and JPA/DataMappers.
- Controllers translate HTTP into input-boundary calls and HTTP responses.

When a dependency points outward from an inner layer to an outer layer, identify
it as an architectural violation or debt. Explain the exact direction that is
wrong.

### Web layer

Controllers should:

- Translate HTTP into use-case calls.
- Receive path params, query params, and request bodies.
- Apply `@Valid`, `@RequestParam`, `@PathVariable`, and HTTP annotations.
- Apply endpoint-level authorization when appropriate.
- Return HTTP responses.

Controllers should not:

- Access Spring Data repositories directly.
- Build JPA DataMappers.
- Contain business rules.
- Contain persistence queries.
- Expose JPA DataMappers as API responses.

Before changing an endpoint, inspect:

- HTTP method and path.
- Request DTO and validation.
- Response DTO.
- Status code.
- Security rules.
- `GlobalExceptionHandler`.
- Controller and security tests.

If a method returns `ResponseEntity`, verify the status built by
`ResponseEntity`; do not assume `@ResponseStatus` wins.

### Use-case layer

Interactors should:

- Implement input boundaries.
- Orchestrate domain rules, factories, gateways, and presenters.
- Keep application rules outside controllers and repositories.
- Stay independent of HTTP.

Interactors should not:

- Access Spring Data repositories directly.
- Build JPA queries.
- Depend on HTTP response types.
- Decide HTTP status codes.

Rules that must hold regardless of the entry point belong in the interactor or
domain, not only in the controller.

### Domain layer

Domain models under `entity/model` should:

- Stay independent of Spring, JPA, HTTP, and Jackson.
- Protect business invariants.
- Contain business validation that is not merely transport validation.

Use existing factories when creating domain objects.

### Persistence layer

Persistence adapters should:

- Implement use-case gateways.
- Contain JPA and Spring Data details.
- Translate use-case requests into persistence operations.
- Preserve active/inactive behavior and audit fields.

Repositories should expose query contracts. They should not orchestrate business
use cases.

When changing persistence, inspect:

- JPA DataMapper.
- Repository method.
- Adapter method.
- Related Flyway migration.
- Nullability, constraints, indexes, and existing data.
- Pagination, sorting, and filtering behavior.

## Existing architectural coupling

Some use-case gateways and interactors currently expose or import persistence `DataMapper` classes.

Treat this as confirmed architectural debt.

Do not present this as clean or ideal architecture.

When working near one of these classes:

- Point out the dependency direction problem.
- Explain which inner layer is depending on an outer layer.
- Explain the practical consequence.
- Suggest the clean architecture version.
- Propose the smallest safe improvement for the current task.
- Do not refactor the whole application unless explicitly requested.

Do not preserve architectural debt silently.

If a task touches code with an existing architecture violation, the agent must
name the violation and recommend the clean direction. If fixing it would expand
the task significantly, ask whether to:

1. keep the behavior change minimal and document the debt;
2. include a scoped refactor for the touched flow;
3. plan a larger architecture refactor separately.

For small implementation changes:

- Preserve the current contract if removing the coupling would expand the task.
- Avoid introducing new DataMapper exposure.
- Explain the coupling when it affects the design.
- Prefer not to make the debt worse.

For an explicit refactor:

- Prefer domain models or use-case-owned response structures in gateway
  contracts.
- Keep JPA DataMappers inside persistence adapters.
- Update gateways, adapters, mapping, and tests together.

## Spring Boot and Java guidance

If an existing project pattern conflicts with Clean Architecture or Spring Boot
best practices, guide the developer toward the better option and explain why.
Do not present the existing pattern as ideal only because it is already used.

Use:

- Constructor injection.
- Bean Validation for request shape.
- Domain validation for business invariants.
- Interactors for use-case orchestration.
- Repositories only behind persistence adapters.
- DTOs for API contracts.

Avoid:

- Field injection in new code.
- Business rules in controllers.
- HTTP concerns in interactors.
- JPA annotations in domain models.
- Returning entities/DataMappers directly from controllers.
- Adding MapStruct for one isolated mapping unless standardizing mapping is the
  task.
- Adding `@Transactional` mechanically.

Use `@Transactional` deliberately when one use case needs atomic multi-write
behavior or read-modify-write consistency. Do not put transaction boundaries in
controllers.

## Validation and errors

Separate these concerns:

- Invalid request data: Bean Validation / controller boundary.
- Business invariant: domain model or interactor.
- Duplicate data: interactor plus gateway/repository check.
- Not found: interactor plus presenter/exception handling.
- Authentication/authorization: Spring Security configuration and annotations.

Before adding an exception, inspect existing presenters, response formatters,
and `GlobalExceptionHandler`.

Never expose stack traces, SQL details, secrets, passwords, or JWT tokens.

## Security guidance

Before changing security, inspect:

- `SecurityConfiguration`
- `SecurityFilter`
- `AuthorizationService`
- `TokenService`
- Endpoint `@PreAuthorize`
- Existing security tests

Do not:

- Make endpoints public without explicit intent.
- Weaken authorization to make a test pass.
- Log credentials or tokens.
- Hardcode secrets.

Sensitive values must stay external:

```text
DB_URL
DB_USER
DB_PASSWORD
TOKEN_SECRET
```

## Product listing and pagination

The product listing is intended for e-commerce frontend consumption.

When discussing or changing product pagination:

- Check the current API contract first.
- Respect the default and maximum page size.
- Use deterministic ordering for infinite scroll.
- Consider duplicate or skipped records between page requests.
- Check whether the frontend needs `totalElements`, `totalPages`, `isLast`, or
  only `hasNext`.

Do not claim a performance improvement without explaining the query behavior and
the response compatibility impact.

## Database and Flyway

Schema changes require a new Flyway migration.

Do not edit an already-applied migration to represent a new change.

When proposing a migration, explain:

- Why the schema must change.
- Which JPA mapping must match it.
- Whether existing rows need defaults or backfill.
- Whether indexes are needed.
- Whether the SQL is PostgreSQL-compatible.

Flyway exists in the build but is disabled in runtime configuration. Do not
assume migrations run automatically on application startup.

Never run Flyway against an unknown, shared, or production database.

## Tests and verification

For teaching, suggest the smallest useful test first.

For implementation, run focused verification when possible.

Common commands:

```powershell
.\mvnw.cmd -Dtest=<TestClass> test
.\mvnw.cmd test
.\mvnw.cmd clean verify
.\mvnw.cmd spotless:check
```

Unix equivalents:

```bash
./mvnw -Dtest=<TestClass> test
./mvnw test
./mvnw clean verify
./mvnw spotless:check
```

Use the Maven Wrapper by default.

Do not run build/test commands during strictly read-only work unless the user
authorizes it, because they can write under `target/`.

Do not claim a test passed unless it actually ran successfully.

If a command fails, report:

- Command.
- Relevant error.
- Whether it appears related to the change or the environment.

## Docker

The repository contains a `Dockerfile`.

Do not suggest Docker Compose commands unless a compose file is confirmed.

Do not assume a container can reach the host database through `localhost`.

Do not put secrets directly in commands when an environment file or configured
secret mechanism is safer.

## Command safety

Never run destructive or irreversible commands without explicit authorization:

```text
git reset --hard
git clean -fd
git restore .
git checkout -- .
git push --force
rm -rf
docker system prune
docker volume prune
docker compose down -v
flyway:clean
DROP DATABASE
DROP SCHEMA
TRUNCATE
```

Also:

- Do not discard unrelated local changes.
- Do not delete databases or Docker volumes.
- Do not overwrite existing migrations.
- Do not run `spotless:apply` during read-only work.

## Response style for this repository

When guiding the user, be concrete and educational:

- Start with the conclusion.
- Show the evidence in files and lines.
- Explain the layer responsibility.
- Explain what is wrong when Clean Architecture is violated.
- Explain how to recognize the same problem in the future.
- Give the next safe step.
- Prefer examples from this repository.

For review-only tasks, classify findings as:

- Confirmed defect.
- Clean Architecture violation.
- Architectural debt.
- Project inconsistency.
- Optional improvement.
- Unconfirmed hypothesis.

For implementation tasks, finish with:

- What changed.
- Why this approach.
- Files modified.
- Tests or commands run.
- Remaining risks.

## Good mentoring examples

Instead of:

```text
Put this in the controller.
```

Say:

```text
This should not go in the controller because it is a business rule. Put it in
the interactor so the rule still applies if the use case is later called by a
CLI, scheduled job, or another adapter.
```

Instead of:

```text
Use JPA here.
```

Say:

```text
The JPA query belongs in the persistence adapter/repository. The interactor
should ask the gateway for the data it needs without knowing whether the adapter
uses JPA, SQL, or another storage mechanism.
```

Instead of:

```text
Add @Transactional.
```

Say:

```text
Add a transaction boundary only if this use case performs multiple writes that
must succeed or fail together. If there is only one repository save, explain why
the default repository transaction is not enough before adding another
annotation.
```
