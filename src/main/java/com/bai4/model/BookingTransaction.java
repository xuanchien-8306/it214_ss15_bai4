package com.bai4.model;

public class BookingTransaction {

    private String bookingId;
    private String concertCode;
    private String customerId;
    private String customerEmail;
    private int ticketQuantity;
    private long amount;
    private BookingState currentState;

    public BookingTransaction() {
    }

    public BookingTransaction(String bookingId, String concertCode, String customerId,
                              String customerEmail, int ticketQuantity, long amount) {
        this.bookingId = bookingId;
        this.concertCode = concertCode;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.ticketQuantity = ticketQuantity;
        this.amount = amount;
        this.currentState = BookingState.INITIATED;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getConcertCode() { return concertCode; }
    public void setConcertCode(String concertCode) { this.concertCode = concertCode; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public int getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(int ticketQuantity) { this.ticketQuantity = ticketQuantity; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public BookingState getCurrentState() { return currentState; }
    public void setCurrentState(BookingState currentState) { this.currentState = currentState; }
}
