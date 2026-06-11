# NovaPay Digital Bank Compliance Gates

## 1. Purpose

This document defines the automated compliance gate architecture for NovaPay Digital Bank’s regulated CI/CD pipeline.

NovaPay is a fictional RBI-licensed digital bank migrating from manual SSH-based deployments to a controlled DevSecOps pipeline. The compliance gates in this document ensure that every production change is tested, scanned, approved, traceable, and deployable only when banking-grade risk controls are satisfied.

The objective is not merely to run tools. The objective is to convert security, regulatory, operational, and change-management expectations into automated pass/fail controls inside the CI/CD workflow.

## 2. Compliance Gate Objectives

The compliance gate system is designed to achieve the following objectives:

* Prevent vulnerable or unapproved code from reaching production.
* Enforce segregation of duties between developers, approvers, and production deployers.
* Generate audit-ready evidence for every release.
* Block unsafe Kubernetes and infrastructure configurations.
* Enforce software supply chain integrity through SBOM, image scanning, and image signing.
* Verify runtime security through DAST.
* Ensure production deployments are traceable to approved change records.
* Provide a formal exception process with expiry and risk ownership.

## 3. Compliance Gate Summary

| Gate ID | Gate Name                                          | Primary Tooling                                  | Pipeline Location            | Blocking?   |
| ------- | -------------------------------------------------- | ------------------------------------------------ | ---------------------------- | ----------- |
| CG-01   | Source Control and Change Approval Gate            | GitHub Enterprise, branch protection, CODEOWNERS | Pull request                 | Yes         |
| CG-02   | SAST and Code Quality Gate                         | SonarQube/SonarCloud                             | CI pipeline                  | Yes         |
| CG-03   | Dependency and SBOM Gate                           | Trivy, Syft, Grype                               | CI pipeline                  | Yes         |
| CG-04   | Container Image Security Gate                      | Trivy, Cosign                                    | Image build stage            | Yes         |
| CG-05   | DAST Gate                                          | OWASP ZAP                                        | Staging                      | Yes         |
| CG-06   | Kubernetes Policy Gate                             | OPA/Kyverno                                      | Pre-deployment and admission | Yes         |
| CG-07   | Infrastructure Compliance Gate                     | Checkov, Terraform validate/plan                 | IaC pipeline                 | Yes         |
| CG-08   | Segregation of Duties and Production Approval Gate | GitHub Environments, CAB approval, RBAC          | Pre-prod to production       | Yes         |
| CG-09   | Deployment Verification and Evidence Gate          | Prometheus, smoke tests, ArgoCD, Helm            | Post-deployment              | Yes         |
| CG-10   | Vulnerability Exception and Risk Acceptance Gate   | Ticketing workflow, CISO approval                | Exception path               | Conditional |

## 4. Regulatory Mapping Overview

| Regulatory / Governance Area | NovaPay Pipeline Control                                                       |
| ---------------------------- | ------------------------------------------------------------------------------ |
| Change management            | Pull request approval, change ticket linkage, CAB/dual approval, rollback plan |
| Segregation of duties        | Developers cannot self-approve production deployment                           |
| Vulnerability management     | SAST, DAST, dependency scan, container scan                                    |
| Audit trail                  | Immutable evidence pack for every pipeline run                                 |
| Data protection              | Secret scanning, encryption policy, TLS/mTLS checks                            |
| Third-party/software risk    | SBOM generation, licence checks, dependency scanning                           |
| Incident readiness           | Automated rollback triggers, runbook, incident playbook                        |
| Production stability         | Blue-green/canary verification, smoke tests, SLO checks                        |

## 5. Gate CG-01: Source Control and Change Approval Gate

### Purpose

This gate ensures that all changes entering the pipeline are reviewed, traceable, and linked to an approved change process.

### Tooling

* GitHub Enterprise
* Branch protection rules
* CODEOWNERS
* Signed commits
* Pull request approval workflow
* Change ticket integration

### Required Controls

* Direct push to `main` is blocked.
* Pull request is mandatory for all changes.
* Minimum one peer review is required.
* Security-sensitive files require security owner approval.
* Infrastructure and Kubernetes files require platform/SRE approval.
* Database migration files require DBA approval.
* Production deployment requires Release Manager and SRE Lead approval.
* Every production change must reference a change ticket.

