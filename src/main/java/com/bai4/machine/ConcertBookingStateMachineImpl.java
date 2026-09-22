package com.bai4.machine;

import com.bai4.listener.StateChangeListener;
import com.bai4.model.BookingEvent;
import com.bai4.model.BookingState;
import com.bai4.model.BookingTransaction;
import com.bai4.service.PaymentOrchestrationService;
import com.bai4.service.PaymentProcessingService;
import com.bai4.service.ReservationOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcertBookingStateMachineImpl - Orchestrator điều phối toàn bộ booking workflow.
 *
 * Chỉ chịu trách nhiệm:
 *  - Quản lý state
 *  - Xử lý event
 *  - Chuyển state
 *  - Gọi PaymentOrchestrationService (có retry)
 *  - Gọi ReservationOrchestrationService
 *  - Compensation
 *  - Log (qua StateChangeListener)
 *
 * Không chứa business logic thanh toán hay giữ ghế.
 */
@Service
public class ConcertBookingStateMachineImpl implements ConcertBookingStateMachine {

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ReservationOrchestrationService reservationOrchestrationService;
    private final PaymentProcessingService paymentProcessingService;
    private final StateChangeListener stateChangeListener;

    // In-memory store: bookingId -> BookingTransaction
    private final ConcurrentHashMap<String, BookingTransaction> store = new ConcurrentHashMap<>();

    public ConcertBookingStateMachineImpl(PaymentOrchestrationService paymentOrchestrationService,
                                          ReservationOrchestrationService reservationOrchestrationService,
                                          PaymentProcessingService paymentProcessingService,
                                          StateChangeListener stateChangeListener) {
        this.paymentOrchestrationService = paymentOrchestrationService;
        this.reservationOrchestrationService = reservationOrchestrationService;
        this.paymentProcessingService = paymentProcessingService;
        this.stateChangeListener = stateChangeListener;
    }

    @Override
    public void processBooking(BookingTransaction transaction, String paymentMode, String reservationMode) {
        // Lưu transaction vào store
        store.put(transaction.getBookingId(), transaction);

        // ---- BƯỚC 1: INITIATED -> PROCESS_PAYMENT -> PAYMENT_PENDING ----
        transition(transaction, BookingEvent.PROCESS_PAYMENT, BookingState.PAYMENT_PENDING);

        // ---- BƯỚC 2: Thực hiện payment với retry ----
        boolean paymentSuccess = paymentOrchestrationService.executePaymentWithRetry(transaction, paymentMode);

        if (paymentSuccess) {
            // PAYMENT_PENDING -> PAYMENT_SUCCESS -> PAYMENT_COMPLETED
            transition(transaction, BookingEvent.PAYMENT_SUCCESS, BookingState.PAYMENT_COMPLETED);

            // ---- BƯỚC 3: PAYMENT_COMPLETED -> RESERVE_SEATS -> SEAT_RESERVING ----
            transition(transaction, BookingEvent.RESERVE_SEATS, BookingState.SEAT_RESERVING);

            // ---- BƯỚC 4: Thực hiện giữ ghế ----
            boolean reservationSuccess = reservationOrchestrationService.executeReservation(transaction, reservationMode);

            if (reservationSuccess) {
                // SEAT_RESERVING -> RESERVATION_SUCCESS -> BOOKING_CONFIRMED
                transition(transaction, BookingEvent.RESERVATION_SUCCESS, BookingState.BOOKING_CONFIRMED);
                stateChangeListener.onFinalState(BookingState.BOOKING_CONFIRMED, transaction.getBookingId());
            } else {
                // SEAT_RESERVING -> RESERVATION_FAILED -> CANCELLED
                transition(transaction, BookingEvent.RESERVATION_FAILED, BookingState.CANCELLED);
                stateChangeListener.onFinalState(BookingState.CANCELLED, transaction.getBookingId());

                // Compensation: hoàn tiền vì payment đã thành công nhưng reservation thất bại
                triggerCompensation(transaction, true);
            }
        } else {
            // PAYMENT_PENDING -> PAYMENT_FAILED -> CANCELLED
            transition(transaction, BookingEvent.PAYMENT_FAILED, BookingState.CANCELLED);
            stateChangeListener.onFinalState(BookingState.CANCELLED, transaction.getBookingId());

            // Compensation: không hoàn tiền vì payment chưa thành công
            triggerCompensation(transaction, false);
        }
    }

    /**
     * Thực hiện transition state và notify listener.
     */
    private void transition(BookingTransaction transaction, BookingEvent event, BookingState newState) {
        BookingState oldState = transaction.getCurrentState();
        transaction.setCurrentState(newState);
        stateChangeListener.onStateChange(oldState, event, newState, transaction.getBookingId());
    }

    /**
     * Thực hiện compensation.
     * needRefund=true chỉ khi payment đã thành công (để hoàn tiền).
     */
    private void triggerCompensation(BookingTransaction transaction, boolean needRefund) {
        System.out.println("[Orchestrator] Compensation triggered for booking: " + transaction.getBookingId());
        if (needRefund) {
            paymentProcessingService.refund(transaction);
        }
    }

    @Override
    public BookingState getState(String bookingId) {
        BookingTransaction tx = store.get(bookingId);
        return (tx != null) ? tx.getCurrentState() : null;
    }

    @Override
    public BookingTransaction getTransaction(String bookingId) {
        return store.get(bookingId);
    }
}
