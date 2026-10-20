# PRD: Refatoração SOLID / Clean Architecture + Observabilidade — Financial Reminder API

> **Origem:** auditoria de código de 2026-10-20 (somente leitura de código).
> **Natureza:** refatoração. Nenhuma funcionalidade nova. Roda **depois** do PRD `prd-financial-reminder-v1.md` (ou em branch separada), para não conflitar com as stories ainda abertas dele.
> **Stack:** Java 17, Spring Boot, JPA, PostgreSQL, Redis, Liquibase.

---

## 1. Introdução

A API tem boa organização de pastas (`adapters`, `application`, `domains`, `infrastructure`), mas as dependências entre camadas não respeitam a regra da Clean Architecture, alguns services acumulam responsabilidades e a observabilidade de erros é insuficiente.

### Achados verificados no código

- `domains/services/*` importa `infrastructure.utils.SpecificationFactory` e DTOs/exceções de `application`.
- `LoginUseCaseImpl` importa `infrastructure.security.TokenProvider` direto.
- `application` usa `Pageable`/`Page`/`PageImpl` (Spring Data) e `PasswordEncoder` (Spring Security).
- `PaymentTransactionService` (165 linhas) mistura CRUD, montagem de `Specification`, regra de recorrência e soft delete/auditoria.
- `ListMonthlyBillsUseCaseImpl` (196 linhas) concentra validação de data, busca, simulação, merge, totais e paginação em memória.
- 89 usos de `@Autowired` em campo.
- `ApplicationExceptionHandler` repete o mesmo bloco 10 vezes, não loga, devolve `getMessage()` em 500 e mapeia `AuthenticationException` para 403.
- E-mail do usuário logado em texto puro em `PaymentReminderService`, `PaymentTransactionService` e `ListMonthlyBillsUseCaseImpl`.

## 2. Objetivos

- Dependências apontando para dentro: `adapters`/`infrastructure` → `application` → `domains`.
- Cada classe com uma responsabilidade; nenhuma classe acima de 150 linhas nem método acima de 40 (exceto records e enums de dados).
- Injeção por construtor em 100% dos beans.
- Todo erro 4xx/5xx deixa rastro no log com `traceId`; nenhum dado pessoal em texto puro nos logs.
- Zero mudança de contrato HTTP, de schema de banco e de `openapi.yaml`.

## 3. Regras para todas as stories

- [ ] `mvn test` verde, sem remover nem enfraquecer testes existentes
- [ ] `openapi.yaml` sem diferença (exceto onde a story disser o contrário)
- [ ] Sem migration nova
- [ ] Sem abstração sem uso: interface só se tiver 2+ implementações ou for porta entre camadas
- [ ] Cada story é só movimentação/extração: nenhum comportamento muda

## 4. User Stories (em ordem de prioridade)

### Fase 1 — Rastreabilidade (baixo risco, alto valor)

#### US-R01: Logar erros no `ApplicationExceptionHandler`
**Description:** Como operador, quero ver no log todo erro devolvido ao cliente, para investigar com o `traceId`.

**Acceptance Criteria:**
- [ ] 4xx logam `warn` com método, path, status e mensagem; 5xx logam `error` com stack trace
- [ ] Handler genérico (500) devolve mensagem fixa ("Unexpected error"), sem `exception.getMessage()`
- [ ] `AuthenticationException` passa a responder 401 (hoje 403); `AccessDeniedException` segue 403
- [ ] "Data no passado" em `CreatePaymentReminderUseCaseImpl` deixa de usar `ConflictException` e passa a responder 400
- [ ] Os 10 handlers repetidos são consolidados num método privado que monta o `ErrorResponse`
- [ ] Teste de integração: uma requisição que gera 500 produz uma linha `ERROR` contendo o `traceId` da resposta
- [ ] `openapi.yaml` atualizado para os status 401/400 alterados

#### US-R02: Remover dados pessoais dos logs
**Acceptance Criteria:**
- [ ] Nenhum `logger.*` imprime e-mail, título ou valor de reminder; usar `userId`/`reminderId`
- [ ] `user` do MDC passa a ser o `userId` (ou hash do e-mail), nunca o e-mail
- [ ] `grep -rn "logger\." src/main` não retorna mensagem com `email`, `Email` ou `userEmail`

#### US-R03: Endurecer o `HttpTracingFilter`
**Acceptance Criteria:**
- [ ] Reaproveita o `X-Trace-Id` recebido quando válido (UUID ou até 64 chars `[A-Za-z0-9-]`), senão gera um novo
- [ ] `finally` remove só as chaves `traceId` e `user` em vez de `MDC.clear()`
- [ ] Filtro roda com ordem explícita antes do `SecurityFilter`, para que o log de JWT inválido já tenha `traceId`
- [ ] Teste do filtro cobre: header ausente, header válido, header inválido

#### US-R04: Logar eventos de negócio nos use cases de escrita
**Acceptance Criteria:**
- [ ] Create/Update/Delete/Toggle de reminder, pagamento e atualização de transação, criação/remoção de usuário logam `info` com ids e resultado
- [ ] Falha de login e bloqueio por tentativas logam `warn` sem o e-mail
- [ ] Mensagens em formato `chave=valor` (ex.: `reminder created reminderId=... userId=...`)

