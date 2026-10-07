# PRD: Fechamento da v1 + Módulo Scheduler — Financial Reminder API

> **Derivado de:** `tasks/financial-reminder-api-v0.2.md`
> **Escopo:** Itens pendentes da v1 (seção 12 do PRD original) + módulo de agendamento (seção 6)
> **Stack:** Java 17, Spring Boot, PostgreSQL, Redis, Liquibase, JWT, Arquitetura Hexagonal
> **Granularidade das stories:** por camada (migration → domínio/serviço → usecase → controller/DTO), seguindo a arquitetura hexagonal existente
> **Nota:** Este documento consolida e substitui os dois rascunhos anteriores de PRD da v1 (versão por-feature + versão por-camada) num único plano.

---

## 1. Introdução / Visão Geral

Este PRD transforma o documento de visão `tasks/financial-reminder-api-v0.2.md` em um plano de implementação acionável para **fechar a v1** da Financial Reminder API e entregar o **módulo separado de agendamento** que marca transações vencidas.

A v1 já possui autenticação (JWT + rate limiting), CRUD de lembretes (`PaymentReminder`), transações (`PaymentTransaction`) e visão mensal. Faltam: **Categorias**, campos de **desconto** nas transações (incluindo o `baseValue` que ainda não existe na transação), **endpoints de métricas**, o campo **`timezone`** no usuário, a **limpeza da redundância `deleted`/`deletedAt`** no audit, a **documentação OpenAPI** atualizada e o **módulo scheduler** que transiciona transações `PENDENTE → VENCIDO`.

Cada feature é fatiada em stories por camada, para que cada story seja pequena o suficiente para uma sessão de implementação focada.

### Estado verificado do código (baseline)

- `AbstractAudit` possui **ambos** `deleted: Boolean` e `deletedAt: Instant` — redundância a remover (Feature A).
- `AbstractUser`/`User` **não** possuem `timezone` (Feature B).
- `AbstractReminder` **já possui** `baseValue` (`BigDecimal`); `PaymentTransaction` **não** — só tem `paymentValue`. O desconto exige adicionar `baseValue` à transação (Feature D, US-010a).
- `PaymentTransaction` **não** possui `hasDiscount`/`discountValue` (Feature D).
- Não existem entidade/endpoints de `Category` nem de métricas.
- Existem `src/main/resources/openapi.yaml` (fonte alvo) e `API_REFERENCE.md` na raiz (desatualizada, a remover — Feature G).

---

## 2. Objetivos

- Eliminar a redundância `deleted: Boolean` + `deletedAt: Instant`, usando `deletedAt != null` como fonte única da verdade.
- Adicionar `timezone` (IANA) à entidade de usuário, habilitando cálculos de fronteira de data no fuso correto.
- Entregar CRUD completo de **Categorias** definidas pelo usuário, associáveis a lembretes (0..N).
- Registrar **desconto** em transações (`baseValue` herdado do lembrete + `hasDiscount` + `discountValue` calculado).
- Expor os **dois endpoints de métricas** iniciais (resumo anual e por categoria) aplicando CQRS na leitura.
- Criar o **módulo `financial-reminder-scheduler`** que atualiza status vencidos respeitando o fuso do usuário.
- Migrar a documentação de API para o `openapi.yaml` **derivado das controllers reais** (a `API_REFERENCE.md` está incompleta), mantendo-o atualizado a cada feature e removendo a `API_REFERENCE.md` ao final.

---

## 3. User Stories

> Projeto é uma **API backend** (sem UI). Onde o exemplo do skill pediria verificação no browser, usamos: *build Maven passa, testes passam, e o endpoint é validado via OpenAPI / requisição HTTP*.

### Feature A — Refactor do Soft Delete (`deletedAt` como fonte única)

#### US-001: Migrar `validateIsDeleted()` para `deletedAt != null`
**Description:** Como desenvolvedor, quero que a verificação de exclusão use `deletedAt`, para eliminar a redundância com o booleano `deleted`.

**Acceptance Criteria:**
- [ ] `AbstractAudit.validateIsDeleted()` passa a checar `this.deletedAt != null` em vez de `Boolean.TRUE.equals(this.deleted)`
- [ ] Todo ponto que seta `deleted = true` passa a setar (ou apenas confiar em) `deletedAt = Instant.now()`
- [ ] Nenhuma query/filtro de listagem depende mais do booleano `deleted`
- [ ] Testes existentes de soft delete continuam verdes
- [ ] Build Maven (`mvn -q -DskipTests=false test`) passa

