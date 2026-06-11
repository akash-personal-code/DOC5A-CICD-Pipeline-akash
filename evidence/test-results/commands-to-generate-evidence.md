# Commands to Generate Fresh Evidence Outputs

Run these commands from the repository root. For local Docker Compose, copy `.env.example` to `.env` first.

```bash
# One-time local setup
cp .env.example .env
chmod +x gradlew scripts/*.sh pipeline/scripts/*.sh pipeline/security/*.sh

# Repository structure
find . -maxdepth 4 -type f \
  -not -path './.git/*' \
  -not -path './.gradle/*' \
  -not -path './build/*' | sort > evidence/test-results/repository-file-list.txt

# Java tests and coverage gate
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification --no-daemon > evidence/test-results/gradle-test-output.txt 2>&1

# Docker build
docker build -t novapay-lite:local . > evidence/test-results/docker-build.txt 2>&1

# Docker Compose runtime
docker compose up -d --build
docker compose ps > evidence/test-results/docker-compose-ps.txt

# Health/version/OpenAPI/Prometheus endpoint evidence
curl -s http://localhost:8080/api/health > evidence/test-results/health-endpoint.txt
curl -s http://localhost:8080/api/version > evidence/test-results/version-endpoint.json
curl -s http://localhost:8080/v3/api-docs > evidence/test-results/openapi-runtime.json
curl -s http://localhost:8080/actuator/prometheus > evidence/test-results/prometheus-metrics.txt

# API smoke test
curl -i -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: evidence-customer-001" \
  --data-binary "@customer.json" \
  -o evidence/test-results/customer-api-response.txt

# Helm validation
helm lint pipeline/helm/novapay-lite > evidence/test-results/helm-lint.txt 2>&1
helm template novapay-lite pipeline/helm/novapay-lite \
  --set image.tag=local \
  > evidence/test-results/rendered-k8s.yaml

# OPA/Conftest policy validation
docker run --rm -v "$PWD":/project -w /project openpolicyagent/conftest:v0.51.0 \
  test evidence/test-results/rendered-k8s.yaml \
  --policy pipeline/policies/opa \
  --namespace main \
  > evidence/test-results/opa-policy-validation.txt 2>&1

# Trivy image scan and SBOM
trivy image --severity CRITICAL,HIGH --ignore-unfixed novapay-lite:local > evidence/test-results/trivy-image-critical-high.txt 2>&1
trivy image --format cyclonedx --output evidence/test-results/sbom-cyclonedx.json novapay-lite:local

# DAST baseline scan
./scripts/run-zap-baseline.sh http://localhost:8080 > evidence/test-results/zap-command-output.txt 2>&1
```

After running the commands, take screenshots listed in `AZURE_VALIDATION_AND_SCREENSHOT_GUIDE.md`.
