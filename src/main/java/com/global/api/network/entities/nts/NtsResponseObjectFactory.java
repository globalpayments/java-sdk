package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NtsHostResponseCode;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.paymentMethods.Credit;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.MessageReader;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.NonNull;

public class NtsResponseObjectFactory {

    static final Integer RESPONSE_HEADER = 52;

    public static <T extends TransactionBuilder<Transaction>> NtsResponse getNtsResponseObject(byte[] buffer, @NonNull T builder) {

        INtsResponseMessage ntsResponseMessage = null;
        MessageReader mr = new MessageReader(buffer);
        // EMV flag requirements.
        boolean emvFlag = (!StringUtils.isNullOrEmpty(builder.getTagData()) && (!builder.getTagData().contains("\\99\\FALLBACK")));

        PaymentMethodType paymentMethodType = (builder.getPaymentMethod() != null)
                ? builder.getPaymentMethod().getPaymentMethodType() : null;
        TransactionType transactionType = builder.getTransactionType();

        boolean isVisaReadyLink=false;
        if(builder.getPaymentMethod() instanceof Credit){
            Credit card=(Credit) builder.getPaymentMethod();
            isVisaReadyLink=card.getCardType().equals("VisaReadyLink");
        } else if(builder.getPaymentMethod() instanceof TransactionReference){
            TransactionReference reference = (TransactionReference)  builder.getPaymentMethod();
            if( reference.getOriginalPaymentMethod() instanceof Credit){
                Credit card=(Credit) reference.getOriginalPaymentMethod() ;
                isVisaReadyLink=card.getCardType().equals("VisaReadyLink");
            }
        }

        NtsResponse ntsResponse = new NtsResponse();

        // Setting NTS response message header.
        NtsResponseMessageHeader ntsResponseMessageHeader = INtsResponseMessage.getHeader(mr.readBytes(RESPONSE_HEADER));

        NtsUtils.log("--------------------- RESPONSE PAYLOAD ---------------------");
        if ((( paymentMethodType != null && paymentMethodType.equals(PaymentMethodType.Debit) ) || isVisaReadyLink)
                && (transactionType.equals(TransactionType.Auth) || transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.Void)
                || transactionType.equals(TransactionType.PreAuthCompletion)
                || transactionType.equals(TransactionType.Refund)
                || transactionType.equals(TransactionType.Reversal)
                || transactionType.equals(TransactionType.AddValue)
                || transactionType.equals(TransactionType.LoadReversal))) {
            ntsResponseMessage = new NtsDebitResponse();

            if (ntsResponseMessageHeader.getNtsNetworkMessageHeader().getResponseCode().equals(NtsHostResponseCode.Success)) {
                ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
            }
        } else if (paymentMethodType != null
                && (isCreditAuthBalanceTransaction(transactionType, paymentMethodType)
                || NtsUtils.isSVSGiftCard(transactionType, paymentMethodType))) {
            ntsResponseMessage = new NtsAuthCreditResponseMapper();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.DataCollect)
                || transactionType.equals(TransactionType.Capture)) {
            ntsResponseMessage = new NtsDataCollectResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.BatchClose)) {
            ntsResponseMessage = new NtsRequestToBalanceResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.Mail)) {
            ntsResponseMessage = new NtsMailResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.Sale)
                && paymentMethodType != null && paymentMethodType.equals(PaymentMethodType.Credit)) {
            ntsResponseMessage = new NtsSaleCreditResponseMapper();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if ((transactionType.equals(TransactionType.Void) || transactionType.equals(TransactionType.Reversal))
                && paymentMethodType != null && paymentMethodType.equals(PaymentMethodType.Credit)) {
            ntsResponseMessage = new NtsVoidReversalResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.MagnumPDL)) {
            ntsResponseMessage = new NtsPDLResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.PDL)) {
            ntsResponseMessage = new NtsPDLResponseData();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.EmvPdl)) {
            if (builder instanceof AuthorizationBuilder) {
                AuthorizationBuilder authorizationBuilder = (AuthorizationBuilder) builder;
                ntsResponseMessage = new NtsEMVPDLResponse(authorizationBuilder.getNtsPDLData().isEMVPDLParameterVersion002());
                ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
            }
        } else if (NtsUtils.isEBTCard(transactionType, paymentMethodType)) {
            ntsResponseMessage = new NtsEbtResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.UtilityMessage)) {
            ntsResponseMessage = new NtsUtilityMessageResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        }else if (transactionType.equals(TransactionType.RequestPendingMessages)) {
            ntsResponseMessage = new NtsRequestPendingMessagesResponse();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        }
        ntsResponse.setNtsResponseMessageHeader(ntsResponseMessageHeader);
        ntsResponse.setNtsResponseMessage(ntsResponseMessage);
        return ntsResponse;
    }

    private static Boolean isCreditAuthBalanceTransaction(TransactionType transactionType, PaymentMethodType paymentMethodType) {
        return (transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Balance))
                && paymentMethodType.equals(PaymentMethodType.Credit);
    }
}
