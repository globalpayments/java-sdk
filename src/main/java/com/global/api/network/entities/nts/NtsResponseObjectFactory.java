package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NtsHostResponseCode;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
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
        boolean emvFlag = !StringUtils.isNullOrEmpty(builder.getTagData());

        PaymentMethodType paymentMethodType = (builder.getPaymentMethod() != null)
                ? builder.getPaymentMethod().getPaymentMethodType() : null;
        TransactionType transactionType = builder.getTransactionType();

        NtsResponse ntsResponse = new NtsResponse();

        // Setting NTS response message header.
        NtsResponseMessageHeader ntsResponseMessageHeader = INtsResponseMessage.getHeader(mr.readBytes(RESPONSE_HEADER));

        if ((transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.Void)
                || transactionType.equals(TransactionType.PreAuthCompletion)
                || transactionType.equals(TransactionType.Refund)
                || transactionType.equals(TransactionType.Reversal))
                && (paymentMethodType != null && paymentMethodType.equals(PaymentMethodType.Debit))) {
            ntsResponseMessage = new NtsDebitResponse();

            if (ntsResponseMessageHeader.getNtsNetworkMessageHeader().getResponseCode().equals(NtsHostResponseCode.Success)) {
                ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
            }
        } else if (paymentMethodType != null
                && (isCreditAuthBalanceTransaction(transactionType, paymentMethodType)
                || NtsUtils.isSVSGiftCard(transactionType, paymentMethodType))) {
            ntsResponseMessage = new NtsAuthCreditResponseMapper();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        } else if (transactionType.equals(TransactionType.DataCollect)) {
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
