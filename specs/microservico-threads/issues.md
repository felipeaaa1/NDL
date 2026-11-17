# Issues — Trilha 3: Microsserviço e concorrência

Fase A: T3-01..T3-10 (podem rodar em paralelo à trilha 1). Fase B:
T3-11..T3-18 (após T1-12 no mínimo). Ordem recomendada das issues abertas:
**#16 (T3-14) -> #17 (T3-15) -> #19 (T3-12)**.

Modelo de relatório de experimento (usar em `docs/labs/T3-xx.md`):

```
Hipótese:  o que espero e por quê
Método:    código, parâmetros, N execuções, máquina (CPU/núcleos), JDK
Resultado: tabela com números (média, mín, máx)
Conclusão: o que aprendi; o que me surpreendeu
```

Verificação padrão no app: `./mvnw verify`. Nos labs:
`cd labs/concurrency-lab && ../../mvnw -q exec:java` (ou `java Classe.java`
com source launcher do Java 21).

---

## [TRILHA-3] T3-01 — Montar o laboratório e o modelo de relatório

**Necessidade/contexto:** experimentos precisam de um lugar que não polua o
app nem o `pom.xml`.

**Objetivo:** pasta `labs/concurrency-lab/` com Java 21 e o modelo acima.

**O que você vai aprender:** Java 21 source launcher (`java Main.java`),
estrutura mínima, como medir (`System.nanoTime`, repetição, warm-up).

**Tarefas:**
- [ ] Criar `labs/concurrency-lab/` com `README.md` e um `Main.java` "olá"
      imprimindo `Runtime.getRuntime().availableProcessors()` e
      `Thread.currentThread()`.
- [ ] Criar `docs/labs/_modelo.md` com o modelo de relatório.
- [ ] Criar utilitário `Bench` (medir N execuções e imprimir média/mín/máx).

**Dicas/perguntas:**
- Por que `nanoTime` e não `currentTimeMillis` para medir duração?
- O que é warm-up da JIT e como ele distorce a 1ª execução?

**Arquivos prováveis:** `labs/concurrency-lab/*`, `docs/labs/_modelo.md`.

**Critério de pronto:** `java labs/concurrency-lab/Main.java` imprime núcleos
e nome da thread.

**Dependências:** nenhuma.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-02 — Threads: criar, esperar, estados

**Necessidade/contexto:** base de tudo.

**Objetivo:** entender `Thread`, `Runnable`, `join`, daemon, estados.

**O que você vai aprender:** ciclo de vida (`NEW`, `RUNNABLE`, `BLOCKED`,
`WAITING`, `TIMED_WAITING`), `interrupt`, `Thread.Builder` do Java 21.

**Tarefas:**
- [ ] Criar 3 threads que imprimem nome/ordem; observar a não determinismo.
- [ ] Usar `join` e comparar com não esperar.
- [ ] Imprimir `getState()` em diferentes momentos.
- [ ] Interromper uma thread dormindo e tratar `InterruptedException`
      corretamente (restaurar a flag).
- [ ] Testar daemon x não daemon ao final do `main`.

**Dicas/perguntas:**
- Por que não se deve engolir `InterruptedException`?
- Quantas threads de plataforma a sua máquina cria antes de falhar? (faça
  isso com cuidado, comece com 1.000.)

**Arquivos prováveis:** `labs/concurrency-lab/ThreadBasics.java`.

**Critério de pronto:** relatório `docs/labs/T3-02.md` com a saída observada
e a explicação de cada estado.

**Dependências:** T3-01.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-03 — Experimento: race condition num contador

**Necessidade/contexto:** o exemplo clássico, mas medido.

**Objetivo:** provocar perda de atualização e corrigir de 4 formas.

**O que você vai aprender:** atomicidade de `count++` (ler-modificar-gravar),
`synchronized`, `ReentrantLock`, `AtomicInteger`, `LongAdder`, custo de cada.

**Tarefas:**
- [ ] 8 threads x 1.000.000 incrementos num `int` simples; esperar 8.000.000
      e ver o resultado real (repetir 10 vezes).
- [ ] Corrigir com `synchronized`.
- [ ] Corrigir com `ReentrantLock`.
- [ ] Corrigir com `AtomicInteger`.
- [ ] Corrigir com `LongAdder`.
- [ ] Tabela de tempo (média de 10 execuções) e de resultado correto.
- [ ] Largar todas as threads juntas com `CountDownLatch`.

