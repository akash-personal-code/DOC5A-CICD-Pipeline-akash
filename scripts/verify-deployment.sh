#!/usr/bin/env bash
set -euo pipefail
URL="${1:-http://localhost:8080}"
echo "Verifying deployment at ${URL}"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${URL}/api/health")
if [[ "${HTTP_STATUS}" == "200" ]]; then
  echo "Deployment is healthy."
  curl -fsS "${URL}/api/version"
  exit 0
fi
echo "Deployment verification failed. Status code: ${HTTP_STATUS}"
exit 1
