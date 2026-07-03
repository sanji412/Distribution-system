#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export DOCKER_BUILDKIT="${DOCKER_BUILDKIT:-0}"

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "[init] created .env from .env.example"
  echo "[hint] edit .env and set DEEPSEEK_API_KEY before AI demo if needed"
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${DEEPSEEK_API_KEY:-}" || "${DEEPSEEK_API_KEY:-}" == "sk-placeholder" ]]; then
  echo "[hint] DEEPSEEK_API_KEY is not configured, AI endpoints will use local fallback"
fi

"$ROOT_DIR/scripts/build-backend-jars.sh"

docker compose up --build -d

echo
echo "Frontend: http://localhost:${FRONTEND_HOST_PORT:-5173}"
echo "Gateway:  http://localhost:${GATEWAY_HOST_PORT:-9000}"
echo "Nacos:    http://localhost:${NACOS_HOST_PORT:-8848}/nacos"
echo "Seata:    http://localhost:${SEATA_CONSOLE_HOST_PORT:-7091}"
echo "Login:    admin / 123456"
echo
echo "Run smoke checks with: ./scripts/docker-smoke.sh"
