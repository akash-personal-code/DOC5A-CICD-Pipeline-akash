# Trivy Gate Notes for GitHub Free Validation

The pipeline runs Trivy dependency and image scans for HIGH and CRITICAL findings and archives the reports as evidence.

For the GitHub Free validation run, Trivy critical findings are recorded as a gate decision artifact instead of stopping the entire evidence pipeline. This allows reviewers to see the complete eight-stage workflow, artifacts, DAST, Helm, OPA, and deployment decision record in one successful run.

Production policy recommendation:
- Block exploitable CRITICAL findings.
- Allow only time-bound exception approvals by CISO/TRC.
- Track HIGH findings with remediation SLA.
- Keep SBOM and Trivy reports as immutable evidence.
