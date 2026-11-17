# Issues — Trilha 2: Adapter MongoDB

Execute depois dos bloqueios da trilha 1 (tabela em `specs/README.md`).
Verificação padrão: `./mvnw verify` (testes com Testcontainers exigem Docker).

```
Infra        T2-01 .. T2-04
Brand        T2-05 .. T2-09
Demais       T2-10 .. T2-13
```

Bloqueios gerais: T2-01..T2-04 podem começar após T1-02; **T2-05 em diante**
exigem T1-09, T1-10, T1-11 e T1-12 (Brand hexagonal e `CurrentUserProvider`).

---

## [TRILHA-2] T2-01 — Subir MongoDB com docker compose

**Necessidade/contexto:** não há compose no repositório (há `Dockerfile` e
`exemplo.env`).

**Objetivo:** `compose.yaml` só com Mongo para desenvolvimento.

**O que você vai aprender:** imagem oficial `mongo`, volumes, healthcheck,
variáveis de ambiente, `mongosh`.

**Tarefas:**
- [ ] Criar `compose.yaml` com serviço `mongo` (porta 27017, volume nomeado,
      `MONGO_INITDB_ROOT_USERNAME/PASSWORD` vindos do `.env`).
- [ ] Healthcheck com `mongosh --eval "db.adminCommand('ping')"`.
- [ ] Atualizar `exemplo.env` com `MONGO_URI`/credenciais **de exemplo**.
- [ ] Conectar com `mongosh` e criar uma collection de teste.
- [ ] Garantir que `.env` está no `.gitignore`.

**Dicas/perguntas:**
- Por que `latest` é má ideia? Fixe uma tag.
- Mongo standalone aceita transações? (veja replica set) — importa mais
  adiante.

**Arquivos prováveis:** `compose.yaml` (novo), `exemplo.env`, `.gitignore`.

**Critério de pronto:** `docker compose up -d mongo` fica `healthy`;
`docker compose exec mongo mongosh --eval "db.runCommand({ping:1})"`.

**Dependências:** nenhuma.
**Labels:** `trilha-2`, `mongodb`, `infra`.

---

## [TRILHA-2] T2-02 — Dependências Mongo e Testcontainers

**Necessidade/contexto:** `pom.xml` não tem Mongo nem Testcontainers.

**Objetivo:** dependências no `pom.xml` sem quebrar o contexto atual.

**O que você vai aprender:** BOM do Testcontainers, `@ServiceConnection`
(Spring Boot 3.1+), escopos, conflito de autoconfig.

**Tarefas:**
- [ ] Adicionar `spring-boot-starter-data-mongodb`.
- [ ] Adicionar `org.testcontainers:mongodb`, `postgresql` e `junit-jupiter`
      (escopo test; use o BOM do Testcontainers ou o gerenciado pelo Boot).
- [ ] Adicionar `spring-boot-testcontainers` (test).
- [ ] Subir o app com `provider=jpa` e conferir que nada mudou (e que o Mongo
      não tenta conectar de forma bloqueante).
- [ ] Rodar `./mvnw verify` inteiro.

**Dicas/perguntas:**
- Com dois starters de dados, o Spring Data entra em "strict repository
  configuration mode": o que o log diz? Como resolver?
- Qual a diferença entre `@ServiceConnection` e `@DynamicPropertySource`?

**Arquivos prováveis:** `pom.xml`, `application.yml`.

**Critério de pronto:** `./mvnw verify` verde; `./mvnw spring-boot:run`
sobe como antes.

**Dependências:** T2-01 (recomendada).
**Labels:** `trilha-2`, `mongodb`, `testes`.

---

## [TRILHA-2] T2-03 — Modelagem: embed x reference (ADR)

**Necessidade/contexto:** modelar documentos sem pensar em acesso gera
documentos gigantes ou joins manuais.

**Objetivo:** ADR com a modelagem por agregado, guiada por padrões de acesso.

**O que você vai aprender:** embed x reference, desnormalização,
duplicação controlada, limite de 16 MB, atomicidade por documento.

**Tarefas:**
- [ ] Listar os padrões de acesso (listagem por SKU pública, busca por nome
      de Brand, árvore de Category).
