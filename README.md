# NovaPay Quality and Security Remediation Pack

Merge this pack at the repository root to document the two remaining submission risks:

1. JaCoCo branch coverage is below the production gate target.
2. Trivy scan evidence contains Critical and High findings.

This pack does not pretend the risks are fixed. It records them as controlled exceptions for the local prototype only, adds remediation plans, and provides a JaCoCo gate patch that can be applied when you want to make the coverage gate cleaner.

Recommended placement after extraction:

```text
repo-root/
├── evidence/quality-security/
├── evidence/test-results/
├── docs/03-compliance-gates/exception-register.md
├── pipeline/security/
└── patches/
```

Use these files together with your existing `reports/trivy-image-report.txt` and `build/reports/jacoco/test/jacocoTestReport.xml` evidence.
