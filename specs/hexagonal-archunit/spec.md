# Spec — Trilha 1: Arquitetura Hexagonal + ArchUnit

## Contexto (estado real do código)

O repositório se diz Clean Architecture (`entity`, `useCase`, `adapters`,
`config`), mas o código mostra desvios que a migração vai corrigir:

- `useCase` importa `adapters`: `CategoryRegisterDsGateway`,
  `CategoryRegisterInteractor`, `CustomerRegisterDsGateway`,
  `CustomerRegisterInteractor`, `EmailValidationDsGateway`,
  `UserRegisterDsGateway`, `UserRegisterInteractor` usam `*DataMapper`
  (que são `@Entity` JPA) como retorno de gateway. A regra de dependência
  está invertida nesses casos.
- `*DataMapper` não é mapper: é a **entidade JPA** (`@Entity`, `@Table`).
- `*ResponseFormatter` implementa `*Presenter` (porta de saída do use case) mas
  mora em `adapters.persistence.*`, e lança exceções de `config.exception`.
- `useCase.request.*` DTOs importam `jakarta`/`config` (validação, exceções).
- `config` mistura beans de segurança, e-mail e exceções; `SecurityFilter`
  depende de `adapters.persistence.user` e é injetado em `JpaBrand`/`JpaCategory`
  para obter o usuário logado (auditoria).
- Beans são registrados por um scanner manual em `NdlCommerceApplication`
  (`genericApplicationContext`), não só por `@Component`.
- `entity` (hoje) **não** importa Spring/JPA: ótimo ponto de partida.
- Não há ArchUnit, nenhum teste de arquitetura e o CI roda só `mvn verify`.

**Feature escolhida primeiro: Brand.** Motivos vistos no código:
`BrandRegisterDsGateway` já devolve `BrandGatewayResponseDTO` (não a entidade
JPA); `BrandRegisterInteractor` não importa nada de `adapters`; tem 7 métodos
simples (CRUD + `existsBy*`); possui testes (`BrandRegisterInteractorTests`,
`JpaBrandTest`, `BrandControllerTest`, `BrandResponseFormatterTest`,
`CommonBrandTest`, `CommonBrandFactoryImpTest`). Category fica em segundo
lugar porque o gateway vaza a entidade JPA e **não tem testes** (escreva-os
antes de mexer).

## Objetivo

1. Reorganizar o código em **domain / application (ports in/out) / adapters
   (in/out) / config** de forma incremental, uma feature por vez, sem quebrar
   o build.
2. Impedir regressão com **ArchUnit** rodando no `mvn verify` local e no CI.
3. Tratar as violações existentes com `FreezingArchRule` (ou lista de
   exceções) que **só diminui**.

## Arquitetura alvo

```
            +--------------------------------------------+
  HTTP ---> | adapters.in.web  (controller, dto, presen- |
            |                   ter, exception handler)  |
            +----------------------+---------------------+
                                   | usa
                                   v
            +--------------------------------------------+
            | application                                |
            |   port.in   (use cases: interfaces)        |
            |   port.out  (gateways, presenters, user)   |
            |   service   (implementa port.in)           |
            |   dto       (commands / results)           |
            +----------------------+---------------------+
                                   | usa
                                   v
            +--------------------------------------------+
            | domain  (model, factory, enum, exception)  |
            +--------------------------------------------+
                                   ^
            +----------------------+---------------------+
            | adapters.out.persistence.jpa               |
 Postgres<--|   (implementa port.out: entity, repo,      |
            |    mapper, adapter)                        |
            +--------------------------------------------+

  config: monta tudo (beans, segurança, wiring). Pode ver todas as camadas.
  Dependências: adapters -> application -> domain. Nunca o contrário.
```

## De-para de pacotes

