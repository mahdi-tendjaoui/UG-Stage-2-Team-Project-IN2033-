#!/usr/bin/env bash
# Launches IPOS-SA via the Maven wrapper that ships with the project.
# (No new tooling — Maven was already part of the project.)
set -e
cd "$(dirname "$0")"
chmod +x ./mvnw 2>/dev/null || true
./mvnw -q clean javafx:run