### Numeric Thresholds

| Control                     | Threshold                                 |
| --------------------------- | ----------------------------------------- |
| Pull request reviews        | Minimum 1 peer review                     |
| Sensitive file reviews      | Minimum 1 domain owner review             |
| Production approval         | 2 approvals: Release Manager and SRE Lead |
| Direct pushes to main       | 0 allowed                                 |
| Unsigned commits            | 0 allowed for protected branches          |
| Unlinked production changes | 0 allowed                                 |

### Failure Behaviour

If the gate fails:

* Pull request cannot merge.
* CI/CD pipeline cannot start production promotion.
* Change is returned to author for correction.
* Missing approval is highlighted in the pull request check.

### Remediation

* Add required reviewer approval.
* Link the change ticket.
* Re-sign commits if signature is missing.
* Request CODEOWNERS review for protected paths.
* Re-run pipeline after approval.

### Exception Process

Emergency hotfixes may use expedited approval, but they cannot bypass this gate entirely.

Required exception approval:

* Release Manager
* SRE Lead
* CISO for security-sensitive changes

Maximum exception validity: 24 hours.

## 6. Gate CG-02: SAST and Code Quality Gate

### Purpose

This gate prevents insecure or low-quality source code from progressing through the pipeline.

### Tooling

* SonarQube or SonarCloud
* Optional Semgrep rules for additional banking/security patterns

### Scope

* Java/Spring Boot source code
* Controller, service, repository, and configuration classes
* Authentication/authorization logic
* PII handling logic
* SQL/database access
* Logging and exception handling
* Cryptography usage

### Numeric Thresholds

| Finding Type                     | Threshold                            |
| -------------------------------- | ------------------------------------ |
| Critical vulnerabilities         | 0 allowed                            |
| High vulnerabilities             | Maximum 2 allowed, must have tickets |
| Security hotspots                | 100% reviewed before production      |
| New blocker issues               | 0 allowed                            |
| New code line coverage           | Minimum 80% target                   |
| New code branch coverage         | Minimum 70% target                   |
| Technical debt ratio on new code | Maximum 5%                           |
| Duplicated code on new code      | Maximum 3%                           |

### Failure Behaviour

If the gate fails:

* Pipeline stops before artefact promotion.
* Developer receives report link.
* Security ticket is created automatically for Critical/High issues.
* Pull request cannot merge until the quality gate passes or exception is approved.

### Remediation

* Fix vulnerable code.
* Add missing validation.
* Remove unsafe logging of PII.
* Replace weak cryptographic functions.
* Add unit tests for untested critical paths.
* Mark false positives only after security review.

### Exception Process

Allowed only for non-exploitable findings or false positives.

Required approval:

* Tech Lead for quality-only exception.
* CISO for security exception.
* Compliance owner if production deployment is affected.

Maximum exception validity: 24 hours for Critical, 72 hours for High.

## 7. Gate CG-03: Dependency and SBOM Gate

### Purpose

This gate controls third-party dependency risk and generates a formal software inventory.

### Tooling

* Trivy
* Syft
* Grype
* CycloneDX or SPDX SBOM format

### Scope

* Java dependencies
* Gradle dependency tree
* Container image packages
* Transitive dependencies
* Open-source licence obligations

### Numeric Thresholds

| Finding Type              | Threshold                           |
| ------------------------- | ----------------------------------- |
| Critical CVEs             | 0 allowed for production            |
| High CVEs with CVSS > 8.0 | 0 allowed unless exception approved |
| Medium CVEs               | Allowed with remediation ticket     |
| Unknown severity CVEs     | Manual review required              |
| SBOM generation           | Mandatory for every release         |
| Prohibited licences       | 0 allowed without legal approval    |

### Blocked Licence Categories

* GPL
* AGPL
* SSPL
* Unknown licences in critical runtime dependencies

Permitted with review:

* MIT
* Apache-2.0
* BSD
* EPL, subject to legal review

### Failure Behaviour

If the gate fails:

* Pipeline stops before image signing.
* SBOM is generated but marked non-compliant.
* Vulnerability remediation ticket is created.
* Production promotion is blocked.

### Remediation