**Dicas/perguntas:**
- Por que `volatile int` **não** resolve `count++`?
- Quando `LongAdder` ganha de `AtomicLong`? E quando não faz diferença?
- Se o resultado deu correto "por sorte" com poucas threads, o que isso
  ensina sobre testar concorrência?

**Arquivos prováveis:** `labs/concurrency-lab/RaceCounter.java`.

**Critério de pronto:** relatório com tabela de 5 variantes (resultado e
tempo) e conclusão.

**Dependências:** T3-02.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-04 — Visibilidade, `volatile` e deadlock

**Necessidade/contexto:** além da atomicidade, há visibilidade e ordem
(modelo de memória do Java).

**Objetivo:** reproduzir um loop que nunca termina por falta de `volatile` e
um deadlock, e diagnosticá-los.

**O que você vai aprender:** happens-before, `volatile`, ordem de aquisição de
locks, `jstack`/`jcmd Thread.print`.

**Tarefas:**
- [ ] Flag `running` não `volatile` lida em loop por outra thread; observar
      (pode depender de JIT); depois corrigir.
- [ ] Criar deadlock com 2 locks em ordem inversa.
- [ ] Capturar thread dump com `jcmd <pid> Thread.print` e localizar o ciclo.
- [ ] Corrigir impondo ordem global de locks ou `tryLock` com timeout.

**Dicas/perguntas:**
- O que o JIT pode fazer com leitura de variável não `volatile` em loop?
- Quais as 4 condições de Coffman e qual você quebrou?

**Arquivos prováveis:** `labs/concurrency-lab/Visibility.java`,
`Deadlock.java`.

**Critério de pronto:** relatório com trecho do thread dump que mostra o
deadlock e a correção validada (programa termina).

**Dependências:** T3-03.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-05 — ExecutorService: pools, Future e shutdown

**Necessidade/contexto:** criar thread manualmente não escala.

**Objetivo:** usar pools corretamente e dimensionar.

**O que você vai aprender:** `newFixedThreadPool`, `newCachedThreadPool`,
`ThreadPoolExecutor` (fila, rejeição), `Future.get(timeout)`,
`shutdown`/`awaitTermination`, dimensionamento CPU-bound x IO-bound.

**Tarefas:**
- [ ] Executar 100 tarefas CPU-bound com pools de 1, N núcleos e 4N; medir.
- [ ] Executar 100 tarefas IO-bound (`sleep(200ms)`) nos mesmos pools.
- [ ] Criar `ThreadPoolExecutor` com fila limitada e `RejectedExecutionHandler`;
      provocar rejeição.
- [ ] Usar `Future.get` com timeout e cancelar tarefa.
- [ ] Fechar o executor com `try-with-resources` (Java 21 `close()`).

**Dicas/perguntas:**
- Pool ilimitado (`cachedThreadPool`) sob carga: qual o risco?
- Fórmula de dimensionamento: de que depende a proporção
  espera/computação?
- O que acontece com exceção lançada dentro de `execute` x `submit`?

**Arquivos prováveis:** `labs/concurrency-lab/Pools.java`.

**Critério de pronto:** relatório com tabela tempo x tamanho do pool para
CPU e IO.

**Dependências:** T3-03.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-06 — CompletableFuture: composição, erros e timeout

**Necessidade/contexto:** microsserviços chamam vários serviços; é preciso
compor chamadas.

**Objetivo:** modelar 3 chamadas lentas (simuladas) em paralelo e combinar.

**O que você vai aprender:** `supplyAsync`, `thenCompose`, `thenCombine`,
`allOf`, `exceptionally`/`handle`, `orTimeout`/`completeOnTimeout`, qual
executor é usado por padrão (`ForkJoinPool.commonPool`).

**Tarefas:**
- [ ] 3 serviços falsos (200/300/500 ms): sequencial x paralelo, comparar.
- [ ] `thenCombine` para agregar e `allOf` para N.
- [ ] Uma das chamadas falha: tratar com `handle` e valor padrão.
- [ ] `orTimeout(400ms)`; observar a exceção.
- [ ] Usar executor próprio em vez do common pool; observar nomes de threads.

