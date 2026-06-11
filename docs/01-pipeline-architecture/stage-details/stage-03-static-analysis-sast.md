# Stage 03: Static Analysis and SAST

## 1. Purpose

This stage detects insecure code patterns, maintainability issues, and banking-specific coding risks before the application is packaged for deployment. It blocks vulnerabilities early, when remediation is cheapest and safest.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| SAST and quality analysis | SonarQube / SonarCloud | Current supported version |
| Optional rule supplement | Semgrep | Current stable |
| Secret scanning | Gitleaks / GitHub Secret Scanning | Current stable |
| Java security rules | OWASP, CWE, banking custom rules | Enabled |
| Quality gate reporting | Sonar quality gate | Mandatory |

## 3. Inputs

* Source code.
* Compiled classes where required by scanner.
* Unit test and coverage reports.
* Sonar project key.
* Custom banking security rule profile.
* Pull request metadata.

## 4. Execution Flow

1. Download source and build context from previous stage.
2. Load SonarQube project configuration.
3. Import JaCoCo coverage results.
4. Run Sonar analysis.
5. Run custom security rules for PII, encryption, SQL injection, logging, and authentication patterns.
6. Publish findings to SonarQube dashboard.
7. Evaluate quality gate thresholds.
8. Block or pass the pipeline based on severity and coverage thresholds.
9. Create remediation ticket for blocking findings.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Critical vulnerabilities | 0 |
| High vulnerabilities | <= 2 for non-production; 0 preferred for production |
| New code line coverage | >= 80% |
| New code branch coverage | >= 70% |
| Technical debt ratio on new code | <= 5% |
| Security hotspots | 100% reviewed before production |
| Duplicated lines on new code | <= 3% |
| Custom rule profile | Banking profile enabled |

## 6. Banking Custom Rules

| Rule Area | Control Intent |
|---|---|
| PII logging | Prevent account, PAN, card, phone, email, and customer identifiers from being logged in plaintext |
| SQL injection | Detect unsafe query construction |
| Cryptography | Block weak algorithms and hardcoded keys |
| Authentication | Detect insecure session/token handling |
| Error handling | Prevent stack traces and sensitive errors from leaking to customers |
| Authorization | Detect missing role checks on protected endpoints |
| Idempotency | Ensure payment APIs include duplicate-processing protection |

## 7. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Critical SAST issues | 0 | Block pipeline |
| High SAST issues | <= 2 with no exploitable payment/data path; 0 for production target | Block or require CISO exception |
| Coverage import | Coverage report present and valid | Block quality gate |
| Security hotspots | Reviewed and marked safe | Block production promotion |
| New code maintainability | Debt ratio <= 5% | Block PR merge |
| Sensitive logging | 0 confirmed PII/secret logging findings | Block pipeline |

## 8. Outputs and Evidence

* SonarQube analysis report.
* Quality gate status.
* Security hotspot review record.
* Coverage import status.
* Finding list by severity.
* Remediation ticket links.
* Exception record where applicable.

Example evidence:

```json
{
  "stage": "static_analysis_sast",
  "tool": "SonarQube",
  "quality_gate": "passed",
  "critical_findings": 0,
  "high_findings": 1,
  "line_coverage_percent": 84.2,
  "branch_coverage_percent": 72.4,
  "security_hotspots_reviewed": true,
  "exception_required": false
}
```

## 9. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Critical vulnerability | SQL injection, auth bypass, hardcoded secret, unsafe deserialization | Fix immediately and rerun |
| Coverage below threshold | Insufficient tests for new code | Add tests for normal, boundary, and failure paths |
| False positive | Scanner flags safe pattern | Document evidence and request CISO-approved exception |
| Sonar server unavailable | Tool outage | Retry once; if outage persists, pause promotion until scan evidence exists |
| Missing project key | Misconfigured pipeline | Fix CI configuration |

## 10. Retry and Skip Logic

* SAST is never skippable for production changes.
* Scanner infrastructure failures may be retried up to two times.
* False positives require formal exception with owner, expiry date, evidence, and CISO approval.
* Hotfixes may use expedited review but cannot bypass critical or high findings on payment/data paths.

## 11. SLA Target

| Metric | Target |
|---|---:|
| SAST scan duration | < 10 minutes |
| Quality gate result availability | < 2 minutes after scan |
| Blocking ticket creation | < 1 minute after failure |

## 12. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI vulnerability management | Automated source-level vulnerability checks |
| PCI-DSS software security | Secure code review and vulnerability prevention |
| Audit readiness | Quality gate record attached to deployment evidence |
| Segregation of duties | Developers fix issues; CISO approves exceptions |

## 13. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
