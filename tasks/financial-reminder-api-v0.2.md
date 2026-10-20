# PRD — Financial Reminder API

> **Versão:** 0.2
> **Data:** 2026-06-05
> **Autor:** vitor-sb
> **Status:** Em construção

---

## 1. Visão Geral

API backend para gerenciamento de despesas pessoais por meio de lembretes de pagamento. O sistema permite que o usuário registre contas recorrentes ou únicas, acompanhe os pagamentos realizados, visualize projeções futuras e entenda para onde está indo o seu dinheiro por meio de métricas.

**Inspiração:** Gerenciador de lembretes de pagamento da Nubank, Mobills e Despezzas — com extensões significativas de funcionalidade e foco em controle analítico.

**Natureza do projeto:** Portfólio técnico pessoal, com objetivo de aprender e demonstrar práticas e ferramentas utilizadas no mercado (CQRS, Kafka, Kubernetes, cronjobs, arquitetura modular).

---

## 2. Análise de Mercado

### Referências de produto

| Ferramenta | Pontos fortes | Limitações observadas |
|---|---|---|
| **Nubank Lembretes** | UX simples, integrado ao banco | Recorrência infinita sem parcelas; visão só de ±1 mês; sem métricas; sem forma de pagamento; sem categorias customizadas |
| **Mobills** | Multi-conta, orçamento por categoria, relatórios ricos, sincronização entre dispositivos | App mobile-first; backend fechado; acoplado à plataforma |
| **Despezzas** | Simples, foco em vencimentos e histórico de contas a pagar | Funcionalidades analíticas limitadas; sem controle de contas bancárias/cartões |

### Diferenciais deste projeto

| Limitação dos concorrentes | O que este projeto resolve |
|---|---|
| Recorrência infinita sem controle de parcelas | Número fixo de parcelas e antecipação com desconto |
| Visão restrita a ±1 mês (Nubank) | Horizonte configurável, projeções para meses/anos futuros |
| Sem analytics (Nubank, Despezzas) | Endpoints de métricas por período, categoria e projeção |
| Sem rastreio da conta/cartão usado | `PaymentAccount` com banco, bandeira, tipo (futuro) |
| Categorias fixas ou sem categorias | Categorias criadas pelo usuário, múltiplas por lembrete |
| Plataformas fechadas | API aberta, portável, extensível |

---

## 3. Público-Alvo

- **v1:** Usuário individual gerenciando suas próprias finanças pessoais.
- **Futuro:** Grupos domésticos (ex: casal) com lembretes compartilhados — qualquer membro pode marcar a conta como paga.

---

## 4. Objetivos

### v1 (MVP atual — próximos ajustes)
- API funcional e bem documentada (OpenAPI)
- CRUD de lembretes com recorrências e parcelas
- Registro e rastreamento de transações (com desconto e antecipação)
- Categorias de despesas definidas pelo usuário
- Visão mensal com simulação de contas futuras
- Métricas iniciais: gastos anuais por mês + gastos por categoria
- Controle automático de status "vencido" (módulo agendado separado)

### Fora do escopo (v1)
- Notificações (e-mail / push)
- Módulo de investimentos
- Ledger / controle de receitas
- Módulo compartilhado

---

## 5. Funcionalidades

### 5.1 Autenticação
- Login com e-mail e senha, retorna JWT
- Rate limiting de tentativas de login (Redis)
- Usuário cria conta própria

### 5.2 Lembretes de Pagamento (`PaymentReminder`)
Unidade central do sistema. Representa uma despesa recorrente ou única.

