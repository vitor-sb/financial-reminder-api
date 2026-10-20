# PRD: Teste de carga — Financial Reminder API

> **Origem:** próximo tópico após a refatoração (2026-10-20). Segue o modelo de `validation-flow-engine/loadtest/` (`run.sh`, `load.py`, `REPORT.md`).
> **Natureza:** medição. Sem meta de volume assumida; o resultado é um baseline, não um requisito.

## ⚠️ Lembrete para o Claude: onde rodar

**Rodar o teste de carga a partir de um `git worktree` fora de `/workspace`** (scratchpad ou `/tmp`), nunca direto de `/workspace`. `/workspace` é um mount do `C:` do Windows e mede ~35% pior: no validation-flow-engine o mesmo código deu ~380 req/s em `/workspace` e ~596 req/s num worktree em `/tmp` (ver `validation-flow-engine/loadtest/REPORT.md`, seção "Drop explained"). Também não rodar com Ralph, `mvn` ou outro processo pesado na mesma máquina. Registrar no relatório o commit e o caminho usados.

## Regras para todas as stories

- [ ] Medição feita em worktree fora de `/workspace`, máquina ociosa, com commit e local registrados
- [ ] Nenhum código de produção alterado só para o teste, exceto perfil Spring `loadtest` isolado
- [ ] Dados e credenciais do teste fictícios; nada de segredo real nos scripts

## User Stories

#### US-LT01: Harness de carga
**Description:** Como mantenedor, quero um script reproduzível de carga.

**Acceptance Criteria:**
- [ ] `loadtest/run.sh [concurrency...]` cria o worktree fora de `/workspace`, sobe Postgres e Redis via docker compose, builda e roda a API com perfil `loadtest`, e remove o worktree no fim
- [ ] `loadtest/load.py` em loop fechado, com 5s de warmup e 30s medidos por nível, reporta req/s, p50/p95/p99 e erros
- [ ] Limite de login por IP e por e-mail (SEC03) relaxado só no perfil `loadtest`, sem alterar o padrão
- [ ] README do projeto aponta para `loadtest/`

#### US-LT02: Cenários
**Description:** Como mantenedor, quero cenários representativos.

**Acceptance Criteria:**
- [ ] Cenário A: login (`POST /auth/login`) com usuários pré-criados
- [ ] Cenário B: listagem mensal de contas (`ListMonthlyBills`) com volume realista de lembretes por usuário
- [ ] Cenário C: criação de lembrete e pagamento de transação (escrita)
- [ ] Cada cenário roda com concurrency 8, 32 e 64

#### US-LT03: Baseline e relatório
**Description:** Como mantenedor, quero um baseline documentado.

**Acceptance Criteria:**
- [ ] `loadtest/REPORT.md` com máquina, commit, local da medição, tabela por cenário e nível, e leitura (onde satura, p95)
- [ ] Uso de conexões do Hikari e CPU do Postgres amostrados
- [ ] Gargalos candidatos listados sem otimizar; a otimização vira PRD próprio

## Non-Goals

- Meta de throughput, otimização e tuning.
- Teste de carga em ambiente de produção ou k8s.

## Ordem

LT01 → LT02 → LT03.
