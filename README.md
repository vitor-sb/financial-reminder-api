# Financial Reminder API

API backend para **gerenciar despesas pessoais por lembretes de pagamento**. O usuário cadastra contas únicas ou recorrentes, o sistema gera as transações de cada vencimento, e o usuário registra o pagamento e acompanha o que vem pela frente na visão mensal.

Visão de produto: [`tasks/financial-reminder-api-v0.2.md`](tasks/financial-reminder-api-v0.2.md). Plano de fechamento da v1: [`tasks/prd-financial-reminder-v1.md`](tasks/prd-financial-reminder-v1.md).

## Como funciona

1. O usuário se cadastra (`POST /users`) e faz login (`POST /auth/login`), que devolve um JWT.
2. Cria um **lembrete** (`PaymentReminder`) com título, data base, valor base, forma de pagamento, recorrência e, opcionalmente, número de parcelas.
3. Cada vencimento do lembrete vira uma **transação** (`PaymentTransaction`) em `PENDENTE`. Ao pagar (`POST /payments/transactions/{id}/pay`) ela vai para `PAGO`, com data e valor efetivamente pagos.
4. `GET /payments/reminders/monthly-bills` mostra as contas de um mês, inclusive meses futuros (simulação a partir da recorrência).

| Conceito | Valores |
|---|---|
| Recorrência | `UNICA`, `DIARIA`, `SEMANAL`, `QUINZENAL`, `MENSAL`, `TRIMESTRAL`, `SEMESTRAL`, `ANUAL` |
| Forma de pagamento | `DINHEIRO`, `TRANSFERENCIA`, `PIX`, `DEBITO`, `CREDITO`, `BOLETO`, `OUTRO` |
| Status da transação | `PENDENTE`, `PAGO`, `VENCIDO`, `CANCELADO` |

Lembretes podem ser desativados e reativados (`/disable`, `/enable`). A exclusão é lógica: `PaymentReminder`, `PaymentTransaction` e `User` têm uma entidade de auditoria (`*_audit`) com quem criou, alterou e quando excluiu.

### Autenticação e perfis

JWT no header `Authorization: Bearer <token>`, sessão stateless, expiração de 30 minutos (`infrastructure.security.authentication.token.expiration-minutes`).

Públicos: `POST /users`, `POST /users/me/enable`, `POST /auth/login` e o Swagger. Todo o resto exige token.

| Perfil | Permissões |
|---|---|
| `USER` | `USER_PERMISSION` |
| `MEMBER` | `MEMBER_PERMISSION`, `USER_PERMISSION` |
| `ADMIN` | `ADMIN_PERMISSION` |
| `OWNER` | `OWNER_PERMISSION`, `ADMIN_PERMISSION` |

`/admin/**` exige `ADMIN_PERMISSION` (hoje, `GET /admin/users/list`). O `OWNER` não tem endpoint de criação: é inserido uma vez por SQL, veja "Rodando localmente".

### Proteção contra força bruta

Os erros de login são contados no Redis por usuário. A partir de 5 tentativas erradas a conta é bloqueada por um tempo que cresce a cada nova tentativa (`BLOCK_TIME_MINUTES` × 2 × excesso, em `LoginAttemptService`), e a API responde com erro de excesso de tentativas.

## Endpoints

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/login` |
| Usuário (o próprio) | `POST /users`, `POST /users/me/disable`, `POST /users/me/enable`, `DELETE /users/me` |
| Admin | `GET /admin/users/list` |
| Lembretes | `POST /payments/reminders`, `GET /payments/reminders/{id}`, `PATCH /payments/reminders/{id}`, `DELETE /payments/reminders/{id}`, `POST /payments/reminders/{id}/disable`, `POST /payments/reminders/{id}/enable`, `GET /payments/reminders/list`, `GET /payments/reminders/monthly-bills` |
| Transações | `POST /payments/transactions/{transactionId}/pay`, `PATCH /payments/transactions/{id}`, `GET /payments/transactions/list` |

O contrato completo está em [`src/main/resources/openapi.yaml`](src/main/resources/openapi.yaml). Com a API no ar, o Swagger UI fica em `http://localhost:8080/swagger-ui.html`.

## Stack

