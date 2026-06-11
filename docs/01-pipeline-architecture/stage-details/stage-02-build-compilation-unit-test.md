# Stage 02: Build, Compilation, Unit Test, and Artifact Creation

## 1. Purpose

This stage converts reviewed source code into a reproducible, versioned, tested, and signed build artifact. It ensures that NovaPay never deploys uncompiled, untested, or non-traceable binaries to any environment.

## 2. Tools and Versions

| Capability | Tool | Target Version / Standard |
|---|---|---|
| Java runtime | Java | 21 LTS |
| Application framework | Spring Boot | 3.x |
| Build tool | Gradle | 8.x |
| Unit testing | JUnit 5 + Mockito | Current stable |
| Coverage | JaCoCo | Current Gradle-compatible version |
| Container build | Docker Buildx | Current stable |
| Artifact registry | JFrog Artifactory or Azure Container Registry | Private registry |
| Image signing | Cosign / Sigstore | Current stable |

## 3. Inputs

* Approved pull request or protected branch push.
* Source code from GitHub Enterprise.
* Dependency lock files.
* Dockerfile and build configuration.
* Gradle build scripts.
* Application version metadata.

## 4. Execution Flow

1. Checkout source at immutable commit SHA.
2. Validate Gradle wrapper integrity.
3. Restore dependency and Docker layer caches.
4. Run formatting and compile checks.
5. Execute unit tests.
6. Generate JaCoCo coverage report.
7. Build Spring Boot artifact.
8. Build multi-stage Docker image.
9. Tag image with SemVer and Git SHA.
10. Push artifact to private registry only after tests pass.
11. Sign image and store provenance metadata.

## 5. Configuration Parameters

| Parameter | Required Setting |
|---|---|
| Java version | 21 |
| Build command | `./gradlew clean build` |
| Test command | `./gradlew test jacocoTestReport` |
| Line coverage threshold | >= 80% |
| Branch coverage threshold | >= 70% |
| Artifact version | `MAJOR.MINOR.PATCH+gitsha.runid` |
| Docker tag | SemVer tag and Git SHA tag; never `latest` in production |
| Docker build | Multi-stage build with non-root runtime user |
| Registry access | CI service identity only |
| Image signing | Required before promotion beyond dev |

## 6. Quality Gates

| Gate | Pass Criteria | Failure Action |
|---|---|---|
| Compilation | 100% successful compile | Block pipeline |
| Unit tests | 100% pass rate | Block pipeline and attach test report |
| Line coverage | >= 80% | Block pipeline unless approved non-prod exception |
| Branch coverage | >= 70% | Block pipeline unless documented exception exists |
| Dependency lock | Lock file present and unchanged without approval | Block PR |
| Docker build | Image builds successfully | Block pipeline |
| Image tag | SemVer + Git SHA present | Block registry push |
| Image signing | Cosign signature generated | Block deployment promotion |

## 7. Outputs and Evidence

* Compiled JAR.
* Unit test report.
* JaCoCo coverage report.
* Container image digest.
* Image tags.
* Image signature.
* Build log.
* Provenance metadata.

Example artifact metadata:

```json
{
  "stage": "build_compilation_unit_test",
  "application": "novapay-lite",
  "version": "1.4.2+abc123.run456",
  "commit_sha": "abc123",
  "image_digest": "sha256:example",
  "unit_tests_passed": true,
  "line_coverage_percent": 82.5,
  "branch_coverage_percent": 73.1,
  "image_signed": true
}
```

## 8. Failure Modes and Remediation

| Failure Mode | Cause | Remediation |
|---|---|---|
| Build failure | Compilation error or missing dependency | Fix code or build file and rerun |
| Test failure | Broken logic or unstable test | Fix code; quarantine only with tech lead approval |
| Coverage failure | Insufficient tests | Add unit tests for missing branches and error paths |
| Docker build failure | Invalid Dockerfile or missing artifact | Fix Dockerfile and verify local build |
| Registry push failure | Expired credentials or registry outage | Rotate CI credential or retry after registry recovery |
| Image signing failure | Cosign key/identity issue | Repair signing identity and rerun signing step |

## 9. Retry and Skip Logic

* Compilation and unit tests are never skippable.
* Failed builds may be retried after code or configuration changes.
* Registry push can be retried up to two times for transient network errors.
* Coverage thresholds can only be temporarily waived for non-production through a time-bound exception; production promotion still requires formal approval.

## 10. SLA Target

| Metric | Target |
|---|---:|
| Compile and unit test duration | < 12 minutes |
| Docker build duration | < 8 minutes with cache |
| Total stage duration | < 20 minutes |

## 11. Compliance Mapping

| Requirement Area | Control Mapping |
|---|---|
| RBI change testing | Automated test evidence for every change |
| RBI auditability | Artifact digest and build logs retained |
| PCI-DSS secure development | Tests and peer-reviewed build artifacts before deployment |
| Supply-chain security | Signed artifact and reproducible versioning |

## 12. AI Assistance Disclosure

This stage specification was AI-assisted and reviewed for alignment with the NovaPay assessment scenario. Final configuration values must be validated against the actual repository and evaluator instructions.
