# Spec — Trilha 3: Microsserviço e concorrência em Java 21

## Contexto

O NDL-Commerce é hoje um monólito Spring Boot 3.3.4 / Java 21, sem mensageria,
sem resiliência e sem nenhum código concorrente explícito. Mas ele já tem
terreno real para estudar concorrência:

- `existsByName` seguido de `save` em Brand/Category/Product (check-then-act:
  **race condition real** sob requisições concorrentes).
- `SecurityFilter`/usuário logado em `ThreadLocal` (`SecurityContextHolder`):
  o que acontece em `@Async` e virtual threads?
- Pool de conexões HikariCP e transações (`@Transactional`) como gargalo.
- Envio de e-mail (SendGrid) síncrono: candidato natural a async/timeout/retry.
- Issues abertas que viram laboratório de microsserviço:
  `#16` Transactional Outbox, `#17` RabbitMQ, `#19` resiliência.

## Reaproveitamento das issues abertas (sem duplicar)

| Issue aberta | Papel nesta trilha | Reescopo |
|--------------|--------------------|----------|
| #16 Transactional Outbox | Laboratório T3-14 | Eventos de catálogo (ex.: `BrandCreated`, `ProductCreated`) numa tabela outbox; poller concorrente |
| #17 RabbitMQ + eventos da Outbox | Laboratório T3-15 | Publicar a Outbox; confirms, ack, DLQ, listeners concorrentes |
| #18 Consumir eventos e sincronizar Elasticsearch | **Adiada** | T3-16 cria um consumer genérico (read model/notificação) no lugar |
| #19 Resiliência da sincronização | Laboratório T3-12 | Aplicar Retry/CircuitBreaker/Timeout ao consumer e ao cliente HTTP, não ao ES |
| #10-#15, #20 Elasticsearch | **Adiadas** | Fora desta trilha |

Ordem recomendada: **T3-14 (#16) -> T3-15 (#17) -> T3-12 (#19) -> T3-16**.
Isso difere da numeração de propósito: veja "Ordem" abaixo.

## Objetivo

Aprender, com experimentos medidos, os fundamentos de concorrência do Java 21
e aplicá-los a padrões de microsserviço (resiliência, idempotência,
mensageria), usando o próprio repositório como laboratório.

## Fases e ordem

```
 Fase A  (experimentos isolados, sem tocar no app)      T3-01 .. T3-07
 Fase A' (concorrência dentro do Spring)                T3-08 .. T3-10
 Fase B  (microsserviço: resiliência e mensageria)      T3-11 .. T3-18

 T3-01 -> 02 -> 03 -> 04 -> 05 -> 06 -> 07 -> 08 -> 09 -> 10
                                                      |
        T3-11 (timeout) -> T3-13 (idempotência) -> T3-14 (#16 Outbox)
                                                      |
                              T3-15 (#17 RabbitMQ) <--+
                                    |
                              T3-12 (#19 resiliência)
                                    |
                              T3-16 (consumer) -> T3-17 -> T3-18
```

Os experimentos da fase A usam um módulo isolado `labs/concurrency-lab/`
(Java puro, sem alterar o `pom.xml` da aplicação). Os da fase B alteram o app
(portas/adapters da trilha 1) e portanto dependem dela.

## Escopo

- Threads, `ExecutorService`, `CompletableFuture`, virtual threads (Java 21),
  `spring.threads.virtual.enabled=true`, `@Async`.
- Race conditions, `synchronized`, `volatile`, `Atomic*`, `LongAdder`,
  deadlock, visibilidade.
- Timeouts, Retry, Circuit Breaker e Bulkhead com Resilience4j.
- Idempotência (chave idempotente HTTP e consumer idempotente).
- Transactional Outbox, RabbitMQ, contrato de eventos, correlation id.
- Medição (tempo, throughput, thread dump, JFR/Micrometer).

## Fora de escopo

- Kubernetes, service mesh, API gateway, Kafka.
- Elasticsearch (adiado), saga distribuída completa.
- Reescrever a aplicação em reativo (WebFlux).
- Tuning avançado de JVM/GC.

## Critérios de aceite

- [ ] Cada experimento tem **hipótese, método, resultado, conclusão** em
      `docs/labs/` (modelo na issue T3-01).
- [ ] Race condition reproduzida e corrigida de 3 formas, com números.
- [ ] Comparação platform x virtual threads com carga bloqueante, com gráfico
      de tempo/throughput.
- [ ] Condição de corrida real do repo (nome duplicado) reproduzida e
      eliminada com índice único + tratamento de erro.
- [ ] `@Async` com propagação de contexto de segurança/MDC validada.
- [ ] Timeouts explícitos em chamadas externas; Resilience4j com testes de
      estados do circuit breaker.
- [ ] `Idempotency-Key` implementada e testada com requisições concorrentes.
- [ ] Outbox publicada no RabbitMQ sem perda e sem duplicidade lógica
      (consumer idempotente).
- [ ] Segundo serviço mínimo consome eventos; `docker compose` sobe tudo.
- [ ] Issues #16, #17 e #19 referenciam as T3 correspondentes (sem
      trabalho duplicado).

## Riscos e cuidados

- Experimentos de concorrência são **não determinísticos**: repita N vezes,
  use `CountDownLatch`/barreira para largar juntos e registre a variação.
- Medir sem aquecimento (JIT) engana; descarte as primeiras execuções.
- Virtual threads ajudam em I/O bloqueante, não em CPU. `synchronized` longo
  pode "fixar" (pinning) a carrier thread.
- Não use `Thread.sleep` como "correção" de race condition.
