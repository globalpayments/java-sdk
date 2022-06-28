package com.global.api.network.entities.gnap;

import com.global.api.network.enums.gnap.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GnapPosDetails {
    /** Position 1 */
    private CardHolderPresentIndicator cardHolderPresentIndicator=CardHolderPresentIndicator.CardHolderIsPresent;
    /** Position 2 */
    private CardPresentIndicator cardPresentIndicator=CardPresentIndicator.CardPresent;
    /** Position 3 */
    private TransactionStatusIndicator transactionStatusIndicator=TransactionStatusIndicator.NormalRequest;
    /** Position 4 */
    private TransactionSecurityIndicator transactionSecurityIndicator=TransactionSecurityIndicator.NoSecurityConcern;
    /** Position 5 */
    private CardholderActivatedTerminalIndicator cardholderActivatedTerminalIndicator=CardholderActivatedTerminalIndicator.NotACATTransaction;
    /** Position 6 */
    private CardHolderIDMethod cardholderIDMethod=CardHolderIDMethod.Unknown;

    public String getValue()
    {
        StringBuilder sb=new StringBuilder();
        sb.append(cardHolderPresentIndicator.getValue());
        sb.append(cardPresentIndicator.getValue());
        sb.append(transactionStatusIndicator.getValue());
        sb.append(transactionSecurityIndicator.getValue());
        sb.append(cardholderActivatedTerminalIndicator.getValue());
        sb.append(cardholderIDMethod.getValue());
        return sb.toString();
    }
}