#### US-002: Remover a coluna/atributo `deleted` redundante
**Description:** Como desenvolvedor, quero remover o campo booleano `deleted`, para que `deletedAt` seja a única fonte da verdade.

**Acceptance Criteria:**
- [ ] Atributo `private Boolean deleted` removido de `AbstractAudit`
- [ ] Migration Liquibase nova (`changes/0005-drop-deleted-boolean.yaml`) registrada no `db.changelog-master.yaml` que dropa a coluna `deleted` das tabelas auditadas
- [ ] Migration é reversível (bloco `rollback`) e sobe limpa em banco zerado
- [ ] Nenhuma referência a `getDeleted()`/`setDeleted()` restante no código (busca global limpa)
- [ ] Testes passam

---

### Feature B — Campo `timezone` no Usuário

#### US-003: Adicionar `timezone` ao modelo e migration
**Description:** Como desenvolvedor, quero persistir o fuso IANA do usuário, para calcular fronteiras de data no fuso correto.

**Acceptance Criteria:**
- [ ] Campo `timezone` (`String`, IANA, ex: `"America/Sao_Paulo"`) adicionado a `AbstractUser`/`User`
- [ ] Migration Liquibase adiciona coluna `timezone VARCHAR` com default `"America/Sao_Paulo"` para registros existentes
- [ ] Validação: valor deve ser um `ZoneId` válido (`ZoneId.of(...)` não lança) — rejeitar com 400 caso inválido
- [ ] Build e testes passam

#### US-004: Aceitar e retornar `timezone` no cadastro/atualização de usuário
**Description:** Como usuário, quero definir meu fuso ao criar/atualizar minha conta, para que vencimentos e métricas usem meu horário local.

**Acceptance Criteria:**
- [ ] DTOs de criação e atualização de usuário aceitam `timezone` (opcional; default aplicado quando ausente)
- [ ] Resposta de usuário inclui `timezone`
- [ ] Mapper atualizado; usecase persiste o valor validado
- [ ] `openapi.yaml` atualizado com o campo `timezone`
- [ ] Teste de usecase cobrindo default e valor inválido

---

### Feature C — Categorias (`Category`)

#### US-005: Modelo `Category` + migration
**Description:** Como desenvolvedor, quero a entidade `Category` pertencente ao usuário, para sustentar o CRUD e a associação com lembretes.

**Acceptance Criteria:**
- [ ] Modelo `Category` (`id`, `userId` FK→User, `name`) em `domains/models`, herdando `AbstractAudit`
- [ ] Migration Liquibase cria tabela `categories` com FK para `users` e índice em `user_id`
- [ ] Constraint de unicidade `(user_id, name)` entre categorias **não-excluídas** (`deletedAt IS NULL`)
- [ ] Limite de tamanho de `name` definido (sugestão: 30 chars, alinhado a `title` do lembrete)
- [ ] Build e testes passam

#### US-006: Repositório e serviço de domínio de `Category`
**Description:** Como desenvolvedor, quero porta de repositório e serviço de domínio para categorias, seguindo a arquitetura hexagonal.

**Acceptance Criteria:**
- [ ] Interface de repositório em `domains/repositories` com operações por usuário (filtrando `deletedAt IS NULL`)
- [ ] Serviço de domínio em `domains/services` encapsulando regras (nome obrigatório, unicidade por usuário)
- [ ] Testes unitários do serviço cobrindo criação duplicada e busca de categoria de outro usuário (negada)

#### US-007: Usecases de CRUD de `Category`
**Description:** Como usuário, quero criar, listar, atualizar e excluir minhas categorias.

**Acceptance Criteria:**
- [ ] Usecases `Create`, `List`, `Update`, `Delete` (soft delete via `deletedAt`) em `application/usecases/category`
- [ ] Listagem retorna apenas categorias do usuário autenticado e não excluídas
- [ ] Soft delete **não** remove as associações em `reminder_categories` (associação histórica preservada; filtragem por `deletedAt IS NULL` nas listagens/métricas)
- [ ] Update/Delete validam ownership (categoria de outro usuário → 403/404 conforme padrão do projeto)
- [ ] Testes de usecase para cada operação

#### US-008: Controller + DTOs de `Category`
**Description:** Como cliente da API, quero endpoints REST para gerenciar categorias.

