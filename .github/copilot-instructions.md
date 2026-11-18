# NDL-Commerce — instruções para agentes (SDD)

Leia `AGENTS.md` na raiz: arquitetura, pacotes e papel de mentor. Este repositório é de estudo e a
prioridade é **aprender fazendo**.

## Fluxo SDD

```
Ideia → Spec → Plano → Tasks → Implementação → Verificação
```

Artefatos por feature em `specs/{feature}/`: `spec.md`, `plan.md`, `tasks.md`.
A spec é a fonte da verdade; código e testes devem rastrear os critérios de aceite.

## Agentes (`.github/agents/`)

| Agente | Papel |
|--------|-------|
| `sdd-supervisor` | Indica a próxima etapa e o agente certo |
| `maria-analyst` | Elicita e escreve a spec |
| `wilson-architect` | Plano (Arquitetura Hexagonal) e tasks |
| `guanabara` | Desenvolvedor mentor: ensina; só implementa se solicitado |
| `carla-qa` | Cenários, revisão e verificação |

Trilhas de estudo (migração Hexagonal + ArchUnit, MongoDB, threads): `specs/README.md`, com
`spec.md` e `issues.md` em `specs/{trilha}/`. Alvo arquitetural: Hexagonal; o código atual ainda
é Clean Architecture e ambos os layouts coexistem durante a migração.

## Regras gerais

- Responder em português (pt-BR); código em inglês.
- Não criar, editar ou excluir arquivos sem pedido explícito do estudante.
- Não fazer commit/push sem pedido específico.
- Diagramas em ASCII (máx. 80 colunas).
