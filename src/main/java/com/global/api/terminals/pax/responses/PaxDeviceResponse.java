package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.enums.PaxExtData;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.StringUtils;

public class PaxDeviceResponse extends PaxBaseResponse {
    private String referenceNumber;
    private String hostReferenceNumber;

    public String getReferenceNumber() {
        return referenceNumber;
    }
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    public String getHostReferenceNumber() {
        return hostReferenceNumber;
    }
    public void setHostReferenceNumber(String hostReferenceNumber) {
        this.hostReferenceNumber = hostReferenceNumber;
    }

    public PaxDeviceResponse(byte[] buffer, PaxMsgId... messageIds) throws MessageException {
        super(buffer, messageIds);
    }

    protected void mapResponse() {
        // host response
        if(hostResponse != null) {
            setResponseCode(normalizeResponse(hostResponse.getHostResponseCode()));
            setResponseText(hostResponse.getHostResponseMessage());
            setApprovalCode(hostResponse.getAuthCode());
            setHostReferenceNumber(hostResponse.getHostReferenceNumber());
            setAuthorizationCode(hostResponse.getAuthCode());
        }

        // amount
        if(amountResponse != null) {
            setTransactionAmount(amountResponse.getApprovedAmount());
            setAmountDue(amountResponse.getAmountDue());
            setTipAmount(amountResponse.getTipAmount());
            setCashBackAmount(amountResponse.getCashBackAmount());
            setBalanceAmount(amountResponse.getBalance1());
        }

        // account
        if(accountResponse != null) {
            setMaskedCardNumber(StringUtils.padLeft(accountResponse.getAccountNumber(), 16, '*'));
            setEntryMethod(accountResponse.getEntryMode().toString());
            setExpirationDate(accountResponse.getExpireDate());
            setPaymentType(accountResponse.getCardType().replace('_', ' '));
            setCardHolderName(accountResponse.getCardHolder());
            setCvvResponseCode(accountResponse.getCvdApprovalCode());
            setCvvResponseText(accountResponse.getCvdMessage());
            setCardPresent(accountResponse.isCardPresent());
        }

        // trace data
        if(traceResponse != null) {
            setTerminalRefNumber(traceResponse.getTransactionNumber());
            setReferenceNumber(traceResponse.getReferenceNumber());
        }

        // avs
        if(avsResponse != null) {
            setAvsResponseCode(avsResponse.getAvsResponseCode());
            setAvsResponseText(avsResponse.getAvsResponseMessage());
        }

        // commercial data
        if(commercialResponse != null) {
            setTaxExempt(commercialResponse.isTaxExempt());
            setTaxExemptId(commercialResponse.getTaxExemptId());
        }

        // ext data
        if(extDataResponse != null){
            setTransactionId(extDataResponse.get(PaxExtData.HOST_REFERENCE_NUMBER));
            setToken(extDataResponse.get(PaxExtData.TOKEN));
            setCardBIN(extDataResponse.get(PaxExtData.CARD_BIN));
            setSignatureStatus(extDataResponse.get(PaxExtData.SIGNATURE_STATUS));

            // emv stuff
            setApplicationPreferredName(extDataResponse.get(PaxExtData.APPLICATION_PREFERRED_NAME));
            setApplicationLabel(extDataResponse.get(PaxExtData.APPLICATION_LABEL));
            setApplicationId(extDataResponse.get(PaxExtData.APPLICATION_ID));
            setApplicationCryptogramType(ApplicationCryptogramType.TC);
            setApplicationCryptogram(extDataResponse.get(PaxExtData.TRANSACTION_CERTIFICATE));
            setCustomerVerificationMethod(extDataResponse.get(PaxExtData.CUSTOMER_VERIFICATION_METHOD));
            setTerminalVerificationResults(extDataResponse.get(PaxExtData.TERMINAL_VERIFICATION_RESULTS));
        }

        setTransactionType(xlateTransactionType(Integer.parseInt(getTransactionType())));
    }

    protected String xlateTransactionType(int transType) {
        switch(transType) {
            case 0:
                return "MENU";
            case 1:
                return "SALE";
            case 2:
                return "RETURN";
            case 3:
                return "AUTH";
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
                return "VOID";
            default:
                return "UNKNOWN";
        }
    }

    private String normalizeResponse(String input) {
        if(input.equals("0") || input.equals("85"))
            return "00";
        return input;
    }
}
