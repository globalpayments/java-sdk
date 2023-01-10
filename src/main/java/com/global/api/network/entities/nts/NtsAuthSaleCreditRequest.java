package com.global.api.network.entities.nts;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.paymentMethods.*;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;

public class NtsAuthSaleCreditRequest implements INtsRequestMessage {

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {

        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        String userData = ntsObjectParam.getNtsUserData();
        IBatchProvider batchProvider = ntsObjectParam.getNtsBatchProvider();
        OperatingEnvironment operatingEnvironment = ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment();

        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        if (paymentMethod instanceof TransactionReference) {
            paymentMethod = ((TransactionReference) builder.getPaymentMethod()).getOriginalPaymentMethod();
        }
        TransactionType transactionType = builder.getTransactionType();
        boolean isExtendedUserData = false;

        // Entry Method
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), operatingEnvironment);
            request.addRange(entryMethod.getValue(), 1);
            NtsUtils.log("Entry Method", entryMethod);
        } else if (paymentMethod instanceof ICardData) {
            EntryMethod method = NtsUtils.isEcommerceEntryMethod(builder);
            if (method != null) {
                NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(method, TrackNumber.Unknown, operatingEnvironment);
                if (entryMethod != null){
                    request.addRange(entryMethod.getValue(), 1);
                    NtsUtils.log("Entry Method", entryMethod);
                }
            } else {
                request.addRange(NTSEntryMethod.ManualAttended.getValue(), 1);
                NtsUtils.log("Entry Method", NTSEntryMethod.ManualAttended);
            }
        } else if (paymentMethod instanceof GiftCard){
            request.addRange(NTSEntryMethod.MagneticStripeTrack2DataAttended.getValue(), 1);
            NtsUtils.log("Entry Method", NTSEntryMethod.MagneticStripeTrack2DataAttended);
        }

        // Card Type
        NtsUtils.log("CardType : ", cardType);
        request.addRange(cardType.getValue(), 2);

        // Account No & Expiration Date
        if (paymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) paymentMethod;
            String accNumber = cardData.getNumber();
            request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
            NtsUtils.log("Account No", StringUtils.padRight(accNumber, 19, ' '));
            request.addRange(cardData.getShortExpiry(), 4);
            NtsUtils.log("Expiration Date", cardData.getShortExpiry());

        } else if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), operatingEnvironment);
            if (NtsUtils.isNoTrackEntryMethods(entryMethod)) {
                String accNumber = trackData.getPan();
                request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
                NtsUtils.log("Account No", StringUtils.padRight(accNumber, 19, ' '));

                String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());

                request.addRange(expiryDate, 4);
                NtsUtils.log("Expiration Date", expiryDate);
            }
        }


        // Amount
        NtsUtils.log("Transaction Amount 1", StringUtils.toNumeric(builder.getAmount(), 6));
        request.addRange(StringUtils.toNumeric(builder.getAmount(), 6), 6);

        // Track 1 or Track 2 & Expanded User Data
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), operatingEnvironment);
            if (!NtsUtils.isNoTrackEntryMethods(entryMethod)) {
                if (trackData.getTrackNumber().equals(TrackNumber.TrackOne)) {
                    request.addRange(StringUtils.padRight(trackData.getValue(), 79, ' '), 79);
                } else {
                    request.addRange(StringUtils.padRight(trackData.getValue(), 40, ' '), 40);
                }
                NtsUtils.log("Track 1 or Track 2 & Expanded User Data", trackData.getValue());
            }
        } else if (paymentMethod instanceof GiftCard) {
            GiftCard gift = (GiftCard) paymentMethod;
            if (gift.getTrackNumber().equals(TrackNumber.TrackOne)) {
                request.addRange(StringUtils.padRight(gift.getValue(), 79, ' '), 79);
            } else {
                request.addRange(StringUtils.padRight(gift.getValue(), 40, ' '), 40);
            }
            NtsUtils.log("Gift card data: ", gift.getValue());
        }


        // Service code
        if (paymentMethod instanceof CreditCardData) {
            if (!StringUtils.isNullOrEmpty(builder.getServiceCode())) {
                request.addRange(builder.getServiceCode(), 3);
                NtsUtils.log("Service code", builder.getServiceCode());
            } else {
                request.addRange("000", 3);
                NtsUtils.log("Service code", "000");
            }
        } else if (paymentMethod instanceof CreditTrackData) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), operatingEnvironment);
            if (NtsUtils.isUserDataExpansionEntryMethod(entryMethod)) {
                request.addRange("000", 3);
                NtsUtils.log("Service code", "000");
            }
        }


        // Authorization Response Code  // Batch Number & Sequence Number
        if (transactionType.equals(TransactionType.Sale)
                && !paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Gift)) {
            // Authorization Response Code
            request.addRange(String.format("%2s", " "), 2);

            int batchNumber = builder.getBatchNumber();
            if (batchNumber == 0 && batchProvider != null) {
                batchNumber = batchProvider.getBatchNumber();
            }
            // Batch Number
            request.addRange(batchNumber, 2);
            NtsUtils.log("Batch Number", String.valueOf(batchNumber));


            int sequenceNumber = 0;
            if (!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
            }
            // Sequence Number
            request.addRange(StringUtils.padLeft(sequenceNumber, 3, '0'), 3);
            NtsUtils.log("Sequence Number", String.valueOf(sequenceNumber));
        }

        // Expanded User Data
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) builder.getPaymentMethod();
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(), trackData.getTrackNumber(), operatingEnvironment);
            if (!StringUtils.isNullOrEmpty(builder.getTagData()) || NtsUtils.isUserDataExpansionEntryMethod(entryMethod)) {
                request.addRange("E", 1);
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

        NtsUtils.log("USER DATA", userData);
        request.addRange(StringUtils.padRight(userData, userData.length(), ' '), userData.length());

        return request;
    }
}
