# Stage 04: Dependency, SBOM, License, and Container Scanning

## 1. Purpose

This stage validates the software supply chain before deployment by scanning third-party libraries, container images, base images, licenses, and artifact provenance. It prevents vulnerable or non-compliant components from entering NovaPay environments.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| Dependency/container scanning | Trivy | Current stable |
| Secondary vulnerability scan | Grype | Current stable |
| SBOM generation | Syft or Trivy SBOM | CycloneDX and/or SPDX |
| License scanning | FOSSA, ScanCode, or Trivy license scan | Current stable |
| Image signing | Cosign / Sigstore | Current stable |
| Registry | JFrog Artifactory or Azure Container Registry | Private registry |

## 3. Inputs

* Built container image.
* Image digest.
* Dependency lock file.
* Gradle dependency graph.
* Dockerfile.
* Base image metadata.
* License policy.

## 4. Execution Flow

1. Pull image by immutable digest from private registry.
2. Generate SBOM in CycloneDX and/or SPDX format.
3. Scan application dependencies for CVEs.
4. Scan container OS packages and base image layers.
5. Scan licenses against banking-approved allowlist/denylist.
6. Verify image signature and provenance metadata.
7. Evaluate vulnerability and license gates.
8. Archive SBOM and scan outputs as audit evidence.
9. Block promotion if thresholds are breached.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Critical CVEs | 0 for production promotion |
| High CVEs | Block if CVSS >= 8.0 or exploitable path exists |
| Medium CVEs | Ticket required; non-blocking unless active exploit exists |
| SBOM | Generated for 100% of production releases |
| SBOM format | CycloneDX JSON and/or SPDX JSON |
| License denylist | GPL, AGPL, SSPL unless legal exception exists |
| Base image | Approved minimal image only |
| Image signature | Required before deployment |
| `latest` tag | Forbidden for production |

## 6. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Critical CVE scan | 0 Critical vulnerabilities | Block pipeline |
| High CVE scan | No High CVE with CVSS >= 8.0 or known exploit | Block or require security exception |
| SBOM generation | SBOM generated and archived | Block promotion |
| License check | No prohibited licenses | Block and trigger legal review |
| Base image provenance | Approved image and digest pinned | Block image promotion |
| Image signature | Valid Cosign signature | Block Kubernetes admission |
| Registry immutability | Digest exists and tag cannot be overwritten | Block production promotion |

## 7. Outputs and Evidence

* Trivy JSON report.
* Grype report where configured.
* CycloneDX or SPDX SBOM.
* License compliance report.
* Image digest.
* Image signature verification output.
* Base image provenance record.
* Remediation or exception tickets.

Example evidence:

```json
{
  "stage": "dependency_container_scanning",
  "image_digest": "sha256:example",
  "sbom_generated": true,
  "critical_cves": 0,
  "high_cves": 0,
  "prohibited_licenses": 0,
  "image_signature_valid": true,
  "base_image_approved": true
}
```

## 8. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Critical CVE found | Vulnerable library or OS package | Upgrade dependency/base image and rebuild |
| High CVE found | Dependency risk above policy threshold | Upgrade, patch, remove, or request security exception |
| SBOM missing | Tool failure or misconfiguration | Fix scanner command and rerun |
| License violation | GPL/AGPL/SSPL or incompatible license detected | Replace dependency or obtain legal approval |
| Unsigned image | Cosign signing step failed | Re-sign image and verify signature |
| Mutable tag risk | Image pushed as `latest` only | Retag using SemVer and Git SHA |

## 9. Retry and Skip Logic

* Vulnerability scanning and SBOM generation are not skippable for production.
* Scanner network failures may be retried up to two times.
* Vulnerability database update failures may use the last successful DB for non-production, but production requires fresh scan evidence.
* Exceptions must include CVE ID, exploitability analysis, compensating controls, owner, expiry, and CISO approval.

## 10. SLA Target

| Metric | Target |
|---|---:|
| Dependency scan duration | < 8 minutes |
| Container scan duration | < 8 minutes |
| SBOM generation duration | < 3 minutes |
| Total stage duration | < 15 minutes with parallel scans |

## 11. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI third-party risk | SBOM and dependency inventory |
| RBI vulnerability management | CVE scanning and remediation records |
| PCI-DSS vulnerability control | Dependency and application component security |
| Supply-chain security | Image signing, provenance, and immutable digests |

## 12. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
