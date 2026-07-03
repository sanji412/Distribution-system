#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

FRONTEND_URL="${FRONTEND_URL:-http://localhost:${FRONTEND_HOST_PORT:-5173}}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:${GATEWAY_HOST_PORT:-9000}}"
USERNAME="${SMOKE_USERNAME:-admin}"
PASSWORD="${SMOKE_PASSWORD:-123456}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

fail() {
  echo "[fail] $*" >&2
  exit 1
}

http_code() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local output="$4"
  if [[ -n "$body" ]]; then
    curl -sS --max-time 20 -o "$output" -w "%{http_code}" \
      -X "$method" \
      -H "Content-Type: application/json" \
      -d "$body" \
      "$url"
  else
    curl -sS --max-time 20 -o "$output" -w "%{http_code}" \
      -X "$method" \
      "$url"
  fi
}

assert_status() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  [[ "$actual" == "$expected" ]] || fail "$name expected HTTP $expected but got $actual"
  echo "[ok] $name HTTP $actual"
}

assert_json_code_200() {
  local name="$1"
  local file="$2"
  python3 - "$name" "$file" <<'PY'
import json
import sys

name, path = sys.argv[1], sys.argv[2]
with open(path, "r", encoding="utf-8") as fh:
    payload = json.load(fh)
if payload.get("code") != 200:
    raise SystemExit(f"[fail] {name} expected JSON code=200 but got {payload.get('code')}: {payload}")
print(f"[ok] {name} JSON code=200")
PY
}

frontend_body="$tmp_dir/frontend.html"
frontend_status="$(http_code GET "$FRONTEND_URL/" "" "$frontend_body")"
assert_status "frontend" "200" "$frontend_status"
grep -q '<div id="app">' "$frontend_body" || fail "frontend page does not look like Vue app"

login_body="$tmp_dir/login.json"
login_status="$(http_code POST "$GATEWAY_URL/api/auth/login" "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" "$login_body")"
assert_status "login" "200" "$login_status"
assert_json_code_200 "login" "$login_body"

token="$(python3 - "$login_body" <<'PY'
import json
import sys
with open(sys.argv[1], "r", encoding="utf-8") as fh:
    payload = json.load(fh)
token = (payload.get("data") or {}).get("token")
if not token:
    raise SystemExit("missing token")
print(token)
PY
)"

authenticated_get() {
  local name="$1"
  local path="$2"
  local file="$tmp_dir/${name}.json"
  local status
  status="$(curl -sS --max-time 20 -o "$file" -w "%{http_code}" \
    -H "Authorization: Bearer $token" \
    "$GATEWAY_URL$path")"
  assert_status "$name" "200" "$status"
  assert_json_code_200 "$name" "$file"
}

authenticated_get "governance" "/api/governance/overview"
authenticated_get "products" "/api/product/list"
authenticated_get "stocks" "/api/stock/list"
authenticated_get "order_dashboard" "/api/order/dashboard"
authenticated_get "ai_recommendations" "/api/order/ai/recommendations"

echo "[ok] docker smoke checks passed"
