# Coverage Exception and Remediation Record

## Exception ID

`QA-EX-2026-001`

## Title

Time-bound prototype exception for JaCoCo branch coverage below NovaPay production gate target.

## Scope

This exception applies only to the local prototype evidence and does not permit promotion to pre-production or production.

## Evidence Source

```text
build/reports/jacoco/test/jacocoTestReport.xml
build/reports/jacoco/test/html/index.html
```

## Current Coverage Summary

| Metric | Current Evidence | Target | Gate Result |
|---|---:|---:|---|
| Line coverage | 179 / 222 = approximately 80.63% | 80% | Pass |
| Branch coverage | 7 / 346 = approximately 2.02% | 70% | Fail |

## Why Branch Coverage Is Low

The current JaCoCo report includes classes that create many generated or low-value branch counters:

* Lombok-generated model/builders.
* Request/response DTO classes.
* Spring configuration classes.
* Application bootstrap code.
* Repository interfaces.

This does not remove the need for branch testing. It means the production gate should measure business logic and controller behavior separately from generated and framework glue code.

## Production Gate Decision

**Fail until remediated.**

The prototype can be submitted with this exception only if the final evidence clearly states that production promotion is blocked until branch coverage is corrected.

## Remediation Backlog

| Test Area | Required Test | Priority |
|---|---|---|
| Payment service | New idempotency key creates payment | High |
| Payment service | Duplicate idempotency key returns existing payment | High |
| Payment service | Redis key exists but DB payment missing returns conflict | High |
| Payment service | Source account not found returns bad request | High |
| Payment service | Insufficient funds returns bad request | High |
| Payment service | RabbitMQ publish failure is logged without losing saved payment | Medium |
| Customer service | Plain email is converted to encrypted email | High |
| Customer service | Already encrypted email is preserved | Medium |
| Account service | Missing customer returns bad request | High |
| Global exception handler | Validation exceptions return field-level errors | High |
| Global exception handler | Illegal state returns HTTP 409 | Medium |
| Controllers | Validation failures for account and payment requests | High |

## Recommended JaCoCo Gate Update

Apply the patch in:

```text
patches/build.gradle.jacoco-coverage-gate.patch
```

The patch keeps the 80% line and 70% branch gate, but excludes generated/model/framework classes from the calculation so the gate measures meaningful business logic.

## Verification Commands

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

## Expected Evidence After Remediation

Store the following files after rerun:

```text
evidence/test-results/jacoco-summary-after-remediation.md
evidence/test-results/jacocoTestReport.xml
evidence/test-results/jacoco-html-screenshot.png
```

## Approval Placeholders

| Role | Name | Decision | Date |
|---|---|---|---|
| QA Lead | `<name>` | `<approved/rejected>` | `<date>` |
| Tech Lead | `<name>` | `<approved/rejected>` | `<date>` |
| Release Manager | `<name>` | `<approved/rejected>` | `<date>` |

## Audit JSON Record

```json
{
  "exception_id": "QA-EX-2026-001",
  "type": "coverage_gate_exception",
  "scope": "local_prototype_only",
  "line_coverage_percent": 80.63,
  "branch_coverage_percent": 2.02,
  "target_line_coverage_percent": 80,
  "target_branch_coverage_percent": 70,
  "gate_decision": "block_for_production",
  "root_cause": "generated_model_dto_and_framework_classes_counted_plus_missing_branch_tests",
  "required_remediation": [
    "add_service_and_controller_branch_tests",
    "exclude_generated_and_framework_classes_from_jacoco_gate",
    "rerun_jacoco_verification",
    "store_updated_evidence"
  ],
  "expiry_policy": "before_preprod_promotion",
  "status": "pending_remediation"
}
```
