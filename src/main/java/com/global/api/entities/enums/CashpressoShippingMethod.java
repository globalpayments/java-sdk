package com.global.api.entities.enums;

/**
 * Represents the available shipping methods for Cashpresso transactions.
 */
public enum CashpressoShippingMethod {

    /** The order is delivered to the customer's address. */
    DELIVERY,

    /** The customer picks up the order at a designated location. */
    PICKUP,

    /** The order is delivered to a pickup box / parcel locker. */
    PICKUP_BOX,

    /** The order is delivered to a post office for customer pickup. */
    POSTOFFICE
}

