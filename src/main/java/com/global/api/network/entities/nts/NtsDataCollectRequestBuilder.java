package com.global.api.network.entities.nts;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.NTSEntryMethod;
import com.global.api.entities.enums.NtsMessageCode;
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

public class NtsDataCollectRequestBuilder implements INtsRequestMessage {
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
        MessageWriter request = ntsObjectParam.getNtsRequest();
        NTSCardTypes cardType = ntsObjectParam.getNtsCardType();
        String userData = ntsObjectParam.getNtsUserData();
        NtsRequestMessageHeader ntsRequestMessageHeader = builder.getNtsRequestMessageHeader();

        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionReference transactionReference = null;
        if(paymentMethod instanceof TransactionReference){
            transactionReference = (TransactionReference) paymentMethod;
            paymentMethod = transactionReference.getOriginalPaymentMethod();
        }

        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            if (trackData.getEntryMethod() != null) {
                NTSEntryMethod entryMethod=NtsUtils.isAttendedOrUnattendedEntryMethod(trackData.getEntryMethod(),trackData.getTrackNumber(),ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
                request.addRange(entryMethod.getValue(), 1);
                NtsUtils.log("Entry Method", entryMethod);
            } else {
                request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
                NtsUtils.log("Entry Method", NTSEntryMethod.MagneticStripeWithoutTrackDataAttended);
            }
        } else if (paymentMethod instanceof ICardData) {
            request.addRange(NTSEntryMethod.MagneticStripeWithoutTrackDataAttended.getValue(), 1);
            NtsUtils.log("Entry Method", NTSEntryMethod.MagneticStripeWithoutTrackDataAttended);
        } else if (paymentMethod instanceof GiftCard) {
            GiftCard card = (GiftCard) paymentMethod;
            NTSEntryMethod entryMethod = NtsUtils.isAttendedOrUnattendedEntryMethod(card.getEntryMethod(), card.getTrackNumber(), ntsObjectParam.getNtsAcceptorConfig().getOperatingEnvironment());
            request.addRange(entryMethod.getValue(), 1);
            NtsUtils.log("Entry Method", entryMethod);
        }

        // Card Type
        if (cardType != null) {
            NtsUtils.log("CardType : ", cardType);
            request.addRange(cardType.getValue(), 2);
        }

