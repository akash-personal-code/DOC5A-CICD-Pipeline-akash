# Known Limitations Register

## Purpose

This register documents known quality and security limitations found in the NovaPay Lite local implementation evidence. These limitations apply only to the demonstration prototype and are not approved for production deployment.

The production CI/CD design remains strict: production releases must satisfy the quality, vulnerability, compliance, approval, and evidence gates before deployment.

## Summary

| ID | Area | Current Status | Gate Decision | Production Impact | Owner | Target Remediation |
|---|---|---|---|---|---|---|
| KL-001 | JaCoCo branch coverage | Branch coverage below 70% target | Fail for production | Blocks production promotion | QA Lead / Tech Lead | Before final production sign-off |
| KL-002 | Trivy Java dependency findings | Critical and High findings present | Fail for production | Blocks production promotion | Security Lead / App Owner | Before staging-to-pre-prod promotion |
| KL-003 | Base image package finding | Medium OS package finding present | Warn/remediate | Must be patched before release hardening | Platform Lead | Next image rebuild |

## KL-001: JaCoCo Branch Coverage

### Observation

The current JaCoCo evidence indicates branch coverage is below the required production gate target. The current report is affected by generated/model/DTO code being counted in branch coverage, especially Lombok-generated model branches and request/response DTO branches.

### Current Evidence

| Evidence | Value |
|---|---:|
| Source file | `build/reports/jacoco/test/jacocoTestReport.xml` |
| Covered branches | 7 |
| Missed branches | 339 |
| Approximate branch coverage | 2.02% |
| Covered lines | 179 |
| Missed lines | 43 |
| Approximate line coverage | 80.63% |

### Risk

The local prototype cannot be treated as production-ready until the coverage gate is corrected and re-run. Low branch coverage may hide untested validation, exception, rollback, and payment edge cases.

### Gate Decision

**Production gate result: FAIL.**

A time-bound exception may be recorded for assessment prototype evidence only. This exception does not allow deployment to production or pre-production.

### Remediation Plan

1. Add branch tests for service-layer edge cases:
   - Payment idempotency returns existing payment.
   - Payment idempotency key exists in Redis but payment is missing in DB.
   - Source account missing.
   - Insufficient funds.
   - RabbitMQ publish failure does not roll back payment persistence.
   - Customer email encryption branch when plain email is present.
   - Customer email encryption branch when encrypted email already exists.
2. Add controller tests for validation and exception paths:
   - Account creation validation failure.
   - Payment creation validation failure.
   - IllegalArgumentException returns HTTP 400.
   - IllegalStateException returns HTTP 409.
   - Generic exception returns HTTP 500.
3. Configure JaCoCo to exclude generated/non-business-logic classes from gate calculation:
   - Lombok models/builders.
   - Spring Boot application bootstrap class.
   - Repository interfaces.
   - Configuration classes.
   - Request/response DTO classes where validation is separately tested.
4. Re-run:

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

5. Store new evidence under:

```text
evidence/test-results/jacoco-summary-after-remediation.md
```

## KL-002: Trivy Java Dependency Findings

### Observation

The current Trivy image report includes Critical and High vulnerability findings in Java dependencies.

### Current Evidence

| Evidence | Value |
|---|---:|
| Source file | `reports/trivy-image-report.txt` |
| Java dependency findings | 59 total |
| Critical findings | 4 |
| High findings | 25 |
| Medium findings | 19 |
| Low findings | 11 |

### Risk

Critical and High vulnerability findings violate the dependency/container scanning gate. A regulated banking pipeline must block promotion until the findings are remediated, proven false-positive, or covered by a time-bound risk acceptance approved by security leadership.

### Gate Decision

**Production gate result: FAIL.**

The current image must not be promoted to production. The exception is limited to local prototype evidence and architecture demonstration.

### Remediation Plan

1. Upgrade the Spring Boot plugin and dependency-management versions to the latest approved patch level.
2. Upgrade vulnerable direct dependencies such as logging, OpenAPI, Jackson, and any Netty-related transitive dependency using the Spring dependency management BOM where possible.
3. Rebuild from a patched base image.
4. Generate an updated SBOM.
5. Re-run Trivy with production blocking thresholds:

```bash
trivy image --severity CRITICAL,HIGH --exit-code 1 novapay-lite:0.0.1
trivy fs --scanners vuln,secret,misconfig --severity CRITICAL,HIGH --exit-code 1 .
```

6. Store updated evidence under:

```text
evidence/test-results/trivy-remediation-result.txt
```

## Exception Rules

Any exception for KL-001 or KL-002 must follow these rules:

| Rule | Requirement |
|---|---|
| Scope | Prototype/local demonstration only |
| Production use | Not allowed |
| Approval | CISO or delegated Security Lead + Release Manager |
| Expiry | Maximum 72 hours or before pre-prod promotion, whichever comes first |
| Evidence | Signed exception record, scan output, owner, remediation ticket |
| Auto-expiry | Exception expires automatically if not renewed |
| Audit record | Must be stored in the evidence folder |

## Final Submission Note

These known limitations should be visible to evaluators. Hiding them is riskier than documenting them. The final README should state that the local implementation is a prototype and that the architecture gates would block production release until these findings are remediated.