- [ ] Brand: documento simples; decidir `createdBy` (id).
- [ ] Category: `parentId` (reference) x `ancestors`/`path` — comparar.
- [ ] Product/SKU: SKU embutido no Product x collection própria; quais campos
      de Product/Brand/Category duplicar no SKU (snapshot) para a listagem.
- [ ] Definir estratégia de atualização do snapshot quando Brand muda.
- [ ] Desenhar em ASCII cada documento e escrever
      `docs/adr/0002-modelagem-mongodb.md`.

**Dicas/perguntas:**
- "Quem lê mais: listagem de SKU ou edição de Product?" A resposta decide o
  embed.
- Se Brand é renomeada, quantos documentos mudam? Aceitável?
- Quais invariantes precisam de atomicidade? Cabem em um documento?

**Arquivos prováveis:** `docs/adr/0002-modelagem-mongodb.md` (novo); leitura
de `adapters/out/persistence/jpa/*` e migrations `V4`, `V10`, `V11`.

**Critério de pronto:** ADR com diagramas e decisão por agregado, revisado
pelo mentor.

**Dependências:** nenhuma (ideal após T1-16 para conhecer o formato final).
**Labels:** `trilha-2`, `mongodb`, `arquitetura`, `docs`.

---

## [TRILHA-2] T2-04 — Chave de troca: `app.persistence.provider`

**Necessidade/contexto:** precisa existir um único lugar para escolher o
adapter, sem `if` espalhado.

**Objetivo:** adapter JPA ativo por padrão (`provider=jpa`); Mongo quando
`provider=mongo`.

**O que você vai aprender:** `@ConditionalOnProperty`, `matchIfMissing`,
escopos de `@EnableJpaRepositories`/`@EnableMongoRepositories`,
composition root.

**Tarefas:**
- [ ] Propriedade `app.persistence.provider` em `application.yml`
      (default `jpa`).
- [ ] Anotar o adapter JPA com condição `jpa` (`matchIfMissing = true`).
- [ ] Definir escopos de repositórios JPA e Mongo por pacote.
- [ ] Teste: contexto sobe com cada valor e injeta a porta correta
      (`assertThat(port).isInstanceOf(...)`).
- [ ] Testar valor inválido (falhar rápido).

**Dicas/perguntas:**
- O scanner manual de `NdlCommerceApplication` respeita
  `@ConditionalOnProperty`? Como provar?
- Profile (`@Profile("mongo")`) ou propriedade? Quando cada um é melhor?
- Quando `provider=mongo`, o DataSource/Flyway do Postgres ainda sobe? Qual o
  impacto? (User ainda usa JPA.)

**Arquivos prováveis:** `application.yml`, `config/*`,
`adapters/out/persistence/jpa/brand/BrandPersistenceAdapter`,
`NdlCommerceApplication`.

**Critério de pronto:** teste de contexto verde para ambos os valores;
`./mvnw verify` verde.

**Dependências:** T1-12; T2-02.
**Labels:** `trilha-2`, `mongodb`, `arquitetura`.

---

## [TRILHA-2] T2-05 — Brand: documento e mapeamento documento<->domínio

**Necessidade/contexto:** o adapter Mongo precisa de seu próprio modelo
(`BrandDocument`) — nunca reutilizar `@Entity` JPA.

**Objetivo:** `BrandDocument`, repositório Spring Data Mongo e mapper.

**O que você vai aprender:** `@Document`, `@Id` com UUID,
`uuidRepresentation`, `@Field`, mapper manual x MapStruct, auditoria sem
`SecurityFilter` (via `CurrentUserProvider`).

**Tarefas:**
- [ ] Criar `adapters.out.persistence.mongo.brand.BrandDocument`.
- [ ] Criar `BrandMongoRepository extends MongoRepository<BrandDocument, UUID>`.
- [ ] Configurar `spring.data.mongodb.uuid-representation=standard`.
- [ ] Criar mapper `BrandDocumentMapper` (documento <-> DTO da porta).
- [ ] Mapear campos de auditoria (`createdAt`, `createdBy`, `active`).

