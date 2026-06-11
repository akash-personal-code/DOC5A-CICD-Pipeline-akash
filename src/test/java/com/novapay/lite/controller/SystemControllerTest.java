package com.novapay.lite.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemControllerTest {

    @Test
    void getVersion_returnsDeploymentMetadata() {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "deploymentColor", "green");

        var response = controller.getVersion();
        @SuppressWarnings("unchecked")
        Map<String, String> body = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NovaPay Lite", body.get("app"));
        assertEquals("green", body.get("deployment_color"));
        assertTrue(body.containsKey("build_timestamp"));
    }

    @Test
    void getHealth_returnsOkWhenNoFailureIsSimulated() throws Exception {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "simulateFailure", false);
        ReflectionTestUtils.setField(controller, "simulateLatencyMs", 0L);

        var response = controller.getHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void getHealth_returnsServiceUnavailableWhenFailureIsSimulated() throws Exception {
        SystemController controller = new SystemController();
        ReflectionTestUtils.setField(controller, "simulateFailure", true);
        ReflectionTestUtils.setField(controller, "simulateLatencyMs", 0L);

        var response = controller.getHealth();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SIMULATED_FAILURE", response.getBody());
    }
}
