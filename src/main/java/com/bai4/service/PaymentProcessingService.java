package com.bai4.service;

import com.bai4.model.BookingTransaction;

/**
 * PaymentProcessingService xử lý thanh toán và hoàn tiền.
 */
public interface PaymentProcessingService {

    /**
     * Thực hiện thanh toán cho booking.
     * Ném TimeoutException nếu paymentMode=TIMEOUT.
     * Trả về false nếu paymentMode=FAIL.
     * Trả về true nếu paymentMode=SUCCESS.
     *
     * @param transaction booking transaction
     * @param paymentMode chế độ: SUCCESS | TIMEOUT | FAIL
     * @return true nếu thanh toán thành công
     * @throws Exception nếu timeout
     */
    boolean processPayment(BookingTransaction transaction, String paymentMode) throws Exception;

    /**
     * Hoàn tiền khi cần compensation.
     *
     * @param transaction booking transaction
     */
    void refund(BookingTransaction transaction);
}
