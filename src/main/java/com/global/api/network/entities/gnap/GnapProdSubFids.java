package com.global.api.network.entities.gnap;

import com.global.api.network.enums.gnap.CardIdentifierPresenceIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GnapProdSubFids {
    /** SUBFID B - CVV2/CVC2 Value */
    private String cvv2Value;
    /** SUBFID E - POS Entry Mode */
    private String posEntryMode;
    /** SUBFID H - Card Verification Digits Presence indicator and Result */
    private CardIdentifierPresenceIndicator cardPresenceIndicator;
    /** SUBFID I - Transaction Currency Code (TCC) */
    private String transactionCurrencyCode;
    /** SUBFID L - XID/Transaction Token */
    private String transactionToken;
    /** SUBFID O - EMV Request Data */
    private String emvRequestData;
    /** SUBFID P - EMV Additional Request Data */
    private String emvAdditionalRequestData;
    /** SUBFID Q - EMV Response Data */
    private String emvResponseData;
    /** SUBFID R - EMV Additional Response Data */
    private String emvAdditionalResponseData;
    /** SUBFID S - UnionPay Online PIN DUKPT KSN */
    private String unionPayOnlinePINDUKPTKSN;
    /** SUBFID W - CAVV/AAV Result Code */
    private String cavvResultCode;
    /** SUBFID X - Point of Service Data */
    private GnapPosDetails pointOfServiceData;
}
