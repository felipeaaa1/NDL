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
| `entity.enums.UserType` | `domain` |
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
| `config.exception.Business/InvalidField/EntityAlreadyExists` | `application.exception` (ADR 0001; revisão final pendente) |
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
- Migrar o NDL para Java 25 como pré-requisito técnico da rede de proteção,
  alinhando Maven, CI, Docker e documentação relacionada.
- Atualizar `AGENTS.md` e `.github/copilot-instructions.md` ao final (T1-20).

## Regras para o pré-requisito Java 25

- Usar uma versão de Spring Boot 3 com suporte a Java 25 e garantir a
  compatibilidade dos annotation processors e do formatador com essa versão
  do Java. A escolha de versões e a decomposição em tarefas ficam para a
  etapa posterior.
- Alinhar compilação e execução Maven, ambiente do CI, imagens de build e
  execução Docker e documentação relacionada ao Java 25.
- Preservar contratos HTTP, autenticação e autorização JWT, regras de negócio
  e limites arquiteturais; a migração tecnológica não autoriza alterações
  funcionais nem amplia o escopo das trilhas 2 e 3.
- Manter os experimentos da trilha 3 sobre recursos introduzidos no Java 21,
  sem duplicar ou replanejar seu escopo nesta trilha.

### Estado da migração Java 25

O alinhamento estático para Spring Boot 3.5.6 e Java 25 foi aplicado em Maven,
CI, Docker e documentação. Java 25.0.4.1 e Maven 3.9.11 foram confirmados,
mas a migração ainda não está validada nem T1-02 concluída: a resolução do
parent Spring Boot 3.5.6 falhou com HTTP 403 no mirror antes da compilação.
Não há resultados de `clean verify`, `spotless:check` ou do teste focal;
nenhum experimento negativo de ArchUnit foi iniciado. Docker não estava
disponível no `PATH`. Portanto, os critérios de aceite permanecem pendentes.

O congelamento de violações é uma tarefa posterior e não é pré-requisito para
T1-02.

## Fora de escopo

- Mudar regra de negócio, contratos HTTP, migrations ou banco.
- Trocar MapStruct/Lombok, reescrever a segurança JWT.
- Segundo adapter de persistência (trilha 2) e mensageria (trilha 3).
- Migrar para multi-módulo Maven.

## Critérios de aceite

- [ ] Migração para Java 25 demonstrada com Spring Boot 3 suportado,
      annotation processors e formatador compatíveis; Maven, CI, Docker e
      documentação relacionada alinhados.
- [ ] Em Java 25, `./mvnw clean verify`, `./mvnw spotless:check` e
      `./mvnw -Dtest=ArchitectureSmokeTest test` passam, com evidências.
- [ ] O CI executa `clean verify` em Java 25 com sucesso comprovado.
- [ ] Uma violação intencional da regra de `ArchitectureSmokeTest` provoca
      falha verificável; o experimento é restaurado e o teste volta a passar.
- [ ] Build e execução da imagem Docker alinhada são verificados. Se Docker
      estiver indisponível, registrar a verificação como pendente, sem
      alegar aprovação.
- [ ] Evidências de testes confirmam a preservação dos contratos HTTP,
      autenticação e autorização JWT, regras de negócio e proteção arquitetural.
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

## Decisões do domain (T1-06)

- **Lombok no domain: não permitido.** O domain é escrito à mão (getters,
  construtores, `equals`/`hashCode` quando necessários). Motivo: o Lombok gera
  código em tempo de compilação, o que esconde o contrato do domínio e o
  acopla a uma ferramenta; além disso o ArchUnit analisa bytecode e **não
  consegue** impedir o uso (as anotações não ficam no `.class`), então a regra
  é convenção documentada e verificada em code review. Adapters e DTOs de
  adapter continuam podendo usar Lombok (fora de escopo trocar).
- **`UserType`** fica em `com.ndlcommerce.domain` (raiz), não em
  `domain.model`. Controllers, DTOs e adapters importam do domain; o domain
  nunca importa de volta.
- As regras ArchUnit de `domain` são **estritas** (sem freeze e sem
  `allowEmptyShould`): `domain` não depende de `application`/`useCase`,
  `adapters`, `config`, `org.springframework..`, `jakarta..` nem Jackson.

### Débito anotado (não alterar na T1-06)

- **`Common*` + `interfaces`** (ex.: `Brand`/`CommonBrand`): cada interface do
  domain tem uma única implementação. Decisão: colapsar em uma classe concreta
  por conceito, em issue própria, depois da migração de pacotes.
- **`*Factory`**: decisão de **remover** os factories e criar os objetos por
  construtor ou método estático no próprio model (ex.: `Brand.create(...)`),
  com a validação no domain. Antes, conferir se algum factory
  (`CommonProductFactoryImp`, `CommonUserFactoryImp`) tem regra além de `new`;
  se tiver, a regra migra para o model. Remover os factories também elimina o
  `@ComponentScan` manual de `domain.factory` em `NdlCommerceApplication`.
- **Cuidado:** a mudança toca domain, interactors, adapters e testes. Exige
  testes de caracterização do domain antes e não deve ser misturada com
  "só mover".

## Riscos e cuidados

- Incompatibilidades de Spring Boot, annotation processors ou formatador com
  Java 25 podem impedir o build ou invalidar a rede de proteção.
- Docker indisponível impede comprovar a imagem e deixa essa verificação
  pendente até haver ambiente adequado.
- **Scanner manual de beans** (`NdlCommerceApplication`): ao mover pacotes,
  confira o filtro de includes/excludes; `NdlCommerceApplicationTests` e
  `TestConfig` são seu alarme.
- Renomear + mudar lógica no mesmo commit esconde bugs: separe "mover" de
  "mudar".
- Use "Refactor > Move" da IDE para ajustar imports de uma vez.
