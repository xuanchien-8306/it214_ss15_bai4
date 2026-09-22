package com.bai4.service;

import com.bai4.model.BookingTransaction;

/**
 * SeatReservationService xử lý giữ ghế concert.
 */
public interface SeatReservationService {

    /**
     * Thực hiện giữ ghế cho booking.
     *
     * @param transaction     booking transaction
     * @param reservationMode chế độ: SUCCESS | FAIL
     * @return true nếu giữ ghế thành công
     */
    boolean reserveSeats(BookingTransaction transaction, String reservationMode);
}
