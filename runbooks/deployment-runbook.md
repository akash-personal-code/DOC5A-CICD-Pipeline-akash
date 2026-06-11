# NovaPay Digital Bank Production Deployment Runbook

## 1. Purpose

This runbook provides the operational procedure for deploying NovaPay Digital Bank services to production using the regulated CI/CD pipeline.

It is written for SREs, Release Managers, and on-call engineers who may need to execute or monitor a deployment with minimal context. The runbook covers pre-deployment checks, deployment execution, verification, rollback decisions, communication, and evidence collection.

NovaPay Lite is used only as a local evidence target. The production deployment model assumes Kubernetes, Helm, ArgoCD, service mesh traffic routing, Prometheus, Grafana, Alertmanager, and automated compliance gates.

## 2. Deployment Principles

* Deploy only approved artefacts.
* Promote the same image digest from pre-production to production.
* Do not deploy during blackout windows unless emergency approval exists.
* Do not bypass security and compliance gates.
* Do not deploy with unresolved Critical vulnerabilities.
* Do not run destructive database migrations with application release.
* Use blue-green or canary deployment for production.
* Verify deployment before marking it successful.
* Roll back immediately when Category A rollback triggers fire.
* Generate an evidence pack for every production deployment.

## 3. Roles and Responsibilities

| Role                    | Responsibility                                              |
| ----------------------- | ----------------------------------------------------------- |
| Release Manager         | Owns release approval, change ticket, and go/no-go decision |
| SRE Lead                | Confirms operational readiness and rollback readiness       |
| SRE On-Call             | Monitors deployment, executes operational actions           |
| Developer/Service Owner | Supports application-specific troubleshooting               |
| DBA                     | Approves and monitors database migrations                   |
| Security Reviewer/CISO  | Reviews security exceptions                                 |
| Compliance Owner        | Confirms regulatory evidence requirements                   |
| Product Owner           | Provides UAT/business sign-off                              |
| Incident Commander      | Takes over if deployment becomes an incident                |

## 4. Required Inputs

Before deployment, the following information must be available:

| Input                                    | Required?   |
| ---------------------------------------- | ----------- |
| Application name                         | Yes         |
| Release version                          | Yes         |
| Commit SHA                               | Yes         |
| Image tag                                | Yes         |
| Image digest                             | Yes         |
| Change ticket                            | Yes         |
| Deployment strategy                      | Yes         |
| Target environment                       | Production  |
| Rollback version                         | Yes         |
| Rollback method                          | Yes         |
| SAST report                              | Yes         |
| DAST report                              | Yes         |
| SBOM                                     | Yes         |
| Trivy/container scan report              | Yes         |
| OPA/Kyverno policy result                | Yes         |
| DBA approval, if DB change exists        | Conditional |
| CAB approval or standard change category | Yes         |
| Release Manager approval                 | Yes         |
| SRE Lead approval                        | Yes         |

## 5. Deployment Classification

| Type                        | Description                                       | Approval                                |
| --------------------------- | ------------------------------------------------- | --------------------------------------- |
| Standard release            | Normal application release with all gates passing | Release Manager + SRE Lead              |
| Low-risk patch              | Small backward-compatible patch                   | Release Manager + SRE Lead              |
| High-risk release           | Major dependency, schema, payment, or auth change | CAB + Release Manager + SRE Lead        |
| Database expand migration   | Backward-compatible DB change                     | DBA + Release Manager                   |
| Database contract migration | Destructive/irreversible schema change            | CAB + DBA + SRE Lead + Compliance       |
| Emergency hotfix            | Production fix under incident pressure            | Expedited dual approval, no gate bypass |

## 6. Pre-Deployment Checklist

Complete every item before production deployment.

