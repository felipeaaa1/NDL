# Issues — Trilha 1: Hexagonal + ArchUnit

Ordem = ordem de execução. Cada issue é um PR pequeno. Convenções em
`specs/README.md`. Pacote base: `com.ndlcommerce` (`src/main/java/...`).
Comando padrão de verificação: `./mvnw verify` (Windows: `mvnw.cmd verify`).

```
Fase 1: rede de proteção   T1-01 .. T1-06
Fase 2: Brand (fatia)      T1-07 .. T1-13
Fase 3: demais features    T1-14 .. T1-20
```

---

## [TRILHA-1] T1-01 — Decidir o alvo e registrar o de-para (ADR)

**Necessidade/contexto:** sem decisão escrita, cada refactor vira opinião. O
de-para inicial está em `specs/hexagonal-archunit/spec.md`.

**Objetivo:** fechar decisões abertas num ADR curto.

**O que você vai aprender:** diferença entre Clean e Hexagonal na prática;
o que é porta in x out; escrever decisão com trade-offs.

**Tarefas:**
- [x] Criar `docs/adr/0001-arquitetura-hexagonal.md` (contexto, decisão,
      alternativas, consequências).
- [x] Decidir onde ficam as exceções (`domain.exception` x
      `application.exception`) e justificar.
- [x] Decidir nomes: `*PersistenceAdapter`, `*JpaEntity`, `*UseCase` ou
      manter `*InputBoundary` em `port.in`.
- [x] Decidir se o Presenter continua como porta de saída ou se o service
      passa a devolver resultado e o controller formata.

**Dicas/perguntas:**
- Hoje o `Presenter` lança exceções HTTP-like (`BusinessException`). Quem deve
  conhecer "erro de negócio": domain, application ou adapter web?
- Em hexagonal, quem "dirige" (driving) e quem "é dirigido" (driven)? Em qual
  lado está o controller? E o `JpaBrand`?
- Em que a regra de dependência de Clean e Hexagonal é igual? O que muda?

**Arquivos prováveis:** `docs/adr/0001-...md` (novo), consulta a `AGENTS.md`.

**Critério de pronto:** ADR no PR; revisão pelo mentor
(`guanabara`) sem pendências. Verificável: arquivo existe e responde às 4
decisões.

**Status/evidências — QA independente Carla, 2026-11-17:** as quatro tarefas
de ADR/decisões estão concluídas; PR #26 com merge confirmado por squash
`6268cbf`. As alternativas já decididas pelo usuário não são reabertas.
- [x] Revisão do mentor (`guanabara`) sem pendências, realizada nesta
      conversa em 2026-11-17 contra a T1-01 e a spec atual: alvo/de-para
      Hexagonal com dependências para dentro; exceções em
      `application.exception`, sem dependência do domínio nem status HTTP;
      nomes de portas, adapters e entidades JPA definidos; Presenter mantido
      como porta de saída, implementado no adapter web. Parecer: ADR aprovado.
      A seção curta de alternativas foi preservada por decisão do usuário.
      A consulta anterior `reviews: []` não registra esta revisão
      conversacional; não se atribui aprovação ao GitHub ou ao merge.
      Esta aprovação documental não valida a implementação nem altera
      o bloqueio HTTP 403 e as pendências da T1-02.

**Dependências:** nenhuma.
**Labels:** `trilha-1`, `arquitetura`, `docs`.

---

## Pré-requisito técnico — Migrar para Java 25 antes da validação T1-02

Esta sequência é um gate técnico anterior à T1-02; não cria nem renumera
issues. Execute as tarefas em ordem e mantenha a regra de fumaça da T1-02
simples: a migração de plataforma não deve antecipar regras arquiteturais.

**Objetivo:** alinhar build, CI, Docker e documentação a Java 25, preservando
contratos HTTP, JWT, regras de negócio e as trilhas 2 e 3.

**Tarefas:**
- [ ] Atualizar a plataforma e compilação em `pom.xml`: Spring Boot `3.5.6`,
      `java.version`/`release` `25` e compiler plugin herdado na versão
      `3.14.0`; manter os annotation processors existentes e confirmar
      Lombok `1.18.40` gerenciado. Não fixar um plugin antigo nem remover
      processadores.
  - [x] Implementação estática: parent Boot `3.5.6`, Java/release `25`,
        compiler herdado e annotation processors preservados no POM.
  - [ ] Confirmar versões efetivas do compiler e Lombok após resolver o parent.
- [ ] Alinhar dependências e plugins que precisam acompanhar a plataforma em
      `pom.xml`: Spotless `3.0.0` com Google Java Format `1.30.0`, Springdoc
      `2.8.9` e Flyway PostgreSQL/plugin em `11.7.2` gerenciado. Manter
      Hypersistence `hibernate-63:3.8.3`, JaCoCo `0.8.14` e ArchUnit `1.5.1`;
      não atualizar dependências sem relação com a compatibilidade.
  - [x] Implementação estática: Spotless `3.0.0`, Google Java Format `1.30.0`,
        Springdoc `2.8.9` e Flyway sob gerenciamento; demais declarações
        preservadas conforme QA.
  - [ ] Confirmar versões efetivamente resolvidas, inclusive Flyway gerenciado.