**Acceptance Criteria:**
- [ ] `CategoryController` com `POST /categories`, `GET /categories` (paginado), `GET /categories/{id}`, `PUT /categories/{id}`, `DELETE /categories/{id}`
- [ ] DTOs de request/response em `adapters/dtos/category` + mapper
- [ ] Validações de tamanho/obrigatoriedade de `name`
- [ ] `openapi.yaml` atualizado com os endpoints
- [ ] Teste de controller (slice/integration) para o caminho feliz de cada rota
- [ ] Verificado via HTTP: criar → listar → editar → excluir

#### US-009: Associação de categorias a `PaymentReminder`
**Description:** Como usuário, quero anexar 0..N categorias a um lembrete, para filtrar e gerar métricas por categoria.

**Acceptance Criteria:**
- [ ] Relação N..N entre `PaymentReminder` e `Category` + migration da tabela de junção (`reminder_categories`)
- [ ] DTOs de criação/atualização de lembrete aceitam lista de `categoryId`
- [ ] Usecases de criar/atualizar lembrete validam que as categorias pertencem ao usuário (IDs de outro usuário → 400/403)
- [ ] Resposta do lembrete inclui suas categorias
- [ ] `openapi.yaml` do lembrete atualizado
- [ ] Testes cobrindo associação e desassociação

#### US-009b: Filtro de listagens por categoria
**Description:** Como usuário, quero filtrar lembretes e transações por categoria, para focar em um tipo de despesa.

**Acceptance Criteria:**
- [ ] Listagem de lembretes aceita parâmetro opcional `categoryId` e retorna só os associados
- [ ] Listagem de transações aceita filtro por `categoryId` (via categoria do lembrete pai), combinável com os filtros existentes (período, status)
- [ ] Filtro escopado ao usuário autenticado
- [ ] `openapi.yaml` atualizado com o novo parâmetro
- [ ] Verificado via HTTP que o filtro restringe corretamente os resultados

---

### Feature D — Desconto nas Transações

#### US-010a: Adicionar `baseValue` à `PaymentTransaction` + migration
**Description:** Como desenvolvedor, quero que a transação carregue o valor esperado herdado do lembrete, pois hoje a transação só tem `paymentValue` e o desconto não pode ser calculado sem o valor base.

**Acceptance Criteria:**
- [ ] `PaymentTransaction` ganha `baseValue` (`BigDecimal`)
- [ ] Ao gerar uma transação a partir do lembrete (geração lazy), `baseValue` é populado com o `baseValue` do `PaymentReminder`
- [ ] Migration Liquibase adiciona coluna `base_value` em `payment_transactions`, com backfill `base_value = payment_value` para linhas existentes
- [ ] DTO de resposta da transação inclui `baseValue`
- [ ] Build e testes passam

#### US-010b: Campos `hasDiscount` e `discountValue` no modelo + migration
**Description:** Como desenvolvedor, quero persistir desconto na transação, para registrar pagamentos antecipados/com abatimento.

**Acceptance Criteria:**
- [ ] `PaymentTransaction` ganha `hasDiscount` (Boolean) e `discountValue` (`BigDecimal`)
- [ ] Migration Liquibase adiciona as duas colunas (`has_discount` default `false`; `discount_value` nullable)
- [ ] `discountValue` é derivado: `baseValue - paymentValue` quando `paymentValue < baseValue`; `hasDiscount = discountValue > 0`
- [ ] Build e testes passam

#### US-011: Cálculo de desconto no fluxo de pagamento
**Description:** Como usuário, ao pagar com valor menor que o esperado, quero que o desconto seja registrado automaticamente.

**Acceptance Criteria:**
- [ ] `PayTransactionUseCase` seta `hasDiscount = true` e calcula `discountValue` quando `paymentValue < baseValue`
- [ ] `UpdatePaymentTransactionUseCase` recalcula desconto ao alterar `paymentValue`
- [ ] DTO de resposta da transação expõe `hasDiscount` e `discountValue`
- [ ] `openapi.yaml` da transação atualizado
- [ ] Testes: pagamento com desconto, pagamento integral (sem desconto), e edição que muda o valor
- [ ] Verificado via HTTP que o endpoint de pagamento retorna os campos corretos

---

### Feature E — Endpoints de Métricas (CQRS)

#### US-012: Read model / query side para métricas
**Description:** Como desenvolvedor, quero separar a leitura analítica da escrita, introduzindo o lado de query (CQRS) para métricas.