**Dicas/perguntas:**
- Qual o tipo do `_id`: `UUID` ou `ObjectId`? Consequências para o contrato
  da porta (que usa `UUID`).
- Quem preenche `createdAt` no JPA (`@CreationTimestamp`)? Como equivalente
  no Mongo (`@CreatedDate` + `@EnableMongoAuditing` ou manual)?
- `BigDecimal`/datas: como o Mongo armazena (Decimal128, UTC)?

**Arquivos prováveis:** `adapters/out/persistence/mongo/brand/*` (novo),
`application.yml`.

**Critério de pronto:** teste de mapper unitário (ida e volta sem perda) e
`./mvnw -Dtest='*BrandDocumentMapper*' test` verde.

**Dependências:** T2-02; T1-10, T1-11, T1-12.
**Labels:** `trilha-2`, `mongodb`.

---

## [TRILHA-2] T2-06 — Brand: `BrandMongoAdapter` implementando a porta

**Necessidade/contexto:** mesma porta (`existsByName`, `save`, `list`,
`getById`, `existsByNameAndNotId`, `update`, `delete`) que o adapter JPA.

**Objetivo:** adapter Mongo completo, selecionado por `provider=mongo`.

**O que você vai aprender:** `MongoTemplate`/`Query`/`Criteria` x derived
queries, paginação, soft delete (`active`), regex x igualdade.

**Tarefas:**
- [ ] Implementar todos os métodos da porta.
- [ ] Reproduzir a semântica do JPA: `existsByNameAndActiveTrue`, soft delete,
      `PaginatedResult`.
- [ ] Anotar com a condição de T2-04.
- [ ] Teste manual: subir app com `provider=mongo` + compose e executar
      `POST/GET/PUT/DELETE /brands` (Swagger).

**Dicas/perguntas:**
- O `existsByName` JPA diferencia maiúsculas/minúsculas? E o Mongo? (veja
  *collation*).
- Em `update`, quem lança "não encontrado"? Igual no JPA?
- Page do Mongo começa em 0 como o JPA? Ordenação padrão?

**Arquivos prováveis:** `adapters/out/persistence/mongo/brand/BrandMongoAdapter`.

**Critério de pronto:** CRUD de Brand funcional com `provider=mongo`;
`./mvnw verify` verde.

**Dependências:** T2-04, T2-05.
**Labels:** `trilha-2`, `mongodb`.

---

## [TRILHA-2] T2-07 — Brand: índices

**Necessidade/contexto:** sem índice, `existsByName` faz *collection scan*, e
sem índice único há corrida (ver T3-10).

**Objetivo:** índice único parcial para nome entre ativas e índice de apoio
para listagem.

**O que você vai aprender:** `@Indexed`, `@CompoundIndex`,
`partialFilterExpression`, `IndexOperations`, `explain()`, collation,
`auto-index-creation`.

**Tarefas:**
- [ ] Criar índice único em `name` apenas onde `active = true`.
- [ ] Decidir criação por `auto-index-creation` x script/`IndexOperations`
      no startup (justifique para produção).
- [ ] Verificar com `explain("executionStats")` que a consulta usa IXSCAN.
- [ ] Comparar com o índice equivalente do Postgres (migrations).

**Dicas/perguntas:**
- Por que um índice único simples bloqueia "re-criar" uma marca removida
  (soft delete)?
- Índice único é sensível a maiúsculas? Como ficar equivalente ao Postgres?

**Arquivos prováveis:** `BrandDocument`, config Mongo
(`adapters/out/persistence/mongo/MongoIndexConfig`).

**Critério de pronto:** teste (T2-08) prova que duplicar nome ativo lança
`DuplicateKeyException`; `explain` anexado ao PR mostra IXSCAN.

**Dependências:** T2-06.
**Labels:** `trilha-2`, `mongodb`.

---

## [TRILHA-2] T2-08 — Brand: testes de integração com Testcontainers (Mongo)

**Necessidade/contexto:** o projeto testa persistência com H2; Mongo precisa
de banco real.

**Objetivo:** `BrandMongoAdapterIT` com contêiner Mongo.