**Campos:**
- `title` (obrigatório, até 30 chars)
- `description` (opcional, até 100 chars)
- `baseDate` — data base da primeira ocorrência
- `recurrence` — UNICA | DIARIA | SEMANAL | QUINZENAL | MENSAL | TRIMESTRAL | SEMESTRAL | ANUAL
- `method` — forma de pagamento (ex: PIX, CARTAO_CREDITO, CARTAO_DEBITO, BOLETO, DINHEIRO)
- `baseValue` — valor base
- `installmentsNumber` — número de parcelas (nulo = sem limite, para recorrências contínuas)
- `categories` — lista de categorias do usuário (opcional, múltiplas)
- `active` — habilitar/desabilitar sem excluir
- `deletedAt` — timestamp de exclusão lógica (ver seção 5.5)

**Operações:**
- Criar, atualizar, buscar por ID
- Listar com filtros e paginação
- Habilitar / desabilitar
- Excluir (soft delete com timestamp)

### 5.3 Transações de Pagamento (`PaymentTransaction`)
Representa uma ocorrência real ou simulada de um pagamento.

**Campos:**
- `reminder` — referência ao lembrete
- `dueDate` — data de vencimento
- `paymentDate` — data do pagamento efetivo
- `method` — forma de pagamento usada (pode diferir do lembrete)
- `installmentNumber` — número da parcela
- `baseValue` — valor esperado (herdado do lembrete)
- `paymentValue` — valor efetivamente pago
- `hasDiscount` — flag: foi pago com desconto?
- `discountValue` — valor do desconto aplicado (calculado: `baseValue - paymentValue`)
- `paymentAccountId` — conta/cartão utilizado (FK para `PaymentAccount`, opcional — ver seção 5.8)
- `status` — PENDENTE | PAGO | VENCIDO | ANTECIPADO | CANCELADO

**Status `CANCELADO`:** atribuído automaticamente a transações PENDENTE quando o lembrete pai é excluído. Transações PAGO não são alteradas — preservam o histórico.

**Operações:**
- Pagar (presente ou futuro) — define `paymentDate`, `paymentValue`, `hasDiscount`
- Antecipar parcelas futuras — usuário define valor e desconto; transações futuras são criadas na hora
- Listar com filtros (período, status, categoria)
- Atualizar dados de um pagamento já registrado

**Geração de transações:**
- Abordagem **lazy/on-demand**: transações são geradas quando o usuário paga ou ao visualizar o mês
- Sem geração automática em background para evitar acúmulo de dados de usuários inativos
- O cronjob (módulo separado) apenas atualiza status PENDENTE → VENCIDO quando a `dueDate` passa

### 5.4 Categorias (`Category`)
- Criadas pelo usuário (nome livre)
- Opcionais e múltiplas por lembrete
- Usadas como filtro em listagens e métricas

**Operações:**
- CRUD de categorias do usuário
- Associar/desassociar categorias de um lembrete

### 5.5 Exclusão Lógica (Soft Delete)

**Decisão:** Usar `deletedAt` (timestamp) em vez de flag `deleted` (boolean).

**Por quê `deletedAt` e não boolean:**
- Registra *quando* foi excluído — informação útil para auditoria e eventual recovery
- Mesma semântica de filtragem (`WHERE deletedAt IS NULL`), sem custo adicional
- Consistente com o padrão de auditoria já existente no projeto

**Comparação de abordagens consideradas:**

| Abordagem | Prós | Contras |
|---|---|---|
| `deleted` boolean | Simples | Sem rastreio de quando; menos expressivo |
| `deletedAt` timestamp ✅ | Rastreia quando; reversível; padrão de mercado | Queries precisam filtrar `IS NULL` |
| Enum de status (ATIVO/ARQUIVADO) | Mais expressivo, sem semântica de "destruição" | Conflita com o campo `active` já existente |
| Tabela de arquivo separada | Main table fica limpa | Queries de histórico ficam complexas |

**Comportamento em cascata ao excluir um lembrete:**
1. Setar `deletedAt = now()` no `PaymentReminder`
2. Transações com `status = PENDENTE` → atualizar para `CANCELADO`
3. Transações com `status = PAGO` → manter intactas (histórico financeiro)
4. Transações com `status = VENCIDO` → atualizar para `CANCELADO`

