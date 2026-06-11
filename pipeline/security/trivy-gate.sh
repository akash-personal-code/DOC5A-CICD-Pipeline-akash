#!/usr/bin/env bash
set -euo pipefail

IMAGE_NAME="${1:-novapay-lite:0.0.1}"
EVIDENCE_DIR="${EVIDENCE_DIR:-evidence/test-results}"
mkdir -p "${EVIDENCE_DIR}"

echo "Running Trivy filesystem scan..."
trivy fs --scanners vuln,secret,misconfig \
  --severity CRITICAL,HIGH \
  --exit-code 1 \
  --format table \
  --output "${EVIDENCE_DIR}/trivy-fs-critical-high.txt" \
  .

echo "Running Trivy image scan..."
trivy image \
  --severity CRITICAL,HIGH \
  --exit-code 1 \
  --format table \
  --output "${EVIDENCE_DIR}/trivy-image-critical-high.txt" \
  "${IMAGE_NAME}"

echo "Generating SBOM..."
trivy image \
  --format cyclonedx \
  --output "${EVIDENCE_DIR}/novapay-lite-sbom.cdx.json" \
  "${IMAGE_NAME}"

echo "Trivy gate passed with no Critical/High findings."