        if (transactionReference != null) {

            IBatchProvider batchProvider = ntsObjectParam.getNtsBatchProvider();
            int batchNumber = builder.getBatchNumber();
            int sequenceNumber = 0;

            if (!StringUtils.isNullOrEmpty(transactionReference.getDebitAuthorizer())) {
                request.addRange(transactionReference.getDebitAuthorizer(), 2); // Response value from ebt or pin debit authorizer
                NtsUtils.log("DebitAuthorizer", transactionReference.getDebitAuthorizer());
            } else {
                request.addRange(DebitAuthorizerCode.NonPinDebitCard.getValue(), 2);
                NtsUtils.log("DebitAuthorizer", DebitAuthorizerCode.NonPinDebitCard);
            }

            if (paymentMethod instanceof ICardData) {
                ICardData cardData = (ICardData) paymentMethod;
                String accNumber = cardData.getNumber();
                this.setAccNo(accNumber);
                StringUtils.setAccNo(accNumber);
                request.addRange(StringUtils.padRight(accNumber, 19, ' '), 19);
                NtsUtils.log("Account No", StringUtils.maskAccountNumber(accNumber));
                request.addRange(cardData.getShortExpiry(), 4);
                this.setExpDate(cardData.getShortExpiry());
                StringUtils.setExpDate(cardData.getShortExpiry());
                NtsUtils.log("Expiration Date", StringUtils.padRight("",4,'*'));

            } else if (paymentMethod instanceof ITrackData) {
                ITrackData trackData = (ITrackData) paymentMethod;
                if (trackData.getPan() != null) {
                    this.setAccNo(trackData.getPan());
                    StringUtils.setAccNo(trackData.getPan());
                    // Account number
                    NtsUtils.log("Account Number", StringUtils.maskAccountNumber(trackData.getPan()));
                    request.addRange(StringUtils.padRight(trackData.getPan(), 19, ' '), 19);

                    String expiryDate = NtsUtils.prepareExpDateWithoutTrack(trackData.getExpiry());
                    this.setExpDate(expiryDate);
                    StringUtils.setExpDate(expiryDate);
                    // Expiry date
                    NtsUtils.log("Expiry Date", StringUtils.padRight("", 4, '*'));
                    request.addRange(StringUtils.padRight(expiryDate, 4, ' '), 4);
                } else {
                    this.setTrackData(trackData.getValue());
                    StringUtils.setTrackData(trackData.getValue());
                    StringUtils.setAccNo(trackData.getPan());
                    StringUtils.setExpDate(trackData.getExpiry());
                    NtsUtils.log("TrackData 2", StringUtils.maskTrackData(trackData.getValue(),trackData));
                    request.addRange(StringUtils.padRight(trackData.getValue(), 40, ' '), 40);
                }
            } else if (paymentMethod instanceof GiftCard) {
                GiftCard gift = (GiftCard) paymentMethod;
                // Account number
                this.setAccNo(gift.getPan());
                StringUtils.setAccNo(gift.getPan());
                NtsUtils.log("Account Number", StringUtils.maskAccountNumber(gift.getPan()));
                request.addRange(StringUtils.padRight(gift.getPan(), 19, ' '), 19);

                String expiryDate = NtsUtils.prepareExpDateWithoutTrack(gift.getExpiry());
                // Expiry date
                this.setExpDate(expiryDate);
                StringUtils.setExpDate(expiryDate);
                NtsUtils.log("Exp Date", StringUtils.padRight("", 4, '*'));
                request.addRange(StringUtils.padRight(expiryDate, 4, ' '), 4);
            }

            request.addRange(transactionReference.getApprovalCode(), 6);
            NtsUtils.log("ApprovalCode", transactionReference.getApprovalCode());

            request.addRange(transactionReference.getAuthorizer().getValue(), 1);
            NtsUtils.log("Authorizer", transactionReference.getAuthorizer());

            request.addRange(StringUtils.toNumeric(builder.getAmount(), 7), 7);
            NtsUtils.log("Amount", StringUtils.toNumeric(builder.getAmount(), 7));

            request.addRange(ntsRequestMessageHeader.getNtsMessageCode().getValue(), 2);
            NtsUtils.log("MessageCode", ntsRequestMessageHeader.getNtsMessageCode());

            request.addRange(transactionReference.getAuthCode(), 2);
            NtsUtils.log("AuthorizationResponseCode", transactionReference.getAuthCode());

            if (ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.RetransmitCreditAdjustment ||
                    ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.ForceCreditAdjustment ||
                    ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.RetransmitForceCreditAdjustment) {
                request.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);
                NtsUtils.log("OriginalTransactionDate", ntsRequestMessageHeader.getTransactionDate());

                request.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
                NtsUtils.log("OriginalTransactionTime", ntsRequestMessageHeader.getTransactionTime());
            } else if (ntsRequestMessageHeader.getNtsMessageCode() == NtsMessageCode.CreditAdjustment) {

                request.addRange(ntsRequestMessageHeader.getTransactionDate(), 4);
                NtsUtils.log("OriginalTransactionDate", ntsRequestMessageHeader.getTransactionDate());

                request.addRange(ntsRequestMessageHeader.getTransactionTime(), 6);
                NtsUtils.log("OriginalTransactionTime", ntsRequestMessageHeader.getTransactionTime());
            } else {
                request.addRange(transactionReference.getOriginalTransactionDate(), 4);
                NtsUtils.log("OriginalTransactionDate", transactionReference.getOriginalTransactionDate());

                request.addRange(transactionReference.getOriginalTransactionTime(), 6);
                NtsUtils.log("OriginalTransactionTime", transactionReference.getOriginalTransactionTime());
            }
            if (batchNumber == 0 && batchProvider != null) {
                batchNumber = batchProvider.getBatchNumber();
            }
            //BatchNumber
            request.addRange(batchNumber, 2);
            NtsUtils.log("Batch Number", String.valueOf(batchNumber));

            if (!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
            }
            //Sequence Number
            request.addRange(StringUtils.padLeft(sequenceNumber, 3, '0'), 3);
            NtsUtils.log("Sequence Number", String.valueOf(sequenceNumber));

            if (!StringUtils.isNullOrEmpty(userData)) {
                if (userData.length() != 99) {
                    // Extended user data flag
                    request.addRange("E", 1);
                    NtsUtils.log("Extended user data flag", "E");

                    // User data length
                    if(cardType.equals(NTSCardTypes.WexFleet) && ntsRequestMessageHeader.getNtsMessageCode().equals(NtsMessageCode.DataCollectOrSale)) {
                        request.addRange(StringUtils.padLeft(userData.length(), 4, '0'), 4);
                    } else{
                        request.addRange(StringUtils.padLeft(userData.length(), 3, '0'), 3);
                    }
                    NtsUtils.log("User Data Length", Integer.toString(userData.length()));
                }

                request.addRange(userData, userData.length());
                NtsUtils.log("User Data ", userData);
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
}
