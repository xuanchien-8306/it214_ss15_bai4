package com.bai4.service;

import com.bai4.model.BookingTransaction;

/**
 * PaymentOrchestrationService - helper cho State Machine điều phối payment.
 * Được gọi bởi ConcertBookingStateMachine.
 */
public interface PaymentOrchestrationService {

    /**
     * Thực hiện payment với retry policy.
     *
     * @param transaction booking transaction
     * @param paymentMode SUCCESS | TIMEOUT | FAIL
     * @return true nếu payment thành công
     */
    boolean executePaymentWithRetry(BookingTransaction transaction, String paymentMode);
}
