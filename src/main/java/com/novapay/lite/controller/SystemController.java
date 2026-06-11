package com.novapay.lite.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    @Value("${novapay.deployment.color:blue}")
    private String deploymentColor;

    @Value("${novapay.simulate.failure:false}")
    private boolean simulateFailure;

    @Value("${novapay.simulate.latency-ms:0}")
    private long simulateLatencyMs;

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of(
                "app", "NovaPay Lite",
                "version", "0.0.1",
                "git_commit", System.getenv().getOrDefault("GIT_COMMIT", "unknown"),
                "build_timestamp", System.getenv().getOrDefault("BUILD_TIMESTAMP", Instant.now().toString()),
                "deployment_color", deploymentColor
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() throws InterruptedException {
        if (simulateLatencyMs > 0) {
            Thread.sleep(simulateLatencyMs);
        }

        if (simulateFailure) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("SIMULATED_FAILURE");
        }

        return ResponseEntity.ok("OK");
    }
}