* Upgrade vulnerable dependency.
* Replace dependency with approved alternative.
* Use a patched base image.
* Remove unused dependency.
* Obtain legal review for licence issue.
* Generate updated SBOM after remediation.

### Local Evidence Note

The local NovaPay Lite evidence includes a Trivy image scan report. If Critical or High vulnerabilities are present, this is treated as valid vulnerability evidence, not as approval evidence. In a production NovaPay pipeline, such findings would block promotion until remediated or formally accepted.

### Exception Process

Exception requires:

* CISO approval for security vulnerability.
* Legal approval for licence issue.
* Business owner approval if deployment risk is accepted.
* Documented compensating control.
* Expiry date and remediation SLA.

Maximum exception validity:

* Critical: 24 hours.
* High: 72 hours.
* Medium: 30 days.

## 8. Gate CG-04: Container Image Security and Signing Gate

### Purpose

This gate ensures container images are secure, traceable, and tamper-resistant before deployment.

### Tooling

* Docker BuildKit
* Trivy image scan
* Cosign image signing
* Container registry with immutable tags
* Kyverno/OPA image verification policy

### Required Controls

* Image must use approved base image.
* Image must not run as root in production.
* Image must not use `latest` tag in production.
* Image must be tagged with SemVer and Git SHA.
* Image digest must be recorded.
* Image must be signed before deployment.
* Admission controller must reject unsigned images.

### Numeric Thresholds

| Control                   | Threshold                           |
| ------------------------- | ----------------------------------- |
| Unsigned images           | 0 allowed in production             |
| `latest` tag              | 0 allowed in production             |
| Critical image CVEs       | 0 allowed                           |
| High CVEs with CVSS > 8.0 | 0 allowed unless exception approved |
| Unknown base image        | 0 allowed                           |
| Image provenance record   | Required                            |

### Failure Behaviour

If the gate fails:

* Image is not promoted.
* Registry promotion is blocked.
* Kubernetes admission policy rejects deployment.
* Evidence pack marks image as non-compliant.

### Remediation

* Rebuild image using approved base.
* Patch vulnerable packages.
* Re-sign image.
* Use immutable tag and digest.
* Update deployment manifest with approved digest.

### Exception Process

Unsigned images cannot be deployed to production. Vulnerability exceptions follow the vulnerability exception workflow.

## 9. Gate CG-05: DAST Gate

### Purpose

This gate validates the running application for dynamic web/API security issues before production release.

### Tooling

* OWASP ZAP baseline scan
* OWASP ZAP API scan using OpenAPI spec
* Authenticated scan for protected endpoints where test credentials are available

### Scope

* Public HTTP/API endpoints
* OpenAPI-described APIs
* Authentication and session-related endpoints
* Common OWASP Top 10 risk areas
* Security headers
* TLS configuration
* Input validation behaviour

### Numeric Thresholds

| Finding Type                      | Threshold                               |
| --------------------------------- | --------------------------------------- |
| Critical DAST findings            | 0 allowed                               |
| High DAST findings                | 0 allowed                               |
| Medium DAST findings              | Allowed with remediation ticket and SLA |
| OWASP Top 10 exploitable issue    | 0 allowed                               |
| Missing critical security headers | Block if public-facing                  |
| Scan completion                   | Required for staging to pre-prod        |

### Failure Behaviour

If the gate fails:

* Staging deployment is not promoted to pre-prod.
* Security ticket is created.
* Release Manager is notified.
* Evidence pack records failed DAST result.

### Remediation

* Fix vulnerable endpoint.
* Add input validation.
* Harden security headers.
* Fix authentication/session weakness.
* Re-run DAST after fix.
* Document false positives.

### Exception Process

False positives require:

* Security engineer validation.
* CISO approval for production-impacting issue.
* Time-bound risk acceptance.
* Compensating controls such as WAF rule or feature flag disablement.

## 10. Gate CG-06: Kubernetes Policy Gate

### Purpose

This gate prevents unsafe Kubernetes workloads from being deployed.

### Tooling

* OPA Gatekeeper
* Kyverno
* Conftest
* Helm template validation

### Required Kubernetes Policies