**Recuperação:** possível zerar `deletedAt` para restaurar o lembrete, mas as transações canceladas não são revertidas automaticamente (decisão do usuário).

---

### 5.6 Contas e Cartões (`PaymentAccount`) — Futuro

Entidade que representa uma conta bancária ou cartão do usuário. Permite rastrear *com qual conta* cada pagamento foi realizado, indo além do simples tipo (`PIX`, `CREDITO`).

**Modelo proposto:**

```
PaymentAccount
  id          UUID
  userId      FK → User
  name        string         — apelido, ex: "Nubank Crédito", "Itaú Corrente"
  type        enum           — tipo da conta/cartão

  // Dados bancários (opcionais)
  bank        string         — ex: "Nubank", "Itaú", "Bradesco", "Inter", "C6"

  // Campos exclusivos para cartões
  cardFlag    enum           — VISA | MASTERCARD | ELO | AMEX | HIPERCARD | OUTRO
  cardCategory enum          — CREDITO | DEBITO | BENEFICIO_ALIMENTACAO | BENEFICIO_REFEICAO | BENEFICIO_COMBUSTIVEL
```

**Tipo de conta (`PaymentAccountType`):**

| Valor | Descrição |
|---|---|
| `CONTA_CORRENTE` | Conta corrente bancária |
| `CONTA_POUPANCA` | Conta poupança |
| `CARTAO_CREDITO` | Cartão de crédito |
| `CARTAO_DEBITO` | Cartão de débito |
| `CARTAO_BENEFICIO` | Cartão de benefício (VA, VR, combustível) |
| `PIX` | Chave Pix (sem conta específica) |
| `DINHEIRO` | Dinheiro físico |
| `BOLETO` | Boleto bancário |

**Integração com transações:**
- `PaymentTransaction.paymentAccountId` (FK opcional) — o usuário pode ou não informar a conta
- O `method` (enum atual) no lembrete continua como "sugestão padrão de tipo de pagamento"
- Se `paymentAccountId` for informado, o tipo é derivado do `PaymentAccount.type`; caso contrário, usa-se o `method` do lembrete

**Métricas habilitadas por esta feature:**
- Gastos por cartão/conta no mês
- Qual conta mais usada para pagar contas recorrentes
- Saldo estimado por conta (fase Ledger)

**Operações:**
- CRUD de contas/cartões do usuário
- Definir conta padrão por lembrete (pré-seleciona no pagamento)
- Listar transações por conta

---

### 5.7 Visão Mensal (`/payments/reminders/monthly-bills`)
- Retorna transações reais + simuladas para um mês/ano
- Inclui totais (pago, pendente, projetado)
- Suporte a `withSimulatedBills` flag para incluir/excluir simulações

### 5.8 Métricas (endpoints iniciais)

**Endpoint 1 — Gastos mensais do ano**
```
GET /metrics/annual-summary?year=2026
```
Retorna: array com 12 posições, cada uma com `month`, `totalPaid`, `totalPending`, `totalProjected`

**Endpoint 2 — Gastos por categoria**
```
GET /metrics/by-category?year=2026&month=6
```
Retorna: lista de categorias com total gasto no período

> O céu é o limite para métricas futuras. Começamos com essas duas para estabelecer o padrão de endpoint.

---

## 6. Módulo de Agendamento (Projeto Separado)

Responsabilidade única: atualizar status de transações vencidas.

**Lógica:** Todo dia (ou em intervalo configurável), buscar transações com `status = PENDENTE` e `dueDate < hoje`, atualizar para `status = VENCIDO`.

**Tecnologia sugerida:** Spring Batch ou Spring Scheduling com `@Scheduled` em módulo Maven separado dentro do mesmo repositório (monorepo modular). Pode evoluir para um serviço Spring com integração Kafka no futuro.

