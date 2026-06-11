# Evidence Index

This document maps assessment evidence to repository files.

| Evidence Area | Repository Evidence |
|---|---|
| 8-stage CI/CD pipeline | `docs/01-pipeline-architecture/`, `pipeline/.github/workflows/` |
| Blue-green and canary deployment | `docs/02-deployment-strategies/`, `pipeline/helm/` |
| Compliance gates | `docs/03-compliance-gates/`, `pipeline/policies/` |
| Zero-downtime DB migration | `docs/04-database-migration/`, database migration files |
| Environment promotion | `docs/05-environment-promotion/` |
| Rollback specification | `docs/06-rollback-specification/` |
| Runbooks and incident playbooks | `docs/07-runbook-playbook/`, `runbooks/` |
| Observability and DORA | `docs/08-observability/`, `dashboards/` |
| Security/coverage known limitations | `evidence/quality-security/`, `docs/03-compliance-gates/exception-register.md` |
| TRC presentation | `evidence/trc-presentation.pdf` |
| Self-assessment | `evidence/self-assessment.md` |
| Reflection answers | `evidence/reflections.md` |

## Screenshot Limitation

Screenshots are useful supporting evidence, but the main proof in this package is file-based: configuration files, Markdown deliverables, pipeline definitions, policy files, test outputs, exception records, and PDF presentation evidence.