**Acceptance Criteria:**
- [ ] Estrutura de leitura dedicada (query handlers/serviço de leitura) que agrega transações sem passar pelos serviços de escrita
- [ ] Agregações respeitam o `timezone` do usuário ao definir mês/ano (usa US-003)
- [ ] Considera apenas transações não canceladas/não excluídas
- [ ] Testes unitários das agregações com dataset de fixture

#### US-013: Endpoint `GET /metrics/annual-summary`
**Description:** Como usuário, quero o total gasto mês a mês em um ano, para entender minha sazonalidade.

**Acceptance Criteria:**
- [ ] `GET /metrics/annual-summary?year=YYYY` retorna array de 12 posições com `month`, `totalPaid`, `totalPending`, `totalProjected`
- [ ] `totalPaid` inclui transações `PAGO` **e** `ANTECIPADO`
- [ ] `totalProjected` soma as transações simuladas (geradas on-demand dos lembretes recorrentes) dos meses futuros, limitada ao ano consultado
- [ ] Filtra pelo usuário autenticado
- [ ] `year` ausente assume o ano corrente no fuso do usuário; `year` inválido retorna 400
- [ ] `openapi.yaml` atualizado
- [ ] Teste de integração do endpoint cobrindo meses com e sem transações
- [ ] Verificado via HTTP com dados de exemplo

#### US-014: Endpoint `GET /metrics/by-category`
**Description:** Como usuário, quero o total gasto por categoria num período, para saber para onde meu dinheiro vai.

**Acceptance Criteria:**
- [ ] `GET /metrics/by-category?year=YYYY&month=MM` retorna lista de `{ categoryId, categoryName, total }`
- [ ] Transações/lembretes sem categoria são agrupados como **"Sem categoria"** (bucket explícito)
- [ ] Filtra pelo usuário autenticado; `year`/`month` ausentes assumem o período corrente no fuso do usuário; valores inválidos → 400
- [ ] `openapi.yaml` atualizado
- [ ] Teste de integração do endpoint; verificado via HTTP que os totais batem com os dados de exemplo

---

### Feature F — Módulo Scheduler (`financial-reminder-scheduler`)

#### US-015: Criar módulo Maven separado no monorepo
**Description:** Como desenvolvedor, quero um módulo Maven dedicado ao agendamento, separado da API, conforme a estrutura modular planejada.

**Acceptance Criteria:**
- [ ] Novo módulo `financial-reminder-scheduler` adicionado ao `pom.xml` pai (monorepo modular, não microsserviço)
- [ ] Módulo compartilha acesso ao mesmo banco PostgreSQL (config via `application.yaml` próprio)
- [ ] `mvn -q package` na raiz compila ambos os módulos
- [ ] README curto do módulo descrevendo responsabilidade única

#### US-016: Job de transição `PENDENTE → VENCIDO`
**Description:** Como sistema, quero marcar como vencidas as transações pendentes cuja `dueDate` já passou, respeitando o fuso do usuário.

**Acceptance Criteria:**
- [ ] Job agendado **diário** (`@Scheduled`, intervalo configurável por propriedade, default diário) faz **varredura global** das transações `status = PENDENTE` com `dueDate < hoje`
- [ ] "Hoje" é calculado no fuso de cada usuário: `ZonedDateTime.now(ZoneId.of(user.timezone)).toLocalDate()`
- [ ] Atualiza essas transações para `status = VENCIDO`
- [ ] Operação idempotente (rodar duas vezes não causa efeito colateral)
- [ ] Teste cobrindo: transação vencida no fuso do usuário é transicionada; transação ainda no prazo não é
- [ ] Build e testes do módulo passam

---

### Feature G — Documentação (migração `API_REFERENCE.md` → `openapi.yaml`)

> **Fonte da verdade:** as **controllers reais** (`adapters/controllers/*`) e seus DTOs/mappers — **não** o `API_REFERENCE.md`, que está incompleto e desatualizado. O `API_REFERENCE.md` pode no máximo servir de pista do que existia; toda rota, parâmetro, corpo e código de resposta no `openapi.yaml` deve ser conferido contra a assinatura efetiva do controller e do DTO correspondente.

#### US-017: Auditar controllers e mapear lacunas do `openapi.yaml`
**Description:** Como mantenedor, quero comparar o `openapi.yaml` atual com as controllers reais, para enxergar o que falta e o que está divergente antes de migrar.