**Alternativa a avaliar:** Em vez de cronjob puro, publicar eventos de domínio (`TransactionOverdueEvent`) que um consumidor processa — isso abre espaço natural para introduzir Kafka.

---

## 7. Roadmap Futuro

### Fase 2 — Contas e Cartões (`PaymentAccount`)
- CRUD de contas bancárias e cartões do usuário
- Banco, bandeira, categoria do cartão (crédito, débito, benefício)
- Rastreio de qual conta pagou cada transação
- Métricas por conta/cartão

### Fase 4 — Shared Reminders
- Grupos domésticos (ex: casal)
- Qualquer membro do grupo pode marcar uma conta como paga
- Visibilidade compartilhada dos lembretes do grupo

### Fase 5 — Ledger / Receitas
- Registro de fontes de renda (salário mensal, pagamentos pontuais)
- Visão de balanço: entradas vs saídas
- Permite calcular saldo projetado por período

### Fase 6 — Investment Reminder
- Lembrete especial: quando pago, registra um **ativo** do usuário
- Ex: pagamento de título, CDB, ação, fundo
- Painel de patrimônio separado

### Fase 7 — Notificações
- Push notification ou e-mail quando conta está próxima do vencimento
- Configurável por lembrete (X dias antes)
- Cronjob ou event-driven via Kafka

---

## 8. Modelo de Domínio

```
User
 ├─► Category (0..N)         [user-owned]
 ├─► PaymentAccount (0..N)   [user-owned, futuro]
 └─► PaymentReminder (0..N)
       ├── categories: Category[] (0..N, optional)
       └─► PaymentTransaction (0..N)
             └── paymentAccount: PaymentAccount (0..1, optional, futuro)
```

```
PaymentReminder
  id, userId, title, description
  baseDate, recurrence, method
  baseValue, installmentsNumber
  active
  deletedAt (null = não excluído)
  categories[]

PaymentTransaction
  id, reminderId
  dueDate, paymentDate
  method, installmentNumber
  baseValue, paymentValue
  hasDiscount, discountValue
  paymentAccountId (FK opcional, futuro)
  status: PENDENTE | PAGO | VENCIDO | ANTECIPADO | CANCELADO

PaymentAccount (futuro)
  id, userId, name, type, bank
  cardFlag, cardCategory (nullable, só para cartões)

Category
  id, userId, name
```

---

## 9. Estratégia de Timezone

### Regra fundamental
**Armazenar em UTC. Retornar UTC. O cliente converte para exibição.**

### Tipos por caso de uso

| Campo | Tipo Java | Tipo PostgreSQL | Justificativa |
|---|---|---|---|
| `createdAt`, `updatedAt`, `deletedAt`, `enabledAt`, `disabledAt` | `Instant` | `TIMESTAMPTZ` | Momento preciso no tempo, sempre UTC |
| `dueDate`, `paymentDate` | `LocalDate` | `DATE` | Data de calendário — "dia 10" é o dia 10 no fuso do usuário, timezone-agnostic |
| `User.timezone` | `String` (IANA) | `VARCHAR` | Fuso preferido do usuário, ex: `"America/Sao_Paulo"` |

### Por que `Instant` e não `LocalDateTime`?
`LocalDateTime` não carrega informação de fuso — o mesmo valor `"2026-06-05T14:30:00"` significa coisas diferentes dependendo do servidor. `Instant` representa sempre um ponto único na linha do tempo (epoch UTC). É o tipo correto para qualquer timestamp de auditoria.

### Estado atual do código

**Correto — já implementado:**
- `AbstractAudit` usa `Instant` em todos os campos de auditoria ✅
- Migrations Liquibase usam `TIMESTAMP WITH TIME ZONE` em todos os campos de auditoria ✅
- `hibernate.jdbc.time_zone=UTC` configurado no `application.yaml` ✅

**Pendente de implementação:**
- Campo `timezone` (IANA String) ausente em `User` / `AbstractUser` — necessário para cálculos server-side de fronteira de data (ex: "quais contas vencem *hoje*?" depende do fuso do usuário)

