package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.NTSEntryMethod;
import com.global.api.entities.enums.TransactionCode;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class NtsEbtRequest implements INtsRequestMessage {
    private BigDecimal surcharge;
    private TransactionCode transactionCode;
    private BigDecimal cashBackAmount;

    StringBuilder maskedRequest = new StringBuilder("");
    @Getter @Setter
    String accNo;
    @Getter @Setter
    String expDate;
    @Getter @Setter
    String trackData;

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        AcceptorConfig acceptorConfig = ntsObjectParam.getNtsAcceptorConfig();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();

        //message body
        ManagementBuilder manageBuilder;
        AuthorizationBuilder authorizationBuilder = null;

        if (builder instanceof AuthorizationBuilder) {
            authorizationBuilder = (AuthorizationBuilder) builder;
            cashBackAmount = authorizationBuilder.getCashBackAmount();
            surcharge = authorizationBuilder.getSurchargeAmount();
        } else if (builder instanceof ManagementBuilder) {
            manageBuilder = (ManagementBuilder) builder;
            surcharge = manageBuilder.getSurchargeAmount();
            cashBackAmount = manageBuilder.getCashBackAmount();
        }

        transactionCode = NtsUtils.getTransactionCodeForTransaction(builder, cashBackAmount, surcharge);

        if (paymentMethod instanceof TransactionReference) {
            paymentMethod = ((TransactionReference) builder.getPaymentMethod()).getOriginalPaymentMethod();
            // Purchase_Return_Reversal && Withdrawal_Reversal
            TransactionCode originalTransactionCode = ((TransactionReference) builder.getPaymentMethod()).getOriginalTransactionCode();
            if(originalTransactionCode == TransactionCode.Withdrawal && builder.getTransactionType().equals(TransactionType.Reversal)){
                transactionCode = TransactionCode.WithdrawalReversal;
            } else if( originalTransactionCode == TransactionCode.PurchaseReturn && builder.getTransactionType().equals(TransactionType.Reversal)){
                transactionCode = TransactionCode.PurchaseReturnReversal;
            }
        }
        // Entry Method
        if (paymentMethod instanceof EBTTrackData) {
            EBTTrackData trackData = (EBTTrackData) paymentMethod;
            NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            request.addRange(entryMethod.getValue(), 1);
            NtsUtils.log("Entry Method", entryMethod);
        } else if (paymentMethod instanceof EBTCardData) {
            request.addRange(NTSEntryMethod.ManualAttended.getValue(), 1);
            NtsUtils.log("Entry Method", NTSEntryMethod.ManualAttended);
        }

        // Card Type
        NtsUtils.log("CardType : ", cardType);
        request.addRange(cardType.getValue(), 2);
        
        // Transaction Code
        NtsUtils.log("Transaction Code", transactionCode);
        request.addRange(transactionCode.getValue(), 2);

        // Address
        if (acceptorConfig.getAddress() != null) {
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getName(), 20, ' '), 20);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getStreetAddress1(), 18, ' '), 18);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getCity(), 16, ' '), 16);
            request.addRange(StringUtils.padRight(acceptorConfig.getAddress().getState(), 2, ' '), 2);

            NtsUtils.log("Address", acceptorConfig.getAddress().toString());
        }

        // Track Data
        if (paymentMethod instanceof EBTTrackData) {
            EBTTrackData trackData = (EBTTrackData) paymentMethod;
            NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            if (NtsUtils.isNoTrackEntryMethods(entryMethod) || isReversalTransaction(transactionCode)) {
                // Account number
                NtsUtils.log("Account Number", StringUtils.maskAccountNumber(trackData.getPan()));
                request.addRange(StringUtils.padRight(trackData.getPan(), 19, ' '), 19);
                this.setAccNo(trackData.getPan());
                StringUtils.setAccNo(trackData.getPan());

                String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());
                // Expiry date
                NtsUtils.log("Exp Date", StringUtils.padRight("", 4, '*'));
                request.addRange(StringUtils.padRight(expiryDate, 4, ' '), 4);
                this.setExpDate(expiryDate);
                StringUtils.setExpDate(expiryDate);
            } else {
                NtsUtils.log("TrackData 2", StringUtils.maskTrackData(trackData.getValue(),trackData));
                request.addRange(StringUtils.padRight(trackData.getValue(), 40, ' '), 40);
                this.setTrackData(trackData.getValue());
                StringUtils.setTrackData(trackData.getValue());
                StringUtils.setAccNo(trackData.getPan());
                StringUtils.setExpDate(trackData.getExpiry());
            }
        } else if (paymentMethod instanceof EBTCardData) {
            // Account number
            EBTCardData cardData = (EBTCardData) paymentMethod;
            NtsUtils.log("Account Number", StringUtils.maskAccountNumber(cardData.getNumber()));
            request.addRange(StringUtils.padRight(cardData.getNumber(), 19, ' '), 19);
            this.setAccNo(cardData.getNumber());
            StringUtils.setAccNo(cardData.getNumber());

            // Expiry date
            NtsUtils.log("Exp Date", StringUtils.padRight("", 4, '*'));
            request.addRange(StringUtils.padRight(cardData.getShortExpiry(), 4, ' '), 4);
            this.setExpDate(cardData.getShortExpiry());
            StringUtils.setExpDate(cardData.getShortExpiry());
        }

        if (transactionCode.equals(TransactionCode.VoucherSale)
                || transactionCode.equals(TransactionCode.VoucherReturn)) {

            // ORIGINAL TRANSACTION DATE
            NtsUtils.log("ORIGINAL TRANSACTION DATE", authorizationBuilder.getVoucherEntryData().getOriginalTransactionDate());
            request.addRange(authorizationBuilder.getVoucherEntryData().getOriginalTransactionDate(), 4);

            // Transaction amount 1
            NtsUtils.log("Transaction Amount 1", StringUtils.toNumeric(builder.getAmount(), 7));
            request.addRange(StringUtils.toNumeric(builder.getAmount(), 7), 7);

            // VOUCHER NBR
            NtsUtils.log("VOUCHER NBR", authorizationBuilder.getVoucherEntryData().getVoucherNBR());
            request.addRange(authorizationBuilder.getVoucherEntryData().getVoucherNBR(), 15);

            // TELEPHONE AUTH CODE
            NtsUtils.log("TELEPHONE AUTH CODE", authorizationBuilder.getVoucherEntryData().getTelephoneAuthCode());
            request.addRange(authorizationBuilder.getVoucherEntryData().getTelephoneAuthCode(), 6);

        } else {
            if (isReversalTransaction(transactionCode)) {
                // PIN Block Format
                NtsUtils.log("PIN Block Format", StringUtils.padRight("", 1, ' '));
                request.addRange(StringUtils.padRight("", 1, ' '), 1);

                // PIN Block
                NtsUtils.log("PIN Block", StringUtils.padRight("", 16, ' '));
                request.addRange(StringUtils.padRight("", 16, ' '), 16);

            } else {
                // PIN Block Format
                NtsUtils.log("PIN Block Format", "5");
                request.addRange("5", 1);

                // PIN Block
                if (paymentMethod instanceof IPinProtected) {
                    String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();
                    if(pinBlock != null) {
                        NtsUtils.log("PIN Block", pinBlock.substring(0, 16));
                        request.addRange(pinBlock, 16);
                    }
                }
            }


            // Transaction amount 1
            NtsUtils.log("Transaction Amount 1", StringUtils.toNumeric(builder.getAmount(), 7));
            request.addRange(StringUtils.toNumeric(builder.getAmount(), 7), 7);

            if (transactionCode.equals(TransactionCode.BalanceInquiry)
                    || transactionCode.equals(TransactionCode.PurchaseReturn)
                    || transactionCode.equals(TransactionCode.PurchaseReturnReversal)) {
                // Transaction amount 2
                NtsUtils.log("Transaction amount 2", StringUtils.padRight("", 7, '0'));
                request.addRange(StringUtils.padRight("", 7, '0'), 7);

                // Transaction amount 3
                NtsUtils.log("Transaction amount 3", StringUtils.padRight("", 7, '0'));
                request.addRange(StringUtils.padRight("", 7, '0'), 7);
            } else {
                // Transaction amount 2
                if (cashBackAmount != null) {  //with track data request
                    NtsUtils.log("Transaction amount 2", StringUtils.toNumeric(cashBackAmount, 7));
                    request.addRange(StringUtils.toNumeric(cashBackAmount, 7), 7);
                } else {
                    // Transaction amount 2
                    NtsUtils.log("Transaction amount 2", StringUtils.padRight("", 7, '0'));
                    request.addRange(StringUtils.padRight("", 7, '0'), 7);
                }

                // Transaction amount 3
                if (surcharge != null) {
                    NtsUtils.log("Transaction amount 3", StringUtils.toNumeric(surcharge, 7));
                    request.addRange(StringUtils.toNumeric(surcharge, 7), 7);
                } else {
                    // Transaction amount 3
                    NtsUtils.log("Transaction amount 3", StringUtils.padRight("", 7, '0'));
                    request.addRange(StringUtils.padRight("", 7, '0'), 7);
                }
            }

            // KEY SERIAL NUMBER(KSN)
            if (isReversalTransaction(transactionCode)) {
                NtsUtils.log("KEY SERIAL NUMBER(KSN)", StringUtils.padRight("", 20, ' '));
                request.addRange(StringUtils.padRight("", 20, ' '), 20);
            } else {
                if (paymentMethod instanceof IPinProtected) {
                    //EncryptionData encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
                    String pinBlock = ((IPinProtected)paymentMethod).getPinBlock();
                    NtsUtils.log("KEY SERIAL NUMBER(KSN)", StringUtils.padRight(pinBlock.substring(16), 20, ' '));
                    request.addRange(StringUtils.padRight(pinBlock.substring(16), 20, ' '), 20);
                }
            }
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

    private Boolean isReversalTransaction(TransactionCode transactionCode) {
        return transactionCode.equals(TransactionCode.PurchaseReturnReversal)
                || transactionCode.equals(TransactionCode.WithdrawalReversal)
                || transactionCode.equals(TransactionCode.PurchaseReversal)
                || transactionCode.equals(TransactionCode.PurchaseCashBackReversal);
    }
}