**Acceptance Criteria:**
- [ ] Levantamento de todas as rotas a partir dos controllers (`AuthenticationController`, `UserController`, `UserAdminController`, `PaymentReminderController`, `PaymentTransactionController` + futuros `CategoryController` e `MetricsController`): método HTTP, path, params, request body, response body, códigos de status
- [ ] Cada rota cruzada com o `openapi.yaml` atual: marcada como *presente e correta*, *divergente* ou *ausente*
- [ ] Resultado registrado como checklist (no PR/issue ou no topo do próprio `openapi.yaml`) guiando a US-018
- [ ] Nenhuma informação é copiada do `API_REFERENCE.md` sem ser confirmada na controller

#### US-018: Reescrever o `openapi.yaml` a partir das controllers
**Description:** Como mantenedor, quero o `openapi.yaml` fielmente derivado das controllers, para que ele seja a fonte única e confiável da API.

**Acceptance Criteria:**
- [ ] Toda rota exposta pelos controllers está no `openapi.yaml` com método, path e params idênticos à assinatura do controller
- [ ] Schemas de request/response refletem os **DTOs reais** (`adapters/dtos/*`), incluindo campos novos desta v1 (`timezone`, `baseValue`/`hasDiscount`/`discountValue`, categorias no lembrete, filtros `categoryId`)
- [ ] Códigos de status e respostas de erro batem com o tratamento real (`adapters/exception/*`)
- [ ] Autenticação/escopo (JWT) documentada nas rotas protegidas
- [ ] Spec valida sem erros (linter OpenAPI)
- [ ] Conferência amostral via HTTP: pelo menos uma rota de cada controller responde conforme o documentado

#### US-019: Remover `API_REFERENCE.md` e apontar para o `openapi.yaml`
**Description:** Como mantenedor, quero descartar a referência manual após a migração, deixando o `openapi.yaml` como fonte única.

**Acceptance Criteria:**
- [ ] `API_REFERENCE.md` só é removido **após** US-018 (o `openapi.yaml` já cobre tudo)
- [ ] `API_REFERENCE.md` removido do repositório
- [ ] `README.md` aponta para o `openapi.yaml` como referência de API
- [ ] Nenhum link quebrado para `API_REFERENCE.md` restante
- [ ] Checklist da seção 12 do `tasks/financial-reminder-api-v0.2.md` original marcado como concluído

---

## 4. Requisitos Funcionais

- **FR-1:** O sistema deve usar `deletedAt != null` como única condição de "excluído"; o campo booleano `deleted` deve ser removido do modelo e do banco.
- **FR-2:** O usuário deve ter um campo `timezone` (IANA), validado, com default `"America/Sao_Paulo"`.
- **FR-3:** O sistema deve permitir CRUD de categorias pertencentes ao usuário, com `name` único por usuário entre categorias não-excluídas.
- **FR-4:** Um `PaymentReminder` deve poder ser associado a 0..N categorias do próprio usuário; listagens de lembretes e transações devem aceitar filtro por `categoryId`.
- **FR-5:** A `PaymentTransaction` deve possuir `baseValue` (herdado do lembrete); ao registrar pagamento com `paymentValue < baseValue`, o sistema deve setar `hasDiscount = true` e calcular `discountValue = baseValue - paymentValue`.
- **FR-6:** O sistema deve expor `GET /metrics/annual-summary?year=YYYY` retornando 12 meses com totais pago/pendente/projetado.
- **FR-7:** O sistema deve expor `GET /metrics/by-category?year=YYYY&month=MM` retornando totais por categoria, com bucket "Sem categoria".
- **FR-8:** As agregações de métricas e o cálculo de "hoje" do scheduler devem usar o `timezone` do usuário.
- **FR-9:** Deve existir um módulo Maven `financial-reminder-scheduler` que, em intervalo configurável, transiciona transações `PENDENTE` com `dueDate` passada para `VENCIDO`.
- **FR-10:** O job de vencimento deve ser idempotente.
- **FR-11:** Cada feature nova deve atualizar o `openapi.yaml` com base na assinatura real do controller/DTO; o `openapi.yaml` deve ser reescrito a partir das controllers (não da `API_REFERENCE.md`), e a `API_REFERENCE.md` deve ser removida ao final da migração.
- **FR-12:** Todas as queries de listagem devem continuar filtrando `deletedAt IS NULL`.

---

## 5. Não-Objetivos (Fora de Escopo)

- **PaymentAccount** (contas/cartões) — Fase 2 do roadmap, fora desta v1.
- **Shared Reminders** (grupos domésticos) — Fase 4.
- **Ledger / Receitas** — Fase 5.
- **InvestmentReminder** — Fase 6.
- **Notificações** (e-mail/push) — Fase 7.
- **Kafka / event-driven** — o scheduler desta entrega usa `@Scheduled` puro; eventos de domínio ficam para evolução futura.
- **Kubernetes / infra de deploy** — fora do escopo de código da v1.
- Nenhuma interface de usuário (frontend) — projeto é API.

