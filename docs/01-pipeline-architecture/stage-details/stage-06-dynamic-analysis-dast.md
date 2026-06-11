# Stage 06: Dynamic Analysis and DAST

## 1. Purpose

This stage tests the running NovaPay application for exploitable web and API vulnerabilities. It validates security behavior from an attacker-like perspective before the release is allowed to move toward production.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| DAST scanner | OWASP ZAP | Current stable |
| API scan input | OpenAPI / Swagger spec | OpenAPI 3.x |
| Authenticated scanning | ZAP context + test credentials | Secure non-prod credentials |
| Target environment | Staging or isolated ephemeral environment | Non-production only |
| Report format | HTML, JSON, SARIF where supported | Archived evidence |

## 3. Inputs

* Running application endpoint.
* OpenAPI specification.
* Authentication context and non-production test credentials.
* ZAP baseline and active scan configuration.
* Excluded false-positive rules with approval history.
* Synthetic test data.

## 4. Execution Flow

1. Deploy candidate build to staging or isolated test namespace.
2. Confirm target endpoint is healthy.
3. Load OpenAPI specification into OWASP ZAP.
4. Configure authenticated context using secure test credentials.
5. Run passive scan against discovered endpoints.
6. Run active scan against non-production target.
7. Map findings to OWASP Top 10 and internal severity model.
8. Evaluate DAST quality gates.
9. Archive reports and create remediation tickets for findings.
10. Destroy or reset test data where required.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Critical DAST findings | 0 |
| High DAST findings | 0 for OWASP Top 10 and payment/customer endpoints |
| Medium findings | Ticket required with remediation SLA |
| Authenticated scan | Required for protected APIs |
| Scan target | Non-production only |
| Test credentials | Stored in Vault or GitHub encrypted secret; rotated periodically |
| False positive handling | Approved suppression file with expiry |
| Scan timeout | 15 minutes for standard pipeline; deeper scan scheduled periodically |

## 6. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Target health | Application reachable and ready | Fail stage and notify SRE/platform team |
| OpenAPI load | Spec valid and scanable | Block pipeline until spec fixed |
| Authenticated scan | Login/session successful | Block scan result acceptance |
| Critical findings | 0 | Block pipeline |
| High findings | 0 for OWASP Top 10/payment/data paths | Block pipeline |
| False positives | Approved, documented, and not expired | Block if suppression expired |
| Report archive | JSON/HTML report stored | Block promotion evidence completion |

## 7. Outputs and Evidence

* OWASP ZAP HTML report.
* OWASP ZAP JSON report.
* Finding summary by severity.
* OpenAPI scan coverage summary.
* Authentication scan proof.
* False-positive suppression record.
* Remediation ticket links.

Example evidence:

```json
{
  "stage": "dynamic_analysis_dast",
  "tool": "OWASP ZAP",
  "target_environment": "staging",
  "authenticated_scan": true,
  "critical_findings": 0,
  "high_findings": 0,
  "medium_findings": 2,
  "owasp_top_10_blockers": 0,
  "report_archived": true
}
```

## 8. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Critical vulnerability | Injection, authentication bypass, sensitive exposure | Fix code/config and rerun scan |
| High vulnerability | OWASP Top 10 issue on protected route | Fix or obtain formal risk acceptance for non-production only |
| Authentication failed | Invalid test credential or login flow change | Update ZAP context and rotate credentials if needed |
| Target unavailable | Deployment or environment failure | Fix deployment and rerun DAST |
| Excessive false positives | Scanner rule noise | Document approved suppressions with expiry and evidence |
| Scan timeout | Too many endpoints or slow target | Split scan by API group and optimize target health |

## 9. Retry and Skip Logic

* DAST is not skippable for production-bound public or authenticated APIs.
* Scanner timeouts may be retried once after target health is verified.
* Authenticated scanning cannot be replaced by unauthenticated scanning for protected banking APIs.
* Emergency hotfixes must run focused DAST on changed and payment-critical endpoints.

## 10. SLA Target

| Metric | Target |
|---|---:|
| Target readiness check | < 2 minutes |
| ZAP baseline/passive scan | < 5 minutes |
| ZAP active/API scan | < 15 minutes |
| Total stage duration | < 20 minutes |

## 11. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI vulnerability assessment | Dynamic security test evidence |
| PCI-DSS public-facing application protection | DAST and OWASP Top 10 coverage |
| Audit readiness | Archived scan report tied to release ID |
| Change management | Security gate blocks unsafe deployment promotion |

## 12. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
