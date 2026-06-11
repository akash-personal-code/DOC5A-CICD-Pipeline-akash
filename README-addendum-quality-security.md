## Quality and Security Evidence Status

The NovaPay Lite implementation is a local prototype used to demonstrate CI/CD, testing, scanning, observability, and deployment-control concepts. It is not marked production-ready.

Two known limitations are documented in the evidence pack:

1. JaCoCo branch coverage is currently below the production gate target. The production gate remains 80% line coverage and 70% branch coverage. A remediation plan and coverage exception record are provided under `evidence/quality-security/`.
2. The current Trivy image report contains Critical and High dependency findings. The production dependency/container scanning gate would block promotion until these are remediated or covered by a CISO-approved time-bound exception.

The final production pipeline design intentionally blocks these issues. They are included transparently as prototype limitations and audit evidence rather than hidden from the submission.
