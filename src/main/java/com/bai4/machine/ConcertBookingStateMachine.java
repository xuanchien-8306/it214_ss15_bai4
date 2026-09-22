package com.bai4.machine;

import com.bai4.model.BookingState;
import com.bai4.model.BookingTransaction;

/**
 * ConcertBookingStateMachine - interface của orchestrator.
 * Chịu trách nhiệm quản lý state, xử lý event, chuyển state,
 * gọi service, retry và compensation.
 */
public interface ConcertBookingStateMachine {

    /**
     * Khởi tạo và chạy toàn bộ booking workflow.
     *
     * @param transaction     booking transaction (state=INITIATED)
     * @param paymentMode     SUCCESS | TIMEOUT | FAIL
     * @param reservationMode SUCCESS | FAIL
     */
    void processBooking(BookingTransaction transaction, String paymentMode, String reservationMode);

    /**
     * Lấy trạng thái hiện tại theo bookingId.
     *
     * @param bookingId ID booking
     * @return state hiện tại hoặc null nếu không tìm thấy
     */
    BookingState getState(String bookingId);

    /**
     * Lấy toàn bộ transaction theo bookingId.
     *
     * @param bookingId ID booking
     * @return BookingTransaction hoặc null
     */
    BookingTransaction getTransaction(String bookingId);
}
