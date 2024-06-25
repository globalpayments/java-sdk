package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class NtsDebitRequest implements INtsRequestMessage {
    private BigDecimal settlementAmount;
    private TransactionCode transactionCode;
    private BigDecimal cashBackAmount;
    private NTSEntryMethod entryMethod;

    StringBuilder maskedRequest = new StringBuilder("");
    @Getter
    @Setter
    String accNo;
    @Getter @Setter
    String expDate;
    @Getter @Setter
    String trackData;

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) {

        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        AcceptorConfig acceptorConfig = ntsObjectParam.getNtsAcceptorConfig();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        String userData;
        boolean isEnableLogging = ntsObjectParam.isNtsEnableLogging();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();

        boolean isReadyLinkEMV = false;
        boolean isReadyLink = false;


        //message body
        ManagementBuilder manageBuilder;
        AuthorizationBuilder authorizationBuilder;

        if (builder instanceof AuthorizationBuilder) {
            authorizationBuilder = (AuthorizationBuilder) builder;
            cashBackAmount = authorizationBuilder.getCashBackAmount();
        } else if (builder instanceof ManagementBuilder) {
            manageBuilder = (ManagementBuilder) builder;
            settlementAmount = manageBuilder.getSettlementAmount();
        }
        transactionCode = NtsUtils.getTransactionCodeForTransaction(builder, cashBackAmount, settlementAmount);

        if (paymentMethod instanceof TransactionReference) {
            paymentMethod = ((TransactionReference) builder.getPaymentMethod()).getOriginalPaymentMethod();
            // Purchase_Return_Reversal && Withdrawal_Reversal
            TransactionCode originalTransactionCode = ((TransactionReference) builder.getPaymentMethod()).getOriginalTransactionCode();
            if (originalTransactionCode == TransactionCode.Withdrawal && builder.getTransactionType().equals(TransactionType.Reversal)) {
                transactionCode = TransactionCode.WithdrawalReversal;
            } else if (originalTransactionCode == TransactionCode.PurchaseReturn && builder.getTransactionType().equals(TransactionType.Reversal)) {
                transactionCode = TransactionCode.PurchaseReturnReversal;
            }
        }
        if (paymentMethod instanceof Credit) {
            Credit card = (Credit) paymentMethod;
            isReadyLink = card.getCardType().equals("VisaReadyLink");
        }
        TransactionType transactionType = builder.getTransactionType();
        TransactionModifier modifier = builder.getTransactionModifier();

        if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)) {
            ITrackData trackData = (ITrackData) paymentMethod;
            if (trackData.getEntryMethod() != null) {
                entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                request.addRange(entryMethod.getValue(), 1);
                NtsUtils.log("Entry Method", entryMethod);
            }
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Credit)) {
            if (paymentMethod instanceof ITrackData) {
                ITrackData trackData = (ITrackData) paymentMethod;
                if (trackData.getEntryMethod() != null) {
                    entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                    request.addRange(entryMethod.getValue(), 1);
                    NtsUtils.log("Entry Method", entryMethod);
                }
            } else {
                request.addRange(NTSEntryMethod.ManualAttended.getValue(), 1);
                NtsUtils.log("Entry Method", NTSEntryMethod.ManualAttended);
            }
        }
        // Card Type
        if (cardType != null) {
            NtsUtils.log("CardType : ", cardType);
            request.addRange(cardType.getValue(), 2);
        }
        if (transactionCode != null) {
            NtsUtils.log("Transaction Code", transactionCode);
            request.addRange(transactionCode.getValue(), 2);
        }

        if (acceptorConfig.getAddress() != null) {
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getName(), 20, ' '), 20);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getStreetAddress1(), 18, ' '), 18);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getCity(), 16, ' '), 16);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getState(), 2, ' '), 2);

            NtsUtils.log("Address", acceptorConfig.getAddress().toString());
        }
        if (paymentMethod instanceof Debit && paymentMethod instanceof ITrackData) {
            if (!NtsUtils.isNoTrackEntryMethods(entryMethod) && !(transactionCode.equals(TransactionCode.PreAuthCompletion)
                    || transactionCode.equals(TransactionCode.PreAuthCancelation)
                    || transactionCode.equals(TransactionCode.PurchaseReversal)
                    || transactionCode.equals(TransactionCode.PurchaseCashBackReversal)
                    || transactionCode.equals(TransactionCode.PurchaseReturnReversal))) {
                ITrackData trackData = (ITrackData) paymentMethod;
                NtsUtils.log("TrackData 2", StringUtils.maskTrackData(trackData.getValue(),trackData));
                request.addRange(trackData.getValue(), 40);
                this.setTrackData(trackData.getValue());
                StringUtils.setTrackData(trackData.getValue());
            } else {
                ITrackData trackData = (ITrackData) paymentMethod;  //Without Track Data
                String accNumber = trackData.getPan();
                String expYear = trackData.getExpiry().substring(0, 2);
                String expMonth = trackData.getExpiry().substring(2, 4);

                String shortExpiry = expMonth + expYear;
                String panWithExpiry = accNumber + "=" + shortExpiry;
                String maskedPanWithExpiry = StringUtils.maskAccountNumber(accNumber) + "*" + StringUtils.padLeft("",4,'*');
                this.setAccNo(accNumber);
                StringUtils.setAccNo(accNumber);
                this.setExpDate(shortExpiry);
                StringUtils.setExpDate(shortExpiry);
                request.addRange(StringUtils.padRight(panWithExpiry, 40, ' '), 40);
                NtsUtils.log("PAN With Expiry", StringUtils.padRight(maskedPanWithExpiry, 40, ' '));
                request.addRange(StringUtils.padLeft(" ", 1, ' '), 1);
                request.addRange(StringUtils.padLeft(" ", 16, ' '), 16);
            }
        } else if (paymentMethod instanceof Credit && isReadyLink) {
            if (paymentMethod instanceof CreditCardData) {
                CreditCardData card = (CreditCardData) paymentMethod;
                String trackFormat = card.getNumber() + "=" + card.getExpYear() + card.getExpMonth();
                int len = card.getExpMonth().toString().length() + card.getExpYear().toString().length();
                String maskedTrackFormat = StringUtils.maskAccountNumber(card.getNumber()) + "*" + StringUtils.padLeft("",len,'*');
                NtsUtils.log("TrackData 2", maskedTrackFormat);
                request.addRange(trackFormat, 40);
                this.setTrackData(trackFormat);
                StringUtils.setAccNo(card.getNumber());
                String expDate = card.getExpYear().toString() + card.getExpMonth().toString();
                StringUtils.setExpDate(expDate);
                StringUtils.setTrackData(trackFormat);
            } else {
                ITrackData trackData = (ITrackData) paymentMethod;
                NtsUtils.log("TrackData 2", StringUtils.maskTrackData(trackData.getValue(),trackData));
                this.setTrackData(trackData.getValue());
                StringUtils.setTrackData(trackData.getValue());
                StringUtils.setAccNo(trackData.getPan());
                StringUtils.setExpDate(trackData.getExpiry());
                request.addRange(trackData.getValue(), 40);
            }
        }

        // PIN Block Format, PIN Block, Transaction Amount 1, Transaction Amount 2, KSN
        if (!NtsUtils.isNoTrackEntryMethods(entryMethod) && !isReadyLink) {
            if (builder.getTransactionModifier().equals(TransactionModifier.Offline)) {
                NtsUtils.log("PIN Block Format", "0");
                request.addRange("0", 1);

                NtsUtils.log("PIN Block", StringUtils.padRight("", 16, ' '));
                request.addRange(StringUtils.padRight("", 16, ' '), 16);
            } else if ((transactionCode.equals(TransactionCode.Purchase)
                    || transactionCode.equals(TransactionCode.PurchaseCashBack)
                    || transactionCode.equals(TransactionCode.PurchaseReturn)
                    || transactionCode.equals(TransactionCode.PreAuthorizationFunds))) {
                NtsUtils.log("PIN Block Format", "5");
                request.addRange("5", 1);

                if (paymentMethod instanceof IPinProtected) {
                    String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();
                    if (pinBlock != null) {
                        NtsUtils.log("PIN Block", pinBlock.substring(0, 16));
                        request.addRange(pinBlock, 16);
                    }
                }
            }
        }

        BigDecimal amount = builder.getAmount();
        if (amount == null && paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference) builder.getPaymentMethod();
            amount = transactionReference.getOriginalAmount();
            NtsUtils.log("Transaction Original Amount1", StringUtils.toNumeric(amount, 7));
            request.addRange(StringUtils.toNumeric(amount, 7), 7);
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit) || isReadyLink) {
            NtsUtils.log("Transaction Amount 1", StringUtils.toNumeric(builder.getAmount(), 7));
            request.addRange(StringUtils.toNumeric(builder.getAmount(), 7), 7);
        }

        if (cashBackAmount != null) {  //with track data request
            NtsUtils.log("Transaction CashBack Amount2", StringUtils.toNumeric(cashBackAmount, 7));
            request.addRange(StringUtils.toNumeric(cashBackAmount, 7), 7); //04=cash back amount
        } else if (settlementAmount != null) {
            NtsUtils.log("Transaction Amount 2", StringUtils.toNumeric(settlementAmount, 7));
            request.addRange(settlementAmount.toString(), 7);
        } else if ((transactionCode != null)
                && transactionCode.equals(TransactionCode.Purchase) //03,05,06,08,13,15 = zero
                || transactionCode.equals(TransactionCode.PurchaseReturn)
                || transactionCode.equals(TransactionCode.PreAuthorizationFunds)
                || transactionCode.equals(TransactionCode.PurchaseReversal)
                || transactionCode.equals(TransactionCode.PreAuthCancelation)
                || transactionCode.equals(TransactionCode.PurchaseReturnReversal)
                || (isReadyLink && StringUtils.isNullOrEmpty(builder.getTagData()))) {
            NtsUtils.log("Transaction Amount 2", "0000000");
            request.addRange("0000000", 7);
        }
        if (paymentMethod instanceof IPinProtected && !isReadyLink) {
            String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();
            if (pinBlock != null && !NtsUtils.isNoTrackEntryMethods(entryMethod)
                    && !builder.getTransactionModifier().equals(TransactionModifier.Offline) &&
                    (transactionCode.equals(TransactionCode.Purchase)
                            || transactionCode.equals(TransactionCode.PurchaseCashBack)
                            || transactionCode.equals(TransactionCode.PurchaseReturn)
                            || transactionCode.equals(TransactionCode.PreAuthorizationFunds))) {
                NtsUtils.log("KEY SERIAL NUMBER(KSN)", StringUtils.padRight(pinBlock.substring(16), 20, ' '));
                request.addRange(StringUtils.padRight(pinBlock.substring(16), 20, ' '), 20);
            } else {
                NtsUtils.log("KEY SERIAL NUMBER(KSN)", StringUtils.padRight("", 20, ' '));
                request.addRange(StringUtils.padRight("", 20, ' '), 20);
            }
        }
        if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
            //Card Sequence no
            setCardSequenceNumber(builder,request,ntsObjectParam);

            NtsUtils.log("Unique device id", StringUtils.padLeft(builder.getUniqueDeviceId() != null ? builder.getUniqueDeviceId() : "", 4, ' '));
            request.addRange(StringUtils.padLeft(builder.getUniqueDeviceId(), 4, ' '), 4);

            //offline decline indicator
            NtsUtils.log("offline decline indicator", builder.getOfflineDeclineIndicator() != null ? builder.getOfflineDeclineIndicator() : "N");
            request.addRange(builder.getOfflineDeclineIndicator() != null ? builder.getOfflineDeclineIndicator() : "N", 1);

            EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging);
            userData = tagData.getAcceptedTagData();
            if (transactionType.equals(TransactionType.Auth)) {
                NtsUtils.log("EMV DATA LENGTH", StringUtils.padLeft(userData.length(), 4, '0'));
                request.addRange(StringUtils.padLeft(userData.length(), 4, '0'), 4);
            } else {
                NtsUtils.log("EMV DATA LENGTH", StringUtils.padLeft(userData.length(), 4, '0'));
                request.addRange(StringUtils.padLeft(userData.length(), 4, '0'), 4);
            }

            NtsUtils.log("EMV DATA", userData);
            request.addRange(StringUtils.padRight(userData, userData.length(), ' '), userData.length());
        }
         if (isReadyLink) {
            NtsUtils.log("Added FILLER", "");
            request.addRange("                    ", 20);
         }

        maskedRequest.append(request.getMessageRequest());
        if (this.getTrackData() != null) {
            int startIndex = maskedRequest.indexOf(this.getTrackData());
            int stopIndex = startIndex + this.getTrackData().length();
            maskedRequest.replace(startIndex, stopIndex, StringUtils.maskTrackData(this.getTrackData()));
        }

        if (this.getAccNo() != null) {
            int startIndex1 = maskedRequest.indexOf(this.getAccNo());
            int stopIndex1 = startIndex1 + this.getAccNo().length();
            maskedRequest.replace(startIndex1, stopIndex1, StringUtils.maskAccountNumber(this.getAccNo()));
        }

        if (this.getExpDate() != null) {
            int startIndex2 = maskedRequest.indexOf(this.getExpDate());
            int stopIndex2 = startIndex2 + this.getExpDate().length();
            maskedRequest.replace(startIndex2, stopIndex2, "****");
        }


        StringUtils.setMaskRequest(maskedRequest);
        return request;
    }

    private static void setCardSequenceNumber(TransactionBuilder builder, MessageWriter request,NtsObjectParam ntsObjectParam) {
        if (!StringUtils.isNullOrEmpty(builder.getCardSequenceNumber())) {
            NtsUtils.log("Card sequence no", StringUtils.padLeft(builder.getCardSequenceNumber(), 3, ' '));
            request.addRange(StringUtils.padLeft(builder.getCardSequenceNumber(), 3, ' '), 3);
        }else if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
            EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), ntsObjectParam.isNtsEnableLogging());

            if (!StringUtils.isNullOrEmpty(tagData.getCardSequenceNumber())) {
                NtsUtils.log("Card sequence no", tagData.getCardSequenceNumber());
                request.addRange(tagData.getCardSequenceNumber(), 3);
            }
        }else {
            NtsUtils.log("Card sequence no", StringUtils.padLeft("", 3, '0'));
            request.addRange(StringUtils.padLeft("", 3, '0'), 3);
        }
    }
}
