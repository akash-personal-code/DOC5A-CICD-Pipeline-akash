# NovaPay CI/CD Submission Evidence & Screenshot Guide

This guide explains how to upload the project to GitHub Free, run the CI/CD workflow, download evidence artifacts, and capture the screenshots required for final submission.

## 1. Create the GitHub repository

1. Open <https://github.com/new>.
2. Repository name: `DOC5A-CICD-Pipeline-YourName`.
3. Choose Private or Public based on the submission instruction.
4. Do not initialize with README, because the project already contains one.

## 2. Push the project

From the project folder:

```bash
git init
git branch -M main
git add .
git commit -m "feat: initialise NovaPay regulated CI/CD submission"
git remote add origin https://github.com/YOUR_USERNAME/DOC5A-CICD-Pipeline-YourName.git
git push -u origin main
```

Replace `YOUR_USERNAME` and `DOC5A-CICD-Pipeline-YourName` with your real values.

## 3. Run GitHub Actions

1. Open the GitHub repository.
2. Go to **Actions**.
3. Select **NovaPay Regulated CI/CD Evidence Pipeline**.
4. Click **Run workflow**.
5. Wait until the run is green/successful.

The workflow should show these stages:

- Stage 2 - Build, Unit Test, Coverage
- Stage 3 - SAST Quality Gate
- Stage 4A - Dependency Scan and SBOM Gate
- Stage 4B - Container Build and Image Scan
- Stage 5 - Integration, Smoke, and Contract Evidence
- Stage 6 - DAST Baseline Gate
- Stage 7 - Policy and Compliance Gates
- Stage 8 - Deployment Verification Decision Record

## 4. Screenshots to capture

Save screenshots inside `evidence/screenshots/` using these names:

| File name | What to capture |
|---|---|
| `01-repository-home.png` | GitHub repository home page showing repository name |
| `02-repository-structure.png` | Repository root showing `README.md`, `docs`, `pipeline`, `dashboards`, `runbooks`, `evidence` |
| `03-workflow-file.png` | `.github/workflows/novapay-ci-cd.yml` showing the 8-stage workflow |
| `04-actions-success-summary.png` | GitHub Actions run summary showing green success |
| `05-workflow-graph-success.png` | Full workflow graph with all stages green |
| `06-build-test-coverage.png` | Stage 2 logs showing Gradle build/test/Jacoco completed |
| `07-sast-quality-gate.png` | Stage 3 logs showing Semgrep/SAST gate completed |
| `08-dependency-sbom-gate.png` | Stage 4A logs showing Trivy filesystem scan and SBOM generation |
| `09-container-image-scan.png` | Stage 4B logs showing Docker build and Trivy image scan |
| `10-integration-smoke-contract.png` | Stage 5 logs showing Docker Compose, health, version, OpenAPI, and customer API test |
| `11-dast-baseline-gate.png` | Stage 6 logs showing OWASP ZAP baseline scan |
| `12-policy-compliance-gates.png` | Stage 7 logs showing Helm lint/template and OPA/Conftest validation |
| `13-deployment-verification.png` | Stage 8 logs showing deployment decision record generated |
| `14-artifacts-list.png` | Artifacts section showing all generated evidence artifacts |
| `15-trc-presentation-path.png` | GitHub file browser showing `evidence/trc-presentation.pdf` |
| `16-runbooks-folder.png` | GitHub file browser showing `runbooks/deployment-runbook.md` and `runbooks/incident-playbook.md` |
| `17-final-evidence-folder.png` | GitHub file browser showing populated `evidence/screenshots` and `evidence/test-results` |

## 5. Download artifacts and save test evidence

After the workflow is successful:

1. Open the successful workflow run.
2. Scroll to **Artifacts**.
3. Download all artifacts.
4. Extract them locally.
5. Copy useful outputs into `evidence/test-results/`.

Suggested evidence files:

```text
evidence/test-results/github-actions-run-summary.md
evidence/test-results/gradle-test-result.txt
evidence/test-results/jacoco-coverage-result.txt
evidence/test-results/sast-semgrep-result.txt
evidence/test-results/trivy-fs-scan-result.txt
evidence/test-results/trivy-image-scan-result.txt
evidence/test-results/sbom-generation-result.txt
evidence/test-results/integration-smoke-test-result.txt
evidence/test-results/zap-dast-result.txt
evidence/test-results/helm-validation-result.txt
evidence/test-results/opa-conftest-result.txt
evidence/test-results/deployment-decision-record.json
```

## 6. Commit screenshots and evidence

After placing screenshots and evidence files:

```bash
git add evidence/ README.md
git commit -m "docs: add final validation screenshots and evidence"
git push
```

## 7. Final submission checks

Before submitting, confirm:

- GitHub Actions final run is green.
- 8 stages are visible in the workflow graph.
- Artifacts are available and downloaded.
- `evidence/screenshots/` contains final screenshots.
- `evidence/test-results/` contains downloaded evidence outputs.
- `evidence/trc-presentation.pdf` exists.
- `runbooks/` contains both deployment and incident runbooks.
- `docs/01` through `docs/08` exist.
- `dashboards/` exists.
- `ERRATA.md` exists.
- README states that validation was performed using GitHub Actions and Docker-based CI evidence.