|  # | Check                                                   | Status  |
| -: | ------------------------------------------------------- | ------- |
|  1 | Change ticket approved and linked to release            | Pending |
|  2 | Release version, commit SHA, and image digest confirmed | Pending |
|  3 | Same artefact verified in pre-production                | Pending |
|  4 | SAST gate passed or approved exception exists           | Pending |
|  5 | Dependency/container scan reviewed                      | Pending |
|  6 | SBOM generated and archived                             | Pending |
|  7 | DAST gate passed or approved exception exists           | Pending |
|  8 | OPA/Kyverno policy checks passed                        | Pending |
|  9 | Helm chart rendered and validated                       | Pending |
| 10 | Database migration plan reviewed, if applicable         | Pending |
| 11 | Rollback plan confirmed                                 | Pending |
| 12 | On-call engineer confirmed available                    | Pending |
| 13 | Dashboards opened and alerting enabled                  | Pending |
| 14 | Deployment is outside blackout window                   | Pending |
| 15 | Release Manager approval recorded                       | Pending |
| 16 | SRE Lead approval recorded                              | Pending |

Go/no-go rule:

```text id="xjqi67"
If any mandatory pre-deployment check is not complete, do not deploy.
```

## 7. Blackout Window Check

Production deployments are blocked during high-risk periods unless emergency approval exists.

Blackout windows:

* Salary days: 1st, 7th, and 15th of each month.
* Month-end processing: 28th to 31st.
* Peak UPI/payment windows: 10 AM-12 PM IST and 5 PM-8 PM IST.
* Major festivals: Diwali, Holi, Eid, Christmas.
* RBI settlement windows.
* Regulatory filing deadlines.
* Planned marketing campaign events.
* Active SEV-1 or SEV-2 incident.

Decision:

```text id="n7233r"
If current time is inside blackout window:
    stop deployment
    request emergency approval only if business-critical
```

## 8. Deployment Strategy Selection

| Condition                    | Strategy                                    |
| ---------------------------- | ------------------------------------------- |
| Low-risk standard release    | Canary                                      |
| High-risk application change | Blue-green                                  |
| Major dependency upgrade     | Blue-green                                  |
| Feature-flagged release      | Canary                                      |
| Database expand phase        | Canary or blue-green                        |
| Database contract phase      | Blue-green with manual approval             |
| Emergency hotfix             | Fast canary or blue-green depending on risk |

Default strategy:

```text id="w40pif"
Use canary for normal changes.
Use blue-green for high-risk or major changes.
```

## 9. Pre-Deployment Evidence Capture

Before deployment, capture baseline metrics:

* Current production version.
* Current image digest.
* Current traffic routing state.
* Current error rate.
* Current p95/p99 latency.
* Current payment success rate.
* Current database connection pool usage.
* Current RabbitMQ queue depth.
* Current Redis health.
* Current active alerts.

Evidence file examples:

```text id="sqpgmo"
evidence/production/baseline-version.json
evidence/production/baseline-metrics.json
evidence/production/baseline-alerts.txt
evidence/production/current-routing.yaml
```

## 10. Deployment Execution: Canary

Use this procedure for standard progressive deployment.

### Step 1: Deploy Canary Version

Deploy the new version with low traffic weight.

Target:

```text id="95sjtd"
stable: 98-99%
canary: 1-2%
```

### Step 2: Verify Canary Pods

Check:

* Pods are ready.
* No CrashLoopBackOff.
* No OOMKilled.
* Correct image digest is running.
* Metrics endpoint is available.
* Logs show no critical startup errors.

### Step 3: Run Smoke Tests

Minimum smoke tests:

* Health endpoint.
* Version endpoint.
* Metrics endpoint.
* Synthetic customer journey.
* Synthetic payment or transaction simulation.
* Database read/write check.
* Redis connectivity.
* RabbitMQ connectivity.

### Step 4: Monitor Phase 1

Duration: 15 minutes.

Success criteria:

* Error rate < 0.1%.
* No critical alerts.
* p99 latency within threshold.
* Synthetic tests pass.
* Payment success rate remains healthy.

Decision:

```text id="69ebm8"
If healthy: promote to next traffic phase.
If unhealthy: rollback canary to 0%.
```

### Step 5: Increase Traffic