- [ ] Com JDK 25 disponível, validar a configuração Maven local antes da
      T1-02: `./mvnw clean verify` e `./mvnw spotless:check` (Windows:
      `mvnw.cmd clean verify` e `mvnw.cmd spotless:check`). Se o ambiente não
      tiver JDK 25, registrar o bloqueio; não instalar nem alterar configuração
      global da máquina como parte desta trilha.
- [x] Atualizar `.github/workflows/ci.yml` para JDK 25; executar Maven pelo
      Wrapper e incluir `spotless:check`, mantendo `clean verify`. Conferir a
      ordem e os comandos do workflow.
- [ ] Atualizar as duas etapas do `Dockerfile` para Eclipse Temurin 25
      (JDK no build e JRE na execução), inclusive os metadados que declaram a
      versão. Validar com `docker build`; se Docker estiver indisponível,
      registrar a evidência como pendente, sem declarar aprovação.
  - [x] Implementação estática: duas etapas e metadados em Temurin 25.
  - [ ] Validar build da imagem; Docker ausente do `PATH` no ambiente de QA.
- [x] Atualizar referências à versão da stack em `README.md`, `AGENTS.md` e
      documentação de setup relacionada (incluindo instruções do Copilot, se
      mencionarem a versão). Limitar esta tarefa às referências tecnológicas:
      a atualização arquitetural desses documentos continua na T1-20. Preservar
      as referências a Java 21 que descrevem deliberadamente os experimentos
      da trilha 3.
- [ ] Depois de concluídas as tarefas acima, executar a T1-02 em Java 25 sem
      ampliar sua regra simples. Com `ArchitectureSmokeTest` criado, comprovar
      `./mvnw -Dtest=ArchitectureSmokeTest test`, repetir
      `./mvnw clean verify` e `./mvnw spotless:check`, provocar a falha
      intencional prevista na T1-02, restaurar a regra e confirmar que o teste
      volta a passar. Registrar os resultados do CI Java 25 e do build Docker
      (ou o bloqueio documentado).

**Status — 2026-11-17:** migração de plataforma aplicada, não validada.
Os itens combinados acima permanecem abertos quando dependem de resolução
efetiva ou execução; os subitens distinguem a implementação estática concluída.
Evidências e bloqueios de execução estão registrados na T1-02 abaixo.

**Arquivos prováveis:** `pom.xml`, `.github/workflows/ci.yml`, `Dockerfile`,
`README.md`, `AGENTS.md` e documentação de setup relacionada.

**Critério de pronto:** Maven local e CI usam Java 25; `clean verify` e
`spotless:check` passam; o teste de fumaça da T1-02 passa após o experimento
de falha e restauração; build Docker comprovado ou pendência registrada; as
referências de stack estão alinhadas. Nenhum contrato HTTP/JWT ou
comportamento funcional foi alterado.

**Fora do escopo:** instalar/configurar Java globalmente, alterar banco ou
executar Flyway, criar/editar migrations, refatorar arquitetura, alterar
contratos ou ampliar as trilhas 2 e 3.

