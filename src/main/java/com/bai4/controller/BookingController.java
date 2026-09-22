package com.bai4.controller;

import com.bai4.machine.ConcertBookingStateMachine;
import com.bai4.model.BookingState;
import com.bai4.model.BookingTransaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST Controller xử lý booking API.
 *
 * POST /api/bookings?paymentMode=SUCCESS&reservationMode=SUCCESS
 * GET  /api/bookings/{bookingId}
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final ConcertBookingStateMachine stateMachine;

    public BookingController(ConcertBookingStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * Tạo và xử lý booking.
     *
     * @param paymentMode     SUCCESS | TIMEOUT | FAIL (default: SUCCESS)
     * @param reservationMode SUCCESS | FAIL (default: SUCCESS)
     * @param transaction     Booking request body
     * @return bookingId và state cuối
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createBooking(
            @RequestParam(defaultValue = "SUCCESS") String paymentMode,
            @RequestParam(defaultValue = "SUCCESS") String reservationMode,
            @RequestBody BookingTransaction transaction) {

        // Đảm bảo state ban đầu là INITIATED
        transaction.setCurrentState(BookingState.INITIATED);

        // Chạy state machine
        stateMachine.processBooking(transaction, paymentMode, reservationMode);

        // Trả về kết quả
        BookingState finalState = stateMachine.getState(transaction.getBookingId());
        Map<String, String> response = new LinkedHashMap<>();
        response.put("bookingId", transaction.getBookingId());
        response.put("state", finalState != null ? finalState.name() : "UNKNOWN");

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy state hiện tại của booking theo bookingId.
     *
     * @param bookingId ID booking
     * @return bookingId và state
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Map<String, String>> getBooking(@PathVariable String bookingId) {
        BookingTransaction transaction = stateMachine.getTransaction(bookingId);

        if (transaction == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Booking not found: " + bookingId);
            return ResponseEntity.status(404).body(error);
        }

        Map<String, String> response = new LinkedHashMap<>();
        response.put("bookingId", bookingId);
        response.put("state", transaction.getCurrentState().name());
        return ResponseEntity.ok(response);
    }
}