Recommended progression:

| Phase   | Traffic |     Duration |
| ------- | ------: | -----------: |
| Phase 1 |    1-2% |   15 minutes |
| Phase 2 |   5-10% |   30 minutes |
| Phase 3 |  25-50% |   60 minutes |
| Phase 4 |    100% | 24-hour bake |

### Step 6: Mark Stable

After canary reaches 100% and verification passes:

* Mark release as stable.
* Update release record.
* Archive evidence.
* Keep rollback target available through bake period.

## 11. Deployment Execution: Blue-Green

Use this procedure for high-risk releases.

### Step 1: Identify Active Environment

Determine active environment:

```text id="sf0dhx"
blue = active
green = inactive
```

or:

```text id="wepbjx"
green = active
blue = inactive
```

### Step 2: Deploy to Inactive Environment

Deploy new version to inactive environment only.

Checks:

* Correct namespace.
* Correct production configuration.
* Correct image digest.
* Secrets loaded from approved secret manager.
* Database schema compatibility confirmed.

### Step 3: Verify Inactive Environment

Run internal route verification:

* Health endpoint.
* Version endpoint.
* Metrics endpoint.
* Synthetic transaction.
* Database connectivity.
* Redis connectivity.
* RabbitMQ connectivity.
* Log ingestion.

### Step 4: Drain Active Environment

Before traffic switch:

* Stop new traffic to old environment.
* Allow in-flight HTTP requests to finish.
* Allow long-running payment jobs to drain.
* Confirm queue consumers are safe.
* Confirm termination grace period.

### Step 5: Switch Traffic

Change routing:

```text id="5qhg2y"
old environment: 100% → 0%
new environment: 0% → 100%
```

### Step 6: Verify After Switch

Run post-switch checks:

* Public route health.
* Version identity.
* Synthetic transaction.
* Error rate.
* p99 latency.
* Payment success rate.
* Alert status.

### Step 7: Keep Previous Environment Warm

Do not immediately delete the old environment.

Recommended retention:

```text id="4v93sk"
Standard release: 2 hours
High-risk release: 24 hours
Major release: 24-48 hours
```

## 12. Database Migration Procedure

If deployment includes a database migration, follow the expand-contract model.

### Expand Phase

Allowed with application deployment only if backward compatible.

Checks:

* DBA approval exists.
* Migration tested in staging.
* Migration tested in pre-production.
* Rollback or mitigation plan exists.
* Query latency impact reviewed.
* Locking behaviour reviewed.

### Migrate/Backfill Phase

Backfill must be:

* Batched.
* Throttled.
* Resumable.
* Idempotent.
* Observable.
* Pausable.

Pause migration if:

* p99 database latency increases more than 20%.
* Database CPU exceeds 70%.
* Connection pool usage exceeds 80%.
* Replication lag exceeds 30 seconds.
* Payment success drops more than 2%.
* Any SEV-1/SEV-2 incident begins.

### Contract Phase

Contract is not deployed with normal application rollout.

Required approvals:

* DBA.
* SRE Lead.
* Release Manager.
* Compliance Owner.
* CAB or formal change approval.

## 13. Deployment Verification

Deployment is successful only after verification passes.

Required verification:

| Check                      | Expected Result            |
| -------------------------- | -------------------------- |
| Health endpoint            | Healthy/OK                 |
| Version endpoint           | Expected release version   |
| Prometheus endpoint        | Metrics available          |
| Pods                       | Ready and stable           |
| Image digest               | Matches approved digest    |
| Synthetic customer journey | Pass                       |
| Synthetic payment journey  | Pass                       |
| Error rate                 | Below threshold            |
| p99 latency                | Within baseline            |
| DB connection pool         | Healthy                    |
| Redis                      | Healthy                    |
| RabbitMQ                   | Queue depth normal         |
| Alerts                     | No critical active alerts  |
| Logs                       | No critical startup errors |

## 14. Rollback Decision Rules

Rollback immediately for Category A triggers.

