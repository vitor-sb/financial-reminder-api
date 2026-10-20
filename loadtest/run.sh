#!/usr/bin/env bash
# Baseline reproduzível: Postgres+Redis (docker compose) + API (perfil loadtest) + load.py, medidos num worktree fora de /workspace.
# Uso: loadtest/run.sh [CONCURRENCY...]   (padrão: 8 32 64; 5s de warmup + 30s medidos por cenário e nível)
# Escreve loadtest/results.txt. Rode com a máquina ociosa (sem mvn/Ralph em paralelo).
set -euo pipefail
SRC=$(cd "$(dirname "$0")/.." && pwd)
OUT=$SRC/loadtest/results.txt
LEVELS=("$@"); [ $# -eq 0 ] && LEVELS=(8 32 64)
WT=$(mktemp -d /tmp/fra-loadtest-XXXXXX); PORT=18080
COMPOSE="docker compose -f $SRC/loadtest/docker-compose.yaml"
git -C "$SRC" worktree add --detach "$WT/src" HEAD >/dev/null
# o perfil/scripts novos ainda não commitados também entram na medição
cp -r "$SRC/loadtest/." "$WT/src/loadtest/" ; cp "$SRC/src/main/resources/application-loadtest.yaml" "$WT/src/src/main/resources/"
cleanup() { kill ${APP:-} ${SAMPLER:-} 2>/dev/null || true; $COMPOSE down -v >/dev/null 2>&1 || true
  git -C "$SRC" worktree remove --force "$WT/src" >/dev/null 2>&1 || true; rm -rf "$WT"; }
trap cleanup EXIT
$COMPOSE up -d --wait >/dev/null
# daemon docker irmão: portas publicadas ficam no host do daemon, então tenta o IP do container
ip_of() { docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$1"; }
HOST=localhost; DBPORT=15432; RPORT=16379
PIP=$(ip_of fra-loadtest-pg); RIP=$(ip_of fra-loadtest-redis)
if [ -n "$PIP" ] && ! timeout 2 bash -c "</dev/tcp/localhost/15432" 2>/dev/null && timeout 2 bash -c "</dev/tcp/$PIP/5432" 2>/dev/null; then
  DBHOST=$PIP; DBPORT=5432; RHOST=$RIP; RPORT=6379; else DBHOST=localhost; RHOST=localhost; fi
(cd "$WT/src" && mvn -q -B -DskipTests package)
JWT_SECRET=loadtest-secret-not-real-0123456789abcdef DATABASE_HOST=$DBHOST DATABASE_PORT=$DBPORT \
API_DB_USER=postgres API_DB_PASSWORD=loadtest REDIS_HOST=$RHOST REDIS_PORT=$RPORT REDIS_PASSWORD=loadtest \
SPRING_PROFILES_ACTIVE=loadtest SERVER_PORT=$PORT \
  java -jar "$WT"/src/target/financial-reminder-api-0.0.1-SNAPSHOT.jar >"$SRC/loadtest/app.log" 2>&1 & APP=$!
until curl -sf localhost:$PORT/actuator/health >/dev/null; do kill -0 $APP; sleep 1; done
psql_() { docker exec fra-loadtest-pg psql -U postgres -d finance-management -Atc "$1"; }
# amostra a cada 2s: conexões Hikari ativas (via Postgres, pelo application_name da API) e CPU do container Postgres
sample() { while sleep 2; do
  echo "$(psql_ "select count(*) from pg_stat_activity where datname='finance-management' and state='active' and pid<>pg_backend_pid()") $(psql_ "select count(*) from pg_stat_activity where datname='finance-management' and application_name like 'PostgreSQL JDBC%'") $(docker stats --no-stream --format '{{.CPUPerc}}' fra-loadtest-pg | tr -d %)"
done >"$1" 2>/dev/null; }
{
  echo "date: $(date -Is)"; echo "commit: $(git -C "$SRC" rev-parse --short HEAD)$(git -C "$SRC" diff --quiet HEAD -- . || echo '+dirty')"
  echo "measured in: $WT/src (git worktree, outside /workspace)"
  echo "cpu: $(grep -m1 'model name' /proc/cpuinfo | cut -d: -f2) x$(nproc)"; echo "mem_mb: $(free -m | awk '/Mem:/{print $2}')"
  echo "java: $(java -version 2>&1 | head -1)"; echo "postgres: $(psql_ 'show server_version') (docker, default config); hikari: defaults (pool 10)"
  echo "note: app, DB, Redis and driver share one machine"
  python3 "$SRC/loadtest/load.py" setup http://localhost:$PORT
  for sc in A B C; do for c in "${LEVELS[@]}"; do
    S=$(mktemp); sample "$S" & SAMPLER=$!
    python3 "$SRC/loadtest/load.py" run http://localhost:$PORT $sc "$c" 30 5
    kill $SAMPLER 2>/dev/null || true; wait $SAMPLER 2>/dev/null || true
    awk '{if($1>a)a=$1; if($2>c)c=$2; s+=$3; if($3>m)m=$3; n++} END{if(n)printf "  sampled (%d samples/2s): max active pg backends %d, max pg connections %d, pg cpu avg %.0f%% max %.0f%%\n",n,a,c,s/n,m}' "$S"
  done; done
} | tee "$OUT"
