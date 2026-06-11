# Stage 01: Source Control and Pipeline Trigger

## 1. Purpose

This stage ensures that every change entering the NovaPay delivery pipeline is traceable, reviewed, signed, and linked to an approved work item or emergency change record before any build, scan, or deployment activity begins.

The stage replaces NovaPay's previous manual SSH deployment habit with a controlled entry point for regulated banking software delivery.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| Source control | GitHub Enterprise | Current enterprise SaaS or GHES supported version |
| CI trigger | GitHub Actions | Reusable workflow with protected environments |
| Commit signing | GPG or SSH signing | Required for all commits to protected branches |
| Branch protection | GitHub branch rules | Required on `main`, `release/*`, and `hotfix/*` |
| Change traceability | Jira / GitHub Issues / ServiceNow | Every PR mapped to issue/change ticket |
| Secret scanning | GitHub secret scanning or Gitleaks | Mandatory on PR and push |

## 3. Inputs

* Developer pull request from short-lived feature branch.
* Signed commit history.
* Linked work item or change ticket.
* Pull request description with testing notes and risk classification.
* CODEOWNERS-based reviewer assignment.
* Optional emergency hotfix declaration for urgent production fixes.

## 4. Execution Flow

1. Developer creates a branch from `main` using naming convention:
   * `feature/<ticket-id>-<description>`
   * `fix/<ticket-id>-<description>`
   * `hotfix/<incident-id>-<description>`
2. Developer commits code using signed commits only.
3. Pull request is opened against `main`.
4. GitHub branch protection validates:
   * signed commits,
   * no direct push to protected branches,
   * required reviewers,
   * linked ticket,
   * required status checks,
   * no unresolved conversations.
5. Path filters decide which application, infrastructure, policy, or documentation workflows must run.
6. Pipeline run ID is generated and attached to the change record.
7. Build and security jobs start only after source-control gates pass.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Protected branches | `main`, `release/*`, `hotfix/*` |
| Direct push to main | Blocked |
| Commit signing | Required |
| Pull request reviews | Minimum 2 approvals for production-impacting code |
| CODEOWNERS | Required for application, infrastructure, security, and database paths |
| Status checks | Required before merge |
| Force push | Disabled |
| Branch deletion | Restricted after merge for audit-sensitive branches |
| PR linked ticket | Required |
| Emergency hotfix | Allowed only through `hotfix/*` branch and still must pass mandatory security gates |

## 6. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Signed commits | 100% of commits signed and verified | Block PR merge |
| Branch protection | All required protection rules pass | Block PR merge |
| Reviewer approval | Required reviewers approve | Block PR merge |
| Segregation of duties | Author cannot be sole approver or production deploy approver | Block promotion |
| Secret scan | 0 confirmed secrets | Block pipeline and create security ticket |
| Change ticket | Valid linked ticket exists | Block PR merge |
| Path trigger | Correct workflow selected | Fail fast and notify platform team |

## 7. Outputs and Evidence

* Git commit SHA.
* Signed commit verification status.
* Pull request review record.
* Change ticket reference.
* GitHub Actions run ID.
* Branch protection status.
* Secret scan result.
* CODEOWNERS review evidence.
* Initial audit event in the deployment evidence pack.

Example audit event:

```json
{
  "stage": "source_control_trigger",
  "status": "passed",
  "commit_sha": "abc123",
  "branch": "feature/NP-102-payment-validation",
  "pr_number": 245,
  "pipeline_run_id": "run-123456",
  "change_ticket": "CHG-2026-0012",
  "signed_commits_verified": true,
  "reviewer_count": 2,
  "segregation_of_duties_validated": true
}
```

## 8. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Unsigned commit | Developer did not sign commit | Re-sign or squash with signed commit |
| Missing approval | Reviewer not assigned or not approved | Assign CODEOWNER and request review |
| Secret detected | API key, password, token, or certificate committed | Revoke secret, rotate credential, remove from history, rerun scan |
| Missing ticket | PR not linked to change or issue record | Add valid ticket ID and risk classification |
| Direct push attempt | Protected branch bypass attempt | Reject push and raise audit event |
| Hotfix bypass risk | Emergency change attempts to skip gates | Route through expedited hotfix pipeline without disabling gates |

## 9. Retry and Skip Logic

* This stage is not skippable for any change.
* Failed checks may be retried after remediation.
* Secret scan failures require credential rotation evidence before rerun acceptance.
* Emergency hotfixes may skip non-critical manual waiting periods, but cannot skip signed commits, security scanning, or production approvals.

## 10. SLA Target

| Metric | Target |
|---|---:|
| Source-control validation time | < 2 minutes |
| Secret scan feedback | < 3 minutes |
| PR trigger to build start | < 1 minute after gate pass |

## 11. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI change management | Every change has PR, approval, rollback reference, and ticket linkage |
| RBI audit trail | Commit, PR, and pipeline evidence retained immutably |
| Segregation of duties | Developers cannot self-approve production changes |
| PCI-DSS change control | Peer review and traceability before software promotion |

## 12. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
