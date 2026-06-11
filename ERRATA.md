## NovaPay Digital Bank CI/CD Pipeline Assessment — Deliberate Error Findings

This file documents the three deliberate technical errors identified in the assessment PDF and explains the corrected interpretation used in this project.

## Error 1 — Part A: DNS is not an atomic blue-green traffic switch

### Location

Part A, Section A3: Zero-Downtime Deployment Strategies
Subsection A.3.1: Blue-Green Deployments

### Problem

The document describes blue-green traffic switching as happening atomically through options including DNS update, load balancer reconfiguration, or service mesh routing.

The issue is that DNS changes are not truly atomic. DNS records are cached by clients, resolvers, ISPs, and intermediate systems according to TTL behaviour. Even with a low TTL, some clients may continue using the old endpoint for a period of time. Therefore, DNS alone should not be treated as an atomic cutover mechanism for strict zero-downtime banking deployments.

### Correct Interpretation

For regulated banking workloads, blue-green traffic cutover should be handled through one of the following:

* Kubernetes service selector switch.
* Load balancer target group switch.
* Ingress controller routing update.
* Istio VirtualService traffic weight change.
* API gateway route update.

DNS may be used at the outer edge of a system, but it should not be the primary mechanism for precise, fast, reversible, zero-downtime production traffic switching.

### Correction Applied in This Project

This project uses service mesh or load-balancer-level routing as the primary blue-green and canary mechanism. The deployment strategy documents use Istio VirtualService-style traffic switching for blue-green and canary release flows.

Relevant project files:

* `docs/02-deployment-strategies/deployment-strategies.md`
* `docs/06-rollback-specification/rollback-specification.md`
* `runbooks/deployment-runbook.md`

---

## Error 2 — Part C: Cloudflare outage duration

### Location

Part C, Case Study 3: Cloudflare Global Outage

### Problem

The deliberate error in the case study concerns the duration of the Cloudflare July 2019 outage. The incorrect version states that the outage lasted 21 minutes.

### Correct Interpretation

The corrected outage duration is 27 minutes.

### Why This Matters

The lesson is that configuration changes can be as risky as application code changes. WAF rules, routing rules, feature flags, and other operational configuration should pass through controlled CI/CD validation, canary testing, performance checks, and independent rollback paths.

### Correction Applied in This Project

This project treats configuration changes as deployment-controlled artefacts. WAF rules, Kubernetes manifests, Helm values, policy files, traffic routing rules, and feature flag changes are included in the same governed delivery process.

Relevant project files:

* `docs/01-pipeline-architecture/architecture.md`
* `docs/02-deployment-strategies/deployment-strategies.md`
* `docs/03-compliance-gates/compliance-gates.md`
* `runbooks/incident-playbook.md`

---

## Error 3 — Part D: gh-ost is described incorrectly for migration research

### Location

Part D, Daily Breakdown
Day 6: Database Migration Strategy

### Problem

The document asks students to study the “gh-ost trigger-based replication approach.”

This is technically inaccurate. `gh-ost` is a MySQL online schema migration tool that is designed to avoid using triggers. It uses the MySQL binary log/replication stream approach. The tool that is commonly associated with trigger-based online schema change in MySQL is `pt-online-schema-change`.

Also, NovaPay’s assumed database is PostgreSQL 16. Therefore, gh-ost is not the primary tool for NovaPay’s production database migration strategy.

### Correct Interpretation

For NovaPay’s PostgreSQL 16 database, appropriate approaches include:

* Expand-contract migration design.
* PostgreSQL-safe online DDL practices.
* `CREATE INDEX CONCURRENTLY`.
* `NOT VALID` constraints followed by `VALIDATE CONSTRAINT`.
* Batched and throttled backfill jobs.
* `pgroll` or equivalent PostgreSQL-oriented online migration tooling.
* Flyway for controlled versioned migration execution where suitable.

For MySQL environments:

* `gh-ost` is triggerless and binlog-based.
* `pt-online-schema-change` is trigger-based.

### Correction Applied in This Project

This project uses PostgreSQL-oriented migration guidance and treats Flyway as the local demonstration migration tool. It does not rely on gh-ost for NovaPay’s PostgreSQL production migration design.

Relevant project files:

* `docs/04-database-migration/database-migration.md`
* `src/main/resources/db/migration/`
* `evidence/local-runs/customer-db-row.txt`

---

## Summary

| Error  | Incorrect Statement / Assumption                                      | Corrected Position                                                                              |
| ------ | --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| Part A | DNS can be treated as an atomic blue-green switch                     | DNS is not atomic; use load balancer, ingress, or service mesh routing                          |
| Part C | Cloudflare outage lasted 21 minutes                                   | Correct duration: 27 minutes                                                                    |
| Part D | gh-ost is trigger-based and suitable as a primary migration reference | gh-ost is MySQL triggerless/binlog-based; PostgreSQL should use pgroll/safe online DDL patterns |

## Final Note

These corrections were incorporated into the NovaPay Digital Bank CI/CD pipeline design. The final architecture avoids DNS-based atomic cutover assumptions, treats configuration changes as pipeline-controlled artefacts, and uses PostgreSQL-appropriate expand-contract database migration practices.
