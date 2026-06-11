param(
  [string]$ImageTag = "0.0.1",
  [switch]$SkipTests,
  [switch]$SkipTrivy
)

$ErrorActionPreference = "Stop"

function Write-Step($Message) {
  Write-Host "`n==== $Message ====" -ForegroundColor Cyan
}

New-Item -ItemType Directory -Force evidence/local-runs | Out-Null
New-Item -ItemType Directory -Force evidence/test-results | Out-Null
New-Item -ItemType Directory -Force reports | Out-Null

Write-Step "Stage 1 - Source control evidence"
git status --short 2>&1 | Out-File -Encoding utf8 evidence/local-runs/git-status.txt

Write-Step "Stage 2 - Build and tests using Docker Gradle JDK 21"
if (-not $SkipTests) {
  docker run --rm -v ${PWD}:/app -w /app gradle:8.10.2-jdk21 gradle clean test jacocoTestReport jacocoTestCoverageVerification | Tee-Object -FilePath evidence/test-results/gradle-test-output.txt
} else {
  "Skipped by user" | Out-File -Encoding utf8 evidence/test-results/gradle-test-output.txt
}

Write-Step "Stage 3/4 - Docker image build"
docker build -t novapay-lite:$ImageTag . | Tee-Object -FilePath evidence/local-runs/docker-build-output.txt

Write-Step "Stage 4 - Trivy image scan"
if (-not $SkipTrivy) {
  docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${PWD}/reports:/reports aquasec/trivy:0.57.1 image --severity CRITICAL,HIGH --ignore-unfixed --format table --output /reports/trivy-image-report.txt novapay-lite:$ImageTag
} else {
  "Skipped by user" | Out-File -Encoding utf8 reports/trivy-image-report.txt
}

Write-Step "Stage 5 - Start Docker Compose stack"
if (-not (Test-Path .env)) {
  Copy-Item .env.example .env
}
docker compose up -d --build
Start-Sleep -Seconds 30
docker compose ps | Out-File -Encoding utf8 evidence/local-runs/docker-compose-ps.txt

Write-Step "Stage 8 - Deployment verification smoke tests"
curl.exe http://localhost:8080/api/health -o evidence/local-runs/health-endpoint.txt
curl.exe http://localhost:8080/api/version -o evidence/local-runs/version-endpoint.json
curl.exe http://localhost:8080/actuator/prometheus -o evidence/local-runs/prometheus-metrics.txt
curl.exe http://localhost:8080/v3/api-docs -o evidence/test-results/openapi.json

Write-Step "Customer API and database evidence"
curl.exe -i -X POST http://localhost:8080/api/customers `
  -H "Content-Type: application/json" `
  -H "X-Correlation-ID: local-pipeline-customer-001" `
  --data-binary "@customer.json" `
  -o evidence/local-runs/customer-api-response.txt

docker compose exec -T postgres psql -U novapay -d novapay_lite -c "select * from customers limit 5;" |
  Out-File -Encoding utf8 evidence/local-runs/customer-db-row.txt

Write-Step "Policy/Helm render check"
if (Get-Command helm -ErrorAction SilentlyContinue) {
  helm lint pipeline/helm/novapay-lite | Tee-Object -FilePath evidence/test-results/helm-lint.txt
  helm template novapay-lite pipeline/helm/novapay-lite --set image.tag=$ImageTag | Out-File -Encoding utf8 evidence/test-results/rendered-k8s.yaml
} else {
  "Helm not installed locally. Run the GitHub Actions pipeline for Helm evidence." |
    Out-File -Encoding utf8 evidence/test-results/helm-lint.txt
}

Write-Step "DONE"
Write-Host "Evidence saved under evidence/local-runs, evidence/test-results, and reports."
