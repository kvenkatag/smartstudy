package org.example.controller;

import org.example.service.UsageTrackingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TrackingController {

    private final UsageTrackingService usageTrackingService;

    public TrackingController(UsageTrackingService usageTrackingService) {
        this.usageTrackingService = usageTrackingService;
    }

    @GetMapping("/stats")
    public UsageTrackingService.UsageStats getStats() {
        return usageTrackingService.getStats();
    }

    @PostMapping("/track-visit")
    public UsageTrackingService.UsageStats trackVisit(@RequestParam(required = false) String sessionId) {
        usageTrackingService.registerVisit(sessionId);
        return usageTrackingService.getStats();
    }

    @PostMapping("/track-attempt")
    public UsageTrackingService.UsageStats trackAttempt(@RequestParam(required = false) String sessionId) {
        usageTrackingService.registerAttempt(sessionId);
        return usageTrackingService.getStats();
    }
}
