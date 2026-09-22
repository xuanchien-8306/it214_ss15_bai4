package com.bai4.service;

import com.bai4.model.BookingTransaction;
import org.springframework.stereotype.Service;

/**
 * Triển khai PaymentProcessingService.
 * Hỗ trợ mô phỏng lỗi qua paymentMode: SUCCESS | TIMEOUT | FAIL
 */
@Service
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    @Override
    public boolean processPayment(BookingTransaction transaction, String paymentMode) throws Exception {
        String mode = (paymentMode == null || paymentMode.isBlank()) ? "SUCCESS" : paymentMode.toUpperCase();

        switch (mode) {
            case "TIMEOUT":
                throw new java.util.concurrent.TimeoutException(
                        "Payment service timeout for booking: " + transaction.getBookingId());
            case "FAIL":
                System.out.println("[PaymentService] Payment failed for booking: " + transaction.getBookingId());
                return false;
            default: // SUCCESS
                System.out.println("[PaymentService] Payment successful for booking: " + transaction.getBookingId());
                return true;
        }
    }

    @Override
    public void refund(BookingTransaction transaction) {
        System.out.println("[PaymentService] Refund payment for booking: " + transaction.getBookingId());
    }
}
