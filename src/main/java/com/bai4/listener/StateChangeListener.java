package com.bai4.listener;

import com.bai4.model.BookingEvent;
import com.bai4.model.BookingState;
import org.springframework.stereotype.Component;

/**
 * StateChangeListener chịu trách nhiệm log mỗi lần transition xảy ra.
 * Không chứa business logic.
 */
@Component
public class StateChangeListener {

    public void onStateChange(BookingState oldState, BookingEvent event, BookingState newState, String bookingId) {
        System.out.printf("[Orchestrator] State: %s -> Event: %s -> New State: %s%n",
                oldState, event, newState);
    }

    public void onFinalState(BookingState finalState, String bookingId) {
        System.out.printf("[Orchestrator] Final State: %s for booking %s%n",
                finalState, bookingId);
    }
}