**Redundância a resolver:**
- `AbstractAudit` tem `deleted: Boolean` E `deletedAt: Instant` simultaneamente. O booleano é verificado em `validateIsDeleted()`. O `deletedAt` deveria ser a única fonte da verdade (`deletedAt != null` = excluído). A migração para remover o booleano envolve atualizar `validateIsDeleted()` para checar `deletedAt != null`.

### Uso do `timezone` no backend

O campo `User.timezone` é utilizado em:
1. **Cronjob de vencidos** — calcular "hoje" no fuso do usuário: `ZonedDateTime.now(ZoneId.of(user.timezone)).toLocalDate()`
2. **Endpoint `monthly-bills`** — determinar o mês corrente no fuso do usuário quando `year`/`month` não são informados
3. **Endpoints de métricas** — agregar por mês no fuso do usuário

### Fluxo resumido

```
Pagamento registrado:
  paymentDate = "2026-06-05" (LocalDate — sem fuso, correto)
  audit.updatedAt = Instant.now() → armazenado como UTC no banco

API retorna:
  "paymentDate": "2026-06-05"
  "updatedAt": "2026-06-05T17:32:00Z"   ← UTC

Frontend exibe (usuário em UTC-3):
  "Pago em 05/06/2026 às 14:32"          ← cliente converteu
```

---

## 11. Arquitetura Técnica

### Stack atual
- **Java 17** + **Spring Boot**
- **PostgreSQL** — persistência principal
- **Redis** — rate limiting de login
- **JWT** — autenticação stateless
- **Docker / Docker Compose** — ambiente local
- **Arquitetura Hexagonal** (Ports & Adapters)

### Evolução planejada
| Tecnologia | Quando introduzir | Problema que resolve |
|---|---|---|
| **CQRS** | Ao criar os endpoints de métricas | Separar leitura (queries analíticas) de escrita — natural para analytics |
| **Spring Batch / Scheduling** | Módulo de agendamento (vencidos) | Processamento em lote de transações |
| **Kafka** | Ao evoluir o módulo de agendamento | Publicar `TransactionOverdueEvent`, desacoplar notificações futuras |
| **Kubernetes** | Deploy / infra | Orquestração de containers, escalabilidade |

### Estrutura modular
Monorepo com módulos Maven separados (não microsserviços):
- `financial-reminder-api` — API principal (atual)
- `financial-reminder-scheduler` — módulo de agendamento (a criar)

---

## 12. O que falta para fechar a v1

- [x] Todos os endpoints documentados no `openapi.yaml`
- [x] Remover `api_reference` desatualizada
- [ ] Implementar **Categorias** (CRUD + associação com lembretes)
- [ ] Campo `hasDiscount` + `discountValue` nas transações
- [ ] **Endpoints de métricas** (annual summary + by-category)
- [ ] Módulo de agendamento para status VENCIDO
- [ ] Campo `timezone` (IANA String) na entidade `User` + migration
- [ ] Migrar `validateIsDeleted()` para usar `deletedAt != null` e remover o `deleted: Boolean` redundante

---

## 13. Questões em Aberto

- **InvestmentReminder:** Definir melhor o modelo. Quando um "lembrete de investimento" é pago, o que exatamente é registrado como ativo? Qual o modelo de dado de um ativo?
- **Ledger:** Como modelar receitas? É uma entidade separada (`Income`) ou um `PaymentReminder` com valor positivo?
- **Antecipação:** Quando o usuário antecipa parcelas, as transações intermediárias (entre hoje e a antecipada) ficam com status `ANTECIPADO` ou são geradas como `PAGO`?
- **Soft delete:** ~~Decidido~~ — usar `deletedAt` timestamp. Cascata: PENDENTE/VENCIDO → CANCELADO; PAGO → preservado. Ver seção 5.5.