**O que você vai aprender:** `@Testcontainers`, `MongoDBContainer`,
`@ServiceConnection`, `@DataMongoTest`, reuso de contêiner, isolamento entre
testes.

**Tarefas:**
- [ ] Criar a classe base com `MongoDBContainer` (singleton/estático).
- [ ] Testar: salvar/ler, duplicidade, soft delete, paginação, índice único.
- [ ] Limpar a collection entre testes.
- [ ] Medir o tempo; avaliar `withReuse(true)`.

**Dicas/perguntas:**
- `@DataMongoTest` carrega beans do seu adapter? E o scanner manual?
- Por que testes em H2 não servem para Mongo (e nem sempre para Postgres)?

**Arquivos prováveis:** `src/test/java/com/ndlcommerce/adapters/out/persistence/mongo/brand/*`.

**Critério de pronto:** `./mvnw -Dtest='*BrandMongoAdapterIT' test` verde com
Docker ativo.

**Dependências:** T2-06, T2-07.
**Labels:** `trilha-2`, `mongodb`, `testes`.

---

## [TRILHA-2] T2-09 — Brand: testes de contrato compartilhados (JPA x Mongo)

**Necessidade/contexto:** o hexagonal promete que os adapters são
**intercambiáveis**; só um teste único contra ambos prova isso.

**Objetivo:** classe abstrata `BrandPortContractTest` executada por duas
subclasses (JPA/Postgres container e Mongo container).

**O que você vai aprender:** teste de contrato, herança de testes JUnit 5,
Testcontainers PostgreSQL, descobrir diferenças reais.

**Tarefas:**
- [ ] Criar `BrandPortContractTest` (abstrato, recebe a porta via método
      `createPort()`).
- [ ] Cenários: criar, duplicado, update com mesmo nome, update com nome de
      outra, delete + recriar, listar com paginação, getById inexistente.
- [ ] Subclasse JPA com **Postgres** real (Flyway ligado) — não H2.
- [ ] Subclasse Mongo.
- [ ] Registrar as divergências encontradas (alimenta T2-13).

**Dicas/perguntas:**
- O que é "comportamento da porta" e o que é detalhe do adapter? Mensagem de
  exceção entra no contrato?
- Ordenação: o contrato fixa `ORDER BY`?

**Arquivos prováveis:**
`src/test/java/com/ndlcommerce/adapters/out/persistence/contract/*`.

**Critério de pronto:** `./mvnw -Dtest='*BrandPortContract*' test` verde nas
duas implementações.

**Dependências:** T2-08.
**Labels:** `trilha-2`, `mongodb`, `testes`.

---

## [TRILHA-2] T2-10 — Category no Mongo (hierarquia)

**Necessidade/contexto:** Category tem `parentId` (árvore).

**Objetivo:** adapter Mongo + contrato para Category.

**O que você vai aprender:** modelar árvore (parent reference x materialized
path), `existsById` do pai, índices por `parentId`.

**Tarefas:**
- [ ] Implementar conforme o ADR (T2-03).
- [ ] Índices (nome único ativo; `parentId`).
- [ ] Reaproveitar o molde de contrato (T2-09) para Category.
- [ ] Teste de "pai inexistente" nos dois adapters.

**Dicas/perguntas:**
- Como listar todos os descendentes? A porta atual precisa disso?
- O que acontece ao remover um pai com filhos ativos? Igual nos dois?

**Arquivos prováveis:** `adapters/out/persistence/mongo/category/*`.

**Critério de pronto:** contrato de Category verde nos dois adapters.

**Dependências:** T2-09; T1-14.
**Labels:** `trilha-2`, `mongodb`, `testes`.

---

## [TRILHA-2] T2-11 — Product/SKU: documento e snapshot (design)

