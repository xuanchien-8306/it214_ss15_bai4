package com.bai4.service;

import com.bai4.model.BookingTransaction;
import org.springframework.stereotype.Service;

/**
 * Triển khai SeatReservationService.
 * Hỗ trợ mô phỏng qua reservationMode: SUCCESS | FAIL
 */
@Service
public class SeatReservationServiceImpl implements SeatReservationService {

    @Override
    public boolean reserveSeats(BookingTransaction transaction, String reservationMode) {
        String mode = (reservationMode == null || reservationMode.isBlank()) ? "SUCCESS" : reservationMode.toUpperCase();

        if ("FAIL".equals(mode)) {
            System.out.println("[ConcertReservationService] Seat reservation failed for booking: " + transaction.getBookingId());
            return false;
        }

        // SUCCESS
        System.out.println("[ConcertReservationService] Seats reserved for booking: " + transaction.getBookingId());
        return true;
    }
}
