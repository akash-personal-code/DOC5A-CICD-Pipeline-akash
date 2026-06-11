# Deliverable 8 — Observability and DORA Metrics

## DORA Metrics

| Metric | Target | Measurement |
|---|---|---|
| Deployment Frequency | Multiple per day | Count production deployments per day |
| Lead Time for Changes | <1 hour elite, project target <2 hours | Commit timestamp to production deployment timestamp |
| Change Failure Rate | <5% | Rollbacks + hotfixes / total deployments |
| MTTR | <1 hour elite, NovaPay target <15 min | Detection to full service restoration |

## Pipeline Metrics

1. Build success rate >95%.
2. Average build duration.
3. Test pass rate.
4. Flaky test rate.
5. Coverage trend.
6. SAST pass/fail rate.
7. Dependency gate pass/fail rate.
8. Container scan vulnerability count.
9. SBOM generation success.
10. DAST high/critical count.
11. Policy gate failures by rule.
12. Deployment duration.
13. Canary promotion success rate.
14. Rollback frequency.
15. Rollback trigger distribution.
16. False positive rate per scanning tool.

## Dashboards

- Engineering dashboard: live deployment, error rate, latency, rollback status.
- Management dashboard: DORA trends, deployment frequency, lead time, MTTR.
- Regulatory dashboard: audit evidence, gate outcomes, approvals, exceptions, SBOM and scan archive.

## Alert Routing

- SEV-1: CTO + CISO + VP Engineering, response under 5 minutes.
- SEV-2: VP Engineering + SRE Lead, response under 15 minutes.
- SEV-3: SRE on-call + Tech Lead, response under 1 hour.
- SEV-4: assigned engineer next business day.

