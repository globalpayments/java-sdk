package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NtsHostResponseCode;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.Credit;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.MessageReader;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.NonNull;

public class NtsResponseObjectFactory {

    static final Integer RESPONSE_HEADER = 52;

    private static final String WEX_FALLBACK="FALLBACK";
    private static final String FALLBACK_99="\\99\\FALLBACK2";

    public static <T extends TransactionBuilder<Transaction>> NtsResponse getNtsResponseObject(IDeviceMessage request, byte[] buffer, @NonNull T builder) throws GatewayException {

        INtsResponseMessage ntsResponseMessage = null;
        MessageReader mr = new MessageReader(buffer);
        // EMV flag requirements.
        boolean emvFlag = (!StringUtils.isNullOrEmpty(builder.getTagData()) && (!builder.getTagData().contains(FALLBACK_99) && (!builder.getTagData().contains(WEX_FALLBACK))));

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
        String str = new String(buffer);

        String ntsHostResponseCode = str.substring(6,8);

        if(ntsHostResponseCode.equals(NtsHostResponseCode.TerminalDisabled.getValue())){
            throw new GatewayException(
                    String.format("Unexpected response from gateway: %s %s",  NtsHostResponseCode.getValueByString(ntsHostResponseCode),
                            ntsHostResponseCode),
                    ntsHostResponseCode,
                    NtsHostResponseCode.getValueByString(ntsHostResponseCode));
        }else {
            MessageReader mr2 = new MessageReader(request.getSendBuffer());
            mr2.readString(15);
            String requestStr = new String(mr2.readRemainingBytes());
            requestStr = requestStr.trim();

            MessageReader mr3 = new MessageReader(buffer);
            mr3.readString(14);
            String responseStr = new String(mr3.readRemainingBytes());
            responseStr = responseStr.trim();

            if (responseStr.contains(requestStr)) {
                throw new GatewayException(
                        String.format("Unexpected response from gateway due to request echoed in response : %s %s", NtsHostResponseCode.getValueByString(ntsHostResponseCode),
                                ntsHostResponseCode),
                        ntsHostResponseCode,
                        NtsHostResponseCode.getValueByString(ntsHostResponseCode));
            }
        }

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
                || NtsUtils.isSVSGiftCard(transactionType, paymentMethodType)) && builder.getEcommerceInfo()==null) {
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
                && (builder.getEcommerceInfo()==null)
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
        }else if((paymentMethodType != null && isCreditAuthBalanceTransaction(transactionType,paymentMethodType))
                && (builder.getEcommerceInfo()!=null)) {
            ntsResponseMessage = new NtsEcommerceAuthResponseMapper();
            ntsResponseMessage = ntsResponseMessage.setNtsResponseMessage(mr.readRemainingBytes(), emvFlag);
        }else if(transactionType.equals(TransactionType.Sale)
                && (builder.getEcommerceInfo()!=null)) {
            ntsResponseMessage = new NtsEcommerceSaleResponseMapper();
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
