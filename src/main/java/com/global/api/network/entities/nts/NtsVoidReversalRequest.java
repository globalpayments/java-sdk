package com.global.api.network.entities.nts;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.NTSEntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.paymentMethods.ICardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITrackData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;

public class NtsVoidReversalRequest implements INtsRequestMessage {

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

        paymentMethod = transactionReference.getOriginalPaymentMethod();

        // Entry Method
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            request.addRange(entryMethod.getValue(), 1);
            NtsUtils.log("Entry Method", entryMethod.getValue());
        } else if (paymentMethod instanceof ICardData) {
            EntryMethod method = NtsUtils.isEcommerceEntryMethod(builder);
            if(method != null) {
                NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(method, TrackNumber.Unknown, ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                if(entryMethod != null){
                    request.addRange(entryMethod.getValue(), 1);
                    NtsUtils.log("Entry Method", entryMethod.getValue());
                }
            } else {
                request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
                NtsUtils.log("Entry Method", NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue());
            }
        }

        // Card Type
        NtsUtils.log("CardType : ", cardType.getValue());
        request.addRange(cardType.getValue(), 2);

        // DEBIT AUTHORIZER
        NtsUtils.log("DEBIT AUTHORIZER", "00");
        request.addRange("00", 2);

        // Account No & Expiration Date
        IPaymentMethod originalPaymentMethod = transactionReference.getOriginalPaymentMethod();
        if (originalPaymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) originalPaymentMethod;
            String accNumber = cardData.getNumber();
            request.addRange(StringUtils.padLeft(accNumber, 19, ' '), 19);
            NtsUtils.log("Account No", StringUtils.padLeft(accNumber, 19, ' '));

            request.addRange(cardData.getShortExpiry(), 4);
            NtsUtils.log("Expiration Date", cardData.getShortExpiry());

        } else if (originalPaymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) originalPaymentMethod;
            String accNumber = trackData.getPan();

            request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
            NtsUtils.log("Account No", StringUtils.padRight(accNumber, 19, ' '));

            String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());

            request.addRange(expiryDate, 4);
            NtsUtils.log("Expiration Date", expiryDate);
        }


        // APPROVAL CODE
        request.addRange(transactionReference.getApprovalCode(), 6);
        NtsUtils.log("APPROVAL CODE", transactionReference.getApprovalCode());

        // FILLER
        request.addRange(" ", 1);
        NtsUtils.log("Added filter", "");

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
        request.addRange(transactionReference.getOriginalTrasactionDate(), 4);
        NtsUtils.log("ORIGINAL TRANSACTION DATE", transactionReference.getOriginalTrasactionDate());

        // ORIGINAL TRANSACTION TIME
        request.addRange(transactionReference.getOriginalTransactionTime(), 6);
        NtsUtils.log("ORIGINAL TRANSACTION TIME", transactionReference.getOriginalTransactionTime());

        // Batch Number & Sequence Number
        if (transactionReference.getOriginalMessageCode().equals("02")) {
            // Batch Number & Sequence Number
            int batchNumber = builder.getBatchNumber();
            if (batchNumber == 0 && batchProvider != null) {
                batchNumber = batchProvider.getBatchNumber();
            }
            request.addRange(batchNumber, 2);
            NtsUtils.log("Batch Number", String.valueOf(batchNumber));


            int sequenceNumber = 0;
            if (!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
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
        } else if (transactionType.equals(TransactionType.Reversal)
                && (cardType == NTSCardTypes.WexFleet
                || cardType == NTSCardTypes.WexProprietaryFleet)) {
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

        return request;
    }


}
