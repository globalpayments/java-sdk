package com.global.api.terminals.upa.responses;

import com.global.api.entities.enums.CardType;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.JsonDoc;

public class UpaTransactionResponse extends TerminalResponse {
    public UpaTransactionResponse(JsonDoc responseData) {
        JsonDoc host = responseData.get("host");
        approvalCode = host.getString("approvalCode");
        avsResponseCode = host.getString("AvsResultCode");
        avsResponseText = host.getString("AvsResultText");
        balanceAmount = host.getDecimal("availableBalance");      
        responseCode = host.getString("responseCode");
        responseText = host.getString("responseText");
        terminalRefNumber = host.getString("tranNo");
        token = host.getString("tokenValue");
        transactionId = host.getString("referenceNumber");
        transactionAmount = host.getDecimal("totalAmount");

        JsonDoc payment = responseData.get("payment");
        if (payment != null) { // is null on decline response
            cardHolderName = payment.getString("cardHolderName");

            if (payment.getString("cardType") != null) {
                switch (payment.getString("cardType")) {
                    case "VISA":
                        cardType = CardType.VISA;
                        break;
                    case "MASTERCARD":
                        cardType = CardType.MC;
                        break;
                    case "DISCOVER":
                        cardType = CardType.DISC;
                        break;
                    case "AMERICAN EXPRESS":
                        cardType = CardType.AMEX;
                        break;
                    default:
                        break;
                }
            }

            entryMethod = payment.getString("cardAcquisition");
            maskedCardNumber = payment.getString("maskedPan");
            paymentType = payment.getString("cardGroup");    
        }    
    }
}