**Dicas/perguntas:**
- `join()` x `get()`: diferença de exceção.
- Por que bloquear dentro de `commonPool` é perigoso?
- Timeout cancela a tarefa de fundo? (não necessariamente)

**Arquivos prováveis:** `labs/concurrency-lab/Futures.java`.

**Critério de pronto:** relatório com tempos sequencial x paralelo e
comportamento dos erros/timeouts.

**Dependências:** T3-05.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-07 — Virtual threads: medir com e sem

**Necessidade/contexto:** Java 21 trouxe virtual threads (Project Loom).

**Objetivo:** comparar plataforma x virtual em I/O bloqueante e em CPU.

**O que você vai aprender:** `Thread.ofVirtual()`,
`Executors.newVirtualThreadPerTaskExecutor()`, por que 100.000 threads
virtuais cabem, pinning com `synchronized`, `jdk.tracePinnedThreads`.

**Tarefas:**
- [ ] 10.000 tarefas com `sleep(1s)`: pool de plataforma (200) x virtual;
      medir tempo total e memória.
- [ ] 10.000 tarefas CPU-bound: qual ganha?
- [ ] Criar 100.000 virtual threads; tentar o mesmo com plataforma
      (cuidado, comece por 5.000).
- [ ] Reproduzir pinning (sleep dentro de `synchronized`) e observar com
      `-Djdk.tracePinnedThreads=full`.
- [ ] Substituir `synchronized` por `ReentrantLock` e reavaliar.

**Dicas/perguntas:**
- Virtual threads são mais rápidas? Ou só **mais baratas de bloquear**?
- Por que não se faz pool de virtual threads?
- O que é a carrier thread e quantas existem por padrão?

**Arquivos prováveis:** `labs/concurrency-lab/VirtualThreads.java`.

**Critério de pronto:** relatório com tabela comparativa (tempo, memória,
nº de threads) e conclusão de quando usar.

**Dependências:** T3-05.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-08 — Virtual threads no Spring Boot 3.3

**Necessidade/contexto:** `spring.threads.virtual.enabled=true` troca o
executor do Tomcat (e de `@Async`/scheduling) por virtual threads.

**Objetivo:** medir a API real com e sem a flag.

**O que você vai aprender:** onde o gargalo realmente está (HikariCP,
Tomcat `max-threads`), observar nomes de threads, teste de carga básico.

**Tarefas:**
- [ ] Criar profile `loadtest` (não altere o padrão) com a flag ligada/
      desligada por propriedade.
- [ ] Criar endpoint de laboratório (apenas no profile `loadtest`) que
      simula I/O bloqueante (`sleep`) — não misture com endpoints de negócio.
- [ ] Rodar carga (ex.: `hey`, `k6`, `ab` ou script Java) com 500 conexões
      simultâneas nos dois modos.
- [ ] Logar `Thread.currentThread()` no endpoint para ver
      `http-nio-8080-exec-N` x `VirtualThread[#..]`.
- [ ] Repetir com endpoint que usa o banco; observar o pool Hikari
      (`maximumPoolSize`).
- [ ] Registrar p50/p95/p99, throughput e uso de memória.

**Dicas/perguntas:**
- Se o pool de conexões tem 10, adianta ter 10.000 threads?
- Qual o efeito em `ThreadLocal` (SecurityContext, MDC)?
- Por que não ligar a flag em produção "só porque é novo"?

**Arquivos prováveis:** `application-loadtest.yml` (novo), classe de
laboratório em `src/main/java/.../lab` (restrita ao profile `loadtest`),
`docs/labs/T3-08.md`.

**Critério de pronto:** relatório com métricas dos dois modos; endpoint de
laboratório inexistente fora do profile (`@Profile("loadtest")`).

**Dependências:** T3-07.
**Labels:** `trilha-3`, `experimento`, `concorrencia`.

---

## [TRILHA-3] T3-09 — `@Async`: executor, proxy e propagação de contexto

**Necessidade/contexto:** envio de e-mail (`EmailService`/SendGrid) é
candidato a async; `SecurityContextHolder` usa `ThreadLocal`.

**Objetivo:** executar uma tarefa em background corretamente.

**O que você vai aprender:** `@EnableAsync`, `ThreadPoolTaskExecutor`,
armadilha de self-invocation (proxy), `TaskDecorator`, propagar
`SecurityContext`/MDC, tratamento de exceções (`AsyncUncaughtExceptionHandler`).

