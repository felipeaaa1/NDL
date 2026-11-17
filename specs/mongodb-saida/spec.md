# Spec — Trilha 2: Adapter de saída MongoDB ao lado do PostgreSQL

## Contexto

Após a trilha 1, cada feature tem uma **porta de saída** (`application.port.out`)
implementada por um adapter JPA/PostgreSQL. O objetivo aqui é provar a tese
hexagonal: **trocar a persistência sem tocar no núcleo**, criando um segundo
adapter com MongoDB.

Estado atual relevante (código como fonte da verdade):

- Sem Mongo, Testcontainers nem docker compose no repositório; testes de
  persistência usam H2 (`src/test/resources/application-test.yml`, modo
  PostgreSQL, `ddl-auto: create-drop`, Flyway desligado).
- Os gateways (`*DsGateway`) hoje ficam em `useCase.interfaces`; a trilha 1
  os move para `application.port.out`. Brand já retorna DTO; Category/Customer/
  User ainda vazam entidade JPA até serem migrados.
- Beans são registrados por scanner manual em `NdlCommerceApplication`.
- `JpaBrand`/`JpaCategory` obtêm o usuário logado via `SecurityFilter`
  (resolvido em T1-10 com `CurrentUserProvider`).
- IDs são `UUID` (`@GeneratedValue(strategy = UUID)`); listagem com
  cursor/keyset (issue #7) e filtros; índice de SKU no PR #22.

## Objetivo

1. Subir MongoDB local via `docker compose`.
2. Implementar adapters Mongo para Brand (primeiro), Category e Product/SKU.
3. Alternar o adapter ativo por propriedade/profile
   (`app.persistence.provider=jpa|mongo`) sem mudar `application`/`domain`.
4. Testar com Testcontainers e provar equivalência de comportamento com
   **testes de contrato compartilhados** rodando nos dois adapters.
5. Registrar decisões de modelagem (embed x reference) e índices.

## Arquitetura alvo

```
   Controller --> port.in --> Service --> port.out (BrandRepositoryPort)
                                               ^
                        +----------------------+--------------------+
                        |                                           |
        adapters.out.persistence.jpa            adapters.out.persistence.mongo
        BrandPersistenceAdapter                 BrandMongoAdapter
        (JPA -> PostgreSQL)                     (Spring Data Mongo)
                        ^                                           ^
                        +---------- escolhido por --------+---------+
                                    app.persistence.provider
                                    (jpa | mongo)

   Testes de contrato:  BrandPortContractTest (abstract)
                          |-- BrandJpaAdapterContractTest   (Postgres container)
                          '-- BrandMongoAdapterContractTest (Mongo container)
```

## Escopo

- Compose do Mongo, dependências (`spring-boot-starter-data-mongodb`,
  Testcontainers) e propriedades.
- Documento de modelagem (ADR) com embed x reference para Brand, Category,
  Product e SKU.
- Adapters Mongo de Brand, Category, Product/SKU; mappers documento<->DTO/domínio.
- Índices (únicos e de apoio), cursor/keyset no Mongo.
- Testes de integração com Testcontainers e testes de contrato compartilhados.
- Relatório de diferenças de comportamento entre os adapters.

## Fora de escopo

- Migrar dados existentes do PostgreSQL para o Mongo (ETL).
- Persistir **User/Customer/Auth** em Mongo (ficam em Postgres; a aplicação
  roda com persistência mista por feature, se necessário).
- Dual-write, sincronização e CDC entre as bases.
- Elasticsearch e busca full-text (issues #10-#15, #18, #20 adiadas).
- Sharding, réplica e transações multi-documento avançadas.

## Critérios de aceite

- [ ] `docker compose up -d mongo` sobe o Mongo; credenciais por env, nunca
      commitadas.
- [ ] `app.persistence.provider=mongo` usa o adapter Mongo para Brand (e
      demais migrados); `jpa` mantém o comportamento atual (default).
- [ ] `application` e `domain` **sem** imports de Mongo/JPA (ArchUnit da T1
      continua verde, com regra nova para `..mongo..`).
- [ ] Documentos Mongo não vazam para fora de `adapters.out.persistence.mongo`.
- [ ] Índice único para nome de Brand ativa verificado por teste.
- [ ] Testes de contrato idênticos passam nos dois adapters.
- [ ] Testes Testcontainers rodam local e no CI (`ubuntu-latest` tem Docker).
- [ ] ADR de modelagem e relatório de diferenças commitados em
      `docs/`.

## Riscos e cuidados

- **UUID no Mongo**: configurar `uuidRepresentation=STANDARD`; sem isso o ID
  muda de formato entre drivers.
- Dois starters de repositório (JPA + Mongo) no classpath exigem escopos
  explícitos (`@EnableJpaRepositories` / `@EnableMongoRepositories`).
- Scanner manual de beans: confirme que `@ConditionalOnProperty`/`@Profile`
  são respeitados pelos beans do adapter.
- Diferenças sutis: case-sensitivity em unicidade, ordenação com `null`,
  precisão de data/hora, transações. Documente-as na T2-13.
