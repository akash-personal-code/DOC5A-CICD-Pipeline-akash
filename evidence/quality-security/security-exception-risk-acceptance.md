# Security Exception and Risk Acceptance Record

## Exception ID

`SEC-EX-2026-001`

## Title

Time-bound prototype exception for Trivy Critical and High findings in NovaPay Lite local image evidence.

## Scope

This exception applies only to the local demonstration image and local evidence artifacts for the NovaPay Lite prototype.

It does not apply to:

* Production.
* Pre-production.
* Customer-facing environments.
* Real banking data.
* Any internet-exposed deployment.

## Evidence Source

```text
reports/trivy-image-report.txt
```

## Finding Summary

| Target | Finding Summary | Gate Status |
|---|---:|---|
| Alpine/base image packages | 1 Medium | Warn/remediate |
| Java dependencies inside `app.jar` | 4 Critical, 25 High, 19 Medium, 11 Low | Block |

## Regulatory Relevance

The finding is relevant to vulnerability management, secure change control, audit evidence, and production deployment controls. A production release must not proceed while Critical or High findings remain unresolved unless a formally approved and time-bound exception exists.

## Risk Statement

If this image were deployed to production without remediation, NovaPay could expose regulated banking services to known vulnerable dependency paths. This would weaken secure software delivery controls, increase incident likelihood, and create audit non-conformance risk.

## Gate Decision

| Environment | Decision |
|---|---|
| Local demo | Allowed with documented exception |
| Development | Allowed only in isolated sandbox |
| Staging | Block until remediation or CISO-approved exception |
| Pre-production | Block |
| Production | Block |

## Compensating Controls

* Image is local prototype evidence only.
* No production secrets are included.
* No production data is used.
* Production pipeline design blocks Critical and High findings.
* SBOM and Trivy report are retained as evidence.
* Findings are visible in the final evidence pack.
* Remediation ticket is mandatory before promotion.

## Required Remediation

| Action | Owner | Evidence |
|---|---|---|
| Upgrade Spring Boot and managed dependency BOM to latest approved patch | App Owner | Updated `build.gradle` and dependency tree |
| Upgrade vulnerable direct dependencies | App Owner | Dependency diff |
| Rebuild image from patched base image | Platform Lead | Docker build log |
| Re-run Trivy for filesystem and image | Security Lead | Updated Trivy report |
| Re-generate SBOM | Security Lead | SBOM file |
| Re-run CI gate with `--exit-code 1` for Critical/High | Release Manager | CI log |

## Exception Expiry

This exception expires at the earliest of:

* 72 hours after approval.
* Before staging-to-pre-prod promotion.
* Before final production release readiness review.
* When a patched image is available.

## Approval Placeholders

| Role | Name | Decision | Date |
|---|---|---|---|
| Security Lead / CISO delegate | `<name>` | `<approved/rejected>` | `<date>` |
| Release Manager | `<name>` | `<approved/rejected>` | `<date>` |
| Application Owner | `<name>` | `<accepted/remediation committed>` | `<date>` |

## Audit JSON Record

```json
{
  "exception_id": "SEC-EX-2026-001",
  "type": "security_vulnerability_exception",
  "scope": "local_prototype_only",
  "environment_allowed": ["local_demo"],
  "environment_blocked": ["staging", "pre-prod", "production"],
  "evidence_file": "reports/trivy-image-report.txt",
  "critical_findings": 4,
  "high_findings": 25,
  "gate_decision": "block_for_production",
  "compensating_controls": [
    "no_production_data",
    "no_production_secrets",
    "production_gate_blocks_critical_high",
    "sbom_and_scan_evidence_retained"
  ],
  "expiry_policy": "72_hours_or_before_preprod_promotion",
  "approval_required_from": ["Security Lead", "Release Manager"],
  "status": "pending_remediation"
}
```
