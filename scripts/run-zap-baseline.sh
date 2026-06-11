#!/usr/bin/env bash
set -euo pipefail
TARGET_URL="${1:-http://localhost:8080}"
mkdir -p reports
echo "Running OWASP ZAP baseline scan against ${TARGET_URL}"
docker run --rm --network host -v "$PWD/reports:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
  -t "${TARGET_URL}" \
  -J zap-report.json \
  -r zap-report.html \
  -I
