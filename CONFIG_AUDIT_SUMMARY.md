# NovaPay Lite Configuration Audit Summary

## Audit conclusion

The submitted configuration is now close enough to validate through GitHub Actions, local Docker Compose, or an Azure VM after applying this fix pack. A full Azure AKS deployment is optional and should only be attempted if you are comfortable managing trial/free-credit usage.

## Main issues fixed

| Area | Previous issue | Fix applied |
|---|---|---|
| Duplicate CI workflow | Weak placeholder `.github/workflows/ci.yml` still ran on pushes | Removed from fixed package; retained one regulated evidence pipeline |
| Workflow Helm path | Used `helm/novapay-lite`, but chart is under `pipeline/helm/novapay-lite` | Corrected all workflow and evidence commands |
| Workflow service logs | Used `docker compose logs app`, but service name is `novapay-lite` | Corrected to `docker compose logs novapay-lite` |
| Dockerfile | UTF-8 BOM risk and root runtime user | Rewritten without BOM and with non-root runtime user |
| Gradle wrapper | May lose executable permission when zipped | Fixed executable bit in corrected ZIP; workflow also runs `chmod +x` |
| Build dependencies | Older Spring Boot/springdoc versions created security scan risk | Updated Spring Boot and springdoc versions |
| JaCoCo branch coverage | Lombok-generated code made branch gate unreliable | Added `lombok.config` and extra unit tests |
| Application secrets | Default DB password exposed in app/Helm/Compose | Removed plaintext production secret defaults; uses `.env` locally and Kubernetes Secret refs in Helm |
| Helm security | Missing non-root container security context and compliance labels | Added pod/container security context and required compliance labels |
| OPA policy | Admission-request policy did not validate rendered Helm YAML with Conftest | Replaced with Conftest policy for rendered Kubernetes manifests |
| Evidence commands | Used incorrect `/version` endpoint and wrong Helm path | Corrected to `/api/version` and `pipeline/helm/novapay-lite` |
| Azure claim | Terraform was a placeholder without safe guidance | Added safety-first Azure validation notes |

## Items still requiring your local/GitHub/Azure execution

1. Run the GitHub Actions workflow after pushing to GitHub.
2. Run Docker Compose locally or on an Azure VM.
3. Capture screenshots listed in `AZURE_VALIDATION_AND_SCREENSHOT_GUIDE.md`.
4. If Trivy finds any current High/Critical vulnerability, document remediation or risk acceptance.
5. Replace `repoURL` in `pipeline/argocd/novapay-lite-application.yaml` before using ArgoCD.