---

## 6. Considerações Técnicas

- **Arquitetura Hexagonal:** respeitar a separação `domains` (models, repositories, services) → `application` (usecases, dtos) → `adapters` (controllers, dtos, mappers) → `infrastructure`. Cada feature segue esse fluxo, espelhando o que já existe para `PaymentReminder`.
- **Migrations:** Liquibase, arquivos em `src/main/resources/db/changelog/changes/` registrados no `db.changelog-master.yaml`, numerados sequencialmente a partir de `0005`. Todas com bloco `rollback`.
- **Timezone:** armazenar/retornar UTC para timestamps (`Instant`/`TIMESTAMPTZ`); `dueDate`/`paymentDate` permanecem `LocalDate`/`DATE`. Cálculos de fronteira de data usam `User.timezone`.
- **CQRS:** introduzir o lado de leitura apenas para métricas (não reescrever o lado de escrita existente).
- **Monorepo modular:** o scheduler é um módulo Maven adicional no mesmo repositório, compartilhando o PostgreSQL; não é microsserviço.
- **OpenAPI a partir das controllers:** o `openapi.yaml` é a fonte única da API e deve refletir as controllers reais (`adapters/controllers/*`) e seus DTOs/mappers/handlers de exceção. A `API_REFERENCE.md` está incompleta e **não** deve ser usada como base — no máximo como pista a ser confirmada no código. Ao documentar/atualizar uma rota, conferir método, path, params, request/response e códigos de status diretamente na assinatura do controller.
- **Ordem de implementação sugerida:** A (soft delete) → B (timezone) → C (categorias) → D (desconto, com US-010a antes de US-010b) → E (métricas) → F (scheduler) → G (docs). Métricas dependem de B e C; scheduler depende de B.

---

## 7. Métricas de Sucesso

- Checklist da seção 12 do `tasks/financial-reminder-api-v0.2.md` 100% concluído.
- `openapi.yaml` cobre todos os endpoints; `API_REFERENCE.md` removido.
- Suíte de testes verde após cada feature (`mvn test`).
- Banco zerado sobe todas as migrations sem erro; rollback funciona.
- Nenhuma referência ao booleano `deleted` no código após a Feature A.
- Scheduler transiciona corretamente vencidos respeitando fuso, de forma idempotente.
- **Fluxo ponta-a-ponta validado via API:** criar categoria → associá-la a um lembrete → pagar uma transação com desconto → ver o gasto refletido nos dois endpoints de métricas.

---

## 8. Decisões (questões anteriormente em aberto)

Todas as questões em aberto foram resolvidas e travadas para a v1:

- **Antecipação vs status:** transações antecipadas recebem `status = ANTECIPADO` e **contam como pago** (`totalPaid`) nas métricas. Preserva a distinção semântica entre pago no prazo e antecipado, sem criar bucket extra. Impacta US-011/US-013/US-016.
- **`totalProjected` no resumo anual:** é a soma das **transações simuladas** (geradas on-demand a partir dos lembretes recorrentes) dos meses futuros, **limitada ao ano consultado**. Coerente com a visão mensal. Impacta US-015.
- **Intervalo do scheduler:** job **diário**, **varredura global** de todas as transações `PENDENTE` vencidas, calculando "hoje" no fuso de cada usuário. Intervalo permanece configurável por propriedade (default diário). Impacta US-018.
- **Soft delete de categoria em uso:** **manter a associação histórica** — excluir uma categoria não remove as linhas de junção; listagens e métricas filtram categorias com `deletedAt IS NULL`. Impacta US-007/US-009/US-016.
- **by-category sem categoria:** bucket explícito **"Sem categoria"** (US-016).
- **Limite de nome de categoria:** **30 chars** (alinhado a `title` do lembrete). US-005/US-008.
- **Backfill de `timezone`:** default **`"America/Sao_Paulo"`** para registros existentes. US-003.
- **CQRS — profundidade:** lado de leitura faz **query direta** nas tabelas existentes (sem read models materializados nesta v1). US-014.
- **Recuperação de soft delete:** ao restaurar um lembrete (`deletedAt = null`), as transações `CANCELADO` **permanecem canceladas** — nenhuma story reverte isso automaticamente.
