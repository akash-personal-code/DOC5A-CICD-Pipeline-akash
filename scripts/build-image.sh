#!/usr/bin/env bash
set -euo pipefail
IMAGE_TAG="${1:-0.0.1}"
echo "Building Docker image novapay-lite:${IMAGE_TAG}"
docker build -t "novapay-lite:${IMAGE_TAG}" .
