# Stage 08: Deployment and Verification

## 1. Purpose

This stage safely releases the approved NovaPay artifact into Kubernetes with zero-downtime deployment patterns, automated verification, and rollback triggers. It supports both blue-green and canary release strategies.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| GitOps deployment | ArgoCD | 2.x or current stable |
| Package manager | Helm | 3.x |
| Kubernetes | AKS / EKS / GKE | 1.29+ managed Kubernetes |
| Traffic management | Istio VirtualService / Gateway | Current stable |
| Metrics | Prometheus | Current stable |
| Dashboards | Grafana | Current stable |
| Alerting | Alertmanager / PagerDuty / OpsGenie | Current stable |
| Rollback control | ArgoCD rollback + Istio traffic shift | Automated where safe |
| Session continuity | Redis cluster | Redis 7 |

## 3. Inputs

* Signed and scanned container image digest.
* Helm chart and environment-specific values.
* ArgoCD application manifest.
* Approved change ticket.
* Compliance evidence pack.
* Database migration plan and compatibility matrix.
* Rollback specification.
* Deployment blackout calendar result.

## 4. Execution Flow

1. Verify production deployment window is allowed.
2. Confirm Release Manager and SRE Lead approval.
3. Confirm on-call engineer and rollback owner are available.
4. Validate database migration compatibility using expand-contract rules.
5. Sync approved manifests through ArgoCD.
6. Deploy to inactive blue/green environment or canary subset.
7. Run readiness, health, version, smoke, and synthetic checks.
8. Shift traffic using Istio according to selected strategy.
9. Continuously monitor rollback triggers.
10. Mark deployment verified only after success criteria pass.
11. Store deployment evidence and DORA event.

## 5. Blue-Green Strategy

| Step | Action | Verification |
|---|---|---|
| 1 | Deploy new version to inactive environment | Pods ready, image digest correct |
| 2 | Run smoke and synthetic tests against inactive environment | 100% critical checks pass |
| 3 | Enable connection draining on active environment | In-flight requests complete |
| 4 | Switch Istio route from blue to green or green to blue | Traffic split confirms 100% target |
| 5 | Monitor post-switch metrics | No rollback trigger for verification window |
| 6 | Keep old environment warm for rollback | Previous version available |

## 6. Canary Strategy

| Phase | Traffic | Minimum Duration | Success Criteria | Auto Action |
|---|---:|---:|---|---|
| Canary 1 | 1-2% | 15 minutes | Error rate < 0.1%, p99 within threshold, no critical alerts | Promote or rollback |
| Early adopter | 5-10% | 30 minutes | Error rate < 0.05%, no payment success degradation | Promote or rollback |
| Expansion | 25-50% | 60 minutes | SLOs met and no degradation vs baseline | Promote or rollback |
| Full rollout | 100% | 24-hour bake monitoring | SLO compliance maintained | Mark stable |

## 7. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Deployment strategy | Blue-green and canary both documented; one selected per release |
| Rollback availability | Previous version kept deployable |
| Health checks | Liveness, readiness, startup probes required |
| Smoke tests | Health, version, core API, synthetic payment journey |
| Traffic switch | Istio VirtualService controlled |
| Connection draining | 30-60 seconds for HTTP; longer for payment jobs as configured |
| Database migrations | Expand-contract only; contract phase separately approved |
| Blackout calendar | No deployment during peak or restricted windows |
| Verification timeout | Fail deployment if critical verification cannot complete |

## 8. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Deployment window | Not in blackout period | Block deployment |
| Approval gate | Release Manager + SRE Lead approval | Block production deployment |
| ArgoCD sync | Healthy and synced | Pause and investigate |
| Pod readiness | 100% required pods ready | Rollback or stop traffic shift |
| Version verification | All pods run expected image digest | Rollback or block traffic switch |
| Smoke tests | 100% critical checks pass | Rollback |
| Synthetic checks | Critical user journeys pass | Rollback or pause canary |
| Canary metrics | Error/latency/payment thresholds met | Promote or rollback |
| Evidence generation | Deployment evidence pack complete | Block final release closure |

## 9. Rollback Triggers

| Category | Trigger | Action |
|---|---|---|
| A | HTTP 5xx > 5% for 60 seconds | Immediate automated rollback |
| A | 3 consecutive health check failures | Immediate automated rollback |
| A | CrashLoopBackOff or OOM kill in active release | Immediate rollback or traffic shift away |
| A | Database connection pool exhaustion | Immediate rollback/pause and DBA alert |
| B | p99 latency > 2x baseline for 5 minutes | Alert SRE; rollback if not acknowledged |
| B | Payment success drops > 2% below baseline | Pause rollout and escalate |
| B | Error budget burn > 10x normal | Escalated rollback decision |
| C | Customer complaints without metric confirmation | Manual incident commander decision |
| C | Retroactive compliance anomaly | Manual release manager and compliance decision |

## 10. Outputs and Evidence

* ArgoCD sync log.
* Helm release metadata.
* Kubernetes rollout status.
* Istio traffic-shift record.
* Health check and smoke test results.
* Synthetic monitoring result.
* Metrics snapshot before, during, and after deployment.
* Canary analysis result.
* Blue-green switch result.
* Rollback trigger evaluation.
* DORA deployment event.

Example evidence:

```json
{
  "stage": "deployment_verification",
  "deployment_id": "deploy-2026-001",
  "strategy": "canary",
  "environment": "production",
  "image_digest": "sha256:example",
  "argocd_sync_status": "healthy",
  "smoke_tests_passed": true,
  "synthetic_checks_passed": true,
  "rollback_triggered": false,
  "production_verified": true,
  "lead_time_minutes": 94
}
```

## 11. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| ArgoCD sync failed | Manifest error, policy rejection, or cluster issue | Fix manifest/policy and resync |
| Pods not ready | Config error, dependency unavailable, or resource issue | Inspect logs/events and rollback if production affected |
| Version mismatch | Some pods run old or wrong digest | Stop rollout and enforce digest consistency |
| Smoke test failure | Critical endpoint or business journey failed | Rollback and open incident/change defect |
| Canary degradation | New version performs worse than baseline | Shift traffic back to stable version |
| DB migration impact | Query latency or lock impact detected | Abort migration, rollback app where safe, alert DBA |
| Evidence missing | Logs or metrics not archived | Regenerate evidence before closing change |

## 12. Retry and Skip Logic

* Production deployment verification is not skippable.
* Failed pre-traffic checks may be retried after remediation.
* Failed post-traffic critical checks trigger rollback, not repeated blind retries.
* Canary promotion can pause, hold, rollback, or continue based on success criteria.
* Contract database migrations are never bundled with application rollback; they require separate approval and forward-only handling.

## 13. SLA Target

| Metric | Target |
|---|---:|
| ArgoCD sync and pod readiness | < 10 minutes |
| Blue-green pre-switch verification | < 10 minutes |
| Canary first phase decision | 15 minutes |
| Smoke and synthetic verification | < 5 minutes |
| Automated rollback execution | < 60 seconds for Category A |
| Commit-to-production standard path | < 2 hours |

## 14. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI change management | Approved deployment, verification, and rollback procedures |
| RBI business continuity | Blue-green/canary with automated rollback and warm previous version |
| RBI audit trail | Deployment evidence pack and DORA event |
| PCI-DSS change process | Controlled production promotion and post-deployment verification |
| Segregation of duties | Release Manager and SRE Lead dual approval |

## 15. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
