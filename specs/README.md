# Trilhas de estudo — NDL-Commerce

Três trilhas para **aprender fazendo**. Você escreve o código; o mentor
(`guanabara`) só orienta e revisa. Cada trilha tem `spec.md` (o quê e por quê)
e `issues.md` (issues pequenas, ordenadas, prontas para virar issues do GitHub).

| Trilha | Pasta | Issues | Tema |
|--------|-------|--------|------|
| 1 | `specs/hexagonal-archunit/` | 20 | Clean Architecture -> Hexagonal + ArchUnit |
| 2 | `specs/mongodb-saida/` | 13 | Segundo adapter de saída (MongoDB) |
| 3 | `specs/microservico-threads/` | 18 | Concorrência, resiliência e mensageria |

> Estes arquivos são documentos de planejamento. Nada aqui altera `src/` ou
> `pom.xml`: quem implementa é você, uma issue por vez.

## Ordem sugerida

1. **Pré-requisitos soltos**: fechar a issue #9 (baseline de
   performance no PostgreSQL, útil para comparar com o Mongo depois).
2. **Trilha 1** (inteira). É a base: sem portas limpas não há "segundo adapter".
3. **Trilha 3, fase A** (experimentos T3-01 a T3-10) pode ser intercalada com a
   trilha 1 como "descanso": não depende de refatoração, só de Java 21.
4. **Trilha 2** (após as issues bloqueantes da trilha 1, abaixo).
5. **Trilha 3, fase B** (T3-11 a T3-18: resiliência, Outbox, RabbitMQ), que
   reaproveita as issues #16, #17 e #19.

## Mapa de dependências

```
 #9 (baseline Postgres)
        |
        v
 +--------------------------+
 | T1 Hexagonal + ArchUnit  |  T1-01..T1-06  (ArchUnit + CI + freeze)
 |                          |  T1-07..T1-13  (Brand fatia vertical)
 |                          |  T1-14..T1-20  (demais features, regras finais)
 +-----------+--------------+
             |                         +-----------------------------+
             |  bloqueia               | T3 fase A (T3-01..T3-10)    |
             |  T1-09, T1-11,          | experimentos, sem bloqueio  |
             |  T1-12, T1-13           +--------------+--------------+
             v                                        |
 +--------------------------+                         |
 | T2 MongoDB (saída)       |                         |
 |  T2-01..T2-04 infra      |                         |
 |  T2-05..T2-09 Brand      |                         |
 |  T2-10..T2-13 resto      |                         |
 +-----------+--------------+                         |
             |                                        v
             |                   +-----------------------------+
             +------------------>| T3 fase B (T3-11..T3-18)    |
               T2 opcional       | resiliência, #16, #17, #19  |
                                 +-----------------------------+
```

Bloqueios detalhados da trilha 1 sobre a trilha 2:

| Issue T2 | Bloqueada por (T1) |
|----------|--------------------|
| T2-05 a T2-09 (Brand) | T1-09 (ports/DTOs), T1-11 (adapter JPA), T1-12 (wiring) |
| T2-05 (auditoria/usuário) | T1-10 (porta `CurrentUserProvider`) |
| T2-10 (Category) | T1-14 |
| T2-11, T2-12 (Product/SKU) | T1-16 |
| T2-13 (CI/relatório final) | T1-05 (ArchUnit no CI) |

## Relação com as issues abertas (#8 a #20) e PR #22 (fechado)

| Item | Situação | Decisão |
|------|----------|---------|
| #8 CORS | Independente | Fazer quando quiser; não bloqueia nada |
| #9 Medir/otimizar listagem Postgres | Útil | Fazer **antes** da trilha 2 (baseline) |
| PR #22 índice de SKU (migration) | Fechado | Nada a fazer |
| #10, #11, #12, #13, #14, #15 | Elasticsearch | **Adiar** (conflita com foco em Mongo) |
| #18 consumir eventos e sincronizar ES | Elasticsearch | **Adiar**; ver T3-16 (consumer genérico) |
| #20 recovery/versionamento do índice ES | Elasticsearch | **Adiar** |
| #16 Transactional Outbox | Reaproveitar | Vira o laboratório T3-14 (não duplicar) |
| #17 RabbitMQ + eventos da Outbox | Reaproveitar | Vira o laboratório T3-15 |
| #19 Resiliência da sincronização | Reaproveitar | Reescopar: T3-12 (aplicar no consumer/HTTP) |

Sugestão: ao criar as issues das trilhas, adicione um comentário em #16, #17 e
#19 apontando para as issues T3 correspondentes, e rotule #10-#15, #18, #20
como `adiado`. Não é preciso fechá-las.

Se um dia retomar a busca: Elasticsearch seria **mais um adapter de saída**
(porta `ProductSearchGateway`), exatamente o exercício das trilhas 1 e 2.

## Convenções

- Pacotes: `com.ndlcommerce.{domain,application,adapters,config}` (alvo da T1).
- Código em inglês; documentação e commits em pt-BR conforme o repositório.
- Uma issue = um PR pequeno; `./mvnw verify` verde antes de abrir.
- Labels usadas: `trilha-1`, `trilha-2`, `trilha-3`, `arquitetura`, `archunit`,
  `refactor`, `mongodb`, `testes`, `concorrencia`, `resiliencia`, `mensageria`,
  `ci`, `docs`, `experimento`, `estudo`.