| Trigger                            | Threshold              |
| ---------------------------------- | ---------------------- |
| HTTP 5xx rate                      | > 5% for 60 seconds    |
| Health check failure               | 3 consecutive failures |
| CrashLoopBackOff                   | Any new release pod    |
| OOMKilled                          | Any new release pod    |
| DB connection pool exhaustion      | Immediate              |
| Critical synthetic journey failure | Repeated failure       |
| Payment success collapse           | Severe sudden drop     |

For Category B triggers, pause rollout and alert SRE:

| Trigger              | Threshold                   |
| -------------------- | --------------------------- |
| p99 latency          | > 2x baseline for 5 minutes |
| Error budget burn    | > 10x normal                |
| Payment success rate | > 2% below baseline         |
| CPU saturation       | > 90% for 5 minutes         |
| Memory saturation    | > 85% for 5 minutes         |
| RabbitMQ queue depth | > 1000 sustained            |

## 15. Rollback Execution

### Canary Rollback

```text id="ss2w21"
stable: current traffic → 100%
canary: current traffic → 0%
```

Steps:

1. Pause rollout.
2. Set canary weight to 0%.
3. Route all traffic to stable version.
4. Verify stable health.
5. Run smoke tests.
6. Open incident or deployment failure record.
7. Archive canary metrics.

### Blue-Green Rollback

```text id="6llrgp"
new environment: 100% → 0%
previous stable environment: 0% → 100%
```

Steps:

1. Freeze deployment.
2. Route traffic back to previous environment.
3. Verify previous environment.
4. Run smoke tests.
5. Preserve failed environment for analysis if safe.
6. Notify stakeholders.
7. Archive evidence.

## 16. Post-Rollback Verification

After rollback:

* Health endpoint passes.
* Version endpoint shows previous stable version.
* Error rate returns below threshold.
* p99 latency returns to baseline.
* Synthetic tests pass.
* Payment success rate recovers.
* No critical alerts remain.
* Incident record is created if customer impact occurred.
* Failed release is blocked from automatic redeployment.

## 17. Communication Plan

### Pre-Deployment Announcement

Send to release channel:

```text id="5pd7y5"
NovaPay production deployment scheduled.

Application: [service]
Version: [version]
Change ticket: [ticket]
Strategy: [canary/blue-green]
Start time: [time]
Expected duration: [duration]
Rollback version: [version]
On-call: [name/team]
```

### Deployment Started

```text id="jjdz6m"
Deployment started.

Application: [service]
Version: [version]
Strategy: [canary/blue-green]
Current phase: [phase]
Dashboard: [link]
```

### Deployment Successful

```text id="kyuzow"
Deployment completed successfully.

Application: [service]
Version: [version]
Verification: passed
Customer impact: none observed
Evidence pack: [location]
```

### Deployment Rolled Back

```text id="5e1h6q"
Deployment rollback executed.

Application: [service]
Failed version: [version]
Restored version: [version]
Trigger: [metric/alert]
Current status: [stable/verifying]
Incident ID: [id]
Next update: [time]
```

## 18. Evidence Pack

Every production deployment must produce an evidence pack.

Required contents:

* Change ticket.
* Approval record.
* Release version.
* Commit SHA.
* Image digest.
* Build log.
* Test result.
* SAST result.
* DAST result.
* Dependency/container scan report.
* SBOM.
* Image signature.
* OPA/Kyverno policy result.
* Helm rendered manifest.
* ArgoCD sync/deployment result.
* Traffic shifting log.
* Smoke test results.
* Prometheus metrics snapshot.
* Alert status.
* Rollback record, if applicable.
* Deployment communication log.
* Final release summary.

## 19. Local NovaPay Lite Evidence Reference

Local evidence generated by NovaPay Lite can be used as sample evidence for the assessment.

