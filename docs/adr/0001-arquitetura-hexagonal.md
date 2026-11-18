# ADR 0001 — Arquitetura Hexagonal

**Status:** decisões registradas; revisão final e PR pendentes.

## Contexto

O NDL-Commerce usa uma organização inspirada em Clean Architecture, com
`entity`, `useCase`, `adapters` e `config`. A regra de dependências também
aponta para dentro, mas há violações: por exemplo, o gateway de Category em
`useCase` expõe `CategoryDataMapper`, uma entidade JPA de `adapters`.

Esse acoplamento faz a aplicação conhecer um detalhe de persistência e dificulta
a troca ou a inclusão de outro adapter. A trilha
`specs/hexagonal-archunit/` propõe uma migração incremental para explicitar
portas e adapters e proteger os limites com ArchUnit.

## Decisão: direção arquitetural

Adotar como alvo a Arquitetura Hexagonal descrita na spec, preservando o
comportamento existente durante a migração:

- `domain`: modelos e regras de domínio, sem dependências de infraestrutura.
- `application`: casos de uso e suas portas de entrada e saída.
- `adapters.in`: traduzem chamadas externas, como HTTP, para portas de entrada.
- `adapters.out`: implementam portas de saída para recursos externos, como JPA.
- `config`: conecta os componentes e pode conhecer as demais camadas.

As dependências de código apontam para o núcleo:
`adapters -> application -> domain`. A aplicação define suas portas; os
adapters dependem delas, não o contrário.

O de-para inicial está em `specs/hexagonal-archunit/spec.md`. Durante a
migração, os pacotes atuais e os novos coexistirão temporariamente; isso não é
o estado final desejado.

### Exceções

Centralizar as exceções específicas da aplicação em `application.exception`,
aproximando a nomenclatura da utilizada no serviço gerência-convivência
consultado. O adapter web traduz essas exceções para respostas HTTP; as
exceções não definem status HTTP.

O domínio não pode importar `application.exception`. Para começar por Brand,
a validação do nome continuará retornando um booleano; a aplicação consultará
esse resultado e lançará a exceção apropriada quando necessário. O adapter web
deverá preservar a resposta HTTP existente.

Esse caminho é próximo do código atual, mas depende de o chamador verificar
o resultado e não informa o motivo da falha. Reavaliar a sinalização em
outras features caso suas regras precisem de mais detalhes ou de garantias
mais fortes.

### Nomes

Adotar os seguintes nomes para a migração, usando Brand como exemplo:

| Papel | Nome |
|------|------|
| Porta de entrada | `BrandUseCase` |
| Porta de saída de persistência | `BrandPersistencePort` |
| Adapter de persistência | `BrandPersistenceAdapter` |
| Entidade JPA | `BrandJpaEntity` |

`*UseCase` e `*PersistencePort` seguem a nomenclatura observada no
gerência-convivência. `*PersistenceAdapter` e `*JpaEntity` seguem o alvo da
spec do NDL; não são apresentados como uma convenção universal do banco.

### Apresentação dos resultados

Manter o Presenter como porta de saída em `application.port.out`. O caso de
uso depende dessa interface, independente de HTTP; sua implementação pertence
ao adapter web em `adapters.in.web.presenter`. O controller continua
responsável pela resposta HTTP.

O formatter atual de Brand formata datas e sinaliza erros. Sua migração deve
preservar o contrato HTTP e o comportamento existente, respeitando a decisão
de tratar a falha de validação na aplicação.

## Alternativas consideradas

Escolhemos migrar por fatias para aprender e validar cada feature durante
o processo.

## Consequências

- A migração será incremental, começando pela fatia Brand prevista na spec.
- As portas serão contratos da aplicação, independentes de JPA e HTTP.
- O código existente precisará de adaptações e testes para preservar os
  contratos e comportamentos; nomes de pacotes, sozinhos, não garantem os
  limites.
- As regras ArchUnit e o tratamento das violações preexistentes serão
  implementados nas issues posteriores, não por este ADR.
- O domínio não lançará diretamente exceções de `application.exception`;
  em Brand, a aplicação deverá verificar o resultado da validação.
- O Presenter acrescenta uma porta e uma implementação de apresentação, mantendo
  o caso de uso independente dos detalhes HTTP.
