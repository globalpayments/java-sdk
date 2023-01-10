package com.global.api.network.entities.nts;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.paymentMethods.Credit;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import lombok.NonNull;

import java.util.Objects;

public class NtsRequestObjectFactory {

    private NtsRequestObjectFactory() {
        throw new IllegalStateException("NtsRequestObjectFactory.class");
    }

    public static MessageWriter getNtsRequestObject(@NonNull NtsObjectParam ntsObjectParam) throws ApiException {
        MessageWriter request = null;

        PaymentMethodType paymentMethodType = ntsObjectParam.getNtsBuilder().getPaymentMethod() != null ?
                ntsObjectParam.getNtsBuilder().getPaymentMethod().getPaymentMethodType() : null;
        TransactionType transactionType = ntsObjectParam.getNtsBuilder().getTransactionType();
        INtsRequestMessage ntsRequestMessage = null;

        // Setting the request header.
        request = INtsRequestMessage.prepareHeader(ntsObjectParam);
        ntsObjectParam.setNtsRequest(request);
        boolean isVisaReadyLink=false;
        if(ntsObjectParam.getNtsBuilder().getPaymentMethod() instanceof Credit){
            Credit card=(Credit) ntsObjectParam.getNtsBuilder().getPaymentMethod();
            isVisaReadyLink=card.getCardType().equals("VisaReadyLink");
        } else if(ntsObjectParam.getNtsBuilder().getPaymentMethod() instanceof TransactionReference){
            TransactionReference reference = (TransactionReference)  ntsObjectParam.getNtsBuilder().getPaymentMethod();
            if( reference.getOriginalPaymentMethod() instanceof Credit){
                Credit card=(Credit) reference.getOriginalPaymentMethod() ;
                isVisaReadyLink=card.getCardType().equals("VisaReadyLink");
            }
        }

        NtsUtils.log("--------------------- REQUEST PAYLOAD ---------------------");
        if ( (Objects.equals(paymentMethodType, PaymentMethodType.Debit) || isVisaReadyLink)
                && (transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Void)
                || transactionType.equals(TransactionType.PreAuthCompletion)
                || transactionType.equals(TransactionType.Refund)
                || transactionType.equals(TransactionType.Reversal)
                || transactionType.equals(TransactionType.AddValue)
                || transactionType.equals(TransactionType.LoadReversal)) ) {
            ntsRequestMessage = new NtsDebitRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (paymentMethodType != null
                && (isCreditSalesAuthBalanceTransaction(transactionType, paymentMethodType)
                || NtsUtils.isSVSGiftCard(transactionType, paymentMethodType))) {
            ntsRequestMessage = new NtsAuthSaleCreditRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (paymentMethodType != null
                    && isDataCollectTransaction(transactionType, paymentMethodType)) {
            ntsRequestMessage = new NtsDataCollectRequestBuilder();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (transactionType.equals(TransactionType.BatchClose)) {
            ntsRequestMessage = new NtsRequestsToBalanceRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (transactionType.equals(TransactionType.Mail)) {
            ntsRequestMessage = new NtsMailRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if ((transactionType.equals(TransactionType.Void) || transactionType.equals(TransactionType.Reversal))
                && Objects.equals(paymentMethodType, PaymentMethodType.Credit)) {
            ntsRequestMessage = new NtsVoidReversalRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (transactionType.equals(TransactionType.MagnumPDL)
                        || transactionType.equals(TransactionType.EmvPdl)
                        || transactionType.equals(TransactionType.PDL)
        ) {
            ntsRequestMessage = new NtsPDLRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (NtsUtils.isEBTCard(transactionType, paymentMethodType)) {
            ntsRequestMessage = new NtsEbtRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (transactionType.equals(TransactionType.UtilityMessage)) {
            ntsRequestMessage = new NtsUtilityMessageRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        } else if (transactionType.equals(TransactionType.PosSiteConfiguration)) {
            ntsRequestMessage = new NtsPOSSiteConfigurationRequest();
            request = ntsRequestMessage.setNtsRequestMessage(ntsObjectParam);
            return request;
        }
        return request;
    }

    private static boolean isCreditSalesAuthBalanceTransaction(TransactionType transactionType, PaymentMethodType paymentMethodType) {
        return (transactionType.equals(TransactionType.Sale)
                || transactionType.equals(TransactionType.Auth)
                || transactionType.equals(TransactionType.Balance))
                && paymentMethodType.equals(PaymentMethodType.Credit);
    }

    private static boolean isDataCollectTransaction(TransactionType transactionType, PaymentMethodType paymentMethodType){
        return  (
                    transactionType.equals(TransactionType.DataCollect)
                    ||transactionType.equals(TransactionType.Capture)
                )
                &&
                (
                        Objects.equals(paymentMethodType, PaymentMethodType.Debit)
                        || Objects.equals(paymentMethodType, PaymentMethodType.Credit)
                        || Objects.equals(paymentMethodType, PaymentMethodType.Gift)
                        || Objects.equals(paymentMethodType, PaymentMethodType.EBT)
                );
    }
}