**Tarefas:**
- [ ] Criar um serviço de laboratório com `@Async` e provar que roda em outra
      thread (log do nome).
- [ ] Reproduzir self-invocation (chamar método `@Async` da própria classe) e
      observar que não é assíncrono.
- [ ] Configurar executor nomeado com fila limitada e política de rejeição.
- [ ] Mostrar que o usuário logado **some** na thread async e propagar com
      `DelegatingSecurityContextAsyncTaskExecutor` ou `TaskDecorator`.
- [ ] Capturar exceção de método `void` async.
- [ ] Comparar com `virtual threads` ligadas.

**Dicas/perguntas:**
- O que acontece com a transação do chamador dentro do método async?
- Fire-and-forget perde a falha de e-mail: qual o custo? (conecta com Outbox
  em T3-14).

**Arquivos prováveis:** `config/AsyncConfig` (novo), serviço de laboratório,
`config/EmailService`/`adapters.out.email` (observação, não obrigatório).

**Critério de pronto:** teste que prova thread distinta, contexto de
segurança propagado e exceção tratada (`./mvnw -Dtest='*Async*' test`).

**Dependências:** T3-08; T1-10 (usuário logado atrás de porta) recomendada.
**Labels:** `trilha-3`, `concorrencia`, `testes`.

---

## [TRILHA-3] T3-10 — Race condition real: nome duplicado (check-then-act)

**Necessidade/contexto:** em Brand/Category, o service faz
`existsByName(...)` e depois `save(...)`. Duas requisições simultâneas
passam no `exists` e criam duplicatas se não houver restrição no banco.

**Objetivo:** reproduzir, medir e corrigir a corrida do repositório.

**O que você vai aprender:** thread safety em serviços stateless x estado no
banco, restrições únicas como fonte da verdade, tradução de
`DataIntegrityViolationException`, otimismo x pessimismo.

**Tarefas:**
- [ ] Escrever teste que dispara N `POST /brands` (mesmo nome) simultâneos
      com `ExecutorService` + `CountDownLatch`; contar quantas criaram.
- [ ] Verificar o schema (migrations `V5`, `V11`): existe unique parcial para
      nome ativo? Documente.
- [ ] Corrigir com índice único (nova migration) + tratar a exceção e
      devolver o mesmo erro de "já existe".
- [ ] Repetir o teste: exatamente 1 sucesso, N-1 conflitos.
- [ ] Comparar com lock pessimista/`synchronized` (por que `synchronized`
      **não** serve com múltiplas instâncias?).
- [ ] Revisar o repo em busca de estado mutável em singletons
      (`@Service`/`@Component` com campos) e listar achados.

**Dicas/perguntas:**
- Onde está a verdade da unicidade: no código ou no banco?
- Com H2 (testes) e Postgres (produção) o comportamento é o mesmo? Use
  Testcontainers (T2-02) se precisar.
- Se Mongo (trilha 2), qual o equivalente? (índice único parcial)

**Arquivos prováveis:** `application/service/BrandService`,
`src/main/resources/db/migration/V12__*.sql` (novo), teste de concorrência.

**Critério de pronto:** teste de concorrência determinístico no resultado
(1 sucesso) verde: `./mvnw -Dtest='*Concurrent*' test`.

**Dependências:** T3-03, T3-05; T1-12 recomendada (opcional T2-07).
**Labels:** `trilha-3`, `concorrencia`, `testes`.

---

## [TRILHA-3] T3-11 — Timeouts em chamadas externas

**Necessidade/contexto:** o sistema chama SendGrid e chamará outros serviços;
sem timeout, uma dependência lenta consome threads (e virtual threads só
adiam o problema).

**Objetivo:** todo cliente externo com connect/read timeout explícitos.

**O que você vai aprender:** `RestClient`/`HttpClient` timeouts,
`spring.mvc.async.request-timeout`, timeout do Hikari, `@Transactional(timeout)`,
timeout em cascata (orçamento de tempo).

**Tarefas:**
- [ ] Criar um **serviço falso lento** (ex.: WireMock, ou endpoint do
      profile `loadtest`) com latência configurável.
- [ ] Chamá-lo com `RestClient` sem timeout; observar a thread presa.
- [ ] Configurar connect/read timeout; observar a exceção.
- [ ] Definir o orçamento de tempo (cliente 3s -> API 2s -> dependência 1s) e
      explicar a relação.