### Fase 2 — Injeção por construtor

#### US-R05: Migrar `@Autowired` de campo para construtor
**Acceptance Criteria:**
- [ ] Controllers, use cases, services e `infrastructure` usam campos `private final` + construtor
- [ ] Nenhum `@Autowired` em atributo no código de produção
- [ ] Testes que usavam `@InjectMocks` com campo continuam verdes (ajustar se necessário)

### Fase 3 — Single Responsibility

#### US-R06: Extrair a regra de recorrência de `PaymentTransactionService`
**Acceptance Criteria:**
- [ ] Nova classe `NextTransactionFactory` (ou método de `PaymentReminder`) que decide e cria a próxima `PaymentTransaction`
- [ ] `processSuccessfulPayment` apenas marca como pago e delega
- [ ] Teste unitário puro (sem Spring) cobrindo: `UNICA`, parcelas esgotadas, parcela nula, cada recorrência

#### US-R07: Extrair soft delete/toggle de transações
**Acceptance Criteria:**
- [ ] `togglePendingTransactionStatus` e `cancelAndSoftDeletePendingTransaction` saem para o objeto de audit (`AbstractAudit`) ou para uma classe própria
- [ ] `PaymentTransactionService` fica com no máximo 100 linhas e sem lógica de recorrência

#### US-R08: Decompor `ListMonthlyBillsUseCaseImpl`
**Acceptance Criteria:**
- [ ] `MonthlyBillsSimulator` (gera contas simuladas), `MonthlyBillsTotals` (soma por status) e `MonthlyBillsPaginator` (ou o uso de `PageImpl` isolado) extraídos
- [ ] Use case só orquestra: valida data → busca → combina → totais → página
- [ ] Constante `MAX_YEARS_IN_FUTURE_ALLOWED` em um único lugar; removida de `PaymentReminderController`
- [ ] Testes existentes passam sem alteração; um teste novo por classe extraída

#### US-R09: Mover parse/validação de data para fora do use case
**Acceptance Criteria:**
- [ ] `CreatePaymentReminderRequest.date` passa a `LocalDate` com validação Bean Validation (`@FutureOrPresent`) — ou o parse fica no mapper
- [ ] `CreatePaymentReminderUseCaseImpl` não trata `DateTimeParseException`
- [ ] Contrato HTTP idêntico (mesmo formato `YYYY-MM-DD`, mesmo status de erro)

### Fase 4 — Dependências entre camadas (DIP)

#### US-R10: Tirar `SpecificationFactory` e `Specification` do domínio
**Acceptance Criteria:**
- [ ] Criar `domains/repositories` (interface pura) com métodos de busca por filtro, ex.: `Page<PaymentTransaction> search(User user, TransactionFilter filter, Pageable page)`
- [ ] Implementação com `Specification` fica em `infrastructure/persistence`
- [ ] `domains/**` sem import de `infrastructure`
- [ ] Regra ArchUnit: `domains` não depende de `infrastructure` nem de `adapters`

#### US-R11: Tirar DTOs e exceções de `application` do domínio
**Acceptance Criteria:**
- [ ] `domains/services/*` deixa de importar `application.dtos.*` e `application.exceptions.*`
- [ ] Exceções de negócio (`NotFoundException`, `ConflictException`) movidas para o domínio; `application` e `adapters` passam a importar do domínio
- [ ] Regra ArchUnit: `domains` não depende de `application`

#### US-R12: Porta para token e hash de senha
**Acceptance Criteria:**
- [ ] `LoginUseCaseImpl` e `CreateUserUseCaseImpl` dependem de interfaces em `application` (`TokenIssuer`, `PasswordHasher`)
- [ ] Implementações em `infrastructure/security` (`TokenProvider`, `PasswordEncoder`)
- [ ] `application` sem import de `org.springframework.security`

#### US-R13: Paginação sem Spring Data na camada `application`
**Acceptance Criteria:**
- [ ] Tipos próprios `PageRequest`/`PageResult` em `application`, convertidos nos controllers/repositórios
- [ ] `application` sem import de `org.springframework.data`

### Fase 5 — Travar a arquitetura

#### US-R14: ArchUnit
**Acceptance Criteria:**
- [ ] Regras: sem ciclos entre pacotes; `domains` → nada; `application` → só `domains`; `adapters` não importa `infrastructure`
- [ ] Nenhum `@Autowired` em campo
- [ ] Falha o build se violado

## 5. Non-Goals

- Funcionalidades da v1 (categorias, desconto, métricas, scheduler).
- Separar modelo de domínio de entidade JPA (decisão adiada: custo alto; reavaliar após US-R10).
- Remover interface+impl dos use cases (cerimônia aceita; sem ganho imediato).
- Logging estruturado em JSON.

## 6. Considerações

- A Fase 1 pode ir sozinha e primeiro: é a que mais ajuda a diagnosticar produção.
- US-R10 e US-R11 são as de maior diff; fazer uma por vez.
- Corrigir o nome do pacote `adapters/dtos/paymentremider` (typo) dentro da US-R05, por já tocar nos controllers.
