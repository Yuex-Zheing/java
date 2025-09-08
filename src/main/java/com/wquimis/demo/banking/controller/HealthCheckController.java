package com.wquimis.demo.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    private final JdbcTemplate jdbcTemplate;

    public HealthCheckController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> details = new HashMap<>();

        try {
            // Verificar la conexión a la base de datos
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "UP");
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("error", e.getMessage());
        }

        // Información adicional
        details.put("api", "UP");
        details.put("timestamp", System.currentTimeMillis());

        health.put("status", details.containsValue("DOWN") ? "DOWN" : "UP");
        health.put("details", details);

        return ResponseEntity.ok(health);
    }
}