- [ ] Mapear a exceção de timeout para HTTP 504/503 de forma consistente.

**Dicas/perguntas:**
- Qual a diferença entre connect timeout e read timeout?
- Timeout maior que o do chamador: por que é inútil?
- Retentar após timeout é seguro? (conecta com T3-13)

**Arquivos prováveis:** `adapters/out/http/*` (novo), `config/*`,
`adapters/in/web/exception/GlobalExceptionHandler`.

**Critério de pronto:** teste com serviço falso lento prova a exceção em
tempo `<= timeout + margem`.

**Dependências:** T1-12; T3-06.
**Labels:** `trilha-3`, `resiliencia`, `testes`.

---

## [TRILHA-3] T3-12 — Resiliência com Resilience4j (issue #19, reescopada)

**Necessidade/contexto:** a issue #19 pede resiliência para a sincronização de
busca (Elasticsearch, adiado). Aplicamos os mesmos conceitos ao **consumer e
ao cliente HTTP** desta trilha.

**Objetivo:** Retry, CircuitBreaker, Bulkhead e TimeLimiter nos clientes e
no consumer.

**O que você vai aprender:** estados do circuit breaker
(CLOSED/OPEN/HALF_OPEN), retry com backoff exponencial e jitter, bulkhead,
fallback, métricas Actuator.

**Tarefas:**
- [ ] Adicionar `resilience4j-spring-boot3` (+ AOP).
- [ ] `@Retry` com backoff exponencial **somente** em falhas transitórias.
- [ ] `@CircuitBreaker` com fallback; provocar abertura com o serviço falso.
- [ ] `@Bulkhead` para limitar concorrência e provar a rejeição.
- [ ] Testes que verificam a transição de estados.
- [ ] Expor métricas em `/actuator` e observar.
- [ ] Atualizar a issue #19 com link para esta issue e o novo escopo.

**Dicas/perguntas:**
- Retry + circuit breaker: qual envolve qual? (ordem dos aspectos)
- Retry sem idempotência: o que pode duplicar? (T3-13)
- O que é "retry storm" e como o jitter ajuda?

**Arquivos prováveis:** `pom.xml`, `application.yml`,
`adapters/out/http/*`, `adapters/in/messaging/*` (T3-16).

**Critério de pronto:** teste que abre e fecha o circuito:
`./mvnw -Dtest='*CircuitBreaker*' test`.

**Dependências:** T3-11, T3-13, T3-15 (para o consumer).
**Labels:** `trilha-3`, `resiliencia`, `testes`.

---

## [TRILHA-3] T3-13 — Idempotência: `Idempotency-Key` em POST

**Necessidade/contexto:** com retry (cliente, gateway ou broker), a mesma
operação chega mais de uma vez; `POST /brands` não é idempotente.

**Objetivo:** repetir a mesma requisição devolve o mesmo resultado sem
duplicar efeito.

**O que você vai aprender:** idempotência x segurança de método HTTP, tabela
de chaves, concorrência de requisições com a mesma chave (de novo race!),
`ON CONFLICT`/unique, TTL.

**Tarefas:**
- [ ] Definir o contrato (header `Idempotency-Key`, resposta repetida, 409/
      422 se payload diferente).
- [ ] Tabela `idempotency_key` (migration) com chave única, hash do request e
      resposta.
- [ ] Implementar como porta `out` + adapter (hexagonal).
- [ ] Teste concorrente: 10 requisições simultâneas com a mesma chave ->
      1 efeito.
- [ ] Definir expiração e limpeza.

**Dicas/perguntas:**
- Como tratar "primeira requisição ainda em processamento"?
- O que armazenar: resposta completa ou só o id do recurso?
- Idempotência **de consumer** é a mesma coisa? (T3-15)

**Arquivos prováveis:** `db/migration/V13__*.sql`,
`application/port/out/IdempotencyPort`, `adapters/in/web` (filtro/interceptor).

**Critério de pronto:** teste concorrente verde (1 efeito colateral):
`./mvnw -Dtest='*Idempotency*' test`.

**Dependências:** T3-10; T1-12.
**Labels:** `trilha-3`, `resiliencia`, `concorrencia`, `testes`.

---

