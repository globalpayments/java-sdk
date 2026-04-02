package com.global.api.entities.enums;

/**
 * Enum representing the different funding modes available.
 * <p>
 * Funding modes:
 * <ul>
 *   <li>MERCHANT_FUNDED: Funded by the merchant</li>
 *   <li>CONSUMER_FUNDED: Funded by the consumer</li>
 *   <li>HYBRID_FUNDED: Funded by both merchant and consumer</li>
 *   <li>BILATERAL: Bilateral funding arrangement</li>
 *   <li>ANY: Any funding mode</li>
 * </ul>
 */
public enum FundingMode {
    MERCHANT_FUNDED("MERCHANT_FUNDED"),
    CONSUMER_FUNDED("CONSUMER_FUNDED"),
    HYBRID_FUNDED("HYBRID_FUNDED"),
    BILATERAL("BILATERAL"),
    ANY("ANY");

    private final String value;

    FundingMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
