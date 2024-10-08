package com.global.api.network.entities.nts;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.NTSEntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.*;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class NtsVoidReversalRequest implements INtsRequestMessage {
    StringBuilder maskedRequest = new StringBuilder("");
    @Getter
    @Setter
    String accNo;
    @Getter @Setter
    String expDate;
    @Getter @Setter
    String trackData;
    EncryptionData encryptionData = null;
    private static final String GROUP_SEPARATOR = "\u001D" ;
    private static final String EMPTY_STRING = " ";
    private String tokenizationData;

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        String userData = ntsObjectParam.getNtsUserData();
        IBatchProvider batchProvider = ntsObjectParam.getNtsBatchProvider();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionType transactionType = builder.getTransactionType();
        boolean isExtendedUserData = false;

        TransactionReference transactionReference = null;
        if (paymentMethod instanceof TransactionReference) {
            transactionReference = (TransactionReference) paymentMethod;
        }
        if(transactionReference!=null) {
            paymentMethod = transactionReference.getOriginalPaymentMethod();
        }
        // Entry Method
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            request.addRange(entryMethod.getValue(), 1);
            NtsUtils.log("Entry Method", entryMethod);
        } else if (paymentMethod instanceof ICardData) {
            EntryMethod method = NtsUtils.isEcommerceEntryMethod(builder);
            if(method != null) {
                NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(method, TrackNumber.Unknown, ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                if(entryMethod != null){
                    request.addRange(entryMethod.getValue(), 1);
                    NtsUtils.log("Entry Method", entryMethod);
                }
            } else {
                request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
                NtsUtils.log("Entry Method", NTSEntryMethod.MagneticStripeWithoutTrackDataAttended);
            }
        }

        // Card Type
        NtsUtils.log("CardType : ", cardType);
        request.addRange(cardType.getValue(), 2);

        // DEBIT AUTHORIZER
        NtsUtils.log("DEBIT AUTHORIZER", "00");
        request.addRange("00", 2);

        // Account No & Expiration Date
        IPaymentMethod originalPaymentMethod = transactionReference.getOriginalPaymentMethod();
        if (originalPaymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) originalPaymentMethod;
            if(cardData instanceof IEncryptable && ((IEncryptable) paymentMethod).getEncryptionData() != null) {
                encryptionData = ((IEncryptable) cardData).getEncryptionData();
            }
            String tokenizationData = cardData.getTokenizationData();
            if(encryptionData != null){
                request.addRange(StringUtils.padRight(EMPTY_STRING, 19, ' '), 19);
                request.addRange(StringUtils.padRight(cardData.getShortExpiry(),4,' '),4);

            } else if(tokenizationData != null){
                request.addRange(StringUtils.padRight(EMPTY_STRING, 19, ' '), 19);
                request.addRange(StringUtils.padRight(EMPTY_STRING,4,' '),4);
            }
            else {
                String accNumber = cardData.getNumber();
                request.addRange(StringUtils.padLeft(accNumber, 19, ' '), 19);
                NtsUtils.log("Account No", StringUtils.maskAccountNumber(accNumber));
                this.setAccNo(cardData.getNumber());
                StringUtils.setAccNo(cardData.getNumber());

                request.addRange(cardData.getShortExpiry(), 4);
                NtsUtils.log("Expiration Date", StringUtils.padRight("", 4, '*'));
                this.setExpDate(cardData.getShortExpiry());
                StringUtils.setExpDate(cardData.getShortExpiry());
            }

        } else if (originalPaymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) originalPaymentMethod;

            if (trackData instanceof IEncryptable && ((IEncryptable) paymentMethod).getEncryptionData() != null) {
                encryptionData = ((IEncryptable) trackData).getEncryptionData();
            }
            if (encryptionData != null) {
                String expiryDate  = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());;
                request.addRange(StringUtils.padRight(EMPTY_STRING, 19, ' '), 19);
                request.addRange(StringUtils.padRight( expiryDate, 4, ' '),4);

            } else if ( tokenizationData != null) {
                request.addRange(StringUtils.padRight(EMPTY_STRING,4,' '),4);

            } else {
                String accNumber = trackData.getPan();
                request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
                NtsUtils.log("Account No", StringUtils.maskAccountNumber(accNumber));
                this.setAccNo(accNumber);
                StringUtils.setAccNo(accNumber);

                String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());

                request.addRange(expiryDate, 4);
                NtsUtils.log("Expiration Date", StringUtils.padRight("", 4, '*'));
                this.setExpDate(expiryDate);
                StringUtils.setExpDate(expiryDate);
            }
        }


        // APPROVAL CODE
        if(transactionType.equals(TransactionType.Reversal)) {
            request.addRange(StringUtils.padLeft(" ", 6, ' '),6);
            NtsUtils.log("APPROVAL CODE", "");
        } else{
                request.addRange(transactionReference.getApprovalCode(), 6);
                NtsUtils.log("APPROVAL CODE", transactionReference.getApprovalCode());
            }

        // FILLER
        request.addRange(" ", 1);
        NtsUtils.log("Added filler", "");

        // AMOUNT
        NtsUtils.log("Transaction Amount 1", StringUtils.toNumeric(builder.getAmount(), 7));
        request.addRange(StringUtils.toNumeric(builder.getAmount(), 7), 7);

        // ORIGINAL MESSAGE CODE
        request.addRange(transactionReference.getOriginalMessageCode(), 2);
        NtsUtils.log("ORIGINAL MESSAGE CODE", transactionReference.getOriginalMessageCode());

        // AUTHORIZATION CODE
        request.addRange(transactionReference.getAuthCode(), 2);
        NtsUtils.log("AUTHORIZATION CODE", transactionReference.getAuthCode());


        // ORIGINAL TRANSACTION DATE
        request.addRange(transactionReference.getOriginalTransactionDate(), 4);
        NtsUtils.log("ORIGINAL TRANSACTION DATE", transactionReference.getOriginalTransactionDate());

        // ORIGINAL TRANSACTION TIME
        request.addRange(transactionReference.getOriginalTransactionTime(), 6);
        NtsUtils.log("ORIGINAL TRANSACTION TIME", transactionReference.getOriginalTransactionTime());

        // Batch Number & Sequence Number
        if (transactionReference.getOriginalMessageCode().equals("02") || (!transactionReference.getOriginalMessageCode().equals("01") && transactionType.equals(TransactionType.Reversal))) {
            // Batch Number & Sequence Number
            if (transactionReference.getBatchNumber() != null) {
                int batchNumber = transactionReference.getBatchNumber();
                request.addRange(batchNumber, 2);
                NtsUtils.log("Batch Number", String.valueOf(batchNumber));
            }

            int sequenceNumber = 0;
            if (!builder.getTransactionType().equals(TransactionType.BatchClose) && transactionReference.getSequenceNumber() != null) {
                sequenceNumber = transactionReference.getSequenceNumber();
            }
            request.addRange(StringUtils.padLeft(sequenceNumber, 3, '0'), 3);
            NtsUtils.log("Sequence Number", String.valueOf(sequenceNumber));
        } else {
            request.addRange("00", 2);
            NtsUtils.log("Batch Number", String.valueOf("00"));

            request.addRange(StringUtils.padLeft("000", 3, '0'), 3);
            NtsUtils.log("Sequence Number", String.valueOf("000"));
        }


        if (transactionType.equals(TransactionType.Void)) {
            if (transactionReference.getOriginalPaymentMethod() instanceof ITrackData) {
                ITrackData trackData = (ITrackData) transactionReference.getOriginalPaymentMethod();
                NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                if (builder.getTagData() != null
                        || NtsUtils.isUserDataExpansionEntryMethod(entryMethod)) {
                    request.addRange("E", 1);
                    NtsUtils.log("EXPANDED USER DATA INDICATOR", "E");
                    isExtendedUserData = true;
                }
            }

            // UserDataLength & UserData
            if (isExtendedUserData) {
                NtsUtils.log("USER DATA LENGTH", StringUtils.padLeft(userData.length(), 4, '0'));
                request.addRange(StringUtils.padLeft(userData.length(), 4, '0'), 4);
            } else {
                NtsUtils.log("USER DATA LENGTH", StringUtils.padLeft(userData.length(), 3, ' '));
                request.addRange(userData.length(), 3);
            }

            // USER DATA
            request.addRange(StringUtils.padLeft(userData, userData.length(), ' '), userData.length());
            NtsUtils.log("User data", userData);
        } else if (builder.getTagData() != null || (transactionType.equals(TransactionType.Reversal)
                && (cardType == NTSCardTypes.WexFleet
                || cardType == NTSCardTypes.WexProprietaryFleet))) {
            // Extended user data flag
            request.addRange("E", 1);
            NtsUtils.log("EXPANDED USER DATA INDICATOR", "E");

            // User data length
            request.addRange(StringUtils.padLeft(userData.length(), 4, '0'), 4);
            NtsUtils.log("USER DATA LENGTH", StringUtils.padLeft(userData.length(), 4, '0'));

            // User data
            request.addRange(StringUtils.padLeft(userData, userData.length(), ' '), userData.length());
            NtsUtils.log("User data", userData);
        }

        // 3DE tag data
        if(ntsObjectParam.getEncryptedData() != null) {

            request.addRange(GROUP_SEPARATOR,1);
            request.addRange(  ntsObjectParam.getEncryptedData(), 336);
            NtsUtils.log("3DE Tag DATA", ntsObjectParam.getEncryptedData());

        } else if(ntsObjectParam.getTokenData() != null){

            request.addRange(GROUP_SEPARATOR,1);
            request.addRange(ntsObjectParam.getTokenData(),216);
            NtsUtils.log("Token Tag DATA", ntsObjectParam.getTokenData());

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


}