## [TRILHA-3] T3-14 — Transactional Outbox (issue #16)

**Necessidade/contexto:** a issue **#16** já pede Outbox para o catálogo.
Aqui ela é o laboratório de "gravar e publicar sem dual-write".

**Objetivo:** gravar entidade e evento na **mesma transação** e publicar de
forma assíncrona, com poller seguro para várias instâncias.

**O que você vai aprender:** problema do dual-write, tabela outbox,
`@Transactional`, `SELECT ... FOR UPDATE SKIP LOCKED`, at-least-once,
`@Scheduled` (e sua concorrência), ordenação.

**Tarefas:**
- [ ] Demonstrar o problema: salvar + publicar (fake) com falha entre os dois.
- [ ] Migration da tabela `outbox_event` (id, aggregate, tipo, payload JSON,
      criado em, processado em).
- [ ] Gravar o evento na mesma transação do caso de uso (porta out
      `EventPublisherPort`; adapter grava na outbox).
- [ ] Poller `@Scheduled` lendo lotes com `SKIP LOCKED`.
- [ ] Teste: 2 pollers concorrentes não processam o mesmo evento.
- [ ] Definir retenção/limpeza e comentar na issue #16 o novo escopo.

**Dicas/perguntas:**
- O que acontece se o poller morre depois de publicar e antes de marcar como
  processado? (duplicidade -> consumer idempotente)
- `@Scheduled` com várias instâncias: roda em todas? Como evitar?
- Ordem de eventos por agregado: o que garante?

**Arquivos prováveis:** `db/migration/V14__*.sql`,
`adapters/out/persistence/jpa/outbox/*`, `application/port/out/*`.

**Critério de pronto:** teste de concorrência de pollers verde:
`./mvnw -Dtest='*Outbox*' test`.

**Dependências:** T3-13; T1-12 (+ Postgres via Testcontainers, T2-02).
**Labels:** `trilha-3`, `mensageria`, `concorrencia`, `testes`.

---

## [TRILHA-3] T3-15 — RabbitMQ: publicar a Outbox (issue #17)

**Necessidade/contexto:** issue **#17** ("Adicionar RabbitMQ e publicar
eventos da Outbox"). Mantém o escopo, acrescentando os conceitos de
confiabilidade como critérios.

**Objetivo:** publicar eventos da outbox e consumi-los com confiabilidade.

**O que você vai aprender:** exchange/queue/binding, publisher confirms,
ack manual, DLQ, retry com backoff, prefetch, concorrência de listeners,
mensagem venenosa.

**Tarefas:**
- [ ] Adicionar RabbitMQ ao `compose.yaml` (com management UI).
- [ ] `spring-boot-starter-amqp`; adapter `out` `RabbitEventPublisher`.
- [ ] Publisher confirms: só marcar outbox como processada após confirm.
- [ ] Consumer com ack manual, `prefetch`, `concurrency`.
- [ ] DLQ para mensagens que falham; provocar com payload inválido.
- [ ] Teste de integração com Testcontainers RabbitMQ.
- [ ] Atualizar a issue #17 com link e escopo.

**Dicas/perguntas:**
- At-least-once x at-most-once x exactly-once: qual você tem e por quê?
- `concurrency=5`: ordem de processamento ainda é garantida?
- Quem decide quando mandar para a DLQ: broker ou aplicação?

**Arquivos prováveis:** `compose.yaml`, `pom.xml`, `application.yml`,
`adapters/out/messaging/rabbit/*`, `adapters/in/messaging/*`.

**Critério de pronto:** evento criado via API chega ao consumer; mensagem
inválida cai na DLQ: `./mvnw -Dtest='*Rabbit*' test`.

**Dependências:** T3-14.
**Labels:** `trilha-3`, `mensageria`, `testes`.

---

## [TRILHA-3] T3-16 — Segundo serviço: consumer idempotente (substitui #18)

**Necessidade/contexto:** a issue #18 (consumir e sincronizar Elasticsearch)
está adiada. Ainda assim precisamos de um **consumidor em outro serviço**
para estudar comunicação entre microsserviços.

**Objetivo:** serviço mínimo (`labs/catalog-consumer/`) que consome os eventos
e mantém um read model simples (ex.: lista de marcas em memória/H2 ou arquivo).