* No privileged containers.
* No hostPath volumes unless explicitly approved.
* Run as non-root.
* Resource requests and limits required.
* Read-only root filesystem for production workloads where feasible.
* No `latest` image tag.
* Required labels must exist.
* Secrets must not be hardcoded in manifests.
* Production ingress must require TLS.
* Image signature verification must pass.
* Namespace must match approved environment.

### Required Labels

Every production workload must include:

```yaml
owner: platform-team
environment: production
compliance-scope: pci-rbi
data-classification: confidential
application: novapay
```

### Numeric Thresholds

| Control                 | Threshold                |
| ----------------------- | ------------------------ |
| Policy violations       | 0 allowed for production |
| Missing resource limits | 0 allowed                |
| Privileged containers   | 0 allowed                |
| Latest image tag        | 0 allowed                |
| Missing required labels | 0 allowed                |
| Hardcoded secret values | 0 allowed                |

### Failure Behaviour

If the gate fails:

* Helm chart cannot be promoted.
* ArgoCD sync is blocked or fails.
* Admission controller rejects resource.
* Policy violation report is attached to evidence pack.

### Remediation

* Add missing labels.
* Add CPU/memory requests and limits.
* Remove privileged mode.
* Replace `latest` with immutable tag/digest.
* Move secrets to approved secret manager.
* Add TLS configuration.

### Example Kyverno Policy Intent

```yaml
apiVersion: kyverno.io/v1
kind: ClusterPolicy
metadata:
  name: disallow-latest-image-tag
spec:
  validationFailureAction: Enforce
  rules:
    - name: require-non-latest-image
      match:
        any:
          - resources:
              kinds:
                - Pod
      validate:
        message: "Production images must not use the latest tag."
        pattern:
          spec:
            containers:
              - image: "!*:latest"
```

### Exception Process

Production policy exceptions require dual approval:

* SRE Lead
* Head of Compliance or delegated risk owner

Maximum validity: 7 days.

## 11. Gate CG-07: Infrastructure Compliance Gate

### Purpose

This gate validates cloud infrastructure and Terraform code before provisioning or modification.

### Tooling

* Terraform validate
* Terraform plan
* Checkov
* tfsec or equivalent IaC scanner
* Infracost, optional for FinOps review

### Scope

* Kubernetes cluster configuration
* Network policies
* Security groups/firewall rules
* Database resources
* Secrets manager resources
* IAM/RBAC policies
* Logging and monitoring resources
* Object storage for audit evidence

### Numeric Thresholds

| Control                            | Threshold                |
| ---------------------------------- | ------------------------ |
| Terraform validation errors        | 0 allowed                |
| Checkov Critical findings          | 0 allowed                |
| Public database exposure           | 0 allowed                |
| Public unrestricted inbound access | 0 allowed                |
| Unencrypted storage                | 0 allowed                |
| Missing audit logging              | 0 allowed                |
| Excessive IAM wildcard permissions | 0 allowed for production |

### Failure Behaviour

If the gate fails:

* Terraform apply is blocked.
* Pull request cannot merge.
* Platform team receives remediation report.
* Evidence pack records failed IaC scan.

### Remediation

* Restrict network exposure.
* Enable encryption.
* Enable audit logging.
* Reduce IAM permissions.
* Add required tags and ownership labels.
* Re-run Checkov and Terraform plan.

### Exception Process

Infrastructure exceptions require:

* Cloud Platform Lead approval.
* CISO approval for security-impacting exception.
* Compliance owner approval for audit/logging exception.
* Expiry date and compensating control.

## 12. Gate CG-08: Segregation of Duties and Production Approval Gate

### Purpose

This gate ensures that the same individual cannot author, approve, and deploy a production change.

### Tooling

* GitHub Environments
* Required reviewers
* RBAC
* Change ticket workflow
* ArgoCD role separation

### Required Roles

| Role              | Responsibility                       |
| ----------------- | ------------------------------------ |
| Developer         | Creates code change                  |
| Peer Reviewer     | Reviews code correctness             |
| Tech Lead         | Approves technical design            |
| Security Reviewer | Approves security exceptions         |
| DBA               | Approves database migration          |
| Release Manager   | Approves production release          |
| SRE Lead          | Confirms operational readiness       |
| Compliance Owner  | Reviews regulatory-impacting changes |

### Segregation Rules

