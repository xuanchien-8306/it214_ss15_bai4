package com.bai4.service;

import com.bai4.model.BookingTransaction;
import org.springframework.stereotype.Service;

/**
 * Triển khai ReservationOrchestrationService.
 */
@Service
public class ReservationOrchestrationServiceImpl implements ReservationOrchestrationService {

    private final SeatReservationService seatReservationService;

    public ReservationOrchestrationServiceImpl(SeatReservationService seatReservationService) {
        this.seatReservationService = seatReservationService;
    }

    @Override
    public boolean executeReservation(BookingTransaction transaction, String reservationMode) {
        return seatReservationService.reserveSeats(transaction, reservationMode);
    }
}
