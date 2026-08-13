package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
	private final HealthCheckRepository healthCheckRepository;
	public HealthController(HealthCheckRepository healthCheckRepository) {
		this.healthCheckRepository = healthCheckRepository;
	}
	@GetMapping
	public String health() {
		long count = healthCheckRepository.count();
		return "OK, rows: " + count;
	}
}
