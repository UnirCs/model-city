package com.modelcity.mobility.reservations.repository.model;

/** Lifecycle status of a street parking reservation payment. */
public enum ReservationStatus {
    /** Reservation created; awaiting payment confirmation from Stripe. */
    PENDING,
    /** Payment confirmed by Stripe webhook. */
    PAID,
    /** Payment failed or reservation cancelled. */
    CANCELLED
}
