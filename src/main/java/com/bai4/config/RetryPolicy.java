package com.bai4.config;

import org.springframework.stereotype.Component;

/**
 * RetryPolicy định nghĩa cấu hình retry cho các activity trong State Machine.
 */
@Component
public class RetryPolicy {

    private final int maxAttempts = 3;
    private final long delayMs = 2000L;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getDelayMs() {
        return delayMs;
    }
}
