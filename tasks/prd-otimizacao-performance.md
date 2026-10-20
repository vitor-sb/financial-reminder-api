# PRD: Otimização de desempenho — Financial Reminder API

> **Origem:** gargalos candidatos de `loadtest/REPORT.md` (baseline do commit `81caf67`, 2026-10-20). Sem meta de volume assumida: o objetivo é remover desperdício comprovado, não atingir um número.
> **Natureza:** performance sem mudança de contrato HTTP, de regra de negócio nem de proteção de segurança.

## ⚠️ Lembrete para o Claude: onde medir

Medir sempre pelo `loadtest/run.sh`, que cria um `git worktree` fora de `/workspace` (mount do `C:`, mede ~35% pior). Máquina ociosa: sem Ralph, `mvn` ou outro processo pesado em paralelo durante a medição. Registrar commit, local e carga da máquina no relatório.

## Baseline de referência (commit `81caf67`, conc. 8 / 32 / 64)

| cenário | req/s | p95 ms |
|---|---|---|
| A login | 126 / 171 / 173 | 69 / 273 / 562 |
| B listagem mensal | 447 / 497 / 496 | 21 / 79 / 162 |
| C criar + pagar (3 req) | 237 / 236 / 231 | 39 / 154 / 342 |

Postgres no B consome 3,5–4,5 cores com 4–6 backends ativos; no A fica a ~4%.

## Regras para todas as stories

- [ ] **Medir antes de mexer**: cada story registra o número antes e depois (mesmo cenário, mesma concorrência, mesma máquina) em `loadtest/REPORT.md`; sem ganho ≥ 10% no cenário alvo, a mudança é revertida ou fica desligada e a story documenta o porquê
- [ ] `mvn test` verde, sem remover nem enfraquecer testes (inclui `src/test/.../integration`, SEC01–SEC12)
- [ ] Contrato HTTP, `openapi.yaml` e respostas idênticos; migrations só de índice, reversíveis
- [ ] Nada que enfraqueça segurança (nem fator do bcrypt, nem limite de login, nem logs sem dados pessoais)

## User Stories

#### US-OP01: Plano de execução e índice da listagem mensal (cenário B)
**Description:** Como mantenedor, quero saber por que a consulta mensal consome tanta CPU do banco.

**Acceptance Criteria:**
- [ ] `EXPLAIN (ANALYZE, BUFFERS)` das consultas de `findTransactionsForMonth` e `findRelevantRemindersForSimulation` com o volume do load test (64 usuários x 30 lembretes), colado no `REPORT.md`
- [ ] Migration Liquibase com os índices que o plano justificar (candidatos: `(reminder_id, due_date)` em `payment_transactions`; filtro de `audit.enabled/deleted`), cada um com o ganho medido
- [ ] Índice sem efeito no plano não é criado

#### US-OP02: Totais e paginação da listagem mensal sem carregar tudo (cenário B)
**Description:** Como usuário, quero a listagem mensal rápida mesmo com muitas contas.

**Acceptance Criteria:**
- [ ] Sem N+1: `reminder` buscado junto da transação (fetch join ou projeção); contagem de statements por requisição medida antes e depois
- [ ] Totais (`paid`, `pending`, `overdue`) calculados por agregação SQL ou por uma única passagem, com os mesmos valores do cálculo atual (teste comparando os dois caminhos)
- [ ] Conta simulada continua somada na mesma ordem e com a mesma paginação (`MonthlyBillsSimulator`/`Paginator` sem mudança de resultado)
- [ ] Teste de caracterização do JSON da listagem antes da mudança, mantido verde depois

#### US-OP03: Custo do login e do bcrypt (cenário A)
**Description:** Como mantenedor, quero entender e limitar o custo do bcrypt sem enfraquecer a segurança.

**Acceptance Criteria:**
- [ ] Medir ms por `matches` do bcrypt na máquina de referência e quantos hashes são calculados por login (inclui o hash de equalização da SEC04); registrar no `REPORT.md`
- [ ] Remover qualquer hash redundante por login, se existir, sem alterar a resposta nem o tempo relativo entre e-mail desconhecido e senha errada
- [ ] **Não** reduzir o fator de custo do bcrypt (decisão explícita de segurança); qualquer alteração dele fica fora desta story
- [ ] Avaliar limitar a concorrência de hashes (semáforo / executor dedicado) para o login não matar a latência de B e C sob carga mista; adotar só com ganho medido num cenário misto A+B

#### US-OP04: Pool de conexões configurável (cenários B e C)
**Description:** Como operador, quero ajustar o pool sem recompilar.

**Acceptance Criteria:**
- [ ] `spring.datasource.hikari.maximum-pool-size` e `minimum-idle` lidos de variáveis de ambiente, com o padrão atual (10)
- [ ] Rodar B e C com 10, 20 e 30 conexões depois da OP02; registrar; só mudar o padrão com ganho ≥ 10%
- [ ] `application-loadtest.yaml` continua isolado e sem efeito no padrão

#### US-OP05: Caminho de escrita (cenário C)
**Description:** Como mantenedor, quero menos idas ao banco por escrita.

**Acceptance Criteria:**
- [ ] Contagem de statements e commits por requisição de criar lembrete e de pagar (`log_statement=all` num worktree), antes e depois, no `REPORT.md`
- [ ] SELECTs repetidos na mesma requisição eliminados (reuso da entidade já carregada) sem trocar de transação nem de isolamento
- [ ] `hibernate.jdbc.batch_size` e `order_inserts` avaliados para o insert de parcelas; adotar só com ganho medido
- [ ] Nível de log dos eventos de negócio (R04) medido: se o custo de I/O de `logger.info` for relevante, mover para `debug` só os de alta frequência e manter os eventos de auditoria em `info`

#### US-OP06: Medição com driver mais forte e relatório comparativo
**Description:** Como mantenedor, quero confirmar o teto sem o limite do driver Python.

**Acceptance Criteria:**
- [ ] Repetir A, B e C com k6 (container) contra a API do worktree, nos mesmos níveis; comparar com o driver Python
- [ ] `REPORT.md` com a tabela baseline x final por cenário e a lista do que cada story rendeu (ou "sem ganho")
- [ ] Se o ganho total num cenário for < 10%, registrar que o teto é de CPU/driver e não otimizar mais

## Non-Goals

- Meta de throughput; cache de resposta; réplica de leitura; troca de banco.
- Reduzir o custo do bcrypt, afrouxar limites de login ou remover logs de auditoria para ganhar números.
- Separar entidade de modelo de domínio.

## Ordem

OP01 → OP02 → OP03 → OP04 → OP05 → OP06. Rodar o `loadtest/run.sh` em cada story, sempre fora de `/workspace`.
