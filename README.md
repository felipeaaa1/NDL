# 🛒 NDL-Commerce

NDL-Commerce é um projeto de **E-commerce** desenvolvido em **Java 21** com **Spring Boot**, seguindo os princípios da **Arquitetura Limpa (Clean Architecture)**.

O objetivo é construir uma aplicação modular, escalável e de fácil manutenção, separando regras de negócio de frameworks e detalhes de infraestrutura.

---

## 📖 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **Flyway**
- **PostgreSQL**
- **JPA/Hibernate**
- **Arquitetura Limpa (Clean Architecture)**

---

## 🔐 Segurança & Fluxo de Desenvolvimento

Este repositório possui:

- **Política formal de segurança**, documentada em `SECURITY.md`.
- **Proteções de branch** aplicadas à `main`, incluindo:
  - merges obrigatoriamente via Pull Request
  - histórico linear
  - validação de checks automatizados
  - bloqueio de force-push e exclusões sem permissão
  - exigência de revisão por outro usuário

Essas práticas seguem padrões amplamente utilizados no mercado para garantir integridade do código e rastreabilidade das entregas.

---

## 🚀 Branches

- **`dev`**  
  Branch principal de desenvolvimento.  
  Aqui ficam as implementações em andamento, novas features e ajustes antes da revisão.

- **`main`**  
  Branch estável e protegida.  
  Todas as entregas passam por Pull Request, revisão e validações antes do merge.

---

## 📦 Migrações de Banco de Dados

O projeto utiliza **Flyway** para versionamento de schema.  
As migrações ficam em `src/main/resources/db/migration`.

### 🔧 Configuração

Adicione um profile `flyway` no `settings.xml` do Maven:

```xml
<settings>
    <profiles>
        <profile>
            <id>flyway</id>
            <properties>
                <flyway.url>jdbc:postgresql://localhost:5433/ndlcommerce</flyway.url>
                <flyway.user>ndl_user</flyway.user>
                <flyway.password>ndl_pass</flyway.password>
            </properties>
        </profile>
    </profiles>
</settings>
```
### ▶️ Executando as migrações
``` bash
mvn flyway:migrate -Pflyway
```
## 📌 Status do Projeto

O desenvolvimento segue ativo na branch dev, incluindo:

validações de domínio

tratativas de exceções

camada de produto (entidade, fábrica, presenter e use case)

testes unitários

A branch main contém apenas código revisado e aprovado via PR.
