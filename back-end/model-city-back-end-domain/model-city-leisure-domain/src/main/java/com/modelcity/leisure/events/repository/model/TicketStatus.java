package com.modelcity.leisure.events.repository.model;

/** Lifecycle status of an event ticket. */
public enum TicketStatus {
    /** Ticket created with a Checkout Session; waiting for Stripe confirmation. */
    PENDING,
    /** Ticket confirmed by Stripe webhook (checkout.session.completed). */
    PAID,
    /** Ticket created and paid in a single step (free events). */
    PURCHASED,
    /** Ticket cancelled (e.g. Stripe payment failed or session expired). */
    CANCELLED,
    /** Ticket refunded. */
    REFUNDED
}
