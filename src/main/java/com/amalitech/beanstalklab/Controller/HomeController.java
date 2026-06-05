package com.amalitech.beanstalklab.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple REST controller for the Beanstalk lab.
 * Returns deployment info including a version number that we'll bump
 * to prove redeployments work via the CI/CD pipeline.
 */
@RestController
public class HomeController {
    private static final String APP_VERSION = "5.0.0"; //after adding dynamo db

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("Message", "Hello from Elastic Beanstalk - auto-deployed!");
        response.put("version", APP_VERSION);
        response.put("status", "deployment successful");
        return response;
    }

    // Health check endpoint - Beanstalk pings this to verify the app is alive
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        return response;
    }

}
