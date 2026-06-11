# NovaPay Lite Walkthrough

I have successfully completed the implementation of **NovaPay Lite**, a sample Java 21 and Spring Boot 3.x microservice application tailored for testing CI/CD pipeline capabilities.

## What was Accomplished

### 1. Spring Boot Foundation
- Initialized a Gradle project configured for Java 21 and Spring Boot 3.3.x.
- Included necessary starters: Web, Data JPA, Data Redis, AMQP (RabbitMQ), Actuator, Validation.
- Added dependencies for PostgreSQL, Flyway, OpenAPI, Micrometer Prometheus, and Logstash Logback Encoder.
- Configured Testcontainers for PostgreSQL, Redis, and RabbitMQ to enable robust integration testing.

### 2. Domain & Business Logic
- Implemented `Customer`, `Account`, `Payment`, and `AuditEvent` entities.
- Created robust Services mapping to your requirements:
  - `CustomerService`: Demonstrates schema migration handling logic.
  - `AccountService`: Handles basic account creation.
  - `PaymentService`: Implements Redis for idempotency, deducts balances, creates audit logs, and asynchronously publishes a `payment.initiated` event to RabbitMQ.
  - `AuditService`: Manages system-wide auditing logs.

### 3. REST APIs
- Developed REST Controllers with validation handling and proper documentation.
- Included `X-Correlation-ID` tracking mapped through standard Spring MVC filters and logged into MDC.
- Added a custom `SystemController` providing:
  - `/api/version` returning commit, timestamp, and deployment color details.
  - `/api/health` demonstrating simulate latency and simulated failure specifically for CI/CD rollback validation.

### 4. Database Migrations
- Created Flyway migrations illustrating the **Expand-Contract Pattern**:
  - `V1__initial_schema.sql`
  - `V2__expand_add_encrypted_email.sql`
  - `V3__migrate_backfill_encrypted_email.sql`
  - `V4__contract_drop_plain_email.sql` (Commented as per requirements).

### 5. Deployment Configurations
- `Dockerfile` structured as a multi-stage builder.
- `docker-compose.yml` defining the application and dependent services (PostgreSQL, Redis, RabbitMQ) with health checks.
- Comprehensive Kubernetes manifests in the `k8s/` folder.
- Fully parameterized Helm chart in `pipeline/pipeline/helm/novapay-lite/` supporting canary configuration and colors.

### 6. Observability & Policy
- Defined Prometheus alerts inside `observability/prometheus-alerts.yaml`.
- Exported a simulated Grafana dashboard (`observability/grafana-dashboard.json`).
- Provided sample compliance policies:
  - `policies/kyverno/best-practices.yaml` enforcing non-root, limits, labels.
  - `policies/opa/policies.rego` demonstrating the same with Rego format.

### 7. CI/CD Artifacts
- `.github/workflows/ci.yml` outlining test execution, SonarQube SAST, Trivy/OWASP scanning, docker build, and artifact uploads.
- Helper scripts deployed under `scripts/`:
  - `run-tests.sh`
  - `build-image.sh`
  - `run-zap-baseline.sh`
  - `simulate-canary-failure.sh`
  - `verify-deployment.sh`

### 8. Documentation
- Wrote a detailed `README.md` guiding users through running the application, utilizing CI features, and exploring deployments.

## Next Steps

You can begin testing the environment locally using Docker Compose:

```bash
cd d:\novapay-lite
docker-compose up -d
```

Review the [README.md](file:///d:/novapay-lite/README.md) for full instructions on simulating the pipeline steps.
