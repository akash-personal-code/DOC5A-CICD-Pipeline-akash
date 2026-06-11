# Stage 07: Policy and Compliance Gates

## 1. Purpose

This stage enforces NovaPay's regulatory, security, Kubernetes, infrastructure, and segregation-of-duties controls before deployment. It converts RBI, PCI-DSS, and internal banking controls into automated pass/fail decisions.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| Policy-as-code | OPA / Conftest | Current stable |
| Kubernetes admission policy | Kyverno | Current stable |
| IaC scanning | Checkov / tfsec | Current stable |
| Kubernetes manifests | Helm + kubeconform/kubeval | Current stable |
| Image signature verification | Cosign | Current stable |
| Change approval | GitHub environments / ServiceNow / Jira | Required |
| Evidence storage | Immutable object storage | WORM/retention policy preferred |

## 3. Inputs

* Helm charts and rendered Kubernetes manifests.
* Terraform modules and plans.
* OPA Rego policies.
* Kyverno policies.
* Image digest and signature.
* SBOM and vulnerability scan output.
* SAST and DAST results.
* Change ticket and approval metadata.

## 4. Execution Flow

1. Render Helm templates for target environment.
2. Validate YAML and Kubernetes schemas.
3. Scan Terraform and Kubernetes manifests using IaC scanner.
4. Run OPA/Conftest policy checks.
5. Validate Kyverno policies for production admission requirements.
6. Verify image signature and digest pinning.
7. Verify evidence from previous gates is complete.
8. Validate segregation of duties and approval workflow.
9. Map results to RBI and PCI-DSS controls.
10. Store signed evidence pack.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Privileged containers | Forbidden |
| Root containers | Forbidden unless approved exception exists |
| Resource requests/limits | Required for CPU and memory |
| Liveness/readiness probes | Required for production services |
| TLS | TLS 1.2 minimum; TLS 1.3 preferred |
| Secrets in manifests | Forbidden |
| Image tag | Digest pinned; no `latest` |
| Image signature | Valid signature required |
| Network policies | Required for production namespaces |
| Production approval | Release Manager + SRE Lead dual approval |
| Evidence completeness | 100% before production deployment |

## 6. Mandatory Compliance Gates

| Gate | Tool | Threshold | Failure Action | Exception Process |
|---|---|---|---|---|
| SAST evidence gate | SonarQube evidence | 0 Critical; high findings within approved threshold | Block deployment | CISO approval with expiry |
| DAST evidence gate | OWASP ZAP evidence | 0 Critical/High for OWASP Top 10/payment paths | Block deployment | Risk acceptance + TRC visibility |
| Dependency/container gate | Trivy/Grype/SBOM | 0 Critical CVEs; SBOM present | Block deployment | 72-hour remediation plan or CISO exception |
| License gate | FOSSA/ScanCode/Trivy | No GPL/AGPL/SSPL without approval | Block deployment | Legal sign-off |
| Kubernetes policy gate | OPA/Kyverno | All production policies pass | Reject deployment | Dual approval override only for non-critical controls |
| Infrastructure gate | Checkov/tfsec | No Critical IaC findings | Block PR/promotion | Tech lead + security approval |
| Segregation gate | GitHub/ServiceNow metadata | Author != production approver | Block production promotion | No exception for standard changes |
| Evidence gate | Audit pack validation | 100% required evidence present | Block production promotion | Release Manager remediation |

## 7. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| OPA policy evaluation | All required policies pass | Block deployment |
| Kyverno policy readiness | Policies installed and enforcing | Block production deployment |
| IaC scan | No Critical findings | Block PR/promotion |
| Approval validation | Required approvers present and separate from author | Block production promotion |
| Evidence pack | Complete and immutable | Block final approval |
| Regulatory mapping | RBI/PCI-DSS controls mapped | Block TRC approval package |

## 8. Outputs and Evidence

* OPA/Conftest report.
* Kyverno policy report.
* Checkov/tfsec report.
* Rendered Kubernetes manifests.
* Approval record.
* Regulatory control mapping table.
* Evidence completeness report.
* Exception register, if applicable.

Example evidence:

```json
{
  "stage": "policy_compliance_gates",
  "opa_passed": true,
  "kyverno_passed": true,
  "iac_critical_findings": 0,
  "image_signature_valid": true,
  "segregation_of_duties_valid": true,
  "evidence_pack_completeness_percent": 100,
  "production_approved": true
}
```

## 9. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Privileged container | Manifest sets privileged security context | Remove privileged mode or redesign workload |
| Missing resource limits | Helm values omit CPU/memory constraints | Add resource requests and limits |
| Unsigned image | Signature missing or invalid | Re-sign image and verify digest |
| Missing approval | Required role did not approve | Obtain Release Manager and SRE Lead approvals |
| Same person approves and authors | Segregation violation | Assign independent approver |
| Evidence incomplete | Previous stage report missing | Regenerate evidence and rebuild pack |
| Policy syntax error | Rego/Kyverno issue | Fix policy and validate locally |

## 10. Retry and Skip Logic

* Policy and compliance gates are not skippable for production.
* Policy syntax failures may be retried after correction.
* Infrastructure scanner availability failures may be retried twice; production waits for evidence.
* Exceptions must include risk owner, compensating control, expiry date, and approval authority.
* Segregation-of-duties violations are not waived for standard production changes.

## 11. SLA Target

| Metric | Target |
|---|---:|
| Manifest rendering and validation | < 3 minutes |
| OPA/Kyverno checks | < 5 minutes |
| IaC scan | < 8 minutes |
| Evidence pack validation | < 3 minutes |
| Total stage duration | < 15 minutes |

## 12. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI change management | Approval, test evidence, rollback evidence, and controlled promotion |
| RBI segregation of duties | Independent production approval and deployment credentials |
| RBI audit trails | Immutable evidence pack and policy reports |
| RBI incident/business continuity | Rollback and monitoring checks required before deployment |
| PCI-DSS secure change | Automated evidence for security gates and approval workflow |
| PCI-DSS audit logging | Deployment and approval records retained |

## 13. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
