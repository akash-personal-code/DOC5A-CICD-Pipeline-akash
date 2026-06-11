#!/usr/bin/env bash
set -euo pipefail
echo "Running Gradle tests, JaCoCo report, and coverage gate"
chmod +x ./gradlew
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification --no-daemon