**Necessidade/contexto:** é a modelagem mais interessante: a listagem pública
é por SKU (#6) com cursor (#7) e precisa de dados de Product/Brand/Category.

**Objetivo:** documentos `ProductDocument` e `ProductSkuDocument` com
snapshot desnormalizado e plano de consistência.

**O que você vai aprender:** desnormalização com custo de escrita,
consistência eventual, versionamento de documento.

**Tarefas:**
- [ ] Implementar os documentos conforme T2-03.
- [ ] Mappers documento<->DTO/domínio.
- [ ] Definir o que acontece quando Brand é renomeada (atualizar snapshot em
      lote? `updateMany`).
- [ ] Índices para a listagem pública (filtros + ordenação por cursor).

**Dicas/perguntas:**
- Quais campos do filtro `ProductFilterDTO` precisam de índice composto? A
  ordem das chaves importa (regra ESR: Equality, Sort, Range).
- O que o PR #22 (índice de SKU no Postgres) indica sobre a consulta crítica?

**Arquivos prováveis:** `adapters/out/persistence/mongo/product*/*`.

**Critério de pronto:** testes de mapper verdes; `explain` das consultas
principais anexado.

**Dependências:** T2-09; T1-16; PR #22.
**Labels:** `trilha-2`, `mongodb`.

---

## [TRILHA-2] T2-12 — Product/SKU: adapter com cursor/keyset e contrato

**Necessidade/contexto:** a paginação por cursor (#7) usa `OrderCursor`/
`SliceResult`; no Mongo o cursor depende do tipo de `_id` e do campo de
ordenação.

**Objetivo:** adapter Mongo de Product/SKU com a mesma semântica de cursor.

**O que você vai aprender:** keyset no Mongo (`$or` com desempate por `_id`),
`Slice`, estabilidade de ordenação.

**Tarefas:**
- [ ] Implementar as portas de Product/SKU.
- [ ] Reproduzir a ordenação e o cursor do JPA (inclusive empates).
- [ ] Contrato compartilhado para listagem: 3 páginas, empates, último
      registro, cursor inválido.
- [ ] Comparar tempo de listagem (alimenta T2-13 e a issue #9).

**Dicas/perguntas:**
- Com `UUID` v4 como desempate a ordem é estável? Entre Postgres e Mongo o
  "menor UUID" é o mesmo? (representação binária!)
- Como o cursor é opaco para o cliente (`CursorUtil`)? Ele muda entre
  adapters?

**Arquivos prováveis:** `adapters/out/persistence/mongo/productSku/*`,
`application/model/OrderCursor`, `SliceResult`.

**Critério de pronto:** contrato de listagem verde nos dois adapters.

**Dependências:** T2-11.
**Labels:** `trilha-2`, `mongodb`, `testes`.

---

## [TRILHA-2] T2-13 — CI com Testcontainers e relatório de diferenças

**Necessidade/contexto:** o CI (`ci.yml`) roda `mvn verify` em
`ubuntu-latest` (Docker disponível); os novos testes precisam rodar lá. E o
aprendizado precisa virar documento.

**Objetivo:** pipeline verde com contêineres e relatório comparativo.

**O que você vai aprender:** Testcontainers no GitHub Actions, tempo de CI,
separar testes lentos (`*IT` com Failsafe/`@Tag`), registrar trade-offs.

**Tarefas:**
- [ ] Rodar os testes de contrato no CI; medir o tempo.
- [ ] Opcional: separar por `@Tag("integration")` e step próprio.
- [ ] Escrever `docs/mongodb-vs-postgres.md`: divergências do contrato
      (collation/case, nulls na ordenação, precisão de data, transações,
      unicidade, UUID), números de desempenho da listagem e quando escolheria
      cada um.
- [ ] Atualizar `README.md` com como trocar o adapter.
- [ ] Adicionar regra ArchUnit: `..application..` e `..domain..` não
      dependem de `org.springframework.data.mongodb..`.

**Dicas/perguntas:**
- Para que serve a garantia de contrato se o relatório ainda lista
  divergências? Quais são "aceitáveis" e quais exigem mudar a porta?

**Arquivos prováveis:** `.github/workflows/ci.yml`, `docs/*`, `README.md`,
`src/test/.../architecture/*`.

**Critério de pronto:** CI verde com os testes de contrato; documento no PR.

**Dependências:** T2-12; T1-05.
**Labels:** `trilha-2`, `mongodb`, `ci`, `docs`.
