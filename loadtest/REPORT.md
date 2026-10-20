# Baseline de carga (US-003)

**Baseline, não requisito.** Nenhuma meta de volume é assumida; os números são o que uma rodada mediu.

Reproduzir: `loadtest/run.sh [concorrência...]` (docker, JDK 17, Maven, python3; escreve `loadtest/results.txt`).

- Commit: `81caf67` (+ `loadtest/results.txt` da própria rodada). Data: 2026-10-20 20:31.
- Local: git worktree em `/tmp/fra-loadtest-*` (fora de `/workspace`).
- Máquina: AMD Ryzen 5 5600X 6C/12T, 16 GB, WSL2, OpenJDK 17.0.20, Postgres 17.7 em Docker (config padrão), Hikari padrão (pool 10), Redis em Docker. API, bancos e driver dividem a máquina.
- Ociosidade: sem mvn/Ralph em paralelo, mas os containers do Plane estavam de pé (≤3% CPU) e o load average vinha de 3.8 (caindo de ~9, build anterior). Pode não ser 100% ociosa; uma rodada anterior idêntica (mesmo código) deu números dentro de ±1-2%, exceto B/8 (457 vs 447).
- Dados: 64 usuários x 30 lembretes mensais. Loop fechado, 5s de warmup + 30s medidos por nível. 0 erros em todos os níveis.
- Cenários: **A** `POST /auth/login`; **B** listagem mensal (`ListMonthlyBills`, com simulação); **C** criar lembrete + localizar transação + pagar (1 iteração = 3 requests).

| cenário | conc. | req/s | p50 ms | p95 ms | p99 ms | pg CPU média / máx | máx conexões pg | máx backends ativos |
|---|---|---|---|---|---|---|---|---|
| A | 8  | 125.8 | 63.5  | 68.9  | 74.4  | 4% / 7%     | 10 | 0 |
| A | 32 | 170.8 | 186.9 | 273.1 | 317.4 | 4% / 7%     | 10 | 1 |
| A | 64 | 172.5 | 368.9 | 561.7 | 657.0 | 4% / 6%     | 10 | 0 |
| B | 8  | 446.7 | 17.4  | 21.3  | 25.8  | 348% / 408% | 10 | 5 |
| B | 32 | 496.8 | 65.7  | 79.3  | 90.8  | 395% / 446% | 10 | 6 |
| B | 64 | 495.8 | 134.3 | 162.2 | 180.3 | 400% / 449% | 10 | 4 |
| C | 8  | 237.0 | 33.4  | 38.9  | 42.8  | 78% / 87%   | 10 | 8 |
| C | 32 | 236.4 | 135.8 | 153.7 | 164.5 | 87% / 97%   | 10 | 2 |
| C | 64 | 231.1 | 272.2 | 341.8 | 376.0 | 92% / 107%  | 10 | 3 |

CPU do Postgres é `docker stats` (100% = 1 core; 12 cores no total), amostrado a cada 2s. Conexões são contadas em `pg_stat_activity` (application_name do driver JDBC): o pool Hikari de 10 fica sempre cheio, mas poucas estão ativas por vez.

## Leitura

- **Saturação**: todos os cenários saturam já entre 8 e 32 clientes; de 32 para 64 a vazão não sobe e a latência dobra (p50 ≈ concorrência / vazão). A: ~171 req/s; B: ~497 req/s; C: ~236 iterações/s (~710 req/s).
- **p95 no pior nível (64)**: A 562 ms, B 162 ms, C 342 ms. Com 8 clientes: A 69 ms, B 21 ms, C 39 ms.
- **A (login)**: limitado por CPU de bcrypt na JVM; o Postgres fica a ~4%.
- **B (listagem)**: o Postgres consome 3,5-4,5 cores com só 4-6 backends ativos, então o teto vem da CPU do banco (compartilhada com a JVM e o driver), não do pool.
- **C (escrita)**: Postgres a ~80-100% de um core, JVM e escrita síncrona dividindo a máquina; vazão constante de 8 a 64.
- Limitação: driver Python e tudo na mesma máquina podem encurtar o teto; repetir com k6/Gatling e máquina separada para um número limpo.

## Gargalos candidatos (não otimizados; a otimização vira PRD próprio)

1. **B: consulta de `ListMonthlyBills`** (`ListMonthlyBillsUseCaseImpl` busca todas as transações do mês do usuário e depois soma, simula e pagina em memória; `MonthlyBillsTotals`/`MonthlyBillsPaginator`). CPU de banco desproporcional à carga: verificar plano (`EXPLAIN`), índices por (usuário, vencimento) e paginação/totais no SQL.
2. **A: custo do bcrypt** por login na JVM (fator de custo, e se o `PasswordEncoder` roda na thread do request). Trade-off de segurança: não baixar o custo sem decisão explícita; considerar apenas mais CPU/threads.
3. **Pool Hikari = 10** (padrão): sempre cheio, mas com poucos backends ativos; só vale mexer se o pool virar espera depois de B ser otimizado.
4. **C: 3 requests por iteração**, cada uma com `logger.info` e transação própria; olhar commits/fsync do Postgres (config padrão) se C passar a ser o limite.
5. **Medição**: driver Python, API e bancos no mesmo host.
