package com.bai4.service;

import com.bai4.config.RetryPolicy;
import com.bai4.model.BookingTransaction;
import org.springframework.stereotype.Service;

/**
 * Triển khai PaymentOrchestrationService với retry logic.
 */
@Service
public class PaymentOrchestrationServiceImpl implements PaymentOrchestrationService {

    private final PaymentProcessingService paymentProcessingService;
    private final RetryPolicy retryPolicy;

    public PaymentOrchestrationServiceImpl(PaymentProcessingService paymentProcessingService,
                                           RetryPolicy retryPolicy) {
        this.paymentProcessingService = paymentProcessingService;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public boolean executePaymentWithRetry(BookingTransaction transaction, String paymentMode) {
        int maxAttempts = retryPolicy.getMaxAttempts();
        long delayMs = retryPolicy.getDelayMs();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.printf("[Orchestrator] RetryPolicy: Activity 'processPayment' - Attempt %d/%d%n",
                    attempt, maxAttempts);
            try {
                boolean result = paymentProcessingService.processPayment(transaction, paymentMode);
                return result;
            } catch (java.util.concurrent.TimeoutException e) {
                System.out.printf("[Orchestrator] Payment timeout on attempt %d: %s%n", attempt, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            } catch (Exception e) {
                System.out.printf("[Orchestrator] Payment error on attempt %d: %s%n", attempt, e.getMessage());
                return false;
            }
        }
        // All attempts exhausted
        return false;
    }
}
