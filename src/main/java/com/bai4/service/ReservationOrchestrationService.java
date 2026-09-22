package com.bai4.service;

import com.bai4.model.BookingTransaction;

/**
 * ReservationOrchestrationService - helper cho State Machine điều phối seat reservation.
 */
public interface ReservationOrchestrationService {

    /**
     * Thực hiện giữ ghế.
     *
     * @param transaction     booking transaction
     * @param reservationMode SUCCESS | FAIL
     * @return true nếu giữ ghế thành công
     */
    boolean executeReservation(BookingTransaction transaction, String reservationMode);
}
