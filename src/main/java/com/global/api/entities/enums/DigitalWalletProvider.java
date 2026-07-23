package com.global.api.entities.enums;

/**
 * Enumeration of digital wallet providers supported by the Global Payments API.
 * This enum defines the various digital payment methods that can be used for transactions.
 */
public enum DigitalWalletProvider implements IStringConstant {
    /** Apple Pay digital wallet provider */
    APPLEPAY("applepay"),       // GP API contract uses lowercase
    
    /** Google Pay digital wallet provider */
    GOOGLEPAY("googlepay"),     // GP API contract uses lowercase
    
    /** Click to Pay digital wallet provider */
    CLICK_TO_PAY("CLICK_TO_PAY"); // GP API contract uses uppercase

    private final String value;

    /**
     * Constructs a DigitalWalletProvider with the specified string value.
     *
     * @param value the string representation of the digital wallet provider
     */
    DigitalWalletProvider(String value) {
        this.value = value;
    }

    /**
     * Returns the byte array representation of the digital wallet provider value.
     *
     * @return the bytes of the provider's string value
     */
    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    /**
     * Returns the string value of the digital wallet provider.
     *
     * @return the string representation of this provider
     */
    @Override
    public String getValue() {
        return value;
    }
}
