package com.global.api.terminals.heartSIP.responses;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.utils.Element;
import com.global.api.utils.StringUtils;

public class SipDeviceResponse extends SipBaseResponse implements IDeviceResponse {
    public SipDeviceResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);

        approvalCode = authorizationCode = response.getString("ApprovalCode");
        amountDue = StringUtils.toAmount(response.getString("BalanceDueAmount"));
        avsResponseCode = response.getString("AVS");
        avsResponseText = response.getString("AVSRsltText", "AVSResultText");
        transactionType = response.getString("CardGroup");
        balanceAmount = StringUtils.toAmount(response.getString("AvailableBalance", "BalanceReturned"));
        cardHolderName = response.getString("CardHolderName");
        cvvResponseCode = response.getString("CVV");
        cvvResponseText = response.getString("CVVRsltText", "CVVResultText");
        entryMethod = response.getString("CardAcquisition");
        maskedCardNumber = response.getString("MaskedPAN");
        paymentType = response.getString("CardType");
        // PinVerified
        // QPSQualified
        responseCode = normalizeResponse(response.getString("ResponseCode", "Result"));
        transactionId = response.getString("ResponseId", "TransactionId");
        responseText = response.getString("ResponseText", "ResultText");
        signatureStatus = response.getString("SignatureLine");
        // StoreAndForward
        // TipAdjustAllowed
        terminalRefNumber = response.getString("ReferenceNumber");
        transactionAmount = StringUtils.toAmount(response.getString("AuthorizedAmount"));

        // EMV
        applicationId = response.getString("EMV_AID");
        applicationLabel = response.getString("EMV_ApplicationName");
        applicationCryptogram = response.getString("EMV_Cryptogram");
        if(response.has("EMV_CryptogramType")) {
            String appCryptType = response.getString("EMV_CryptogramType");
            applicationCryptogramType = appCryptType.equals("TC") ? ApplicationCryptogramType.TC : ApplicationCryptogramType.ARQC;
        }
        customerVerificationMethod = response.getString("EMV_TSI");
        terminalVerificationResults = response.getString("EMV_TVR");
    }
}
