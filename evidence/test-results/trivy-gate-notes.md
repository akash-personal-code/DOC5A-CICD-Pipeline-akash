# Trivy Gate Notes

The CI pipeline runs two Trivy checks for dependency and image security evidence:

1. **Evidence scan:** HIGH and CRITICAL vulnerabilities are reported and archived for audit evidence.
2. **Blocking gate:** CRITICAL vulnerabilities fail the workflow. HIGH findings are retained in evidence and handled through remediation/risk acceptance because Trivy's GitHub Action does not provide a direct CVSS > 8.0 threshold switch.

This maps to the project requirement where Critical findings are hard blockers and High findings require remediation tracking or exception approval.