| File                                            | Purpose                      |
| ----------------------------------------------- | ---------------------------- |
| `evidence/local-runs/docker-build.txt`          | Image build evidence         |
| `evidence/local-runs/docker-image-inspect.json` | Image metadata               |
| `reports/trivy-image-report.txt`                | Vulnerability scan evidence  |
| `evidence/local-runs/docker-compose-ps.txt`     | Runtime service status       |
| `evidence/local-runs/health-endpoint.txt`       | Health check evidence        |
| `evidence/local-runs/version-endpoint.json`     | Version identity             |
| `evidence/local-runs/prometheus-metrics.txt`    | Metrics endpoint evidence    |
| `evidence/local-runs/openapi-runtime.json`      | OpenAPI evidence             |
| `evidence/local-runs/customer-api-response.txt` | API smoke test               |
| `evidence/local-runs/customer-db-row.txt`       | DB persistence check         |
| `evidence/local-runs/helm-lint.txt`             | Helm validation              |
| `evidence/local-runs/helm-rendered.yaml`        | Rendered Kubernetes manifest |

## 20. Troubleshooting Guide

| Symptom                 | Likely Cause                               | Action                            |
| ----------------------- | ------------------------------------------ | --------------------------------- |
| Pods not ready          | Config, secret, image, or dependency issue | Check pod events and logs         |
| CrashLoopBackOff        | Startup failure                            | Roll back if production impact    |
| Health endpoint fails   | App or dependency unhealthy                | Pause/rollback                    |
| Version mismatch        | Wrong image or routing                     | Stop deployment and correct route |
| High 5xx rate           | App regression or dependency failure       | Rollback if threshold reached     |
| High latency            | Resource, DB, or downstream issue          | Pause rollout                     |
| DB pool exhaustion      | Connection leak or DB saturation           | Rollback/pause and alert DBA      |
| Queue depth rising      | Consumer failure or downstream issue       | Pause rollout                     |
| DAST/policy gate failed | Security/compliance violation              | Do not deploy                     |
| Trivy critical findings | Vulnerable dependency/image                | Block or obtain formal exception  |

## 21. Deployment Completion Criteria

A deployment is complete only when:

* Deployment verification passed.
* No rollback trigger is active.
* No critical alert is active.
* Evidence pack is archived.
* Release status is updated.
* Stakeholders are notified.
* Monitoring continues through bake period.
* Previous stable rollback target remains available until bake period ends.

## 22. Post-Deployment Monitoring

Monitoring period:

| Release Type      | Monitoring Duration |
| ----------------- | ------------------: |
| Low-risk patch    |             2 hours |
| Standard release  |             4 hours |
| High-risk release |            24 hours |
| Major release     |         24-48 hours |

Monitor:

* Error rate.
* p99 latency.
* Payment success rate.
* Customer onboarding success.
* DB pool usage.
* Queue depth.
* Redis/session errors.
* Alert volume.
* Customer support reports.

## 23. Go/No-Go Decision Template

```text id="zie849"
Release: [version]
Change ticket: [ticket]
Deployment strategy: [canary/blue-green]
Pre-deployment gates: [pass/fail]
Security gates: [pass/fail]
Compliance gates: [pass/fail]
DB migration: [none/expand/migrate/contract]
Rollback plan: [confirmed/not confirmed]
Blackout window: [clear/blocked]
On-call ready: [yes/no]

Decision:
[ ] GO
[ ] NO-GO

Decision owner:
Timestamp:
Reason:
```

## 24. Final Deployment Summary Template

```text id="44zkba"
Deployment Summary

Application:
Version:
Commit SHA:
Image digest:
Environment:
Deployment strategy:
Start time:
End time:
Change ticket:
Approvers:
Verification result:
Rollback triggered:
Customer impact:
Evidence pack location:
Follow-up actions:
```

## 25. Conclusion

This deployment runbook provides the operational procedure for safe production release at NovaPay Digital Bank.

It ensures that every production deployment is approved, observable, reversible, and audit-ready. By combining controlled promotion, blue-green/canary deployment, automated verification, rollback triggers, and evidence generation, NovaPay can reduce deployment risk while meeting regulated banking expectations.
