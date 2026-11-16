# Plano de ação — próximos passos do estudante

## Overview

Este plano organiza, em ordem, o que fazer a seguir nas três trilhas de
`specs/` (ver `specs/README.md`). Você escreve o código; o agente `guanabara`
só ensina e revisa. O ritmo é de aprendizado, não de velocidade.

## Quick Start

1. Pergunte ao `sdd-supervisor`: "Qual é a próxima etapa?"
2. Peça ao `guanabara`: "Me explique a T1-01".
3. Faça sozinho e peça revisão ao `guanabara`.

## Visão geral do fluxo

```
 [Preparação] --> [Trilha 1] --> [Trilha 2] --> [Trilha 3 fase B] --> [Revisão]
   Passo 0         Passo 1        Passo 3          Passo 4            final
                      |
                      +--- intercalada: [Trilha 3 fase A] (Passo 2)
                           T3-01..T3-10, como "descanso" entre issues da T1
```

Legenda: `-->` sequência obrigatória; `---` intercalação opcional.

## Ciclo por issue

Repita para cada issue (T1-xx, T2-xx, T3-xx):

1. [ ] **`sdd-supervisor`**: confirme que a issue é a próxima etapa.
2. [ ] **`guanabara` (entender)**: peça "me explique a T?-??" e leia a issue.
3. [ ] **`carla-qa` (prever testes)**: peça os cenários antes de codar.
4. [ ] **Você implementa**: uma issue = um PR pequeno.
5. [ ] **Revisão**: peça ao `guanabara` (e à `carla-qa`) para revisar.
6. [ ] **Fechar**: rode `.\mvnw.cmd clean verify` e registre o aprendizado
   no PR (modelo abaixo).

### Regra de ouro

Só peça "implemente" ao `guanabara` depois de **tentar e travar**, e apenas
para **uma task pequena**. Antes disso, peça dicas, não a solução.

## Passo 0 — Preparação

1. [ ] Mergear o PR #22 (índice de SKU) para evitar conflito na T1.
2. [ ] Fazer a issue #9 (baseline de performance da listagem no PostgreSQL).
3. [ ] Rotular #10, #11, #12, #13, #14, #15, #18 e #20 como `adiado`.
4. [ ] Comentar em #16, #17 e #19 apontando para as issues T3 equivalentes:

   | Issue | Aponta para |
   |-------|-------------|
   | #16 Outbox | T3-14 |
   | #17 RabbitMQ | T3-15 |
   | #19 Resiliência | T3-12 |

**Critério de pronto**: PR #22 mergeado, #9 fechada com números anotados,
rótulos aplicados, três comentários feitos, `.\mvnw.cmd clean verify` verde.

## Passo 1 — Trilha 1 (Hexagonal + ArchUnit, 20 issues)

| Bloco | Issues | Tema |
|-------|--------|------|
| A | T1-01 a T1-06 | ADR, ArchUnit, regras-alvo, freeze, CI, `entity` -> `domain` |
| B | T1-07 a T1-13 | Brand como fatia vertical (ports, application, JPA, web) |
| C | T1-14 a T1-20 | Category, Customer, Product/SKU, User, `config`, freeze zero, docs |

Verificação em cada bloco:

1. [ ] `.\mvnw.cmd clean verify` verde.
2. [ ] Testes de arquitetura (ArchUnit) passam; o freeze nunca aumenta.
3. [ ] Sem mudança de comportamento (testes de Brand/features intactos).

**Critério de pronto**: T1-20 concluída, freeze zerado (T1-19), regras
estritas ativas no CI (T1-05) e retrospectiva escrita.

## Passo 2 — Trilha 3, fase A (experimentos)

Issues T3-01 a T3-10 (laboratório, threads, race condition, executors,
`CompletableFuture`, virtual threads, `@Async`, check-then-act). Não dependem
da refatoração: use entre issues da Trilha 1 como descanso.

**Critério de pronto**: relatório de cada experimento (modelo da T3-01)
com números medidos e conclusão em suas palavras.

## Passo 3 — Trilha 2 (MongoDB como segundo adapter)

Bloqueios da Trilha 1 sobre a Trilha 2 (conforme `specs/README.md`):

| Issue T2 | Bloqueada por (T1) |
|----------|--------------------|
| T2-05 a T2-09 (Brand) | T1-09, T1-11, T1-12 |
| T2-05 (auditoria/usuário) | T1-10 |
| T2-10 (Category) | T1-14 |
| T2-11, T2-12 (Product/SKU) | T1-16 |
| T2-13 (CI/relatório final) | T1-05 |

Sequência: T2-01..T2-04 (infra) -> T2-05..T2-09 (Brand) -> T2-10..T2-13.

**Critério de pronto**: `app.persistence.provider` troca JPA/Mongo sem alterar
o domínio; testes de contrato passam nos dois adapters; CI verde.

## Passo 4 — Trilha 3, fase B (#16, #17, #19)

Issues T3-11 a T3-18: timeouts, Resilience4j (T3-12, #19), idempotência,
Outbox (T3-14, #16), RabbitMQ (T3-15, #17), consumer idempotente, correlation
id e retrospectiva (T3-18).

**Critério de pronto**: Outbox publica no RabbitMQ sem perda nem duplicidade
visível; falhas simuladas tratadas; T3-18 escrita.

## Critério para avançar de fase

Avance somente se:

1. [ ] Todas as issues da fase fechadas, com `.\mvnw.cmd clean verify` verde.
2. [ ] Registro de aprendizado preenchido em cada PR.
3. [ ] Você consegue explicar a fase sem consultar o código.
4. [ ] Revisão final do `guanabara` e da `carla-qa` sem pendências.

## Registro de aprendizado (cole no PR)

```
## Registro de aprendizado
- Issue: T?-??
- O que aprendi:
- O que errei:
- O que ainda não entendi:
```

## Troubleshooting

| Sintoma | Causa provável | O que fazer |
|---------|----------------|-------------|
| `verify` falha no ArchUnit | Nova violação além do freeze | Corrija a dependência; não aumente o freeze |
| Conflito no PR da T1 | PR #22 não mergeado | Conclua o Passo 0 e atualize a branch |
| T2 bloqueada | Issue T1 exigida pendente | Consulte a tabela de bloqueios |
| Testcontainers não sobe | Docker parado | Inicie o Docker e rode de novo |
| Travou numa task | Conceito não entendido | Peça dica ao `guanabara`; só depois "implemente" |
| Não sabe a próxima etapa | Perdeu o fio | Pergunte ao `sdd-supervisor` |
