#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PLATFORM_DIR="$ROOT_DIR/distribution-platform"
JAR_DIR="$PLATFORM_DIR/docker-build-jars"
MAVEN_REPO="$ROOT_DIR/.docker-cache/m2"

MODULES=(
  user-service
  product-service
  stock-service
  order-service
  pay-service
  auth-service
  gateway-service
)

echo "[build] packaging backend services"
mkdir -p "$MAVEN_REPO"

if command -v java >/dev/null 2>&1; then
  echo "[build] using local Maven Wrapper"
  (
    cd "$PLATFORM_DIR"
    ./user-service/mvnw -Dmaven.repo.local="$MAVEN_REPO" -DskipTests package
  )
else
  echo "[build] local Java not found, using Docker Maven image"
  docker run --rm \
    -v "$PLATFORM_DIR:/workspace" \
    -v "$MAVEN_REPO:/root/.m2" \
    -w /workspace \
    docker.m.daocloud.io/library/maven:3.9.9-amazoncorretto-21 \
    mvn -DskipTests package
fi

mkdir -p "$JAR_DIR"

for module in "${MODULES[@]}"; do
  jar="$PLATFORM_DIR/$module/target/$module-1.0.0.jar"
  if [[ ! -f "$jar" ]]; then
    echo "[error] missing jar: $jar" >&2
    exit 1
  fi

  cp "$jar" "$JAR_DIR/$module.jar"
done

echo "[build] backend jars copied to $JAR_DIR"
