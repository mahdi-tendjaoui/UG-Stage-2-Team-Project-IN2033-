#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
chmod +x ./mvnw 2>/dev/null || true
./mvnw -q clean javafx:run
