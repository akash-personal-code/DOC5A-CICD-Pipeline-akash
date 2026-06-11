#!/usr/bin/env bash
set -euo pipefail
cat <<'MSG'
To simulate a failing canary in this local demo, restart the app container with:
  SIMULATE_FAILURE=true docker compose up -d --build novapay-lite
Then verify failure with:
  curl -i http://localhost:8080/api/health
MSG
