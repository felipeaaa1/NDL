# Política de violações arquiteturais

## Objetivo

O ArchUnit protege a migração gradual da arquitetura atual (`entity` /
`useCase` / `adapters` / `config`) para o layout Hexagonal. O congelamento
permite manter o build funcionando enquanto violações antigas são corrigidas,
mas não significa que essas violações sejam aceitas como arquitetura desejada.

As dependências devem apontar para dentro:

```text
adapters -> application -> domain
```

No código legado, o equivalente é `adapters -> useCase -> entity`. Dependências
de `useCase` para `adapters` ou `config` são dívida arquitetural, não exemplos
a serem reproduzidos.

## Regras congeladas e inventário

As regras estão em
`src/test/java/com/ndlcommerce/architecture/HexagonalRulesTest.java`. A
configuração fica em `src/test/resources/archunit.properties`, e o store em
`src/test/resources/archunit_store/`. O arquivo `stored.rules` associa a
descrição de cada regra ao arquivo que contém suas violações.

Inventário registrado pela T1-03 e quantidade atualmente no store em
19/11/2026:

| Regra | T1-03 | Store atual | Exemplos da dívida |
|---|---:|---:|---|
| `useCase` depende de `adapters` | 87 | 70 | Gateways e interactors ainda expõem ou chamam `*DataMapper` JPA. |
| `useCase` depende de `config` | 3 | 3 | O caso de uso de usuário depende de `EmailService` em `config`. |
| DTOs de `useCase.request` dependem de `jakarta` | 47 | 47 | DTOs da aplicação carregam anotações de validação. |
| Ciclos entre pacotes | 3 | 3 | Ciclos envolvendo `adapters`, `useCase` e `config`. |

As contagens são por regra e podem se sobrepor; não devem ser somadas como
quantidade de problemas únicos. O store é a referência para os detalhes de cada
violação. A T1-03 registrou, por exemplo, `useCase -> adapters.persistence`
e ciclos entre pacotes. A dependência
`CategoryRegisterInteractor.create -> CategoryDataMapper.getCreatedAt()` foi
removida durante a correção de Category; a redução de uma violação específica
não elimina as demais dívidas de Customer, User e EmailValidation.

## Política do store

1. O baseline nunca pode crescer: uma alteração do store em um PR deve conter
   somente violações resolvidas removidas. Inclusões ou substituições precisam
   ser investigadas e não são aprovadas como atualização rotineira.
2. `allowStoreCreation=false`, `allowStoreUpdate=false` e `freeze.refreeze=false`
   são os padrões. Assim, uma execução normal não cria nem aceita alterações
   automaticamente no baseline.
3. Uma nova violação não registrada deve fazer o teste falhar. Não habilite
   atualização ou refreeze para silenciar a falha; primeiro determine se a
   dependência é nova ou se a descrição de uma dívida antiga mudou.
4. Quando uma violação conhecida for corrigida, o ArchUnit pode removê-la do
   store se a atualização for permitida nessa execução. Revise o diff e confirme
   que somente registros obsoletos desapareceram.
5. `freeze.refreeze=true` substitui as violações registradas pelas violações
   encontradas na execução e faz a regra passar. Como todas as regras congeladas
   executadas podem ser afetadas, refreeze não é um comando de validação nem
   deve ser usado rotineiramente. Só use em uma atualização deliberada do
   baseline, após revisar todas as violações atuais e o diff resultante.
6. Os arquivos do store precisam estar versionados para que outras máquinas e o
   CI usem o mesmo baseline. Antes de versioná-los, confirme que a comparação
   com o baseline anterior atende à política de redução.

O matcher padrão do ArchUnit desconsidera números de linha e números gerados de
classes anônimas ou lambdas, mas compara o restante da descrição da violação.
Por isso, mover classes ou alterar relações de dependência pode fazer uma
violação conhecida aparecer com outra descrição. A regra de ciclos é
particularmente sensível: seu relatório agrega o caminho e as dependências dos
pacotes. Uma falha de freeze nessa regra exige comparar os ciclos e suas arestas
com o baseline; não prova, por si só, que o código introduziu um ciclo novo.

## Rotina de validação

Execute o teste focal sem sobrescrever propriedades:

```powershell
.\mvnw.cmd "-Dtest=HexagonalRulesTest" test
```

O teste deve passar com as propriedades padrão. Uma falha por violação nova
exige corrigir o código ou justificar, com evidência, uma atualização controlada
do baseline. Depois, execute a validação completa:

```powershell
.\mvnw.cmd verify
```

No review do PR, inspecione as alterações em
`src/test/resources/archunit_store/`. Aceite apenas remoções explicadas de
violações corrigidas; rejeite aumentos ou mudanças não justificadas. Arquivos
ainda não adicionados ao controle de versão não aparecem no diff normal do Git.

## Freeze versus lista explícita de exceções

| Abordagem | Vantagens | Desvantagens |
|---|---|---|
| `FreezingArchRule` + store | Registra em lote violações existentes; permite acompanhar a remoção incremental sem tornar o build legado permanentemente vermelho. | O store é menos legível que regras de negócio; mudanças na descrição podem gerar violações não reconhecidas; refreeze pode aceitar regressões se usado sem revisão. |
| Lista explícita de exceções, por exemplo `.and(not(exceptions))` | Explicita no código o que está temporariamente permitido; cada exceção pode ter contexto e escopo claros. | Exige manutenção manual; listas extensas ficam verbosas e são fáceis de deixar obsoletas; a remoção precisa ser coordenada com a regra. |

Para a migração atual, o store é adequado ao volume das violações preexistentes,
desde que seja revisado como baseline temporário e só diminua. Exceções
explícitas podem ser preferíveis para poucos casos pontuais e bem delimitados.

## Referência

- [ArchUnit User Guide — Freezing Arch Rules](https://www.archunit.org/userguide/html/000_Index.html#_freezing_arch_rules)