| Hoje | Alvo |
|------|------|
| `entity.model.interfaces` / `.implementation` | `domain.model` |
| `entity.factory.*` | `domain.factory` |
| `entity.enums.UserType` | `domain.model` |
| `useCase.*Interactor` | `application.service` |
| `useCase.interfaces.*.*InputBoundary` | `application.port.in` |
| `useCase.interfaces.*.*DsGateway` | `application.port.out` |
| `useCase.interfaces.*.*Presenter` | `application.port.out` |
| `useCase.request.*` (`*RequestDTO`, `*DbRequestDTO`, `*FilterDTO`) | `application.dto` |
| `useCase.request.*` (`*ResponseDTO`, `*GatewayResponseDTO`) | `application.dto` |
| `useCase.model.*` (`PaginatedResult`, `SliceResult`, `OrderCursor`) | `application.model` |
| `adapters.web.*Controller` | `adapters.in.web` |
| `adapters.web.dto`, `adapters.web.cursor` | `adapters.in.web.dto`, `.cursor` |
| `adapters.persistence.*.*ResponseFormatter` | `adapters.in.web.presenter` |
| `adapters.persistence.*.Jpa{X}` (gateway) | `adapters.out.persistence.jpa.{x}` (`{X}PersistenceAdapter`) |
| `adapters.persistence.*.{X}DataMapper` (@Entity) | `adapters.out.persistence.jpa.{x}` (`{X}JpaEntity`) |
| `adapters.persistence.*.Jpa{X}Repository` | `adapters.out.persistence.jpa.{x}` |
| `adapters.presenter.productSku.*` | `adapters.in.web.presenter` |
| `config.exception.Business/InvalidField/EntityAlreadyExists` | `domain.exception` (ou `application.exception`; decida na T1-01) |
| `config.exception.GlobalExceptionHandler` | `adapters.in.web.exception` |
| `config.EmailService`, `EmailSendException` | `adapters.out.email` + porta em `application.port.out` |
| `config.TokenService`, `AuthorizationService`, `SecurityFilter` | `adapters.in.web.security` (+ porta `CurrentUserProvider`) |
| `config.SecurityConfiguration`, `OpenApiConfiguration` | permanece em `config` |

## Escopo

- Mover/renomear pacotes e classes, criar portas, mover validação para onde
  pertence (ex.: Bean Validation no DTO de entrada web).
- Adicionar `archunit-junit5` (escopo test) e testes em
  `src/test/java/com/ndlcommerce/architecture/`.
- Regras: domain sem application/adapters/Spring/JPA; application sem adapters;
  ports são interfaces; `adapters.in` não acessa `adapters.out`; sem ciclos;
  naming.
- Step de CI para as regras e estratégia de congelamento de violações.
- Atualizar `AGENTS.md` e `.github/copilot-instructions.md` ao final (T1-20).

## Fora de escopo

- Mudar regra de negócio, contratos HTTP, migrations ou banco.
- Trocar MapStruct/Lombok, reescrever a segurança JWT.
- Segundo adapter de persistência (trilha 2) e mensageria (trilha 3).
- Migrar para multi-módulo Maven.

## Critérios de aceite

- [ ] `./mvnw verify` verde após **cada** issue (PR pequeno e verde).
- [ ] `com.tngtech.archunit:archunit-junit5` (test) no `pom.xml`.
- [ ] Testes em `com.ndlcommerce.architecture` rodam no `verify`.
- [ ] Regras cobrem: domain puro; application sem adapters; ports interfaces;
      `adapters.in` -> `adapters.out` proibido; sem ciclos; naming.
- [ ] Violações pré-existentes ficam congeladas e a lista **só diminui**.
- [ ] Brand migrado ponta a ponta com regras em modo estrito.
- [ ] Category, Customer, Product/ProductSku, User/Auth migrados.
- [ ] Pacotes `entity` e `useCase` não existem mais; freeze store vazio.
- [ ] CI (`.github/workflows/ci.yml`) falha quando uma regra é violada.
- [ ] `AGENTS.md` descreve a arquitetura hexagonal real.
- [ ] Comportamento HTTP inalterado (testes de controller verdes).

## Riscos e cuidados

- **Scanner manual de beans** (`NdlCommerceApplication`): ao mover pacotes,
  confira o filtro de includes/excludes; `NdlCommerceApplicationTests` e
  `TestConfig` são seu alarme.
- Renomear + mudar lógica no mesmo commit esconde bugs: separe "mover" de
  "mudar".
- Use "Refactor > Move" da IDE para ajustar imports de uma vez.