**Referências oficiais:** [requisitos de sistema do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html),
[Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html),
[Spotless Maven](https://github.com/diffplug/spotless/tree/main/plugin-maven),
[GitHub setup-java](https://github.com/actions/setup-java) e
[Eclipse Temurin no Docker Hub](https://hub.docker.com/_/eclipse-temurin).

---

## [TRILHA-1] T1-02 — Adicionar ArchUnit e um teste de fumaça

**Necessidade/contexto:** originalmente o `pom.xml` não tinha ArchUnit;
a dependência e o teste de fumaça agora estão implementados, com validação
bloqueada conforme as evidências abaixo.

**Objetivo:** dependência funcionando com uma regra trivial.

**O que você vai aprender:** `ClassFileImporter`, `@AnalyzeClasses`,
`@ArchTest`, escopo `test`, importação sem testes (`DoNotIncludeTests`).

**Tarefas:**
- [x] Adicionar `com.tngtech.archunit:archunit-junit5` com `<scope>test`
      (confira a última versão estável no Maven Central).
- [x] Criar `src/test/java/com/ndlcommerce/architecture/ArchitectureSmokeTest`.
- [x] Declarar uma regra simples: "nenhuma classe depende de `java.util.logging`",
      usando a regra padrão `NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING`.
- [ ] Executar a regra e vê-la passar.
- [ ] Depois forçar falha e ler a mensagem; restaurar e comprovar nova passagem.
- [x] Importar só `com.ndlcommerce` e excluir testes.

**Dicas/perguntas:**
- `@AnalyzeClasses(packages = "...", importOptions = ...)` x `ArchRuleDefinition`
  em método JUnit comum: qual a diferença de ergonomia?
- Por que o ArchUnit analisa bytecode e não código-fonte?
- Quanto tempo a análise adiciona ao `verify`?

**Arquivos prováveis:** `pom.xml`, `src/test/java/com/ndlcommerce/architecture/`.

**Critério de pronto:** `./mvnw -Dtest=ArchitectureSmokeTest test` verde;
alterando a regra para algo falso ele falha com mensagem legível.

**Status:** implementada; validação bloqueada. Não declarar T1-02 concluída.

**Evidências — QA independente Carla, 2026-11-17:**
- Inspeção estática confirmou `archunit-junit5:1.5.1` em escopo `test`,
  `ArchitectureSmokeTest`, a regra padrão
  `NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING` e
  `@AnalyzeClasses` para `com.ndlcommerce` com `DoNotIncludeTests`.
  A versão declarada não equivale a uma consulta de última versão estável.
- Pré-requisito Java 25: POM, CI com Wrapper/`spotless:check`, Docker Temurin 25
  e documentação de stack (incluindo README e AGENTS) atualizados estaticamente.
  XML e `git diff --check` aprovados no QA; não comprovam compilação ou testes.
- Tentativas no QA: `.\mvnw.cmd clean verify`,
  `.\mvnw.cmd spotless:check` e
  `.\mvnw.cmd -Dtest=ArchitectureSmokeTest test`.
  Todas bloqueadas na leitura do projeto: mirror Maven retornou HTTP `403`
  ao resolver o parent `org.springframework.boot:spring-boot-starter-parent:3.5.6`,
  antes da compilação e sem executar as verificações solicitadas.
  Não foi possível confirmar versões efetivas herdadas/gerenciadas.
- Experimento negativo não iniciado; passagem inicial e após restauração
  pendentes. CI Java 25 verde não confirmado; build Docker pendente porque
  `docker` está ausente do `PATH`. Regressão funcional/HTTP/JWT não verificada.
- Este registro utiliza as evidências fornecidas pelo QA; nenhum comando de
  teste/build foi reexecutado nesta atualização documental.
- Regras-alvo, freeze e ciclos pertencem às próximas issues; não são bloqueios
  antecipados da T1-02. T1-03 a T1-20 permanecem inalteradas.

**Dependências:** pré-requisito técnico Java 25 acima.
**Labels:** `trilha-1`, `archunit`, `testes`.

---

## [TRILHA-1] T1-03 — Escrever as regras-alvo e medir as violações atuais

**Necessidade/contexto:** o código atual viola a regra de dependência (ex.:
`useCase` -> `adapters.persistence.*DataMapper`). Antes de corrigir, é preciso
medir.

**Objetivo:** ter as regras-alvo escritas, executadas e as violações listadas.

**O que você vai aprender:** `layeredArchitecture()`,
`noClasses().that().resideInAPackage(..).should().dependOnClassesThat()`,
`onionArchitecture()`, relatório de violações.

**Tarefas:**
- [ ] Criar `HexagonalRulesTest` com as regras na **nomenclatura alvo**
      (`..domain..`, `..application..`, `..adapters.in..`, `..adapters.out..`).
- [ ] Rodar contra o estado atual (como os pacotes ainda são `entity` /
      `useCase`, escreva uma versão "atual" das mesmas ideias) e copiar o
      relatório de violações para a descrição do PR.
- [ ] Contar violações por tipo (useCase->adapters, DTO->jakarta, etc.).

**Dicas/perguntas:**
- `layeredArchitecture().consideringOnlyDependenciesInLayers()` — o que muda?
- Uma regra "domain não usa Spring" precisa de `..org.springframework..` e
  `..jakarta.persistence..`; e Lombok?
- Dá para escrever regras para pacotes que ainda **não existem**? O que o
  ArchUnit faz com "pacote vazio" (`allowEmptyShould`)?

**Arquivos prováveis:** `src/test/java/com/ndlcommerce/architecture/`.

**Critério de pronto:** teste roda e **falha** listando as violações (esperado
nesta issue); lista anexada ao PR.

**Dependências:** T1-02.
**Labels:** `trilha-1`, `archunit`, `arquitetura`.

---

## [TRILHA-1] T1-04 — Estratégia de violações existentes (freeze que só diminui)

**Necessidade/contexto:** o build não pode ficar vermelho durante a migração,
mas **novas** violações precisam ser barradas.

**Objetivo:** congelar as violações atuais com `FreezingArchRule`.

**O que você vai aprender:** `FreezingArchRule.freeze(rule)`,
`archunit.properties`, `freeze.store.default.path`, `freeze.refreeze`,
`freeze.store.default.allowStoreUpdate`; alternativa com lista de exceções
(`.and(not(exceptions))`).

**Tarefas:**
- [ ] Envolver as regras da T1-03 com `FreezingArchRule.freeze`.
- [ ] Criar `src/test/resources/archunit.properties` apontando o store para
      `src/test/resources/archunit_store` (commitado).
- [ ] Gerar o store inicial (primeira execução) e commitar.
- [ ] Documentar a política em `docs/arquitetura-violacoes.md`: o store só
      pode **encolher**; PR que o aumenta é rejeitado.
- [ ] Provar: criar uma violação nova -> build falha; corrigir uma antiga ->
      linha some do store (`freeze.refreeze`/ `allowStoreUpdate`).
- [ ] Comparar com a abordagem de lista de exceções e anotar prós/contras.

**Dicas/perguntas:**
- O que identifica uma violação no store: linha de código ou descrição? O que
  acontece ao mover a classe de pacote?
- No CI, `allowStoreUpdate` deveria ser `true` ou `false`? Por quê?
- Como saber se o store diminuiu? (diff do arquivo no PR)

**Arquivos prováveis:** `src/test/resources/archunit.properties`,
`src/test/resources/archunit_store/*`, `docs/arquitetura-violacoes.md`.

**Critério de pronto:** `./mvnw verify` verde; adicionar de propósito um
`import com.ndlcommerce.adapters...` em `useCase` novo faz o teste falhar.

**Dependências:** T1-03.
**Labels:** `trilha-1`, `archunit`, `arquitetura`.

---

## [TRILHA-1] T1-05 — Rodar as regras de arquitetura no CI

**Necessidade/contexto:** `.github/workflows/ci.yml` já roda
`mvn -DskipTests=false -B clean verify`, o que **já executaria** os testes do
ArchUnit. Falta visibilidade e garantia de que o store não foi alterado
silenciosamente.

**Objetivo:** CI com step dedicado e falha clara.

**O que você vai aprender:** steps nomeados, `-Dtest=` por pacote,
upload de relatório, proteger o store com `git diff --exit-code`.

**Tarefas:**
- [ ] Adicionar step "Architecture rules" antes do build completo, rodando
      `mvn -B -Dtest='com.ndlcommerce.architecture.*' test` (falha cedo).
- [ ] Garantir `freeze.store.default.allowStoreUpdate=false` no CI (via
      `-Darchunit.freeze.store.default.allowStoreUpdate=false`).
- [ ] Step final: `git diff --exit-code src/test/resources/archunit_store`.
- [ ] Incluir `target/surefire-reports` das regras no artefato existente.
- [ ] Abrir PR de teste que viola uma regra e ver o CI vermelho.

**Dicas/perguntas:**
- Se o step já roda dentro do `verify`, por que separar? (feedback rápido,
  mensagem clara).
- Propriedades `archunit.*` podem ser passadas como system properties?
  Confira na documentação.
- Branch protection: marcar o check como obrigatório (configuração do GitHub).

**Arquivos prováveis:** `.github/workflows/ci.yml`.

**Critério de pronto:** PR com violação nova fica vermelho no Actions;
PR sem violação fica verde. Local: `./mvnw -B -Dtest='com.ndlcommerce.architecture.*' test`.

**Dependências:** T1-04.
**Labels:** `trilha-1`, `ci`, `archunit`.

---

## [TRILHA-1] T1-06 — Migrar `entity` para `domain` (só mover)

**Necessidade/contexto:** `entity` já é puro (sem Spring/JPA/adapters), então
é a migração mais barata e dá a primeira regra **estrita** vitoriosa.

**Objetivo:** `entity.*` -> `domain.*` sem mudar comportamento.

**O que você vai aprender:** refactor seguro com IDE, git mv preservando
histórico, regra estrita por pacote.

**Tarefas:**
- [ ] Mover `entity.model.*` -> `domain.model`; `entity.factory.*` ->
      `domain.factory`; `entity.enums.UserType` -> `domain.model`.
- [ ] Mover os testes espelhados (`src/test/.../entity/**`).
- [ ] Conferir o scanner manual em `NdlCommerceApplication` (filtros por
      nome/pacote).
- [ ] Remover a regra de `domain` do freeze e deixá-la **estrita**:
      sem `application`, `adapters`, `config`, `org.springframework..`,
      `jakarta..`.
- [ ] Decidir sobre Lombok em domain (permitido? documente).

**Dicas/perguntas:**
- Por que `Common*` + `interfaces` (Brand/CommonBrand)? Faz sentido em
  hexagonal ou pode virar uma classe só? (não mude agora; anote como débito).
- Os `*Factory` são domínio ou aplicação? O que eles precisam conhecer?

**Arquivos prováveis:** `entity/**` -> `domain/**`; testes em
`src/test/java/com/ndlcommerce/entity/**`.

**Critério de pronto:** `./mvnw verify` verde; regra estrita de domain
passa sem congelamento.

**Dependências:** T1-04 (T1-05 recomendada).
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-07 — Brand: rede de proteção antes de mexer

**Necessidade/contexto:** Brand foi escolhida por ser a mais simples: gateway
já retorna `BrandGatewayResponseDTO`, interactor sem imports de `adapters`,
~120 linhas, controller de ~86. Mas `JpaBrand` usa `SecurityFilter` (config).

**Objetivo:** garantir que o comportamento esteja coberto antes do refactor.

**O que você vai aprender:** characterization tests, onde há lacuna de
cobertura (Jacoco já está no `pom.xml`).

**Tarefas:**
- [ ] Rodar `./mvnw verify` e abrir `target/site/jacoco/index.html`; anotar a
      cobertura de Brand (interactor, controller, JpaBrand, formatter).
- [ ] Listar cenários sem teste (duplicado, update com mesmo nome, delete
      inexistente, filtro, paginação).
- [ ] Escrever os testes que faltarem **sem** alterar o código de produção.
- [ ] Mapear no PR todas as classes e testes de Brand (lista de arquivos).

**Dicas/perguntas:**
- Teste de caracterização documenta o que o código **faz**, não o que
  **deveria**. Se achar bug, abra issue; não corrija aqui.
- Qual teste cobre o contrato HTTP (status e corpo de erro)?

**Arquivos prováveis:** `src/test/java/com/ndlcommerce/useCase/BrandRegisterInteractorTests`,
`adapters/web/BrandControllerTest`, `adapters/persistence/brand/JpaBrandTest`.

**Critério de pronto:** `./mvnw verify` verde e relatório Jacoco anexado
mostrando cobertura de Brand.

**Dependências:** T1-06.
**Labels:** `trilha-1`, `testes`.

---

## [TRILHA-1] T1-08 — Brand: criar `application.port.in` e `port.out`

**Necessidade/contexto:** hoje `BrandInputBoundary`, `BrandPresenter` e
`BrandRegisterDsGateway` vivem em `useCase.interfaces.brand`.

**Objetivo:** mover para `application.port.in` (casos de uso) e
`application.port.out` (gateway e presenter).

**O que você vai aprender:** porta de entrada x saída, "driving x driven",
nomear portas pelo **papel** e não pela tecnologia.

**Tarefas:**
- [ ] `BrandInputBoundary` -> `application.port.in` (considere renomear para
      `BrandUseCase`, conforme ADR T1-01).
- [ ] `BrandRegisterDsGateway`, `BrandPresenter` -> `application.port.out`.
- [ ] Atualizar imports (controller, interactor, `JpaBrand`, formatter, testes).
- [ ] Verificar se o nome `DsGateway` ainda faz sentido (ex.:
      `BrandRepositoryPort`).

**Dicas/perguntas:**
- A porta `out` de persistência deve falar em termos de **domínio** ou de
  DTO? O que o Mongo (trilha 2) precisaria receber?
- `PaginatedResult` em `useCase.model` é conceito de aplicação — ok na porta?

**Arquivos prováveis:** `useCase/interfaces/brand/*`, `useCase/BrandRegisterInteractor`,
`adapters/web/BrandController`, `adapters/persistence/brand/*`.

**Critério de pronto:** `./mvnw verify` verde; nenhuma referência a
`useCase.interfaces.brand` no repositório (`grep`).

**Dependências:** T1-07.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-09 — Brand: mover service e DTOs para `application`

**Necessidade/contexto:** `BrandRegisterInteractor` e `useCase.request.brand.*`
(`BrandRequestDTO`, `BrandDbRequestDTO`, `BrandFilterDTO`,
`BrandGatewayResponseDTO`, `BrandResponseDTO`). `BrandRequestDTO` importa
dependências de `jakarta`/`config` (verifique).

**Objetivo:** `application.service.BrandService` e `application.dto.brand`,
sem `jakarta` nem `config` em `application`.

**O que você vai aprender:** onde fica a validação (borda x núcleo), DTO de
entrada web x command de aplicação.

**Tarefas:**
- [ ] Mover o interactor para `application.service`.
- [ ] Mover os DTOs para `application.dto`.
- [ ] Remover anotações `jakarta.validation` do que ficar em `application`:
      criar `BrandWebRequest` em `adapters.in.web.dto` com as anotações e
      converter para o command.
- [ ] Remover dependência de `config.exception` conforme decisão da T1-01.

**Dicas/perguntas:**
- Se o DTO web valida e o service também valida regras de negócio, qual é a
  diferença entre "formato inválido" e "regra violada"?
- Dá para a conversão ser feita sem MapStruct? E com? Qual o custo agora?

**Arquivos prováveis:** `useCase/BrandRegisterInteractor`,
`useCase/request/brand/*`, `adapters/web/BrandController`.

**Critério de pronto:** `./mvnw verify` verde; regra "application sem
jakarta/adapters/config" passa **para Brand** (ver T1-13).

**Dependências:** T1-08.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-10 — Brand: isolar o usuário logado atrás de uma porta

**Necessidade/contexto:** `JpaBrand` recebe `SecurityFilter` (`config`) e
`UserDataMapper` (entidade JPA de outro agregado) só para preencher
`createdBy`/`updatedBy`. Isso acopla persistência a segurança e impediria um
adapter Mongo (trilha 2).

**Objetivo:** criar `application.port.out.CurrentUserProvider` (ex.:
`UUID currentUserId()`), implementado em `adapters.in.web.security`.

**O que você vai aprender:** inversão de dependência para aspectos
transversais; por que "quem sou eu" é uma porta.

**Tarefas:**
- [ ] Definir a porta (retorna só o necessário, não `UserDataMapper`).
- [ ] Implementar com o `SecurityContextHolder`/`SecurityFilter` atual.
- [ ] Trocar `JpaBrand` para depender da porta.
- [ ] Ajustar testes (mock da porta, sem SecurityContext).

**Dicas/perguntas:**
- Quem deve chamar a porta: o adapter de persistência ou o service e
  repassar o id no DTO de gravação? Pense em testes e em threads (trilha 3).
- Retornar `Optional`? O que acontece em rotas públicas?

**Arquivos prováveis:** `config/SecurityFilter`,
`adapters/persistence/brand/JpaBrand`, novo
`application/port/out/CurrentUserProvider`.

**Critério de pronto:** `JpaBrand` sem import de `config`/`user`
(`grep`); `./mvnw -Dtest='*Brand*' test` verde.

**Dependências:** T1-08.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-11 — Brand: mover persistência para `adapters.out.persistence.jpa`

**Necessidade/contexto:** `BrandDataMapper` é `@Entity`; `JpaBrand` implementa
o gateway; `JpaBrandRepository` é Spring Data.

**Objetivo:** pacote `adapters.out.persistence.jpa.brand` com
`BrandJpaEntity`, `BrandPersistenceAdapter`, `BrandJpaRepository` e mapper
entity<->DTO.

**O que você vai aprender:** adapter de saída; separar entidade JPA de modelo;
nomear por papel.

**Tarefas:**
- [ ] Renomear `BrandDataMapper` -> `BrandJpaEntity` (mantendo `@Table`).
- [ ] Renomear `JpaBrand` -> `BrandPersistenceAdapter` implementando a porta.
- [ ] Mover o repositório; conferir `@EnableJpaRepositories`/entity scan.
- [ ] Verificar que `JpaProduct*` ainda compila (referencia Brand).
- [ ] Conferir que nada fora de `adapters.out` importa `BrandJpaEntity`.

**Dicas/perguntas:**
- Preciso de um mapper entity<->DTO? Onde fica (mesmo pacote)?
- O `@Entity` scan encontra o novo pacote? Como confirmar?
- Mudar o nome de classe muda o nome da tabela? (veja `@Table`).

**Arquivos prováveis:** `adapters/persistence/brand/*`,
`adapters/persistence/product/*` (referências), testes `JpaBrandTest`.

**Critério de pronto:** `./mvnw verify` verde; `JpaBrandTest` (renomeado) verde.

**Dependências:** T1-08, T1-10.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-12 — Brand: adapter in (web + presenter) e wiring em `config`

**Necessidade/contexto:** `BrandController` em `adapters.web` e
`BrandResponseFormatter` (presenter) dentro de `adapters.persistence.brand`
— lugar errado: formata resposta, não persiste.

**Objetivo:** `adapters.in.web` (controller, dto, presenter) e criação dos
beans/wiring em `config`.

**O que você vai aprender:** adapter de entrada; composition root; como o
scanner manual registra beans.

**Tarefas:**
- [ ] Mover `BrandController` -> `adapters.in.web`.
- [ ] Mover `BrandResponseFormatter` -> `adapters.in.web.presenter`.
- [ ] Controller depende só da porta `in` (nunca do service concreto).
- [ ] Revisar como o service é instanciado (scanner manual): o service em
      `application` não deve ter `@Component`; o wiring fica em `config`
      (`@Configuration` com `@Bean`) ou no scanner — decida e documente.
- [ ] Rodar `NdlCommerceApplicationTests` e o `BrandControllerTest`.

**Dicas/perguntas:**
- Hoje, por que o service precisa ser descoberto pelo scanner e não por
  `@Service`? Qual é o ganho de manter `application` sem Spring?
- ArchUnit deve proibir `org.springframework` em `application`? Quais
  exceções (ex.: `Page`)?

**Arquivos prováveis:** `NdlCommerceApplication`, `config/*`,
`adapters/web/BrandController`, `adapters/persistence/brand/BrandResponseFormatter`.

**Critério de pronto:** `./mvnw verify` verde e endpoints de Brand respondem
igual (testes de controller sem alteração de expectativas).

**Dependências:** T1-09, T1-11.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-13 — Regras estritas: ports, naming, in x out e ciclos

**Necessidade/contexto:** Brand está migrado; é hora de travar o que
aprendeu para impedir regressão e servir de molde às outras features.

**Objetivo:** regras estritas e específicas, tirando Brand do freeze.

**O que você vai aprender:** `classes().that().resideInAPackage(..)
.should().beInterfaces()`, `haveSimpleNameEndingWith`, `slices()
.matching("..adapters.(*)..").should().beFreeOfCycles()`.

**Tarefas:**
- [ ] Regra: classes em `..port.in..` e `..port.out..` são interfaces.
- [ ] Regra: `..application..` não depende de `..adapters..`, `..config..`,
      `jakarta..`, `org.springframework..` (listar exceções explícitas).
- [ ] Regra: `..adapters.in..` não depende de `..adapters.out..`.
- [ ] Regra: `..adapters.out..` não depende de `..adapters.in..`.
- [ ] Regra: sem ciclos entre fatias (`slices`).
- [ ] Naming: `*Controller` em `adapters.in.web`; `*PersistenceAdapter` em
      `adapters.out`; `*JpaEntity` com `@Entity`; `*Service` em
      `application.service`.
- [ ] Remover do store as violações de Brand e confirmar que o store diminuiu.

**Dicas/perguntas:**
- `adapters.in` pode usar DTOs de `application`? E `adapters.in.web`
  usando `adapters.out` indiretamente via `config`? Onde está a linha?
- Mensagem de falha: use `.because("...")` para ensinar quem violar.

**Arquivos prováveis:** `src/test/java/com/ndlcommerce/architecture/*`,
`src/test/resources/archunit_store/*`.

**Critério de pronto:** `./mvnw -Dtest='com.ndlcommerce.architecture.*' test`
verde; diff do store só com linhas **removidas**.

**Dependências:** T1-12.
**Labels:** `trilha-1`, `archunit`, `arquitetura`.

---

## [TRILHA-1] T1-14 — Migrar Category

**Necessidade/contexto:** `CategoryRegisterDsGateway` retorna
`CategoryDataMapper` (**@Entity**) e o interactor importa
`adapters.persistence.category.CategoryDataMapper`: violação real, e não há
teste de Category.

**Objetivo:** mesmo caminho de Brand, **corrigindo o vazamento** com
`CategoryGatewayResponseDTO` (ou modelo de domínio).

**O que você vai aprender:** vazamento de entidade JPA e como corrigi-lo;
testar antes de refatorar.

**Tarefas:**
- [ ] Escrever testes de interactor e controller de Category (caracterização).
- [ ] Criar o DTO/modelo de retorno da porta; remover `CategoryDataMapper` de
      `application`.
- [ ] Mover portas, service, DTOs, adapter, entity e controller (checklist de
      T1-08..T1-12).
- [ ] Tratar `parentId` e `PaginatedResult` na porta sem expor JPA.
- [ ] Remover do store as violações de Category.

**Dicas/perguntas:**
- O `Presenter` de Category usa `LocalDateTime` formatado: formatar é
  responsabilidade de quem?
- `getCreatedAt().toString()` no service: dado de apresentação no núcleo?

**Arquivos prováveis:** `useCase/CategoryRegisterInteractor`,
`useCase/interfaces/category/*`, `adapters/persistence/category/*`,
`adapters/web/CategoryController`.

**Critério de pronto:** `./mvnw verify` verde; store sem linhas de Category.

**Dependências:** T1-13.
**Labels:** `trilha-1`, `refactor`, `arquitetura`, `testes`.

---

## [TRILHA-1] T1-15 — Migrar Customer

**Necessidade/contexto:** `CustomerRegisterDsGateway`/`Interactor` importam
`adapters`; `CustomerRequestDTO` importa 2 pacotes proibidos; existe
`CustomCustomerRepository`/`Impl` (repositório customizado Spring Data).

**Objetivo:** Customer hexagonal, mesmo roteiro.

**O que você vai aprender:** repositório customizado como detalhe de adapter;
DTO de entrada x command.

**Tarefas:**
- [ ] Caracterizar com testes (`CustomerRegisterInteractorTest` existe).
- [ ] Portas in/out, service, DTOs, controller, presenter.
- [ ] Mover `CustomCustomerRepository(Impl)`/`JpaCustomer*` para
      `adapters.out.persistence.jpa.customer`.
- [ ] Atualizar store.

**Dicas/perguntas:**
- Customer usa `UserDataMapper`? Que agregado o possui? Referencie por id,
  não por entidade.

**Arquivos prováveis:** `useCase/CustomerRegisterInteractor`,
`adapters/persistence/customer/*`, `adapters/web/CustomerController`.

**Critério de pronto:** `./mvnw verify` verde; sem violações de Customer.

**Dependências:** T1-13 (pode ser em paralelo com T1-14).
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-16 — Migrar Product e ProductSku

**Necessidade/contexto:** são os agregados mais ricos (cursor, filtros, SKU,
migrations V4/V10/V11) e os que a trilha 2 mais usa. Possuem testes e
`ProductSkuResponseFormatter` em `adapters.presenter`.

**Objetivo:** Product/ProductSku hexagonais; referências a Brand/Category por
id ou porta, não por entidade.

**O que você vai aprender:** migrar agregados que se relacionam; cursor/keyset
como contrato da porta.

**Tarefas:**
- [ ] Migrar Product (portas, service, DTOs, adapter, entity, controller).
- [ ] Migrar ProductSku; mover `ProductSkuResponseFormatter` para
      `adapters.in.web.presenter`.
- [ ] Revisar `OrderCursor`, `SliceResult`, `CursorUtil` (cursor é HTTP ou
      aplicação? decida e mova).
- [ ] Testes `JpaProduct*`/`JpaProductSku*` renomeados e verdes.
- [ ] Atualizar store.

**Dicas/perguntas:**
- `CursorUtil` codifica o cursor para a URL: o que é detalhe de transporte e
  o que é critério de ordenação do domínio?
- A listagem por SKU (#6/#7) precisa de join com Product/Brand: a porta
  expõe uma "visão" (read model) em vez de entidades?

**Arquivos prováveis:** `useCase/ProductRegisterInteractor`,
`useCase/ProductSkuInteractor`, `adapters/persistence/product*`,
`adapters/web/Product*Controller`, `adapters/web/cursor/CursorUtil`.

**Critério de pronto:** `./mvnw verify` verde; sem violações de Product no
store.

**Dependências:** T1-14.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-17 — Migrar User e EmailValidation

**Necessidade/contexto:** `UserRegisterInteractor` importa 3 pacotes
proibidos; `UserDataMapper` (entidade JPA) é usado por `SecurityFilter`,
`JpaBrand`, etc. `EmailValidationDsGateway` importa `adapters`.

**Objetivo:** User/EmailValidation hexagonais.

**O que você vai aprender:** agregado transversal (usuário) e como evitar que
todo mundo dependa da entidade JPA dele.

**Tarefas:**
- [ ] Portas in/out; `UserRegisterInteractor` -> `application.service`.
- [ ] `UserDataMapper` -> `UserJpaEntity` em `adapters.out.persistence.jpa.user`.
- [ ] `EmailValidation` (JPA + gateway) para `adapters.out`.
- [ ] Remover os últimos usos de `UserDataMapper` fora de `adapters.out`.
- [ ] Atualizar store.

**Dicas/perguntas:**
- `UpdateUserDTO`, `UserPagedResponseDTO`: pertencem a `application.dto` ou à
  borda web?
- Quem precisa do usuário completo? (segurança) E quem só do id?

**Arquivos prováveis:** `useCase/UserRegisterInteractor`,
`adapters/persistence/user/*`, `adapters/persistence/emailValidaton/*`,
`adapters/web/UserController`.

**Critério de pronto:** `./mvnw verify` verde; `UserRegisterInteractorTests`
verde; sem violações de User no store.

**Dependências:** T1-16.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-18 — Reorganizar `config`: segurança, e-mail e exceções

**Necessidade/contexto:** `config` guarda `TokenService`,
`AuthorizationService`, `SecurityFilter`, `EmailService`,
`config.exception.*`. Parte é adapter (e-mail, filtro JWT), parte é
configuração.

**Objetivo:** `config` só com wiring; o resto em seu adapter.

**O que você vai aprender:** distinguir configuração de adapter; e-mail como
porta de saída.

**Tarefas:**
- [ ] `EmailService` -> `adapters.out.email` + porta
      `application.port.out.EmailSender`.
- [ ] `TokenService`, `AuthorizationService`, `SecurityFilter`,
      `AuthenticationController` -> `adapters.in.web(.security)`.
- [ ] `GlobalExceptionHandler` -> `adapters.in.web.exception`.
- [ ] Exceções -> pacote decidido na T1-01.
- [ ] Manter `SecurityConfiguration` e `OpenApiConfiguration` em `config`.
- [ ] Atualizar store.

**Dicas/perguntas:**
- `SecurityConfiguration` precisa ver o filtro (adapter in): `config` pode
  depender de todos? Regra ArchUnit correspondente?
- Quem lança `EmailSendException`: adapter ou service?

**Arquivos prováveis:** `config/*`, `config/exception/*`,
`adapters/web/AuthenticationController`.

**Critério de pronto:** `./mvnw verify` verde; testes de segurança
(`ProductControllerSecurityTest`) verdes.

**Dependências:** T1-17.
**Labels:** `trilha-1`, `refactor`, `arquitetura`.

---

## [TRILHA-1] T1-19 — Zerar o freeze e endurecer as regras

**Necessidade/contexto:** com tudo migrado o store deve estar vazio.

**Objetivo:** remover `FreezingArchRule` e o store; regras estritas.

**O que você vai aprender:** fechar uma dívida; "ratchet" completo.

**Tarefas:**
- [ ] Confirmar que `src/test/resources/archunit_store` está vazio.
- [ ] Remover `FreezingArchRule.freeze` e o diretório do store.
- [ ] Remover o step `git diff` do CI (T1-05) ou ajustar.
- [ ] Adicionar regra "pacotes `entity` e `useCase` não existem".
- [ ] Rodar `onionArchitecture()`/`layeredArchitecture()` completa.

**Dicas/perguntas:**
- Ainda há exceção que você "sabe" que existe? Se sim, ela está documentada e
  com issue?

**Arquivos prováveis:** `src/test/java/com/ndlcommerce/architecture/*`,
`src/test/resources/archunit*`, `.github/workflows/ci.yml`.

**Critério de pronto:** `./mvnw verify` verde sem nenhum freeze; CI verde.

**Dependências:** T1-18.
**Labels:** `trilha-1`, `archunit`, `ci`.

---

## [TRILHA-1] T1-20 — Atualizar documentação e retrospectiva

**Necessidade/contexto:** `AGENTS.md` e `.github/copilot-instructions.md`
ainda descrevem Clean Architecture e o fluxo `Interactor -> DsGateway`.

**Objetivo:** documentos refletem a arquitetura real.

**O que você vai aprender:** documentar arquitetura como contrato; revisar o
que aprendeu.

**Tarefas:**
- [ ] Atualizar `AGENTS.md` (fluxo de request, pacotes, regras ArchUnit).
- [ ] Atualizar `.github/copilot-instructions.md` e agentes
      (`wilson-architect`, `guanabara`) que citam "Clean Architecture".
- [ ] Atualizar o diagrama do fluxo de request.
- [ ] Escrever `docs/retro-hexagonal.md`: 5 dificuldades e 3 decisões que
      mudaria.

**Dicas/perguntas:**
- Um desenvolvedor novo conseguiria criar a feature "Coupon" lendo só o
  `AGENTS.md`? Teste isso com um colega ou com o mentor.

**Arquivos prováveis:** `AGENTS.md`, `.github/copilot-instructions.md`,
`.github/agents/*.md`, `docs/`.

**Critério de pronto:** `grep -ri "useCase\.\|DsGateway" AGENTS.md` sem
resultados obsoletos; revisão do mentor.

**Dependências:** T1-19.
**Labels:** `trilha-1`, `docs`, `arquitetura`.