**O que você vai aprender:** contrato de evento (versionamento, campos
obrigatórios), consumer idempotente (`processed_event` com unique),
acoplamento temporal, autonomia entre serviços.

**Tarefas:**
- [ ] Projeto Spring Boot separado em `labs/catalog-consumer/` (pom próprio).
- [ ] Definir o contrato JSON do evento (`eventId`, `type`, `version`,
      `occurredAt`, `payload`); documentar em `docs/eventos.md`.
- [ ] Consumer idempotente: reentrega do mesmo `eventId` não duplica.
- [ ] Tratar evento fora de ordem e versão desconhecida.
- [ ] `docker compose` sobe app + rabbit + consumer.
- [ ] Teste: reenviar o mesmo evento 5x -> 1 efeito.

**Dicas/perguntas:**
- Quem é dono do contrato: produtor ou consumidor? Como evoluir sem quebrar?
- O que o consumer faz se o evento chegar antes de a marca existir
  localmente?
- Para a #18: qual seria a única mudança para plugar Elasticsearch aqui
  depois? (um novo adapter de saída)

**Arquivos prováveis:** `labs/catalog-consumer/**`, `compose.yaml`,
`docs/eventos.md`.

**Critério de pronto:** teste de reentrega verde; `docker compose up` sobe
os 3 serviços e o evento fluxa.

**Dependências:** T3-15, T3-12 (resiliência no consumer, em paralelo).
**Labels:** `trilha-3`, `mensageria`, `microsservico`.

---

## [TRILHA-3] T3-17 — Correlation id e observabilidade entre serviços

**Necessidade/contexto:** com dois serviços e threads diferentes, sem
correlação é impossível depurar.

**Objetivo:** um `X-Correlation-Id` atravessar HTTP -> outbox -> RabbitMQ ->
consumer, com MDC nos logs.

**O que você vai aprender:** MDC (`ThreadLocal`) e sua propagação em
async/virtual threads, headers de mensagem, Micrometer, JFR, `jcmd`.

**Tarefas:**
- [ ] Filtro que cria/propaga `X-Correlation-Id` e põe no MDC.
- [ ] Gravar o id no evento da outbox e como header AMQP.
- [ ] Consumer coloca o id no MDC e loga.
- [ ] Validar que o id **não se perde** em `@Async` (T3-09).
- [ ] Expor métricas (`/actuator/metrics`) de pool/Hikari/Rabbit e ler.
- [ ] Gravar JFR de 60s sob carga e abrir no JDK Mission Control (anotar 3
      achados).

**Dicas/perguntas:**
- Onde o MDC se perde? E com virtual threads?
- Qual a diferença entre log, métrica e trace?

**Arquivos prováveis:** `adapters/in/web/*Filter`, `application.yml`,
`labs/catalog-consumer/**`.

**Critério de pronto:** um único id aparece nos logs dos dois serviços para
uma criação de marca (evidência no PR).

**Dependências:** T3-16.
**Labels:** `trilha-3`, `microsservico`, `resiliencia`.

---

## [TRILHA-3] T3-18 — Retrospectiva e plano de próximos passos

**Necessidade/contexto:** consolidar o aprendizado e decidir o que é retomado
(ex.: Elasticsearch).

**Objetivo:** documento final e atualização das issues abertas.

**O que você vai aprender:** sintetizar trade-offs; decidir com evidência.

**Tarefas:**
- [ ] `docs/labs/retrospectiva.md`: 10 lições, 5 erros que cometeu, o que
      mediu que mais surpreendeu.
- [ ] Tabela "problema -> técnica -> issue/PR" (race -> unique index; retry
      -> idempotência; dual-write -> outbox; etc.).
- [ ] Atualizar #16, #17, #19 (fechar como concluídas pelas T3) e decidir
      se #10-#15, #18, #20 voltam ao backlog.
- [ ] Propor 3 experimentos futuros (ex.: saga, CQRS, k6 contínuo).

**Dicas/perguntas:**
- Que decisão você tomaria diferente com os números que tem agora?
- Elasticsearch seria "só mais um adapter"? Prove com a arquitetura.

**Arquivos prováveis:** `docs/labs/retrospectiva.md`, `specs/README.md`.

**Critério de pronto:** documento no PR e issues abertas atualizadas
(revisão do mentor).

**Dependências:** T3-17.
**Labels:** `trilha-3`, `docs`, `estudo`.
