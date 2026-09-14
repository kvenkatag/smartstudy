package org.example.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UsageTrackingService {
    private final AtomicLong totalVisits = new AtomicLong();
    private final AtomicLong totalAttempts = new AtomicLong();
    private final Set<String> seenSessions = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();

    public void registerVisit(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        if (seenSessions.add(sessionId)) {
            totalVisits.incrementAndGet();
        }
        activeSessions.put(sessionId, Instant.now().getEpochSecond());
        pruneExpiredSessions();
    }

    public void registerAttempt(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return;
        totalAttempts.incrementAndGet();
        activeSessions.put(sessionId, Instant.now().getEpochSecond());
        pruneExpiredSessions();
    }

    public UsageStats getStats() {
        pruneExpiredSessions();
        return new UsageStats(totalVisits.get(), totalAttempts.get(), activeSessions.size());
    }

    private void pruneExpiredSessions() {
        long now = Instant.now().getEpochSecond();
        activeSessions.entrySet().removeIf(entry -> now - entry.getValue() > 300);
    }

    public record UsageStats(long totalVisitors, long totalAttempts, long activeUsers) {
    }
}
