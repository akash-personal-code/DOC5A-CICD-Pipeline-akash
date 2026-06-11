# Compliance Exception Register

## Purpose

This register records time-bound exceptions for pipeline quality, security, compliance, and deployment gates. Exceptions are controlled deviations, not permanent bypasses.

NovaPay’s production pipeline must block unresolved Critical/High vulnerabilities, failed coverage gates, unsigned images, failed policy checks, failed DAST/SAST thresholds, and incomplete audit evidence.

## Exception Register

| Exception ID | Gate | Finding | Scope | Production Decision | Owner | Expiry | Status |
|---|---|---|---|---|---|---|---|
| QA-EX-2026-001 | Build / Unit Test / Coverage | Branch coverage below 70% | Local prototype only | Block | QA Lead | Before pre-prod | Pending remediation |
| SEC-EX-2026-001 | Dependency and Container Scanning | Trivy Critical/High dependency findings | Local prototype only | Block | Security Lead | 72h or before pre-prod | Pending remediation |

## Mandatory Exception Fields

Each exception must include:

* Exception ID.
* Pipeline gate name.
* Evidence file path.
* Environment scope.
* Business justification.
* Risk statement.
* Compensating controls.
* Approval roles.
* Expiry date.
* Remediation ticket.
* Final closure evidence.

## Exception Approval Matrix

| Gate Type | Required Approval | Maximum Duration |
|---|---|---:|
| Unit/coverage gate | QA Lead + Tech Lead | Until pre-prod promotion |
| SAST Critical | CISO + TRC approval | 24 hours |
| DAST Critical/High | CISO + TRC approval | 24 hours |
| Dependency Critical/High | Security Lead + Release Manager | 72 hours |
| Licence violation | Legal + CISO | Case-by-case |
| OPA/Kyverno policy failure | SRE Lead + CISO | One deployment only |
| Segregation of duties violation | Not allowed | None |

## Non-Negotiable Rules

1. No exception can bypass segregation of duties.
2. No exception can allow unreviewed direct production deployment.
3. No exception can hide or delete scan results.
4. No exception can be indefinite.
5. Production exceptions require explicit evidence and named approvers.
6. Expired exceptions automatically block promotion.

## Audit Trail Format

```json
{
  "exception_id": "SEC-EX-2026-001",
  "gate": "dependency_container_scanning",
  "environment_scope": "local_prototype_only",
  "production_decision": "blocked",
  "evidence": ["reports/trivy-image-report.txt"],
  "approvers_required": ["Security Lead", "Release Manager"],
  "expiry": "72_hours_or_before_preprod",
  "remediation_ticket": "SEC-REMEDIATE-001",
  "status": "pending_remediation"
}
```
