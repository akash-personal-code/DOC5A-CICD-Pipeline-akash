# Azure / GitHub / Local Validation and Screenshot Guide

## What is enough for submission?

For this assessment, the safest evidence path is:

1. Push the corrected repository to GitHub.
2. Run `.github/workflows/novapay-ci-cd.yml` from GitHub Actions.
3. Run the local Docker Compose validation once from your laptop or an Azure VM.
4. Capture screenshots and save command outputs under `evidence/screenshots/` and `evidence/test-results/`.

A full AKS deployment is optional unless you explicitly claim live Azure deployment. The PDF asks for production-grade architecture and deployment design; it does not require paying for a full production cloud stack. If you do use Azure, keep it small and temporary.

## Prerequisites

Local or Azure VM tools:

- Git
- Docker Desktop or Docker Engine
- Java 21, optional if using Docker for Gradle
- Helm, optional for local Helm evidence
- Trivy, optional because GitHub Actions can run it
- A GitHub repository

## Step 1 - Prepare repository

```bash
cp .env.example .env
chmod +x gradlew scripts/*.sh pipeline/scripts/*.sh pipeline/security/*.sh
find . -maxdepth 4 -type f -not -path './.git/*' | sort > evidence/test-results/repository-file-list.txt
```

Screenshot: `01-repository-structure.png`

## Step 2 - Run tests and coverage locally

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification --no-daemon
```

Capture:

- terminal success output
- `build/reports/tests/test/index.html`
- `build/reports/jacoco/test/html/index.html`

Screenshots:

- `04-build-test-coverage-artifact.png`
- `05-jacoco-coverage-report.png`

## Step 3 - Run Docker Compose runtime

```bash
docker compose up -d --build
docker compose ps
curl -i http://localhost:8080/api/health
curl -s http://localhost:8080/api/version
curl -s http://localhost:8080/v3/api-docs
curl -s http://localhost:8080/actuator/prometheus | head
```

Screenshots:

- `08-docker-compose-running.png`
- `09-health-version-prometheus-endpoints.png`

## Step 4 - Run API smoke test

```bash
curl -i -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: manual-evidence-customer-001" \
  --data-binary "@customer.json"
```

Screenshot: `09b-api-smoke-test.png`

## Step 5 - Run Helm and OPA policy checks

```bash
helm lint pipeline/helm/novapay-lite
helm template novapay-lite pipeline/helm/novapay-lite --set image.tag=local > evidence/test-results/rendered-k8s.yaml

docker run --rm -v "$PWD":/project -w /project openpolicyagent/conftest:v0.51.0 \
  test evidence/test-results/rendered-k8s.yaml \
  --policy pipeline/policies/opa \
  --namespace main
```

Screenshots:

- `10-helm-lint-render-output.png`
- `11-opa-policy-validation.png`

## Step 6 - Run Trivy and SBOM

```bash
docker build -t novapay-lite:local .
trivy image --severity CRITICAL,HIGH --ignore-unfixed novapay-lite:local
trivy image --format cyclonedx --output evidence/test-results/sbom-cyclonedx.json novapay-lite:local
```

Screenshots:

- `06-trivy-scan-output.png`
- `07-sbom-artifact.png`

If Trivy fails with a new vulnerability, keep the screenshot, document it in `evidence/quality-security/security-exception-risk-acceptance.md`, and explain the remediation plan.

## Step 7 - Run DAST baseline

```bash
./scripts/run-zap-baseline.sh http://localhost:8080
```

Screenshot: `12-zap-dast-report.png`

## Step 8 - Run GitHub Actions evidence pipeline

1. Push the repository to GitHub.
2. Go to **Actions**.
3. Select **NovaPay Regulated CI/CD Evidence Pipeline**.
4. Click **Run workflow**.
5. Wait for all stages to complete.
6. Open each uploaded artifact.

Screenshots:

- `02-github-actions-workflow-file.png`
- `03-github-actions-successful-run.png`
- `04-build-test-coverage-artifact.png`
- `06-trivy-scan-output.png`
- `07-sbom-artifact.png`
- `11-opa-policy-validation.png`
- `12-zap-dast-report.png`

## Step 9 - Optional Azure screenshots

Use these only if you actually deploy or run the validation on Azure:

- Azure VM overview or Azure Cloud Shell running validation commands
- Azure Container Registry image pushed, if used
- AKS cluster overview, if used
- ArgoCD application sync status, if used
- Kubernetes pods/services in `novapay-staging`, if used

Screenshot: `18-argocd-or-aks-optional-deployment.png`

## Final screenshot checklist

| # | Screenshot filename | Must-have? | Purpose |
|---|---|---:|---|
| 1 | `01-repository-structure.png` | Yes | Shows required folders and deliverables |
| 2 | `02-github-actions-workflow-file.png` | Yes | Shows 8-stage workflow configuration |
| 3 | `03-github-actions-successful-run.png` | Yes | Shows end-to-end pipeline execution |
| 4 | `04-build-test-coverage-artifact.png` | Yes | Shows tests and coverage evidence |
| 5 | `05-jacoco-coverage-report.png` | Yes | Shows 80%/70% coverage target evidence |
| 6 | `06-trivy-scan-output.png` | Yes | Shows dependency/container scan evidence |
| 7 | `07-sbom-artifact.png` | Yes | Shows SBOM generated |
| 8 | `08-docker-compose-running.png` | Yes | Shows runtime stack up |
| 9 | `09-health-version-prometheus-endpoints.png` | Yes | Shows health/version/metrics endpoints |
| 10 | `10-helm-lint-render-output.png` | Yes | Shows Helm validation |
| 11 | `11-opa-policy-validation.png` | Yes | Shows compliance policy gate evidence |
| 12 | `12-zap-dast-report.png` | Yes | Shows DAST evidence |
| 13 | `13-grafana-dashboard-json-or-import.png` | Recommended | Shows observability dashboard evidence |
| 14 | `14-architecture-diagram-rendered.png` | Yes | Shows required diagrams render |
| 15 | `15-trc-presentation-file.png` | Yes | Shows `evidence/trc-presentation.pdf` |
| 16 | `16-evidence-folder-final.png` | Yes | Shows screenshots/test-results populated |
| 17 | `17-git-commit-history.png` | Recommended | Shows commit history and professional workflow |
| 18 | `18-argocd-or-aks-optional-deployment.png` | Optional | Only if you deploy on Azure/AKS |