Java 17, Spring Boot 3.3, Spring Web, Spring Security (JWT com `java-jwt`), Spring Data JPA (Hibernate), PostgreSQL 17, Redis 7, Liquibase, springdoc, Docker Compose e Maven. Testes com JUnit/Mockito e H2.

## Arquitetura

Pacote base `com.reminder.financial.api`, no estilo hexagonal:

```text
adapters.controllers     controllers REST
adapters.dtos            requests e responses da API
adapters.mappers         DTO <-> command/query/result
adapters.exception       tratamento de erro (ErrorResponse)
application.usecases     um caso de uso por operação
application.dtos         commands, queries e results
application.exceptions   erros de negócio (Conflict, NotFound, ...)
domains.models           entidades JPA, enums e auditoria
domains.repositories     repositórios JPA e Redis
domains.services         regras de usuário, lembrete, transação e login
infrastructure           segurança, filtro de tracing, config de JPA/Redis, utilitários
```

O fuso padrão para cálculos de data é `America/Sao_Paulo` (`AppConstants`). O banco é versionado em `src/main/resources/db/changelog/` e o Hibernate só valida o schema (`ddl-auto: validate`). Cada requisição recebe um `traceId` nos logs.

## Rodando localmente

Pré-requisitos: JDK 17 e Docker. A configuração do `docker-compose.yaml` é só para desenvolvimento local.

```bash
cp .env.example .env                  # ajuste as credenciais
docker compose up -d                  # PostgreSQL (5432) e Redis (6379)
```

1. **Usuário do banco da API**: edite `docker/scripts/db/create-api-db-user.sql` (troque `API_DB_USER` e `API_DB_PASSWORD` pelos valores do `.env`) e execute com `ROOT_DB_USER` no banco `finance-management`.
2. **Suba a aplicação** com o `.env` exportado. Fora do Docker, `DATABASE_HOST` e `REDIS_HOST` devem ser `localhost` (o exemplo traz o nome dos containers):

   ```bash
   set -a && source .env && set +a
   ./mvnw spring-boot:run
   ```

   O Liquibase cria as tabelas na subida. API em `http://localhost:8080`, Swagger UI em `/swagger-ui.html`. O serviço da API no `docker-compose.yaml` está comentado; para rodá-la em container, descomente-o e mantenha os hosts dos containers no `.env`.
3. **Crie o `OWNER` (uma vez)**, com as tabelas já criadas: edite `docker/scripts/db/create-owner-user.sql` (e-mail, nome e **hash bcrypt** da senha, nunca texto puro) e execute com `API_DB_USER`. O script não duplica se o e-mail já existir.

### Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `password authentication failed` | senha com `$ & ? @ #` mal interpretada pelo cliente/JDBC, ou senha alterada com o volume antigo. Use outra senha, rode `docker compose down -v` e refaça os passos 1 a 3 |
| `Port is already allocated` | 8080, 5432 ou 6379 em uso. Pare o outro serviço ou mude o mapeamento no compose (a API fora do Docker aceita `SERVER_PORT=8081`) |
| `Connection refused` / `Could not resolve placeholder` | `.env` ausente, não exportado, ou hosts de container usados fora do Docker |
| Erro de permissão ou tabela ausente no Liquibase | o passo 1 não rodou completo; `API_DB_USER` precisa de `CREATE` no schema `public` |
| Login bloqueado | 5 senhas erradas bloqueiam por tempo crescente (Redis). Em dev: `docker compose exec financial_management_redis redis-cli -a "$REDIS_PASSWORD" FLUSHALL` |
| Dados corrompidos | `docker compose down -v` apaga containers e volumes (todo o banco local) |

## Testes

```bash
./mvnw verify      # não precisa de Docker: os testes usam H2 (application-test.yaml)
```

## O que ainda não existe

Planejado para fechar a v1 e ainda não implementado: categorias, desconto nas transações, endpoints de métricas, campo `timezone` no usuário, remoção da redundância `deleted`/`deletedAt` na auditoria e o módulo agendador que marca transações como `VENCIDO` (hoje nada faz essa transição automaticamente). Detalhes em [`tasks/prd-financial-reminder-v1.md`](tasks/prd-financial-reminder-v1.md).

## Desenvolvimento com Ralph

Os ciclos de implementação rodam pelo agente autônomo em `scripts/ralph/` (`ralph.sh`, `prompt.md`, `progress.txt`). Os PRDs ficam em `tasks/`.
