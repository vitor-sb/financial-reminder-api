#!/usr/bin/env bash
# Build da imagem, import no k3d, Secret a partir do .env e apply dos manifests.
set -euo pipefail
export PATH=$HOME/.local/bin:$PATH
cd "$(dirname "$0")"
API=..

docker build -t financial-reminder-api:local "$API"
k3d image import financial-reminder-api:local -c dev

kubectl apply -f namespace.yaml
kubectl -n financial create secret generic financial-env --from-env-file="$API/.env" \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k .
kubectl -n financial rollout restart deploy/api
