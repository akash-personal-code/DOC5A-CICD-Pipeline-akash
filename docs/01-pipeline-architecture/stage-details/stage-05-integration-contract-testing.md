# Stage 05: Integration and Contract Testing

## 1. Purpose

This stage validates that NovaPay services work correctly with databases, caches, message brokers, APIs, and dependent services before any dynamic security scan or deployment promotion. It prevents broken service contracts and environment-specific failures from reaching pre-production or production.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| Integration testing | JUnit 5 + Testcontainers | Current stable |
| Contract testing | Pact | Current stable |
| API testing | Newman / Postman or REST Assured | Current stable |
| Ephemeral environment | Kubernetes namespace per pipeline run | Kubernetes 1.29+ |
| Database | PostgreSQL | 16 |
| Cache | Redis | 7 |
| Message broker | RabbitMQ | 3.13 |
| GitOps deployment | ArgoCD / Helm | Current stable |

## 3. Inputs

* Signed application image.
* Helm chart or Kubernetes manifests.
* Test data set.
* Pact contracts.
* OpenAPI specification.
* Database migration scripts.
* Ephemeral namespace configuration.

## 4. Execution Flow

1. Create isolated Kubernetes namespace for the pipeline run.
2. Provision PostgreSQL, Redis, RabbitMQ, and test dependencies.
3. Deploy the signed NovaPay image using Helm.
4. Apply database migrations against test schema.
5. Run service readiness checks.
6. Execute integration test suite.
7. Run consumer-driven contract tests with Pact.
8. Verify OpenAPI backward compatibility.
9. Run baseline performance smoke tests.
10. Destroy ephemeral namespace after evidence collection.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Namespace lifecycle | Created per run and deleted after tests |
| Test data | Synthetic or anonymized only |
| Integration test pass rate | 100% |
| Contract test pass rate | 100% provider and consumer compatibility |
| Database migration | Must apply successfully from clean and previous schema state |
| API backward compatibility | No breaking public API changes without versioning |
| Performance baseline | p99 < 500ms under staging test load |
| External dependencies | Mocked or sandboxed; no real payment movement |

## 6. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Ephemeral environment | Namespace and dependencies healthy | Block tests and notify platform team |
| Integration tests | 100% pass | Block promotion |
| Contract tests | 100% pass | Block promotion and notify owning team |
| API compatibility | No unapproved breaking changes | Block PR/promotion |
| Migration validation | Migrations apply and rollback plan documented | Block promotion |
| Test data safety | No production PII in test environment | Block and raise compliance incident |
| Baseline performance | Meets latency and error-rate threshold | Block staging to pre-prod promotion |

## 7. Outputs and Evidence

* Integration test report.
* Pact verification report.
* API compatibility report.
* Ephemeral environment deployment log.
* Migration test result.
* Performance smoke result.
* Namespace cleanup confirmation.

Example evidence:

```json
{
  "stage": "integration_contract_testing",
  "namespace": "novapay-pr-245-run-123456",
  "integration_tests_passed": true,
  "contract_tests_passed": true,
  "api_backward_compatible": true,
  "migration_validated": true,
  "p99_latency_ms": 240,
  "namespace_cleaned_up": true
}
```

## 8. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Integration test failure | Service logic or dependency interaction broken | Fix code or configuration and rerun |
| Contract failure | Provider changed API unexpectedly | Restore compatibility or publish new API version |
| Migration failure | SQL incompatible or locks detected | Fix migration and retest with previous schema |
| Environment provisioning failure | Kubernetes quota or Helm error | Fix manifest/quota and rerun |
| Test data issue | Missing or unsafe data | Regenerate synthetic/masked test data |
| Performance regression | Query, cache, or downstream slowdown | Optimize and rerun baseline test |

## 9. Retry and Skip Logic

* Integration and contract tests are not skippable for production-bound changes.
* Infrastructure provisioning failures may be retried once after cleanup.
* Flaky tests must be tracked; quarantine requires tech lead approval and replacement coverage.
* Emergency hotfixes must still run payment-critical integration and contract tests.

## 10. SLA Target

| Metric | Target |
|---|---:|
| Ephemeral environment provisioning | < 8 minutes |
| Integration test execution | < 15 minutes |
| Contract verification | < 5 minutes |
| Total stage duration | < 25 minutes |

## 11. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI change testing | Integration and migration validation before promotion |
| PCI-DSS secure change | Tested application behavior and API compatibility |
| Operational resilience | Prevents dependency and contract failures from reaching production |
| Data protection | Synthetic/masked data only in non-production testing |

## 12. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