* Developer cannot self-approve pull request.
* Developer cannot approve production deployment.
* Release Manager and SRE Lead must be separate people.
* DBA approval is mandatory for migration scripts.
* CISO approval is mandatory for critical vulnerability exception.
* Compliance approval is mandatory for regulatory-impacting exception.

### Numeric Thresholds

| Control                              | Threshold                                 |
| ------------------------------------ | ----------------------------------------- |
| Production approvals                 | Minimum 2                                 |
| Self-approval                        | 0 allowed                                 |
| DBA approval for DB migration        | Required                                  |
| CISO approval for Critical exception | Required                                  |
| Change ticket linkage                | Required                                  |
| Approval expiry                      | Maximum 24 hours before deployment window |

### Failure Behaviour

If the gate fails:

* Production environment remains locked.
* ArgoCD production sync is blocked.
* Release Manager receives missing approval list.
* Evidence pack records incomplete approval state.

### Remediation

* Obtain missing approval.
* Assign independent approver.
* Update change ticket.
* Re-submit production promotion request.

### Exception Process

Segregation of duties cannot be bypassed for normal production releases. Emergency changes require retrospective review within 24 hours.

## 13. Gate CG-09: Deployment Verification and Evidence Gate

### Purpose

This gate validates that deployment succeeded safely and generates final release evidence.

### Tooling

* Helm
* ArgoCD
* Prometheus
* Grafana
* Smoke test scripts
* Synthetic transactions
* Kubernetes rollout status
* Log aggregation

### Verification Checks

* Application health endpoint returns success.
* Version endpoint returns expected version.
* Prometheus metrics endpoint is available.
* Kubernetes pods are ready.
* No CrashLoopBackOff.
* All pods run expected image digest.
* Database migration version is correct.
* Synthetic customer/payment transaction succeeds.
* Error rate remains below threshold.
* p99 latency remains within baseline.

### Numeric Thresholds

| Metric                        | Threshold                                 |
| ----------------------------- | ----------------------------------------- |
| HTTP 5xx rate                 | Less than 5% immediate rollback threshold |
| Health check failures         | 3 consecutive failures trigger rollback   |
| p99 latency                   | Must not exceed 2x baseline for 5 minutes |
| CPU saturation                | Less than 90% sustained                   |
| Memory saturation             | Less than 85% sustained                   |
| Synthetic transaction success | 100% for critical smoke tests             |
| Pod readiness                 | 100% target for release version           |

### Failure Behaviour

If this gate fails:

* Canary is paused or rolled back.
* Blue-green traffic is routed back to previous stable environment.
* Incident record is opened for severe failures.
* Evidence pack records failed verification and rollback action.

### Remediation

* Roll back to previous stable release.
* Freeze deployment.
* Review metrics and logs.
* Identify faulty change.
* Create incident/postmortem if customer impact occurred.
* Reattempt deployment only after fix and approval.

## 14. Gate CG-10: Vulnerability Exception and Risk Acceptance Gate

### Purpose

This gate governs rare cases where a release must proceed despite a known finding.

### Exception Principles

* Exceptions must be rare.
* Exceptions must be time-bound.
* Exceptions must have named risk owner.
* Exceptions must include compensating control.
* Exceptions must not bypass audit logging.
* Critical exceptions require executive visibility.

### Required Exception Fields

```json
{
  "exception_id": "EXC-2026-0001",
  "pipeline_run_id": "run-123456",
  "commit_sha": "abc123",
  "artifact_digest": "sha256:...",
  "finding_source": "trivy",
  "finding_id": "CVE-XXXX-YYYY",
  "severity": "HIGH",
  "cvss_score": 8.2,
  "business_reason": "Urgent regulatory deadline",
  "compensating_control": "WAF rule and feature flag disabled",
  "risk_owner": "CISO",
  "approved_by": ["CISO", "Release Manager"],
  "approval_timestamp": "2026-06-10T10:00:00Z",
  "expiry_timestamp": "2026-06-13T10:00:00Z",
  "remediation_ticket": "SEC-1234",
  "status": "approved"
}
```

### Maximum Validity

| Severity | Maximum Exception Period      |
| -------- | ----------------------------- |
| Critical | 24 hours                      |
| High     | 72 hours                      |
| Medium   | 30 days                       |
| Low      | Next normal maintenance cycle |

### Failure Behaviour

If exception metadata is incomplete:

* Production promotion is blocked.
* Compliance owner is notified.
* Exception is marked invalid.

## 15. Audit Trail Format

Every compliance gate emits a structured audit event.

### Audit Event Schema

```json
{
  "event_type": "compliance_gate_result",
  "timestamp": "2026-06-10T10:00:00Z",
  "pipeline_run_id": "run-123456",
  "environment": "staging",
  "application": "novapay-lite",
  "gate_id": "CG-03",
  "gate_name": "Dependency and SBOM Gate",
  "tool": "trivy",
  "tool_version": "0.71.0",
  "commit_sha": "abc123",
  "artifact_version": "0.0.1",
  "image_digest": "sha256:...",
  "status": "failed",
  "threshold": "0 Critical CVEs",
  "actual_result": "4 Critical CVEs",
  "regulatory_mapping": ["RBI vulnerability management", "PCI-DSS 6.3"],
  "evidence_uri": "s3://novapay-audit-evidence/run-123456/trivy-report.txt",
  "exception_id": null,
  "remediation_ticket": "SEC-1234",
  "approved_by": [],
  "retention_class": "banking-audit"
}
```

## 16. Evidence Pack Contents

Every production release produces an evidence pack containing:

* Pull request approval record.
* Commit SHA and signed commit status.
* Build log.
* Unit test and coverage report.
* SAST report.
* Dependency scan report.
* SBOM.
* Licence scan report.
* Container scan report.
* Image digest and signature.
* DAST report.
* OPA/Kyverno policy result.
* Terraform/Checkov result if infrastructure changed.
* Approval record.
* Deployment log.
* Smoke test result.
* Prometheus metrics snapshot.
* Rollback result, if triggered.
* Change ticket reference.
* Exception record, if any.

## 17. Promotion Behaviour by Environment

| Environment    | Gate Strictness                                                                    |
| -------------- | ---------------------------------------------------------------------------------- |
| Development    | Build, unit test, SAST warning mode, dependency scan warning mode                  |
| Staging        | Build, unit test, SAST blocking, dependency scan blocking, integration tests, DAST |
| Pre-Production | All staging gates plus compliance approval, migration validation, UAT sign-off     |
| Production     | All gates enforced, dual approval, deployment verification, rollback monitoring    |

## 18. Local Evidence Interpretation

The local NovaPay Lite evidence demonstrates that the sample application can be:

* Packaged as a Docker image.
* Scanned with Trivy.
* Started through Docker Compose.
* Verified through health, version, Prometheus, and OpenAPI endpoints.
* Tested through a customer API call.
* Verified through a PostgreSQL database query.
* Rendered and linted through Helm.

This evidence is not a substitute for a full production banking pipeline. It is used as proof that the assessment target can support CI/CD pipeline activities.

If the local Trivy report contains Critical or High findings, the correct compliance interpretation is:

> The scan executed successfully and produced evidence. In a production NovaPay pipeline, this result would fail the dependency/container security gate and block promotion until remediation or formal exception approval.

## 19. Compliance Gate Decision Matrix

| Scenario                          | Decision              |
| --------------------------------- | --------------------- |
| Build fails                       | Block                 |
| Unit tests fail                   | Block                 |
| SAST Critical finding             | Block                 |
| More than 2 SAST High findings    | Block                 |
| Critical dependency CVE           | Block                 |
| SBOM missing                      | Block                 |
| Image unsigned                    | Block                 |
| Image uses `latest` in production | Block                 |
| DAST Critical or High finding     | Block                 |
| Kubernetes policy violation       | Block                 |
| Missing production approval       | Block                 |
| Failed smoke test                 | Rollback              |
| Canary SLO violation              | Auto-rollback         |
| Approved time-bound exception     | Allow with monitoring |
| Expired exception                 | Block                 |

## 20. Conclusion

NovaPay’s compliance gate architecture converts banking risk controls into automated CI/CD decisions. The pipeline does not rely on manual post-release review. Instead, security, compliance, operational readiness, and audit evidence are embedded directly into the path to production.

The result is a delivery model that supports faster release cycles while preserving regulated banking controls: every change is reviewed, tested, scanned, approved, deployed safely, monitored continuously, and traceable through an immutable evidence record.
