package com.global.api.gateways;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import com.global.api.builders.*;
import com.global.api.entities.enums.*;
import com.global.api.network.elements.*;
import com.global.api.network.entities.*;
import com.global.api.paymentMethods.*;
import com.global.api.network.enums.*;
import com.global.api.serviceConfigs.GatewayConnectorConfig;
import com.global.api.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.NetworkMessage;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.Credit;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.network.enums.DE22_CardDataInputMode;
import com.global.api.network.enums.DE22_CardHolderAuthenticationMethod;
import com.global.api.network.enums.DE22_CardHolderPresence;
import com.global.api.network.enums.DE22_CardPresence;
import com.global.api.network.enums.DE25_MessageReasonCode;
import com.global.api.network.enums.DE39_ActionCode;
import com.global.api.network.enums.DE3_AccountType;
import com.global.api.network.enums.DE3_TransactionType;
import com.global.api.network.enums.DE48_AddressType;
import com.global.api.network.enums.DE48_AddressUsage;
import com.global.api.network.enums.DE48_CardType;
import com.global.api.network.enums.DE48_CustomerDataType;
import com.global.api.network.enums.DE48_EncryptionAlgorithmDataCode;
import com.global.api.network.enums.DE48_KeyManagementDataCode;
import com.global.api.network.enums.DE54_AmountTypeCode;
import com.global.api.network.enums.DataElementId;
import com.global.api.network.enums.FallbackCode;
import com.global.api.network.enums.FeeType;
import com.global.api.network.enums.Iso4217_CurrencyCode;
import com.global.api.network.enums.Iso8583MessageType;
import com.global.api.network.enums.MessageType;
import com.global.api.network.enums.NetworkResponseCode;
import com.global.api.network.enums.NetworkTransactionType;
import com.global.api.network.enums.ProtocolType;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.abstractions.IDeviceMessage;

public class NwsConnector extends GatewayConnectorConfig {
    private static final String DATE_FORMAT_SPAN_10 = "MMddhhmmss";
    private static final String DATE_FORMAT_SPAN_12 = "yyMMddhhmmss";
    private static String cardTypeWexFleet = "WexFleet";
    private static String cardTypeVisaReadyLink = "VisaReadyLink";
    private static final String WEX_SEQ_EXCEPTION_MESSAGE_STRING = "The purchase device sequence number cannot be null for WEX transactions.";

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        if (builder != null) {
            validate(builder);

            byte[] orgCorr1 = new byte[2];
            byte[] orgCorr2 = new byte[8];

        NetworkMessage request = new NetworkMessage();
        IPaymentMethod paymentMethod = null;
        paymentMethod = builder.getPaymentMethod();
        PaymentMethodType paymentMethodType = null;
        if (paymentMethod != null)
            paymentMethodType = builder.getPaymentMethod().getPaymentMethodType();
        TransactionType transactionType = builder.getTransactionType();
        boolean isPosSiteConfiguration = transactionType.equals(TransactionType.PosSiteConfiguration);
        boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getSupportVisaFleet2dot0().getValue() != " ";
        Iso4217_CurrencyCode currencyCode = null;
        EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
            if (!StringUtils.isNullOrEmpty(builder.getCurrency())) {
            currencyCode = builder.getCurrency().equalsIgnoreCase("USD") ? Iso4217_CurrencyCode.USD : Iso4217_CurrencyCode.CAD;
        }
        // MTI
        String mti = mapMTI(builder);
        if(isVisaFleet2 && mti.equals("1200")){
            throw new UnsupportedTransactionException("Visa Fleet 2.0 does not support sale transaction");
        }
        request.setMessageTypeIndicator(mti);

            // pos data code
            DE22_PosDataCode dataCode = new DE22_PosDataCode();

            // handle the payment methods
            if (paymentMethod instanceof ICardData) {
                ICardData card = (ICardData) builder.getPaymentMethod();
                String token = card.getTokenizationData();
                if (token == null) {
                    // DE 2: Primary Account Number (PAN) - LLVAR // 1100, 1200, 1220, 1300, 1310, 1320, 1420
                    request.set(DataElementId.DE_002, card.getNumber());

                    // DE 14: Date, Expiration - n4 (YYMM) // 1100, 1200, 1220, 1420
                    request.set(DataElementId.DE_014, formatExpiry(card.getShortExpiry()));
                }
                // set data codes
                dataCode.setCardDataInputMode(card.isReaderPresent() ? DE22_CardDataInputMode.KeyEntry : DE22_CardDataInputMode.Manual);
                dataCode.setCardHolderPresence(card.isCardPresent() ? DE22_CardHolderPresence.CardHolder_Present : DE22_CardHolderPresence.CardHolder_NotPresent);
                dataCode.setCardPresence(card.isCardPresent() ? DE22_CardPresence.CardPresent : DE22_CardPresence.CardNotPresent);
                DE22_CardDataInputMode cardDataInputMode = acceptorConfig.getCardDataInputMode();
                setDataCodeForCardOnFile(dataCode, cardDataInputMode);
                if (!StringUtils.isNullOrEmpty(card.getCvn())) {
                    dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.OnCard_SecurityCode);
                }
            } else if (paymentMethod instanceof ITrackData) {
                ITrackData card = (ITrackData) builder.getPaymentMethod();
                if (card != null) {
                    String token = card.getTokenizationData();
                    if(token == null){
                    // put the track data
                    if (transactionType.equals(TransactionType.Refund) && (!paymentMethodType.equals(PaymentMethodType.Debit) && !paymentMethodType.equals(PaymentMethodType.EBT))) {
                        if (paymentMethod instanceof IEncryptable && ((IEncryptable) paymentMethod).getEncryptionData() != null) {
                            request.set(DataElementId.DE_002, ((IEncryptable) card).getEncryptedPan());
                        } else {
                            request.set(DataElementId.DE_002, card.getPan());
                        }
                        request.set(DataElementId.DE_014, card.getExpiry());
                    } else if (card.getTrackNumber().equals(TrackNumber.TrackTwo)) {
                        // DE 35: Track 2 Data - LLVAR ns.. 37
                        request.set(DataElementId.DE_035, card.getTrackData());
                    } else if (card.getTrackNumber().equals(TrackNumber.TrackOne)) {
                        // DE 45: Track 1 Data - LLVAR ans.. 76
                        request.set(DataElementId.DE_045, card.getTrackData());
                    } else {
                        if (card instanceof IEncryptable && ((IEncryptable) card).getEncryptionData() != null) {
                            EncryptionData encryptionData = ((IEncryptable) card).getEncryptionData();
                            if (encryptionData.getTrackNumber().equals("1")) {
                                // DE 45: Track 1 Data - LLVAR ans.. 76
                                request.set(DataElementId.DE_045, card.getValue());
                            } else if (encryptionData.getTrackNumber().equals("2")) {
                                // DE 35: Track 2 Data - LLVAR ns.. 37
                                request.set(DataElementId.DE_035, card.getValue());
                            }
                        }
                    }
                }
                    // set data codes
                    if (paymentMethodType.equals(PaymentMethodType.Credit) || paymentMethodType.equals(PaymentMethodType.Debit)) {
                        dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                        dataCode.setCardPresence(DE22_CardPresence.CardPresent);
                        if (tagData != null) {
                            setEmvTagData(tagData, dataCode, card);
                        } else {
                            if (card.getEntryMethod().equals(EntryMethod.Proximity)) {
                                dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                            } else {
                                dataCode.setCardDataInputMode(builder.getEmvChipCondition() != null ? DE22_CardDataInputMode.MagStripe_Fallback : DE22_CardDataInputMode.UnalteredTrackData);
                            }
                        }
                    }
                }
            } else if (paymentMethod instanceof GiftCard) {
                GiftCard giftCard = (GiftCard) paymentMethod;
                if (giftCard != null) {
                    // put the track data
                    if (giftCard.getValueType()!=null && giftCard.getValueType().equals("TrackData")) {
                        if (giftCard.getTrackNumber().equals(TrackNumber.TrackTwo)) {
                            // DE 35: Track 2 Data - LLVAR ns.. 37
                            request.set(DataElementId.DE_035, giftCard.getTrackData());
                        } else if (giftCard.getTrackNumber().equals(TrackNumber.TrackOne)) {
                            // DE 45: Track 1 Data - LLVAR ans.. 76
                            request.set(DataElementId.DE_045, giftCard.getTrackData());
                        }
                    } else {
                        request.set(DataElementId.DE_002, giftCard.getNumber());
                        request.set(DataElementId.DE_014, giftCard.getExpiry());
                    }

                    // set data codes
                    if (!StringUtils.isNullOrEmpty(giftCard.getPin())) {
                        dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
                    } else {
                        dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.NotAuthenticated);
                    }
                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
                    dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                    dataCode.setCardPresence(DE22_CardPresence.CardPresent);
                }
            }
            setPosCodeDataForEcheck(paymentMethod, dataCode);

            if (paymentMethod instanceof IPinProtected) {
                String pinBlock = ((IPinProtected) paymentMethod).getPinBlock();

                if (!StringUtils.isNullOrEmpty(pinBlock)) {
                    // DE 52: Personal Identification Number (PIN) Data - b8
                    request.set(DataElementId.DE_052, StringUtils.bytesFromHex(pinBlock.substring(0, 16)));

                    // DE 53: Security Related Control Information - LLVAR an..48
                    request.set(DataElementId.DE_053, pinBlock.substring(16));

                    // set the data code
                    dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
                } else if(tagData != null && tagData.isOfflinePin()) {
                    dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
                }
            }

            // DE 1: Secondary Bitmap - b8 // M (AUTO GENERATED IN NetworkMessage)

            // DE 3: Processing Code - n6 (n2: TRANSACTION TYPE, n2: ACCOUNT TYPE 1, n2: ACCOUNT TYPE 2) // M 1100, 1200, 1220, 1420

            if (!isPosSiteConfiguration && !transactionType.equals(TransactionType.FileAction)) {
                DE3_ProcessingCode processingCode = mapProcessingCode(builder);
                request.set(DataElementId.DE_003, processingCode);
            }
            // DE 4: Amount, Transaction - n12 // C 1100, 1200, 1220, 1420
            request.set(DataElementId.DE_004, StringUtils.toNumeric(builder.getAmount(), 12));

            // DE 7: Date and Time, Transmission - n10 (MMDDhhmmss) // C
            request.set(DataElementId.DE_007, DateTime.now(DateTimeZone.UTC).toString(DATE_FORMAT_SPAN_10));

            // DE 11: System Trace Audit Number (STAN) - n6 // M
            int stan = getStan(builder.getSystemTraceAuditNumber());
            request.set(DataElementId.DE_011, StringUtils.padLeft(stan, 6, '0'));

            // DE 12: Date and Time, Transaction - n12 (YYMMDDhhmmss)
            String timestamp = getTimeStamp(builder);
            request.set(DataElementId.DE_012, timestamp);

            // DE 15: Date, Settlement - n6 (YYMMDD) // C
            // DE 17: Date, Capture - n4 (MMDD) // C

            // DE 18: Merchant Type - n4 // C 1100, 1200, 1220, 1300, 1320, 1420 (Same as MCC Code - Add to config since will be same for all transactions)
            request.set(DataElementId.DE_018, merchantType);

            // DE 19: Country Code, Acquiring Institution - n3 (ISO 3166) // C Config value perhaps? Same for each message

            /* DE 22: Point of Service Data Code - an12 //  M 1100, 1200, 1220, 1420 // C 1300, 1320 // O 1500, 1520
                22.1 CARD DATA INPUT CAPABILITY an1 The devices/methods available for card/check data input.
                22.2 CARDHOLDER AUTHENTICATION CAPABILITY an1 The methods available for authenticating the cardholder.
                22.3 CARD CAPTURE CAPABILITY an1 Indicates whether the POS application can retain the card if required to do so.
                22.4 OPERATING ENVIRONMENT an1 Indicates whether the POS application is attended by a clerk and the location of the POS application.
                    22.5 CARDHOLDER PRESENT an1 Indicates whether or not the cardholder is present and if not present then why.
                    22.6 CARD PRESENT an1 Indicates whether the card is present.
                    22.7 CARD DATA INPUT MODE an1 The method used for inputting the card data.
                    22.8 CARDHOLDER AUTHENTICATION METHOD an1 The method used for verifying the cardholder identity.
                22.9 CARDHOLDER AUTHENTICATION ENTITY an1 The entity used for verifying the cardholder identity.
                22.10 CARD DATA OUTPUT CAPABILITY an1 The methods available for updating the card data.
                22.11 TERMINAL OUTPUT CAPABILITY an1 The print/display capabilities of the POS.
                22.12 PIN CAPTURE CAPABILITY an1 Indicates whether the PIN data can be captured and if so the maximum PIN data length that can be captured.
             */

            if (!isPosSiteConfiguration) {
                dataCode = setDE22DataCode(dataCode);
                request.set(DataElementId.DE_022, dataCode);
            }
            // DE 23: Card Sequence Number - n3 // C 1100, 1120, 1200, 1220, 1420 (Applies to EMV cards if the sequence number is returned from the terminal)
            // DE 24: Function Code - n3 // M
            request.set(DataElementId.DE_024, mapFunctionCode(builder));

            // DE 25: Message Reason Code - n4 // C 1100, 1120, 1200, 1220, 1300, 1320, 1420, 16XX, 18XX

            // DE 28: Date, Reconciliation - n6 (YYMMDD)
            /* DE 30: Amounts, Original - n24
                30.1 ORIGINAL AMOUNT, TRANSACTION n12 A copy of amount, transaction (DE 4) from the original transaction.
                30.2 ORIGINAL AMOUNT, RECONCILIATION n12 A copy of amount, reconciliation (DE 5) from the original transaction. Since DE 5 is not used, this element will contain all zeros.
             */
            // DE 32: Acquiring Institution Identification Code - LLVAR n.. 11
            request.set(DataElementId.DE_032, acceptorConfig.getAcquiringInstitutionIdentificationCode());

            // DE 34: Primary Account Number, Extended - LLVAR ns.. 28

            // DE 37: Retrieval Reference Number - anp12
            request.set(DataElementId.DE_037, builder.getClientTransactionId());

            // DE 38: Approval Code - anp6
            request.set(DataElementId.DE_038, builder.getOfflineAuthCode());

            // DE 39: Action Code - n3

            // DE 41: Card Acceptor Terminal Identification Code - ans8
            String companyIdValue = builder.getCustomerId();
            if (StringUtils.isNullOrEmpty(companyIdValue)) {
                companyIdValue = companyId;
            }
            request.set(DataElementId.DE_041, StringUtils.padRight(companyIdValue, 8, ' '));

            // DE 42: Card Acceptor Identification Code - ans15
            request.set(DataElementId.DE_042, StringUtils.padRight(terminalId, 15, ' '));

            /* DE 43: Card Acceptor Name/Location - LLVAR ans.. 99
                43.1 NAME-STREET-CITY ans..83 Name\street\city\
                43.2 POSTAL-CODE ans10
                43.3 REGION ans3 Two letter state/province code for the United States and Canada. Refer to the Heartland Integrator’s Guide.
                43.4 COUNTRY-CODE a3 See A.30.1 ISO 3166-1: Country Codes, p. 809.
             */
            if (!isPosSiteConfiguration && acceptorConfig.getAddress() != null) {
                DE43_CardAcceptorData cardAcceptorData = new DE43_CardAcceptorData();
                cardAcceptorData.setAddress(acceptorConfig.getAddress());
                request.set(DataElementId.DE_043, cardAcceptorData);
            }

        /* DE 44: Additional Response Data - LLVAR ans.. 99
            44.1 ACTION REASON CODE n4 Contains the reason code for the action. A value of zeros indicates there is no action reason code.
            44.2 TEXT MESSAGE ans..95 Contains the text describing the action.
         */
        // DE 45: Track 1 Data - LLVAR ans.. 76
        /* DE 46: Amounts, Fees - LLLVAR ans..204
            46.1 FEE TYPE CODE n2
            46.2 CURRENCY CODE, FEE n3
            46.3 AMOUNT, FEE x+n8
            46.4 CONVERSION RATE, FEE n8
            46.5 AMOUNT, RECONCILIATION FEE x+n8
            46.6 CURRENCY CODE, RECONCILIATION FEE n3
         */

        if (!isPosSiteConfiguration && (paymentMethodType.equals(paymentMethodType.Debit) || paymentMethodType.equals(PaymentMethodType.EBT))) {
            if (builder.getFeeAmount() != null) {
                DE46_FeeAmounts feeAmounts = new DE46_FeeAmounts();
                feeAmounts.setFeeTypeCode(builder.getFeeType());
                feeAmounts.setCurrencyCode(currencyCode);
                feeAmounts.setAmount(builder.getFeeAmount());
                feeAmounts.setReconciliationCurrencyCode(currencyCode);
                request.set(DataElementId.DE_046, feeAmounts);
                if (feeAmounts.getFeeTypeCode() == FeeType.Surcharge) {
                    request.set(DataElementId.DE_004, StringUtils.toNumeric(builder.getAmount().add(feeAmounts.getAmount()), 12));
                }
            }
        }
        /* DE 48: Message Control - LLLVAR ans..999
            48-0 BIT MAP b8 C Specifies which data elements are present.
            48-1 COMMUNICATION DIAGNOSTICS n4 C Data on communication connection.
            48-2 HARDWARE & SOFTWARE CONFIGURATION ans20 C Version information from POS application.
            48-3 LANGUAGE CODE a2 F Language used for display or print.
            48-4 BATCH NUMBER n10 C Current batch.
            48-5 SHIFT NUMBER n3 C Identifies shift for reconciliation and tracking.
            48-6 CLERK ID LVAR an..9 C Identification of clerk operating the terminal.
            48-7 MULTIPLE TRANSACTION CONTROL n9 F Parameters to control multiple related messages.
            48-8 CUSTOMER DATA LLLVAR ns..250 C Data entered by customer or clerk.
            48-9 TRACK 2 FOR SECOND CARD LLVAR ns..37 C Used to specify the second card in a transaction by the Track 2 format.
            48-10 TRACK 1 FOR SECOND CARD LLVAR ans..76 C Used to specify the second card in a transaction by the Track 1 format.
            48-11 CARD TYPE anp4 C Card type.
            48-12 ADMINISTRATIVELY DIRECTED TASK b1 C Notice to or direction for action to be taken by POS application.
            48-13 RFID DATA LLVAR ans..99 C Data received from RFID transponder.
            48-14 PIN ENCRYPTION METHODOLOGY ans2 C Used to identify the type of encryption methodology.
            48-15, 48-32 RESERVED FOR ANSI USE LLVAR ans..99 These are reserved for future use.
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the Heartland system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
         */
        // DE48-5
//        messageControl.setShiftNumber(builder.getShiftNumber());
        if (!isPosSiteConfiguration) {
            DE48_MessageControl messageControl = mapMessageControl(builder);
            request.set(DataElementId.DE_048, messageControl);
        }
        // DE 49: Currency Code, Transaction - n3
        // DE 50: Currency Code, Reconciliation - n3
            /* DE 44: Additional Response Data - LLVAR ans.. 99
                44.1 ACTION REASON CODE n4 Contains the reason code for the action. A value of zeros indicates there is no action reason code.
                44.2 TEXT MESSAGE ans..95 Contains the text describing the action.
             */
            // DE 45: Track 1 Data - LLVAR ans.. 76
            /* DE 46: Amounts, Fees - LLLVAR ans..204
                46.1 FEE TYPE CODE n2
                46.2 CURRENCY CODE, FEE n3
                46.3 AMOUNT, FEE x+n8
                46.4 CONVERSION RATE, FEE n8
                46.5 AMOUNT, RECONCILIATION FEE x+n8
                46.6 CURRENCY CODE, RECONCILIATION FEE n3
             */
            if (builder.getFeeAmount() != null) {
                DE46_FeeAmounts feeAmounts = new DE46_FeeAmounts();
                setFeeAmountdata(builder, currencyCode, feeAmounts);
                request.set(DataElementId.DE_046, feeAmounts);
                if (feeAmounts.getFeeTypeCode() == FeeType.Surcharge) {
                    request.set(DataElementId.DE_004, StringUtils.toNumeric(builder.getAmount().add(feeAmounts.getAmount()), 12));
                }
            }

            /* DE 48: Message Control - LLLVAR ans..999
                48-0 BIT MAP b8 C Specifies which data elements are present.
                48-1 COMMUNICATION DIAGNOSTICS n4 C Data on communication connection.
                48-2 HARDWARE & SOFTWARE CONFIGURATION ans20 C Version information from POS application.
                48-3 LANGUAGE CODE a2 F Language used for display or print.
                48-4 BATCH NUMBER n10 C Current batch.
                48-5 SHIFT NUMBER n3 C Identifies shift for reconciliation and tracking.
                48-6 CLERK ID LVAR an..9 C Identification of clerk operating the terminal.
                48-7 MULTIPLE TRANSACTION CONTROL n9 F Parameters to control multiple related messages.
                48-8 CUSTOMER DATA LLLVAR ns..250 C Data entered by customer or clerk.
                48-9 TRACK 2 FOR SECOND CARD LLVAR ns..37 C Used to specify the second card in a transaction by the Track 2 format.
                48-10 TRACK 1 FOR SECOND CARD LLVAR ans..76 C Used to specify the second card in a transaction by the Track 1 format.
                48-11 CARD TYPE anp4 C Card type.
                48-12 ADMINISTRATIVELY DIRECTED TASK b1 C Notice to or direction for action to be taken by POS application.
                48-13 RFID DATA LLVAR ans..99 C Data received from RFID transponder.
                48-14 PIN ENCRYPTION METHODOLOGY ans2 C Used to identify the type of encryption methodology.
                48-15, 48-32 RESERVED FOR ANSI USE LLVAR ans..99 These are reserved for future use.
                48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the Heartland system capabilities and configuration of the POS application.
                48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
                48-35 NAME 1 LLVAR ans..99 D
                48-36 NAME 2 LLVAR ans..99 D
                48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
                48-38 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
                48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
                48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
                48-50, 48-64 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
             */
            // DE48-5
            if (!isPosSiteConfiguration) {
                DE48_MessageControl messageControl = mapMessageControl(builder);
                request.set(DataElementId.DE_048, messageControl);
            }
            // DE 49: Currency Code, Transaction - n3
            // DE 50: Currency Code, Reconciliation - n3

            /* DE 54: Amounts, Additional - LLVAR ans..120
                54.1 ACCOUNT TYPE, ADDITIONAL AMOUNTS n2 Positions 3 and 4 or positions 5 and 6 of the processing code data element.
                54.2 AMOUNT TYPE, ADDITIONAL AMOUNTS n2 Identifies the purpose of the transaction amounts.
                54.3 CURRENCY CODE, ADDITIONAL AMOUNTS n3 Use DE 49 codes.
                54.4 AMOUNT, ADDITIONAL AMOUNTS x + n12 See Use of the Terms Credit and Debit under Table 1-2 Transaction Processing, p. 61.
             */
            if (builder.getCashBackAmount() != null || transactionType.equals(TransactionType.BenefitWithdrawal)) {
                DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
                if (paymentMethod instanceof GiftCard) {
                    amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashCardAccount, currencyCode, builder.getCashBackAmount());
                    amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashCardAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
                } else if (paymentMethod instanceof EBT) {
                    if (transactionType.equals(TransactionType.BenefitWithdrawal)) {
                        amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getAmount());
                    } else {
                        amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getCashBackAmount());
                        amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
                    }
                } else if (paymentMethod instanceof Debit) {
                    amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.PinDebitAccount, currencyCode, builder.getCashBackAmount());
                    amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.PinDebitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
                }

                if (amountsAdditional.size() > 0) {
                    request.set(DataElementId.DE_054, amountsAdditional);
                }
            }
            else if (paymentMethod instanceof Credit && isVisaFleet2) {
                Credit card = (Credit) paymentMethod;
                DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
                if (builder.getProductData() != null) {
                    DE63_ProductData productData = builder.getProductData().toDataElement();
                    if (card.getCardType().equals("VisaFleet") && (isVisaFleet2)) {
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelAmount());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        }
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelWithTax());
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelAmount());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        }
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.TAXRATEFORFUEL, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(1));
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.TAXRATEFORFUEL, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        }
                        request.set(DataElementId.DE_054, amountsAdditional);
                    }
                }
            }
            else if (paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals(cardTypeWexFleet) && builder.getSalesTaxAdditionAmount()!=null) {
                DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.Unspecified, currencyCode, builder.getSalesTaxAdditionAmount());
                request.set(DataElementId.DE_054, amountsAdditional);
            }
            // DE 55: Integrated Circuit Card (ICC) Data - LLLVAR b..512
            if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
                if(tagData.getCardSequenceNumber() != null) { request.set(DataElementId.DE_023, StringUtils.padLeft(tagData.getCardSequenceNumber(), 3, '0'));}
                request.set(DataElementId.DE_055, tagData.getSendBuffer());
            }

            /* DE 56: Original Data Elements - LLVAR n..35
                56.1 Original message type identifier n4
                56.2 Original system trace audit number n6
                56.3 Original date and time, local transaction n12
                56.4 Original acquiring institution identification code LLVAR n..11
             */
            // DE 58: Authorizing Agent Institution Identification Code - LLVAR n..11

            // DE 59: Transport Data - LLLVAR ans..999
            request.set(DataElementId.DE_059, builder.getTransportData());

            // DE 62: Card Issuer Data - LLLVAR ans..999
            if (!isPosSiteConfiguration) {
                DE62_CardIssuerData cardIssuerData = mapCardIssuerData(builder);
                request.set(DataElementId.DE_062, cardIssuerData);
            }

            // DE 63: Product Data - LLLVAR ans…999
            if (builder.getProductData() != null) {
                DE63_ProductData productData = builder.getProductData().toDataElement();
                request.set(DataElementId.DE_063, productData);
            }
            if (paymentMethod instanceof eCheck) {
                eCheck check = (eCheck) paymentMethod;
                if(builder.getCheckCustomerId() != null){request.set(DataElementId.DE_102, builder.getCheckCustomerId());}

                if (builder.getRawMICRData() == null) {
                    DE103_Check_MICR_Data checkData = setCheckData(builder, check);
                    if (checkData.toByteArray().length <= 28) {
                        request.set(DataElementId.DE_103, checkData.toByteArray());
                    }
                }
            }

            if (builder.getPosSiteConfigurationData() != null) {
                DE72_DataRecord dataRecord = new DE72_DataRecord(builder.getPosSiteConfigurationData());
                request.set(DataElementId.DE_072, dataRecord.toByteArray());
            }

            // DE 72: Data Record - LLLVAR ans..999
            // DE 73: Date, Action - n6 (YYMMDD)
            // DE 96: Key Management Data - LLLVAR b..999
            // DE 97: Amount, Net Reconciliation - x + n16
            // DE 102: Account Identification 1 - LLVAR ans..28
            // DE 103: Check MICR Data (Account Identification 2) - LLVAR ans..28
            // DE 115: eWIC Overflow Data - LLLVAR ansb..999
            // DE 116: eWIC Overflow Data - LLLVAR ansb..999n
            // DE 117: eWIC Data - LLLVAR ansb..999
            // DE 123: Reconciliation Totals - LLLVAR ans..999
            // DE 124: Sundry Data - LLLVAR ans..999
            // DE 125: Extended Response Data 1 - LLLVAR ans..999
            // DE 126: Extended Response Data 2 - LLLVAR ans..999

            // DE 127: Forwarding Data - LLLVAR ans..999
            DE127_ForwardingData forwardingData = new DE127_ForwardingData();
            EncryptionData encryptionData = null;
            if (paymentMethod instanceof IEncryptable) {
                encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
                if(encryptionData != null){
                    forwardingData = set3DSEncryptedData(encryptionData, forwardingData, paymentMethod);
                    request.set(DataElementId.DE_127, forwardingData);
                }
            }
            if (paymentMethod instanceof ICardData) {
                ICardData cardData = (ICardData) paymentMethod;
                if (cardData != null) {
                    String tokenizationData = cardData.getTokenizationData();
                    encryptionData = ((IEncryptable) cardData).getEncryptionData();
                    boolean combinedMessage = false;
                    if(tokenizationData != null && encryptionData != null){
                        combinedMessage = true;
                    }
                    if (tokenizationData != null) {
                        setTokenizationData(forwardingData,paymentMethod,tokenizationData,combinedMessage);
                        request.set(DataElementId.DE_127, forwardingData);
                    }
                }
            } else if (paymentMethod instanceof ITrackData) {
                ITrackData trackData = (ITrackData) paymentMethod;
                if (trackData != null) {
                    String tokenizationData = trackData.getTokenizationData();
                    boolean combinedMessage = tokenizationData!=null && encryptionData!=null;
                    if (tokenizationData != null) {
                        setTokenizationData(forwardingData,paymentMethod,tokenizationData,combinedMessage);
                        request.set(DataElementId.DE_127, forwardingData);
                    }
                }
            }
            // EBT EWIC
            DE117_WIC_Data_Fields ewicData = seteWicData(builder);
            if (builder.getEwicData() != null) {
                request.set(DataElementId.DE_117, ewicData);
            }

            return sendRequest(request, builder, orgCorr1, orgCorr2);
        }
        throw new BuilderException("Builder can't be Null");
    }

    private static String getTimeStamp(AuthorizationBuilder builder) {
        String timestamp = builder.getTimestamp();
        if (StringUtils.isNullOrEmpty(timestamp)) {
            timestamp = DateTime.now(DateTimeZone.UTC).toString(DATE_FORMAT_SPAN_12);
        }
        return timestamp;
    }

    private int getStan(int stan) {
        if (stan == 0 && stanProvider != null) {
            stan = stanProvider.generateStan();
        }
        return stan;
    }

    private static void setEmvTagData(EmvData tagData, DE22_PosDataCode dataCode, ITrackData card) {
        if (tagData.isContactlessMsd()) {
            dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
        } else {
            dataCode.setCardDataInputMode(card.getEntryMethod().equals(EntryMethod.Proximity) ? DE22_CardDataInputMode.ContactlessEmv : DE22_CardDataInputMode.ContactEmv);
        }
    }

    private static void setDataCodeForCardOnFile(DE22_PosDataCode dataCode, DE22_CardDataInputMode cardDataInputMode) {
        if (cardDataInputMode != null && cardDataInputMode.equals(DE22_CardDataInputMode.CredentialOnFile)) {
            dataCode.setCardDataInputMode(DE22_CardDataInputMode.CredentialOnFile);
            dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_NotPresent_Internet);
        }
    }

    private static void setFeeAmountdata(AuthorizationBuilder builder, Iso4217_CurrencyCode currencyCode, DE46_FeeAmounts feeAmounts) {
        feeAmounts.setFeeTypeCode(builder.getFeeType());
        feeAmounts.setCurrencyCode(currencyCode);
        feeAmounts.setAmount(builder.getFeeAmount());
        feeAmounts.setReconciliationCurrencyCode(currencyCode);
    }

    private static DE117_WIC_Data_Fields seteWicData(AuthorizationBuilder builder) {
        DE117_WIC_Data_Fields ewicData = new DE117_WIC_Data_Fields();
        if (builder.getEwicData() != null) {
            ewicData = builder.getEwicData().toDataElement();
        }
        return ewicData;
    }

    private static void setTokenizationOperationType(DE127_ForwardingData forwardingData, TokenizationOperationType tokenOperationType) {
        if (tokenOperationType != null) {
            forwardingData.setTokenizationOperationType(tokenOperationType);
            switch (tokenOperationType) {
                case Tokenize:
                case UpdateToken: {
                    forwardingData.setTokenizedFieldMatrix(TokenizedFieldMatrix.AccountNumber);
                }
                break;
                case DeTokenize: {
                    forwardingData.setTokenizedFieldMatrix(TokenizedFieldMatrix.TokenizedData);
                }
                break;
                default:
                    forwardingData.setTokenizedFieldMatrix(TokenizedFieldMatrix.TokenizedData);
            }
        }
    }

    private  DE127_ForwardingData set3DSEncryptedData( EncryptionData encryptionData, DE127_ForwardingData forwardingData ,IPaymentMethod paymentMethod) {

        EncryptionType encryptionType = acceptorConfig.getSupportedEncryptionType();
        if (encryptionType.equals(EncryptionType.TDES)) {
            forwardingData.setServiceType(acceptorConfig.getServiceType());
            forwardingData.setOperationType(acceptorConfig.getOperationType());
        }
        EncryptedFieldMatrix encryptedField = getEncryptionField(paymentMethod, encryptionType);
        if(encryptedField!= null) {
            forwardingData.setEncryptedField(encryptedField);
        }
        forwardingData.addEncryptionData(encryptionType, encryptionData);

        return forwardingData;
    }

    private static DE103_Check_MICR_Data setCheckData(AuthorizationBuilder builder,eCheck check) {
        DE103_Check_MICR_Data checkData = new DE103_Check_MICR_Data();
        checkData.setAccountNumber(check.getAccountNumber());
        checkData.setTransitNumber(check.getRoutingNumber());
        checkData.setSequenceNumber(check.getCheckNumber());

        return checkData;
    }

    private DE22_PosDataCode setDE22DataCode(DE22_PosDataCode dataCode) {
        dataCode.setCardDataInputCapability(acceptorConfig.getCardDataInputCapability());
        dataCode.setCardHolderAuthenticationCapability(acceptorConfig.getCardHolderAuthenticationCapability());
        dataCode.setCardCaptureCapability(acceptorConfig.isCardCaptureCapability());
        dataCode.setOperatingEnvironment(acceptorConfig.getOperatingEnvironment());
        dataCode.setCardHolderAuthenticationEntity(acceptorConfig.getCardHolderAuthenticationEntity());
        dataCode.setCardDataOutputCapability(acceptorConfig.getCardDataOutputCapability());
        dataCode.setTerminalOutputCapability(acceptorConfig.getTerminalOutputCapability());
        dataCode.setPinCaptureCapability(acceptorConfig.getPinCaptureCapability());
        return dataCode;
    }

    private static void setPosCodeDataForEcheck(IPaymentMethod paymentMethod, DE22_PosDataCode dataCode) {
        if (paymentMethod instanceof eCheck) {
            dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
            dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
            dataCode.setCardPresence(DE22_CardPresence.CardPresent);
        }
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        if (builder != null) {
            validate(builder);

            byte[] orgCorr1 = new byte[2];
            byte[] orgCorr2 = new byte[8];

            NetworkMessage request = new NetworkMessage();
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            TransactionType transactionType = builder.getTransactionType();
            Iso4217_CurrencyCode currencyCode = Iso4217_CurrencyCode.USD;
            EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
            String currency = builder.getCurrency();
            currencyCode = getCurrencyCode(currencyCode, currency);
            BigDecimal transactionAmount = builder.getAmount();
            boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getVisaFleet2()!=null && acceptorConfig.getVisaFleet2();
            if (transactionAmount == null && paymentMethod instanceof TransactionReference) {
                TransactionReference transactionReference = (TransactionReference) paymentMethod;
                transactionAmount = transactionReference.getOriginalApprovedAmount();
            }

            // MTI
            String mti = mapMTI(builder);
            request.setMessageTypeIndicator(mti);

            // pos data code
            DE22_PosDataCode dataCode = new DE22_PosDataCode();

            // DE 1: Secondary Bitmap - b8 // M
            request.set(DataElementId.DE_001, new byte[8]);

            // DE 2: Primary Account Number (PAN) - LLVAR // 1100, 1200, 1220, 1300, 1310, 1320, 1420
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference transactionReference = (TransactionReference) paymentMethod;

                // Original Card Data
                if (transactionReference.getOriginalPaymentMethod() != null) {
                    IPaymentMethod originalPaymentMethod = transactionReference.getOriginalPaymentMethod();
                    PaymentMethodType paymentMethodType = originalPaymentMethod.getPaymentMethodType();

                    if (originalPaymentMethod instanceof ICardData) {
                        ICardData cardData = (ICardData) originalPaymentMethod;
                        String token = cardData.getTokenizationData();
                        if (token == null) {
                            // DE 2: PAN & DE 14 Expiry
                            request.set(DataElementId.DE_002, cardData.getNumber());
                            request.set(DataElementId.DE_014, formatExpiry(cardData.getShortExpiry()));
                        }
                        // Data codes
                        dataCode.setCardDataInputMode(cardData.isReaderPresent() ? DE22_CardDataInputMode.KeyEntry : DE22_CardDataInputMode.Manual);
                        dataCode.setCardHolderPresence(cardData.isCardPresent() ? DE22_CardHolderPresence.CardHolder_Present : DE22_CardHolderPresence.CardHolder_NotPresent);
                        dataCode.setCardPresence(cardData.isCardPresent() ? DE22_CardPresence.CardPresent : DE22_CardPresence.CardNotPresent);
                        DE22_CardDataInputMode cardDataInputMode = acceptorConfig.getCardDataInputMode();
                        setDataCodeForCardOnFile(dataCode, cardDataInputMode);

                    } else if (originalPaymentMethod instanceof ITrackData) {
                        ITrackData track = (ITrackData) originalPaymentMethod;

                        if (track instanceof IEncryptable && ((IEncryptable) track).getEncryptionData() != null) {
                            EncryptionData encryptionData = ((IEncryptable) track).getEncryptionData();
                            if (encryptionData.getTrackNumber() != null) {
                                if (encryptionData.getTrackNumber().equals("1")) {
                                    request.set(DataElementId.DE_045, track.getValue());
                                } else if (encryptionData.getTrackNumber().equals("2")) {
                                    request.set(DataElementId.DE_035, track.getValue());
                                }
                            }
                            request.set(DataElementId.DE_002, track.getPan());
                            request.set(DataElementId.DE_014, track.getExpiry());
                        } else {
                            // DE 2: PAN & DE 14 Expiry
                            request.set(DataElementId.DE_002, track.getPan());
                            request.set(DataElementId.DE_014, track.getExpiry());
                        }

                        // set data codes
                        if (paymentMethodType.equals(PaymentMethodType.Credit) || paymentMethodType.equals(PaymentMethodType.Debit)) {
                            dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                            dataCode.setCardPresence(DE22_CardPresence.CardPresent);

                            if (tagData != null) {
                                if (tagData.isContactlessMsd()) {
                                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                                } else {
                                    dataCode.setCardDataInputMode(track.getEntryMethod().equals(EntryMethod.Proximity) ? DE22_CardDataInputMode.ContactlessEmv : DE22_CardDataInputMode.ContactEmv);
                                }
                            } else {
                                if (track.getEntryMethod().equals(EntryMethod.Proximity)) {
                                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                                } else {
                                    dataCode.setCardDataInputMode(builder.getEmvChipCondition() != null ? DE22_CardDataInputMode.MagStripe_Fallback : DE22_CardDataInputMode.UnalteredTrackData);
                                }
                            }
                        }
                    } else if (originalPaymentMethod instanceof GiftCard) {
                        GiftCard gift = (GiftCard) originalPaymentMethod;

                        // DE 35 / DE 45
                        if (gift.getValueType()!= null && gift.getValueType().equals("TrackData")) {
                            if (gift.getTrackNumber().equals(TrackNumber.TrackTwo)) {
                                request.set(DataElementId.DE_035, gift.getTrackData());
                            } else {
                                request.set(DataElementId.DE_045, gift.getTrackData());
                            }
                        } else {
                            // DE 2: PAN & DE 14 Expiry
                            request.set(DataElementId.DE_002, gift.getNumber());
                        }

                        // set data codes
                        if (!StringUtils.isNullOrEmpty(gift.getPin())) {
                            dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
                        } else {
                            dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.NotAuthenticated);
                        }
                        dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
                        dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                        dataCode.setCardPresence(DE22_CardPresence.CardPresent);
                    }
                }
            }
            boolean isNeitherBatchCloseNorTimeRequest = !transactionType.equals(TransactionType.TimeRequest);
            if (isNeitherBatchCloseNorTimeRequest) {
                // DE 3: Processing Code - n6 (n2: TRANSACTION TYPE, n2: ACCOUNT TYPE 1, n2: ACCOUNT TYPE 2) // M 1100, 1200, 1220, 1420
                DE3_ProcessingCode processingCode = mapProcessingCode(builder);
                request.set(DataElementId.DE_003, processingCode);

                // DE 4: Amount, Transaction - n12 // C 1100, 1200, 1220, 1420
                BigDecimal amount = builder.getAmount();
                if (amount == null && paymentMethod instanceof TransactionReference) {
                    TransactionReference transactionReference = (TransactionReference) paymentMethod;
                    amount = transactionReference.getOriginalAmount();
                }
                request.set(DataElementId.DE_004, StringUtils.toNumeric(amount, 12));

                // DE 7: Date and Time, Transmission - n10 (MMDDhhmmss) // C
                request.set(DataElementId.DE_007, DateTime.now(DateTimeZone.UTC).toString(DATE_FORMAT_SPAN_10));
            }
            // DE 11: System Trace Audit Number (STAN) - n6 // M
            int stan = getStan(builder.getSystemTraceAuditNumber());

            request.set(DataElementId.DE_011, StringUtils.padLeft(stan, 6, '0'));

            // DE 12: Date and Time, Transaction - n12 (YYMMDDhhmmss)
            String timestamp = builder.getTimestamp();
            if (StringUtils.isNullOrEmpty(timestamp)) {
                timestamp = DateTime.now(DateTimeZone.UTC).toString(DATE_FORMAT_SPAN_12);
            }
            request.set(DataElementId.DE_012, timestamp);

            // DE 15: Date, Settlement - n6 (YYMMDD) // C
            // DE 17: Date, Capture - n4 (MMDD) // C
            if (transactionType.equals(TransactionType.Capture) || transactionType.equals(TransactionType.PreAuthCompletion)) {
                if (paymentMethod instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) paymentMethod;
                    if (reference.getOriginalPaymentMethod() instanceof GiftCard) {
                        String cardType = ((GiftCard) reference.getOriginalPaymentMethod()).getCardType();
                        if (!cardType.equals("ValueLink")) {
                            request.set(DataElementId.DE_017, DateTime.now(DateTimeZone.UTC).toString("MMdd"));
                        }
                    } else {
                        request.set(DataElementId.DE_017, DateTime.now(DateTimeZone.UTC).toString("MMdd"));
                    }
                } else {
                    request.set(DataElementId.DE_017, DateTime.now(DateTimeZone.UTC).toString("MMdd"));
                }
            }

            // DE 18: Merchant Type - n4 // C 1100, 1200, 1220, 1300, 1320, 1420 (Same as MCC Code - Add to config since will be same for all transactions)
            request.set(DataElementId.DE_018, merchantType);

            // DE 19: Country Code, Acquiring Institution - n3 (ISO 3166) // C Config value perhaps? Same for each message
            if (isNeitherBatchCloseNorTimeRequest) {
        /* DE 22: Point of Service Data Code - an12 //  M 1100, 1200, 1220, 1420 // C 1300, 1320 // O 1500, 1520
            22.1 CARD DATA INPUT CAPABILITY an1 The devices/methods available for card/check data input.
            22.2 CARDHOLDER AUTHENTICATION CAPABILITY an1 The methods available for authenticating the cardholder.
            22.3 CARD CAPTURE CAPABILITY an1 Indicates whether the POS application can retain the card if required to do so.
            22.4 OPERATING ENVIRONMENT an1 Indicates whether the POS application is attended by a clerk and the location of the POS application.
                22.5 CARDHOLDER PRESENT an1 Indicates whether or not the cardholder is present and if not present then why.
                22.6 CARD PRESENT an1 Indicates whether the card is present.
                22.7 CARD DATA INPUT MODE an1 The method used for inputting the card data.
                22.8 CARDHOLDER AUTHENTICATION METHOD an1 The method used for verifying the cardholder identity.
            22.9 CARDHOLDER AUTHENTICATION ENTITY an1 The entity used for verifying the cardholder identity.
            22.10 CARD DATA OUTPUT CAPABILITY an1 The methods available for updating the card data.
            22.11 TERMINAL OUTPUT CAPABILITY an1 The print/display capabilities of the POS.
            22.12 PIN CAPTURE CAPABILITY an1 Indicates whether the PIN data can be captured and if so the maximum PIN data length that can be captured.
         */
                dataCode = setDE22DataCode(dataCode);
                if (paymentMethod instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) paymentMethod;

                    String originalPosDataCode = reference.getPosDataCode();
                    if (!StringUtils.isNullOrEmpty(originalPosDataCode)) {
                        dataCode.fromByteArray(originalPosDataCode.getBytes());
                    }
                }
                request.set(DataElementId.DE_022, dataCode);
            }
            // DE 23: Card Sequence Number - n3 // C 1100, 1120, 1200, 1220, 1420 (Applies to EMV cards if the sequence number is returned from the terminal)
            // DE 24: Function Code - n3 // M
            request.set(DataElementId.DE_024, mapFunctionCode(builder));

            // DE 25: Message Reason Code - n4 // C 1100, 1120, 1200, 1220, 1300, 1320, 1420, 16XX, 18XX
            DE25_MessageReasonCode reasonCode = mapMessageReasonCode(builder);
            request.set(DataElementId.DE_025, reasonCode);

            // DE 28: Date, Reconciliation - n6 (YYMMDD)
            if (transactionType.equals(TransactionType.BatchClose)) {
                request.set(DataElementId.DE_028, DateTime.now(DateTimeZone.UTC).toString("yyMMdd"));
            }

        /* DE 30: Amounts, Original - n24
            30.1 ORIGINAL AMOUNT, TRANSACTION n12 A copy of amount, transaction (DE 4) from the original transaction.
            30.2 ORIGINAL AMOUNT, RECONCILIATION n12 A copy of amount, reconciliation (DE 5) from the original transaction. Since DE 5 is not used, this element will contain all zeros.
         */
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;
                if (reference.getOriginalAmount() != null) {
                    BigDecimal amount = builder.getAmount();
                    if (amount != null) {
                        DE30_OriginalAmounts originalAmounts = new DE30_OriginalAmounts();

                        if (!AmountUtils.areEqual(amount, reference.getOriginalAmount())) {
                            originalAmounts.setOriginalTransactionAmount(reference.getOriginalAmount());
                            request.set(DataElementId.DE_030, originalAmounts);
                        } else if (reference.getOriginalPaymentMethod() instanceof Debit
                                && (transactionType.equals(TransactionType.PreAuthCompletion) || transactionType.equals(TransactionType.Capture))) {
                            originalAmounts.setOriginalTransactionAmount(reference.getOriginalAmount());
                            request.set(DataElementId.DE_030, originalAmounts);

                        } else if (reference.getOriginalPaymentMethod() instanceof Credit) {
                            Credit credit = (Credit) reference.getOriginalPaymentMethod();
                            if (credit.getCardType().equals(cardTypeWexFleet) && transactionType.equals(TransactionType.Capture)) {
                                originalAmounts.setOriginalTransactionAmount(reference.getOriginalApprovedAmount());
                                request.set(DataElementId.DE_030, originalAmounts);
                            }
                        }
                    }
                }
            }

            // DE 32: Acquiring Institution Identification Code - LLVAR n.. 11
            request.set(DataElementId.DE_032, acceptorConfig.getAcquiringInstitutionIdentificationCode());

            // DE 34: Primary Account Number, Extended - LLVAR ns.. 28

            // DE 37: Retrieval Reference Number - anp12
            request.set(DataElementId.DE_037, builder.getClientTransactionId());

            // DE 38: Approval Code - anp6
            request.set(DataElementId.DE_038, builder.getAuthorizationCode());

            // DE 39: Action Code - n3

            // DE 41: Card Acceptor Terminal Identification Code - ans8
            String companyIdValue = builder.getCompanyId();
            if (StringUtils.isNullOrEmpty(companyIdValue)) {
                companyIdValue = companyId;
            }
            request.set(DataElementId.DE_041, StringUtils.padRight(companyIdValue, 8, ' '));

            // DE 42: Card Acceptor Identification Code - ans15
            request.set(DataElementId.DE_042, StringUtils.padRight(terminalId, 15, ' '));

        /* DE 43: Card Acceptor Name/Location - LLVAR ans.. 99
            43.1 NAME-STREET-CITY ans..83 Name\street\city\
            43.2 POSTAL-CODE ans10
            43.3 REGION ans3 Two letter state/province code for the United States and Canada. Refer to the Heartland Integrator’s Guide.
            43.4 COUNTRY-CODE a3 See A.30.1 ISO 3166-1: Country Codes, p. 809.
         */
            if (acceptorConfig.getAddress() != null) {
                DE43_CardAcceptorData cardAcceptorData = new DE43_CardAcceptorData();
                cardAcceptorData.setAddress(acceptorConfig.getAddress());
                request.set(DataElementId.DE_043, cardAcceptorData);
            }

        /* DE 44: Additional Response Data - LLVAR ans.. 99
            44.1 ACTION REASON CODE n4 Contains the reason code for the action. A value of zeros indicates there is no action reason code.
            44.2 TEXT MESSAGE ans..95 Contains the text describing the action.
         */
            // DE 45: Track 1 Data - LLVAR ans.. 76
        /* DE 46: Amounts, Fees - LLLVAR ans..204
            46.1 FEE TYPE CODE n2
            46.2 CURRENCY CODE, FEE n3
            46.3 AMOUNT, FEE x+n8
            46.4 CONVERSION RATE, FEE n8
            46.5 AMOUNT, RECONCILIATION FEE x+n8
            46.6 CURRENCY CODE, RECONCILIATION FEE n3
         */
        /* DE 48: Message Control - LLLVAR ans..999
            48-0 BIT MAP b8 C Specifies which data elements are present.
            48-1 COMMUNICATION DIAGNOSTICS n4 C Data on communication connection.
            48-2 HARDWARE & SOFTWARE CONFIGURATION ans20 C Version information from POS application.
            48-3 LANGUAGE CODE a2 F Language used for display or print.
            48-4 BATCH NUMBER n10 C Current batch.
            48-5 SHIFT NUMBER n3 C Identifies shift for reconciliation and tracking.
            48-6 CLERK ID LVAR an..9 C Identification of clerk operating the terminal.
            48-7 MULTIPLE TRANSACTION CONTROL n9 F Parameters to control multiple related messages.
            48-8 CUSTOMER DATA LLLVAR ns..250 C Data entered by customer or clerk.
            48-9 TRACK 2 FOR SECOND CARD LLVAR ns..37 C Used to specify the second card in a transaction by the Track 2 format.
            48-10 TRACK 1 FOR SECOND CARD LLVAR ans..76 C Used to specify the second card in a transaction by the Track 1 format.
            48-11 CARD TYPE anp4 C Card type.
            48-12 ADMINISTRATIVELY DIRECTED TASK b1 C Notice to or direction for action to be taken by POS application.
            48-13 RFID DATA LLVAR ans..99 C Data received from RFID transponder.
            48-14 PIN ENCRYPTION METHODOLOGY ans2 C Used to identify the type of encryption methodology.
            48-15, 48-32 RESERVED FOR ANSI USE LLVAR ans..99 These are reserved for future use.
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the Heartland system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
         */
            if (mti.equals("1100") || mti.equals("1200") || mti.equals("1220") || mti.equals("1520") || mti.equals("1420")) {
                DE48_MessageControl messageControl = mapMessageControl(builder);
                request.set(DataElementId.DE_048, messageControl);
            }
            // DE 49: Currency Code, Transaction - n3
            // DE 50: Currency Code, Reconciliation - n3
            if (!currencyCode.equals(Iso4217_CurrencyCode.USD) && transactionAmount != null)
                request.set(DataElementId.DE_049, currencyCode.getValue());
            // DE 50: Currency Code, Reconciliation - n3
            if (transactionType.equals(TransactionType.BatchClose) && !currencyCode.equals(Iso4217_CurrencyCode.USD)) {
                request.set(DataElementId.DE_050, currencyCode.getValue());
            }

            // DE 52: Personal Identification Number (PIN)
            if (paymentMethod instanceof TransactionReference) {
                IPaymentMethod originalPaymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
                if (originalPaymentMethod instanceof EBT && transactionType.equals(TransactionType.Refund)) {
                    String pinBlock = ((EBT) originalPaymentMethod).getPinBlock();
                    if (!StringUtils.isNullOrEmpty(pinBlock)) {
                        // DE 52: Personal Identification Number (PIN) Data - b8
                        request.set(DataElementId.DE_052, StringUtils.bytesFromHex(pinBlock.substring(0, 16)));

                        // DE 53: Security Related Control Information - LLVAR an..48
                        request.set(DataElementId.DE_053, pinBlock.substring(16));
                    }
                }
            }
        /* DE 54: Amounts, Additional - LLVAR ans..120
            54.1 ACCOUNT TYPE, ADDITIONAL AMOUNTS n2 Positions 3 and 4 or positions 5 and 6 of the processing code data element.
            54.2 AMOUNT TYPE, ADDITIONAL AMOUNTS n2 Identifies the purpose of the transaction amounts.
            54.3 CURRENCY CODE, ADDITIONAL AMOUNTS n3 Use DE 49 codes.
            54.4 AMOUNT, ADDITIONAL AMOUNTS x + n12 See Use of the Terms Credit and Debit under Table 1-2 Transaction Processing, p. 61.
         */
        if (builder.getCashBackAmount() != null && transactionType.equals(TransactionType.Reversal)) {
            DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
            setAdditionalAmount(builder, paymentMethod, currencyCode, amountsAdditional);
            request.set(DataElementId.DE_054, amountsAdditional);
        }
        else if (paymentMethod instanceof Credit) {
                Credit card = (Credit) paymentMethod;
                DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
            if (builder.getProductData() != null) {
                DE63_ProductData productData = builder.getProductData().toDataElement();

                    if (!StringUtils.isNullOrEmpty(card.getCardType()) && card.getCardType().equals("VisaFleet") && (isVisaFleet2)) {
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelAmount());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                        }
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelWithTax());
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelAmount());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        }
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.TAXRATEFORFUEL, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(1));
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.TAXRATEFORFUEL, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                        }
                        request.set(DataElementId.DE_054, amountsAdditional);
                    }
                }
            }

            // DE 55: Integrated Circuit Card (ICC) Data - LLLVAR b..512

            if(!StringUtils.isNullOrEmpty(builder.getTagData())) {
                if(!StringUtils.isNullOrEmpty(tagData.getCardSequenceNumber())) {
                    String cardSequenceNumber = StringUtils.padLeft(tagData.getCardSequenceNumber(), 3, '0');
                    request.set(DataElementId.DE_023, cardSequenceNumber);
                }
                boolean isPreauthOrCapture = transactionType.equals(TransactionType.PreAuthCompletion);
                if(!(isPreauthOrCapture) || (mapCardType(paymentMethod).equals(DE48_CardType.WEX) && isPreauthOrCapture)) {
                    request.set(DataElementId.DE_055, tagData.getSendBuffer());
                }
            }

        /* DE 56: Original Data Elements - LLVAR n..35
            56.1 Original message type identifier n4
            56.2 Original system trace audit number n6
            56.3 Original date and time, local transaction n12
            56.4 Original acquiring institution identification code LLVAR n..11
         */
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;

                // check that we have enough
                if (!StringUtils.isNullOrEmpty(reference.getMessageTypeIndicator())
                        && !StringUtils.isNullOrEmpty(reference.getSystemTraceAuditNumber())
                        && !StringUtils.isNullOrEmpty(reference.getOriginalTransactionTime())) {
                    DE56_OriginalDataElements originalDataElements = new DE56_OriginalDataElements();
                    originalDataElements.setMessageTypeIdentifier(reference.getMessageTypeIndicator());
                    originalDataElements.setSystemTraceAuditNumber(reference.getSystemTraceAuditNumber());
                    originalDataElements.setTransactionDateTime(reference.getOriginalTransactionTime());
                    originalDataElements.setAcquiringInstitutionId(reference.getAcquiringInstitutionId());

                    request.set(DataElementId.DE_056, originalDataElements);
                }

            }
            // DE 58: Authorizing Agent Institution Identification Code - LLVAR n..11

            // DE 59: Transport Data - LLLVAR ans..999
            request.set(DataElementId.DE_059, builder.getTransportData());

            // DE 62: Card Issuer Data - LLLVAR ans..999
            if (!transactionType.equals(TransactionType.BatchClose) && !transactionType.equals(TransactionType.TimeRequest)) {
                DE62_CardIssuerData cardIssuerData = mapCardIssuerData(builder);
                request.set(DataElementId.DE_062, cardIssuerData);
            } else if ((builder.getBatchCloseType() != null && builder.getBatchCloseType().equals(BatchCloseType.Forced)) || builder.isForceToHost()) {
                DE62_CardIssuerData cardIssuerData = mapCardIssuerData(builder);
                request.set(DataElementId.DE_062, cardIssuerData);
            }

            // DE 63: Product Data - LLLVAR ans…999
            if (builder.getProductData() != null) {
                DE63_ProductData productData = builder.getProductData().toDataElement();
                request.set(DataElementId.DE_063, productData);
            }

            // DE 72: Data Record - LLLVAR ans..999
            // DE 73: Date, Action - n6 (YYMMDD)
            // DE 96: Key Management Data - LLLVAR b..999
            // DE 97: Amount, Net Reconciliation - x + n16
            // DE 102: Account Identification 1 - LLVAR ans..28
            // DE 103: Check MICR Data (Account Identification 2) - LLVAR ans..28
            // DE 115: eWIC Overflow Data - LLLVAR ansb..999
            // DE 116: eWIC Overflow Data - LLLVAR ansb..999
            // DE 117: eWIC Data - LLLVAR ansb..999
            // DE 123: Reconciliation Totals - LLLVAR ans..999
            if (transactionType.equals(TransactionType.BatchClose)) {
                DE123_ReconciliationTotals_nws totals = new DE123_ReconciliationTotals_nws();

                Integer transactionCount = builder.getTransactionCount();
                BigDecimal totalDebits;
                BigDecimal totalCredits;

                // transaction count
                if (transactionCount == null) {
                    if (batchProvider != null) {
                        transactionCount = batchProvider.getTransactionCount();
                        totalDebits = batchProvider.getTotalDebits();
                        totalCredits = batchProvider.getTotalCredits();
                    } else {
                        transactionCount = 0;
                        totalDebits = new BigDecimal(0);
                        totalCredits = new BigDecimal(0);
                    }
                    totals.setTotalDebits(transactionCount, totalDebits);
                    totals.setTotalCredits(totalCredits);
                    request.set(DataElementId.DE_123, totals);
                }
                else {
                    DE123_ReconciliationTotals_nws de123ReconciliationTotalsNws = builder.getReconciliationTotals();
                    if (de123ReconciliationTotalsNws != null) {
                        SetReconciliationTotalData(de123ReconciliationTotalsNws);
                        request.set(DataElementId.DE_123, de123ReconciliationTotalsNws);
                    }
                }
            }


            // DE 124: Sundry Data - LLLVAR ans..999
            // DE 125: Extended Response Data 1 - LLLVAR ans..999
            // DE 126: Extended Response Data 2 - LLLVAR ans..999

            // DE 127: Forwarding Data - LLLVAR ans..999
            DE127_ForwardingData forwardingData = new DE127_ForwardingData();

            if (paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;

                if (reference.getOriginalPaymentMethod() instanceof IEncryptable) {
                    EncryptionData encryptionData = ((IEncryptable) reference.getOriginalPaymentMethod()).getEncryptionData();
                    if (encryptionData != null) {
                        EncryptionType encryptionType = acceptorConfig.getSupportedEncryptionType();
                        EncryptedFieldMatrix encryptedField = getEncryptionField(((TransactionReference) paymentMethod).getOriginalPaymentMethod(), encryptionType);
                        if (encryptionType.equals(EncryptionType.TDES)) {
                            forwardingData.setServiceType(acceptorConfig.getServiceType());
                            forwardingData.setOperationType(acceptorConfig.getOperationType());
                        }

                        if(encryptedField!= null) {
                            forwardingData.setEncryptedField(encryptedField);
                        }
                        forwardingData.addEncryptionData(encryptionType, encryptionData);

                        request.set(DataElementId.DE_127, forwardingData);
                    }
                }
                if (reference.getOriginalPaymentMethod() instanceof ICardData) {
                    ICardData cardData = (ICardData) reference.getOriginalPaymentMethod();
                    if (cardData != null) {
                        String tokenizationData = cardData.getTokenizationData();
                        EncryptionData encryptionData = ((IEncryptable) cardData).getEncryptionData();
                        boolean combinedMessage = false;
                        if(tokenizationData != null && encryptionData != null){
                            combinedMessage = true;
                        }
                        if (tokenizationData != null) {
                            setTokenizationData(forwardingData,paymentMethod, tokenizationData,combinedMessage);
                            request.set(DataElementId.DE_127, forwardingData);
                        }
                    }
                } else if (reference.getOriginalPaymentMethod() instanceof ITrackData) {
                    ITrackData trackData = (ITrackData) reference.getOriginalPaymentMethod();
                    if (trackData != null) {
                        String tokenizationData = trackData.getTokenizationData();
                        if (tokenizationData != null) {
                            setTokenizationData(forwardingData,paymentMethod, tokenizationData,false);
                            request.set(DataElementId.DE_127, forwardingData);
                        }
                    }
                }
            }

            return sendRequest(request, builder, orgCorr1, orgCorr2);
        }
        throw new BuilderException("Builder can't be Null");
    }

    private void setTokenizationData(DE127_ForwardingData forwardingData,IPaymentMethod paymentMethod, String tokenizationData, boolean isCombinedMessage) {

        ICardData cardData = null;
        ITrackData trackData = null;
        if(paymentMethod instanceof ICardData){
            cardData = (ICardData) paymentMethod;
        }
        else if(paymentMethod instanceof ITrackData){
            trackData = (ITrackData) paymentMethod;
        }
        //Tokenization Operation type
        TokenizationOperationType tokenOperationType = acceptorConfig.getTokenizationOperationType();

        //Token data AccountNumber/Token
        forwardingData.setTokenOrAcctNum(tokenizationData);

        if(isCombinedMessage){
            EncryptionType encryptionType = acceptorConfig.getSupportedEncryptionType();
            EncryptedFieldMatrix  encryptedField = getEncryptionField(paymentMethod, encryptionType);

            forwardingData.setTokenOrAcctNum("");
            if(encryptedField.equals(EncryptedFieldMatrix.Pan)) {
                if(cardData!=null){
                    forwardingData.setExpiryDate(formatExpiry(cardData.getShortExpiry()));
                }
            }else if(encryptedField.equals(EncryptedFieldMatrix.Track1) || encryptedField.equals(EncryptedFieldMatrix.Track2)){
                forwardingData.setExpiryDate("");
            }
        }
        else {
            // Card Expiry
            if(cardData != null) {
                forwardingData.setExpiryDate(formatExpiry(cardData.getShortExpiry()));
            }
            if(trackData != null){
                forwardingData.setExpiryDate(trackData.getExpiry());
                if(tokenOperationType.equals(TokenizationOperationType.Tokenize)) {
                    forwardingData.setTokenOrAcctNum(trackData.getTokenizationData());
                }
            }
        }

        //Merchant Id
        String merchantId = acceptorConfig.getMerchantId();
        if(merchantId != null) {
            forwardingData.setMerchantId(merchantId);
        }

        //Service Type #G GP API
        ServiceType serviceType = acceptorConfig.getServiceType();
        if (serviceType != null) {
            forwardingData.setServiceType(serviceType);
        }

        //tokenization type  #Global tokenization-1   #Merchant tokenization-2
        TokenizationType tokenizationType = acceptorConfig.getTokenizationType();
        if (tokenizationType != null) {
            forwardingData.setTokenizationType(tokenizationType);
        }

        //Tokenization Operation type
        setTokenizationOperationType(forwardingData, tokenOperationType);
        forwardingData.addTokenizationData(tokenizationType);

    }

    private static void SetReconciliationTotalData(DE123_ReconciliationTotals_nws de123ReconciliationTotalsNws) {
        List<DE123_ReconciliationTotal> list = de123ReconciliationTotalsNws.getTotals();
        BigDecimal totalCreditAmt = new BigDecimal(0);
        BigDecimal totalDebitAmt = new BigDecimal(0);
        BigDecimal totalCreditVoidAmt = new BigDecimal(0);
        BigDecimal totalDebitVoidAmt = new BigDecimal(0);
        BigDecimal totalCreditRefundAmt = new BigDecimal(0);
        BigDecimal totalDebitRefundAmt = new BigDecimal(0);

        int creditCount = 0;
        int debitCount = 0;
        int creditVoidCount = 0;
        int debitVoidCount =0;
        int creditRefundCount =0;
        int debitRefundCount =0;

        for (DE123_ReconciliationTotal total : list) {

            DE123_TransactionType de123TransactionType = total.getTransactionType();
            BigDecimal totalAmt = total.getTotalAmount();
            int transactionTotalCount = total.getTransactionCount();

            if (de123TransactionType.equals(DE123_TransactionType.DebitLessReversals) && !(total.isRefund())) {
                totalCreditAmt = totalCreditAmt.add(totalAmt);
                creditCount = creditCount + transactionTotalCount;

            } else if (de123TransactionType.equals(DE123_TransactionType.CreditLessReversals) &&  !(total.isRefund())) {
                totalDebitAmt = totalDebitAmt.add(totalAmt);
                debitCount = debitCount + transactionTotalCount;

            } else if (de123TransactionType.equals(DE123_TransactionType.AllVoids_Voids)) {

                if(total.getPaymentMethodType().equals(PaymentMethodType.Credit)){
                    totalCreditVoidAmt = totalCreditVoidAmt.add(totalAmt);
                    creditVoidCount =  creditVoidCount + transactionTotalCount;
                }
                else if(total.getPaymentMethodType().equals(PaymentMethodType.Debit)){
                    totalDebitVoidAmt = totalDebitVoidAmt.add(totalAmt);
                    debitVoidCount = debitVoidCount + transactionTotalCount;
                }
            }
            else if (total.isRefund()) {
                if(total.getPaymentMethodType().equals(PaymentMethodType.Credit)){
                    totalCreditRefundAmt = totalCreditRefundAmt.add(totalAmt);
                    creditRefundCount = creditRefundCount + transactionTotalCount;
                }
                else if(total.getPaymentMethodType().equals(PaymentMethodType.Debit)){
                    totalDebitRefundAmt = totalDebitRefundAmt.add(totalAmt);
                    debitRefundCount = debitRefundCount + transactionTotalCount;
                }
            }

        }
        if (creditCount > 0) {
            de123ReconciliationTotalsNws.setTotalCredits(creditCount, totalCreditAmt,"CT");
        }
        if (debitCount > 0) {
            de123ReconciliationTotalsNws.setTotalDebits(debitCount, totalDebitAmt, "DB");
        }
        if(creditVoidCount > 0 ){
            de123ReconciliationTotalsNws.setTotalVoid(creditVoidCount,totalCreditVoidAmt,"CT",PaymentMethodType.Credit);
        }
        if(debitVoidCount > 0 ){
            de123ReconciliationTotalsNws.setTotalVoid(creditVoidCount,totalCreditVoidAmt,"DB",PaymentMethodType.Debit);
        }
        if(creditRefundCount > 0 ){
            de123ReconciliationTotalsNws.setTotalReturns(creditRefundCount,totalCreditRefundAmt,"CT",PaymentMethodType.Credit);
        }
        if(debitRefundCount > 0 ){
            de123ReconciliationTotalsNws.setTotalReturns(debitRefundCount,totalDebitRefundAmt,"DB",PaymentMethodType.Debit);
        }

    }

    private static Iso4217_CurrencyCode getCurrencyCode(Iso4217_CurrencyCode currencyCode, String currency) {
        if (!StringUtils.isNullOrEmpty(currency)) {
            currencyCode = currency.equalsIgnoreCase("USD") ? Iso4217_CurrencyCode.USD : Iso4217_CurrencyCode.CAD;
        }
        return currencyCode;
    }

    private static void setAdditionalAmount(ManagementBuilder builder, IPaymentMethod paymentMethod, Iso4217_CurrencyCode currencyCode, DE54_AmountsAdditional amountsAdditional) throws ApiException {
        if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.EBT)) {
            amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getCashBackAmount());
            amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
        } else if (paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)) {
            amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.PinDebitAccount, currencyCode, builder.getCashBackAmount());
            amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.PinDebitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
        }
    }

    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        throw new UnsupportedTransactionException("VAPS does not support reporting.");
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException("VAPS does not support hosted payments.");
    }

    @Override
    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        return null;
    }

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }
    @Override
    public boolean supportsOpenBanking() {
        return false;
    }

    private IDeviceMessage buildMessage(byte[] message, byte[] orgCorr1, byte[] orgCorr2) {
        int messageLength = message.length + 32;

        // build the header
        NetworkMessageBuilder buffer = new NetworkMessageBuilder()
                .append(messageLength, 2) // EH.1: Total Tran Length
                .append(NetworkTransactionType.Transaction) // EH.2: ID (Transaction only - Keep Alive not supported)
                .append(0, 2) // EH.3: Reserved
                .append(messageType) // EH.4: Type Message
                .append(characterSet) // EH.5: Character Set
                .append(0) // EH.6: Response Code
                .append(0) // EH.7: Response Code Origin
                .append(0); // EH.8: Processing Flag

        // EH.9: Protocol Type
        if (protocolType.equals(ProtocolType.Async)) {
            if (messageType.equals(MessageType.Heartland_POS_8583) || messageType.equals(MessageType.Heartland_NTS)) {
                buffer.append(0x07);
            } else {
                buffer.append(protocolType);
            }
        } else {
            buffer.append(protocolType);
        }

        // rest of the header
        buffer.append(connectionType) // EH.10: Connection Type
                .append(nodeIdentification) // EH.11: Node Identification
                .append(orgCorr1) // EH.12: Origin Correlation 1 (2 Bytes)
                .append(companyId) // EH.13: Company ID
                .append(orgCorr2) // EH.14: Origin Correlation 2 (8 bytes)
                .append(1); // EH.15: Version (0x01)

        // append the 8683 DATA
        buffer.append(message);

        return new DeviceMessage(buffer.toArray());
    }

    private <T extends TransactionBuilder<Transaction>> Transaction sendRequest(NetworkMessage request, T builder, byte[] orgCorr1, byte[] orgCorr2) throws ApiException {
        byte[] sendBuffer = request.buildMessage();
        printRequestData(request);
        IDeviceMessage message = buildMessage(sendBuffer, orgCorr1, orgCorr2);

        try {
            byte[] responseBuffer = send(message);

            String functionCode = request.getString(DataElementId.DE_024);
            String messageReasonCode = request.getString(DataElementId.DE_025);
            String processingCode = request.getString(DataElementId.DE_003);
            String stan = request.getString(DataElementId.DE_011);

            PriorMessageInformation priorMessageInformation = new PriorMessageInformation();
            IPaymentMethod paymentMethod = builder != null ? builder.getPaymentMethod() : null;
            if (paymentMethod != null) {
                DE48_CardType cardType = mapCardType(paymentMethod);
                if (cardType != null) {
                    priorMessageInformation.setCardType(cardType.getValue());
                }
            }

            priorMessageInformation.setFunctionCode(functionCode);
            priorMessageInformation.setMessageReasonCode(messageReasonCode);
            priorMessageInformation.setMessageTransactionIndicator(request.getMessageTypeIndicator());
            priorMessageInformation.setProcessingCode(processingCode);
            priorMessageInformation.setSystemTraceAuditNumber(stan);

            Transaction response = mapResponse(responseBuffer, request, builder);
            response.setMessageInformation(priorMessageInformation);
            if (batchProvider != null) {
                batchProvider.setPriorMessageData(priorMessageInformation);
            }


            return response;
        } catch (GatewayTimeoutException exc) {
            throwExceptionForBuilderNull(request, builder, exc);

            // add the MTI and ProcessingCode from the original transaction to the timeout
            exc.setMessageTypeIndicator(request.getMessageTypeIndicator());
            exc.setProcessingCode(request.getString(DataElementId.DE_003));
            exc.setTransmissionTime(request.getString(DataElementId.DE_007));

            BigDecimal amount = null;
            BigDecimal cashBackAmount = null;
            IPaymentMethod paymentMethod = null;
            if (builder instanceof AuthorizationBuilder) {
                amount = ((AuthorizationBuilder) builder).getAmount();
                paymentMethod = builder.getPaymentMethod();
                cashBackAmount = ((AuthorizationBuilder) builder).getCashBackAmount();
            } else if (builder instanceof ManagementBuilder) {
                amount = ((ManagementBuilder) builder).getAmount();
                paymentMethod = builder.getPaymentMethod();
            }

            // check if it's a pre-auth and not debit
            if (builder.getTransactionType().equals(TransactionType.Auth) && !(paymentMethod instanceof Debit)) {
                throw exc;
            } else if (builder.getTransactionType().equals(TransactionType.Capture) || builder.getTransactionType().equals(TransactionType.PreAuthCompletion)) {
                throw exc;
            }

            // create transaction reference
            TransactionReference reference = new TransactionReference();
            reference.setOriginalAmount(amount);
            reference.setMessageTypeIndicator(request.getMessageTypeIndicator());
            reference.setNtsData(new NtsData(FallbackCode.CouldNotCommunicateWithHost, AuthorizerCode.Interchange_Authorized));
            reference.setOriginalProcessingCode(request.getString(DataElementId.DE_003));
            setOriginalPaymentmethod(paymentMethod, reference);
            reference.setOriginalTransactionTime(request.getString(DataElementId.DE_012));
            reference.setSystemTraceAuditNumber(request.getString(DataElementId.DE_011));

            // create builder
            ManagementBuilder reversal = new ManagementBuilder(TransactionType.Reversal)
                    .withPaymentMethod(reference)
                    .withAmount(amount)
                    .withCashBackAmount(cashBackAmount);

            createReversalRequest(request, exc, reversal);

            exc.setHost(this.currentHost.getValue());
            throw exc;
        }
    }

    private static void setOriginalPaymentmethod(IPaymentMethod paymentMethod, TransactionReference reference) {
        if (paymentMethod instanceof TransactionReference) {
            reference.setOriginalPaymentMethod(((TransactionReference) paymentMethod).getOriginalPaymentMethod());
        } else {
            reference.setOriginalPaymentMethod(paymentMethod);
        }
    }

    private static <T extends TransactionBuilder<Transaction>> void throwExceptionForBuilderNull(NetworkMessage request, T builder, GatewayTimeoutException exc) throws GatewayTimeoutException {
        if (builder == null || builder.getTransactionType().equals(TransactionType.Reversal)) {
            exc.setTransmissionTime(request.getString(DataElementId.DE_007));
            throw exc;
        }
    }

    private void createReversalRequest(NetworkMessage request, GatewayTimeoutException exc, ManagementBuilder reversal) throws ApiException {
        // reuse the batch and sequence number
        DE48_MessageControl messageControl = request.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);
        if (messageControl != null) {
            reversal.withBatchNumber(messageControl.getBatchNumber(), messageControl.getSequenceNumber());
        }

        for (int i = 0; i < 3; i++) {
            exc.setReversalCount(i + 1);
            try {
                Transaction reversalResponse = this.manageTransaction(reversal);
                exc.setReversalResponseCode(reversalResponse.getResponseCode());
                exc.setReversalResponseText(reversalResponse.getResponseMessage());
                break;
            } catch (GatewayTimeoutException gatewayTimeoutException) {
                exc.setReversalResponseCode(gatewayTimeoutException.getResponseCode());
                exc.setReversalResponseText(gatewayTimeoutException.getResponseText());
            }
        }
    }

    private void printRequestData(NetworkMessage request) {
        if (isEnableLogging()) {
            System.out.println("Request Breakdown:\r\n" + request.toString());
        }
    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(byte[] buffer, NetworkMessage request, T builder) throws GatewayException {
        Transaction result = new Transaction();
        MessageReader mr = new MessageReader(buffer);

        // parse the header
        NetworkMessageHeader header = NetworkMessageHeader.parse(mr.readBytes(30));
        if (!header.getResponseCode().equals(NetworkResponseCode.Success)) {
            setGatewayExceptionData(header);
        } else {
            result.setResponseCode("000");
            result.setResponseMessage(header.getResponseCodeOrigin().toString());

            // parse the message
            if (!header.getMessageType().equals(MessageType.NoMessage)) {
                String messageTransactionIndicator = mr.readString(4);
                NetworkMessage message = NetworkMessage.parse(mr.readBytes(buffer.length), Iso8583MessageType.CompleteMessage);
                message.setMessageTypeIndicator(messageTransactionIndicator);

                // log out the breakdown
                printResponseData(message);
                DE3_ProcessingCode processingCode = message.getDataElement(DataElementId.DE_003, DE3_ProcessingCode.class);
                DE44_AdditionalResponseData additionalResponseData = message.getDataElement(DataElementId.DE_044, DE44_AdditionalResponseData.class);
                DE48_MessageControl messageControl = message.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);
                DE54_AmountsAdditional additionalAmounts = message.getDataElement(DataElementId.DE_054, DE54_AmountsAdditional.class);
                DE62_CardIssuerData cardIssuerData = message.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);

                result.setAuthorizedAmount(message.getAmount(DataElementId.DE_004));
                result.setHostResponseDate(message.getDate(DataElementId.DE_012, new SimpleDateFormat(DATE_FORMAT_SPAN_12)));
                result.setReferenceNumber(message.getString(DataElementId.DE_037));
                String authCode = message.getString(DataElementId.DE_038);
                String responseCode = message.getString(DataElementId.DE_039);
                String responseText = DE39_ActionCode.getDescription(responseCode);
                if (!StringUtils.isNullOrEmpty(responseCode)) {
                    if (additionalResponseData != null) {
                        result.setAdditionalResponseCode(additionalResponseData.getActionReasonCode().toString());
                        responseText += String.format(" - %s: %s", additionalResponseData.getActionReasonCode().toString(), additionalResponseData.getTextMessage());
                    }

                    result.setResponseCode(responseCode);
                    result.setResponseMessage(responseText);

                    // Issuer Data
                    if (cardIssuerData != null) {
                        for (String key : cardIssuerData.getCardIssuerEntries().keySet()) {
                            DE62_2_CardIssuerEntry entry = cardIssuerData.getCardIssuerEntries().get(key);
                            result.setIssuerData(entry.getIssuerTag(), entry.getIssuerEntry());
                        }
                    }

                    if (builder != null) {
                        String transactionToken = checkResponse(responseCode, request, message, builder);
                        result.setTransactionToken(transactionToken);
                    }
                }

                // EMV response
                byte[] emvResponse = message.getByteArray(DataElementId.DE_055);
                if (emvResponse != null) {
                    EmvData emvData = EmvUtils.parseTagData(StringUtils.hexFromBytes(emvResponse), isEnableLogging());
                    result.setEmvIssuerResponse(emvData.getAcceptedTagData());
                }

                if (builder != null) {
                    // transaction reference
                    TransactionReference reference = new TransactionReference();
                    reference.setAuthCode(authCode);

                    IPaymentMethod paymentMethod = builder.getPaymentMethod();
                    if (paymentMethod != null) {
                        // original data elements
                        reference.setMessageTypeIndicator(request.getMessageTypeIndicator());
                        reference.setOriginalProcessingCode(request.getString(DataElementId.DE_003));
                        reference.setOriginalApprovedAmount(message.getAmount(DataElementId.DE_004));
                        reference.setSystemTraceAuditNumber(request.getString(DataElementId.DE_011));
                        reference.setOriginalTransactionTime(request.getString(DataElementId.DE_012));
                        reference.setPosDataCode(request.getString(DataElementId.DE_022));
                        reference.setAcquiringInstitutionId(request.getString(DataElementId.DE_032));

                        // partial flag
                        if(!StringUtils.isNullOrEmpty(responseCode)) {
                            if (responseCode.equals("002")) {
                                reference.setPartialApproval(true);
                            } else if (responseCode.equals("000")) {
                                String requestAmount = request.getString(DataElementId.DE_004);
                                String responseAmount = message.getString(DataElementId.DE_004);
                                reference.setPartialApproval(!requestAmount.equals(responseAmount));
                            }
                        }
                        // message control fields
                        if (messageControl != null) {
                            reference.setBatchNumber(messageControl.getBatchNumber());
                        }

                        // card issuer data
                        if (cardIssuerData != null) {
                            reference.setNtsData(cardIssuerData.get("NTS"));
                            result.setReferenceNumber(cardIssuerData.get("IRR"));
                        }

                        // authorization builder
                        if (builder instanceof AuthorizationBuilder) {
                            AuthorizationBuilder authBuilder = (AuthorizationBuilder) builder;
                            reference.setOriginalAmount(authBuilder.getAmount());
                            reference.setOriginalEmvChipCondition(authBuilder.getEmvChipCondition());
                        }

                        // management builder
                        if (builder instanceof ManagementBuilder) {
                            ManagementBuilder managementBuilder = (ManagementBuilder) builder;
                            reference.setOriginalAmount(managementBuilder.getAmount());
                        }
                        else if(builder instanceof ResubmitBuilder) {
                            reference.setOriginalAmount(request.getAmount(DataElementId.DE_004));
                        }

                        // original payment method
                        if (paymentMethod instanceof TransactionReference) {
                            TransactionReference originalReference = (TransactionReference) paymentMethod;
                            reference.setOriginalPaymentMethod(originalReference.getOriginalPaymentMethod());

                            // check nts specifics
                            if (reference.getNtsData() == null) {
                                reference.setNtsData(originalReference.getNtsData());
                            }

                            // get original amounts
                            if (reference.getOriginalAmount() == null) {
                                reference.setOriginalAmount(originalReference.getOriginalAmount());
                            }
                        } else {
                            reference.setOriginalPaymentMethod(paymentMethod);
                        }
                        result.setTransactionReference(reference);
                    }
                    // balance amounts
                    if(additionalAmounts != null) {
                        final DE3_AccountType fromAccountType = processingCode.getFromAccount();
                        final DE3_AccountType toAccountType = processingCode.getToAccount();

                        // build the list of account types to check
                        ArrayList<DE3_AccountType> accountTypes = new ArrayList<>() ;
                        accountTypes.add(fromAccountType);
                        if(!toAccountType.equals(fromAccountType)) {
                            accountTypes.add(toAccountType);
                        }

                        // account type 60 is generic and the response can contain 60, 65 or 66 we need to check all
                        if(fromAccountType.equals(DE3_AccountType.CashCardAccount) || toAccountType.equals(DE3_AccountType.CashCardAccount)) {
                            accountTypes.add(DE3_AccountType.CashCard_CashAccount);
                            accountTypes.add(DE3_AccountType.CashCard_CreditAccount);
                        }

                        result.setBalanceAmount(additionalAmounts.getAmount(accountTypes, DE54_AmountTypeCode.AccountLedgerBalance));
                        result.setAvailableBalance(additionalAmounts.getAmount(accountTypes, DE54_AmountTypeCode.AccountAvailableBalance));
                    }

                    // batch summary
                    if (builder.getTransactionType().equals(TransactionType.BatchClose)) {
                        BatchSummary summary = new BatchSummary();
                        summary.setResponseCode(responseCode);
                        summary.setResentTransactions(resentTransactions);
                        summary.setResentBatchClose(resentBatch);
                        summary.setTransactionToken(result.getTransactionToken());

                        if (messageControl != null) {
                            summary.setBatchId(messageControl.getBatchNumber());
                            summary.setSequenceNumber(messageControl.getSequenceNumber() + "");
                        }

                        DE123_ReconciliationTotals_nws reconciliationTotals = message.getDataElement(DataElementId.DE_123, DE123_ReconciliationTotals_nws.class);
                        setReconciliationdata(summary, reconciliationTotals);
                        result.setBatchSummary(summary);
                    }
                }
            }
        }

        return result;
    }

    private void printResponseData(NetworkMessage message) {
        if (isEnableLogging()) {
            System.out.println("\r\nResponse Breakdown:\r\n" + message.toString());
        }
    }

    private static void setReconciliationdata(BatchSummary summary, DE123_ReconciliationTotals_nws reconciliationTotals) {
        if (reconciliationTotals != null) {
            int transactionCount = 0;
            BigDecimal totalAmount = new BigDecimal(0);
            for (DE123_ReconciliationTotal total : reconciliationTotals.getTotals()) {
                transactionCount += total.getTransactionCount();
                totalAmount = totalAmount.add(total.getTotalAmount());

                if(total.getTransactionType().equals(DE123_TransactionType.DebitLessReversals)) {
                    summary.setDebitCount(total.getTransactionCount());
                    summary.setDebitAmount(total.getTotalAmount());
                }
                else if(total.getTransactionType().equals(DE123_TransactionType.CreditLessReversals)) {
                    summary.setCreditCount(total.getTransactionCount());
                    summary.setCreditAmount(total.getTotalAmount());
                }
            }

            summary.setTransactionCount(transactionCount);
            summary.setTotalAmount(totalAmount);
        }
    }

    private static void setGatewayExceptionData(NetworkMessageHeader header) throws GatewayException {
        throw new GatewayException(
                String.format("Unexpected response from gateway: %s %s", header.getResponseCode().toString(), header.getResponseCodeOrigin().toString()),
                header.getResponseCode().toString(),
                header.getResponseCodeOrigin().toString());
    }

    private <T extends TransactionBuilder<Transaction>> String mapMTI(T builder) {
        String mtiValue = "1";

        /* MESSAGE CLASS
            0 Reserved for ISO use
            1 Authorization
            2 Financial
            3 File action
            4 Reversal
            5 Reconciliation
            6 Administrative
            7 Fee collection
            8 Network management
            9 Reserved for ISO use
         */
        switch (builder.getTransactionType()) {
            case Auth:
            case Balance:
            case Verify: {
                mtiValue += "1";
            }
            break;
            case Activate:
            case AddValue:
            case BenefitWithdrawal:
            case Capture:
            case PreAuthCompletion:
            case CashOut:
            case Refund:
            case Sale:
            case CashAdvance:
            case StoreAndForward:
            case Payment: {
                mtiValue += "2";
            }
            break;
            case Reversal:
            case Void: {
                mtiValue += "4";
            }
            break;
            case BatchClose: {
                mtiValue += "5";
            }break;
            case FileAction: {
                mtiValue += "3";
            } break;
            case PosSiteConfiguration:
            case TimeRequest: {
                mtiValue += "6";
            }break;
            default:
                mtiValue += "0";
        }
        /* MESSAGE FUNCTION
            0 Request
            1 Request response
            2 Advice
            3 Advice response
            4 Notification
            5–9 Reserved for ISO use
         */
            switch (builder.getTransactionType()) {
                case BatchClose:
                case Capture:
                case PreAuthCompletion:
                case Reversal:
                case PosSiteConfiguration:
                case Void:
                case Payment: {
                    mtiValue += "2";
                }
                break;
                case Refund: {
                    if (builder.getPaymentMethod() instanceof Debit || builder.getPaymentMethod() instanceof EBT ||
                            builder.getPaymentMethod() instanceof GiftCard) {
                        mtiValue += "0";
                    } else mtiValue += "2";
                }
                break;
                case FileAction: {
                    mtiValue += "0";
                }
                break;

                default:
                    mtiValue += "0";
            }


        /* TRANSACTION ORIGINATOR
            0 POS application
            1 POS application repeat
            2 Heartland system
            3 Heartland system repeat
            4 POS application or Heartland system
            5 Reserved for Heartland use
            6–9 Reserved for ISO use
        */
        switch (builder.getTransactionType()) {
            case PosSiteConfiguration:
            case TimeRequest:
                mtiValue += "4";
                break;
            default:
                mtiValue += "0";
        }
        return mtiValue;
    }
    private <T extends TransactionBuilder<Transaction>> DE3_ProcessingCode mapProcessingCode(T builder) throws ApiException {
        DE3_ProcessingCode processingCode = new DE3_ProcessingCode();

        TransactionType type = builder.getTransactionType();
        if(type.equals(TransactionType.Reversal)) {
            // set the transaction type to the original transaction type
            if(builder.getPaymentMethod() instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference)builder.getPaymentMethod();
                return processingCode.fromByteArray(reference.getOriginalProcessingCode().getBytes());
            }
            throw new BuilderException("The processing code must be specified when performing a reversal.");
        }

        switch(type) {
            case Activate: {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                if(authBuilder.getAmount() != null) {
                    processingCode.setTransactionType(DE3_TransactionType.Activate);
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.Activate_PreValuedCard);
                }
            } break;
            case AddValue: {
                processingCode.setTransactionType(DE3_TransactionType.Deposit);
                if(builder.getPaymentMethod() instanceof Credit) {
                    Credit card = (Credit)builder.getPaymentMethod();
                    if(card.getCardType().equals(cardTypeVisaReadyLink)) {
                        processingCode.setTransactionType(DE3_TransactionType.LoadValue);
                    }
                }
                else if(builder.getPaymentMethod() instanceof Debit) {
                    Debit card = (Debit)builder.getPaymentMethod();
                    if(card.getCardType().equals(cardTypeVisaReadyLink)) {
                        processingCode.setTransactionType(DE3_TransactionType.LoadValue);
                    }
                }
            } break;
            case Auth: {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                if(authBuilder.getAmount().equals(BigDecimal.ZERO) && authBuilder.getBillingAddress() != null && !authBuilder.isAmountEstimated()){
                    processingCode.setTransactionType(DE3_TransactionType.AddressOrAccountVerification);
                    processingCode.setToAccount(DE3_AccountType.Unspecified);
                    processingCode.setFromAccount(DE3_AccountType.Unspecified);
                    return processingCode;
                }
                else if (authBuilder.getPaymentMethod() instanceof eCheck){
                    eCheck check = (eCheck)authBuilder.getPaymentMethod();

                    if(check.isCheckVerify()){
                        processingCode.setTransactionType(DE3_TransactionType.CheckVerification);
                    }
                    else if(check.isCheckGuarantee()) {
                        processingCode.setTransactionType(DE3_TransactionType.CheckGuarantee);
                    }
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
                }
            } break;
            case Balance: {
                    processingCode.setTransactionType(DE3_TransactionType.BalanceInquiry);
            } break;
            case BenefitWithdrawal: {
                processingCode.setTransactionType(DE3_TransactionType.Cash);
            } break;
            case CashOut: {
                processingCode.setTransactionType(DE3_TransactionType.UnloadValue);
            } break;
            case Refund: {
                processingCode.setTransactionType(DE3_TransactionType.Return);
            } break;
            case Sale:
            case  StoreAndForward: {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                if(authBuilder.getCashBackAmount() != null) {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndServiceWithCashDisbursement);
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
                }
            } break;
            case Verify: {
                processingCode.setTransactionType(DE3_TransactionType.BalanceInquiry);
            } break;
            case CashAdvance: {
            	processingCode.setTransactionType(DE3_TransactionType.Cash);
            }break;
            case Payment: {
            	processingCode.setTransactionType(DE3_TransactionType.Payment);
            }break;
            case BatchClose:{
                processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
            }break;
            default: {
                processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
            }
        }

        // check for an original payment method
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference)paymentMethod;
            if(transactionReference.getOriginalPaymentMethod() != null) {
                paymentMethod = transactionReference.getOriginalPaymentMethod();
            }
        }

        // setting the accountType
        DE3_AccountType accountType = DE3_AccountType.Unspecified;
        if(paymentMethod instanceof Credit) {
            Credit card = (Credit)paymentMethod;
            if(((Credit) paymentMethod).isFleet()) {
                accountType = DE3_AccountType.FleetAccount;
            } else if(card.getCardType().equals(cardTypeVisaReadyLink)) {
                accountType = DE3_AccountType.PinDebitAccount;
            } else {
                accountType = DE3_AccountType.CreditAccount;
            }
        }
        else if(paymentMethod instanceof Debit) {
            accountType = DE3_AccountType.PinDebitAccount;
        }
        else if(paymentMethod instanceof GiftCard) {
            accountType = DE3_AccountType.CashCardAccount;
        }
        else if(paymentMethod instanceof EBT) {
            EBT ebtCard = (EBT)paymentMethod;
            if(ebtCard.getEbtCardType().equals(EbtCardType.CashBenefit)) {
                accountType = DE3_AccountType.CashBenefitAccount;
            }
            else {
                accountType = DE3_AccountType.FoodStampsAccount;
            }
        }
        else if(paymentMethod instanceof eCheck){
            accountType = DE3_AccountType.CheckingAccount;
        }
        else if (paymentMethod instanceof Ewic) {
            accountType = DE3_AccountType.eWIC;
        }

        switch (type) {
            case Activate:
            case AddValue:
            case Refund: {
                processingCode.setToAccount(accountType);
                processingCode.setFromAccount(DE3_AccountType.Unspecified);
            } break;
            case Payment: {
            	processingCode.setToAccount(DE3_AccountType.PrivateLabelAccount);
            	processingCode.setFromAccount(DE3_AccountType.Unspecified);
            }break;
            default: {
                processingCode.setFromAccount(accountType);
                processingCode.setToAccount(DE3_AccountType.Unspecified);
            }
        }

        return processingCode;
    }
    private <T extends TransactionBuilder<Transaction>> String mapFunctionCode(T builder){
        TransactionType type = builder.getTransactionType();
        TransactionModifier modifier = builder.getTransactionModifier();
        switch (type) {
            case Activate:
            case AddValue:
            case BenefitWithdrawal:
            case CashOut:
            case Refund:
            case CashAdvance:
            case Sale: {
                return "200";
            }
            case PreAuthCompletion:
            case Capture: {
                ManagementBuilder managementBuilder = (ManagementBuilder) builder;

                if (managementBuilder.getAmount() != null && managementBuilder.getPaymentMethod() instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) managementBuilder.getPaymentMethod();

                    if (managementBuilder.getAmount().compareTo(reference.getOriginalAmount()) == 0) {
                        return "201";
                    }
                    return "202";
                }
                return "201";
            }
            case Auth: {
                if (modifier.equals(TransactionModifier.Offline)) {
                    return "190";
                }

                if (builder instanceof AuthorizationBuilder) {
                    AuthorizationBuilder authBuilder = (AuthorizationBuilder) builder;
                    if (authBuilder.isAmountEstimated()) {
                        return "101";
                    } else if (authBuilder.getAmount().equals(BigDecimal.ZERO) && authBuilder.getBillingAddress() != null) {
                        return "181";
                    }
                }
                return "100";
            }
            case Verify:
            case Balance: {
                return "108";
            }
            case BatchClose: {
                ManagementBuilder managementBuilder = (ManagementBuilder) builder;
                if (managementBuilder.getBatchCloseType() == null || managementBuilder.getBatchCloseType().equals(BatchCloseType.Forced)) {
                    return "572";
                }
                return "570";
            }
            case Reversal: {
                ManagementBuilder managementBuilder = (ManagementBuilder) builder;

                if (managementBuilder.getAmount() != null && managementBuilder.getPaymentMethod() instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) managementBuilder.getPaymentMethod();

                    if (managementBuilder.getAmount().compareTo(reference.getOriginalAmount()) == 0) {
                        return "400";
                    }
                    return "401";
                }
                return "400";
            }
            case Void: {
                ManagementBuilder managementBuilder = (ManagementBuilder) builder;

                if (managementBuilder.getAmount() != null && managementBuilder.getPaymentMethod() instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) managementBuilder.getPaymentMethod();
                    if (managementBuilder.getAmount().compareTo(reference.getOriginalAmount()) == 0) {
                        return "444";
                    }
                }
                return "441";
            }
            case FileAction: {
                return "388";
            }
            case PosSiteConfiguration: {
                return "692";
            }
            case TimeRequest: {
                return "641";
            }
            case StoreAndForward:{
                return "203";
            }
            default: {
                return "000";
            }
        }
    }
    private DE25_MessageReasonCode mapMessageReasonCode(ManagementBuilder builder) {
        TransactionReference paymentMethod = (TransactionReference)builder.getPaymentMethod();
        IPaymentMethod originalPaymentMethod = null;
        TransactionType transactionType = builder.getTransactionType();

        // get the NTS data
        NtsData ntsData = null;
        if(paymentMethod != null) {
            ntsData = paymentMethod.getNtsData();
            originalPaymentMethod = paymentMethod.getOriginalPaymentMethod();
        }

        // set the fallback and authorizer codes
        FallbackCode fallbackCode = null;
        AuthorizerCode authorizerCode = null;
        if(ntsData != null) {
            fallbackCode = ntsData.getFallbackCode();
            authorizerCode = ntsData.getAuthorizerCode();
        }

        DE25_MessageReasonCode reasonCode = null;
        if(transactionType.equals(TransactionType.Capture)) {
            if(authorizerCode != null && authorizerCode.equals(AuthorizerCode.Voice_Authorized)) {
                reasonCode = DE25_MessageReasonCode.VoiceCapture;
            }
            else if(fallbackCode != null) {
                reasonCode = setDE25ForFallbackCode(fallbackCode);
            }
            else {
                reasonCode = DE25_MessageReasonCode.AuthCapture;
            }
        }
        if(transactionType.equals(TransactionType.PreAuthCompletion)) {
            reasonCode = DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement;
        }
        else if(transactionType.equals(TransactionType.Reversal) || transactionType.equals(TransactionType.Void)) {
            boolean partial = false;
            if(builder.getAmount() != null && paymentMethod != null) {
                partial = !builder.getAmount().equals(paymentMethod.getOriginalAmount());
            }

            if(fallbackCode != null) {
                switch (fallbackCode) {
                    case Received_IssuerTimeout: {
                        reasonCode = DE25_MessageReasonCode.Failure_To_Dispense;
                    }break;
                    case CouldNotCommunicateWithHost:
                    case Received_IssuerUnavailable: {
                        reasonCode = DE25_MessageReasonCode.TimeoutWaitingForResponse;
                    } break;
                    case Received_SystemMalfunction: {
                        reasonCode = DE25_MessageReasonCode.SystemTimeout_Malfunction;
                    } break;
                    default: {
                        if(builder.isCustomerInitiated()) {
                            reasonCode = partial ? DE25_MessageReasonCode.CustomerInitiated_PartialApproval : DE25_MessageReasonCode.CustomerInitiatedReversal;
                        } else {
                            reasonCode = DE25_MessageReasonCode.MerchantInitiatedReversal;
                        }
                    }
                }
            }
            else {
                if(builder.isCustomerInitiated()) {
                    reasonCode = partial ? DE25_MessageReasonCode.CustomerInitiated_PartialApproval : DE25_MessageReasonCode.CustomerInitiatedReversal;
                }
                else {
                    reasonCode = DE25_MessageReasonCode.MerchantInitiatedReversal;
                }
            }
        }
        else if(transactionType.equals(TransactionType.Refund) && (originalPaymentMethod instanceof Debit || originalPaymentMethod instanceof EBT)) {
            reasonCode = DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement;
        }

        return reasonCode;
    }

    private static DE25_MessageReasonCode setDE25ForFallbackCode(FallbackCode fallbackCode) {
        DE25_MessageReasonCode reasonCode;
        switch (fallbackCode) {
            case Received_IssuerTimeout:
            case Received_IssuerUnavailable:
            case Received_SystemMalfunction: {
                reasonCode = DE25_MessageReasonCode.StandInCapture;
            } break;
            default: {
                reasonCode = DE25_MessageReasonCode.AuthCapture;
            }
        }
        return reasonCode;
    }

    private <T extends TransactionBuilder<Transaction>> DE48_MessageControl mapMessageControl(T builder) throws BatchFullException {
        DE48_MessageControl messageControl = new DE48_MessageControl();
        boolean isTimeRequest=builder.getTransactionType().equals(TransactionType.TimeRequest);
        /* DE 48: Message Control - LLLVAR ans..999
            48-0 BIT MAP b8 C Specifies which data elements are present.
            48-1 COMMUNICATION DIAGNOSTICS n4 C Data on communication connection.
            48-2 HARDWARE & SOFTWARE CONFIGURATION ans20 C Version information from POS application.
            48-3 LANGUAGE CODE a2 F Language used for display or print.
            48-4 BATCH NUMBER n10 C Current batch.
            48-5 SHIFT NUMBER n3 C Identifies shift for reconciliation and tracking.
            48-6 CLERK ID LVAR an..9 C Identification of clerk operating the terminal.
            48-7 MULTIPLE TRANSACTION CONTROL n9 F Parameters to control multiple related messages.
            48-8 CUSTOMER DATA LLLVAR ns..250 C Data entered by customer or clerk.
            48-9 TRACK 2 FOR SECOND CARD LLVAR ns..37 C Used to specify the second card in a transaction by the Track 2 format.
            48-10 TRACK 1 FOR SECOND CARD LLVAR ans..76 C Used to specify the second card in a transaction by the Track 1 format.
            48-11 CARD TYPE anp4 C Card type.
            48-12 ADMINISTRATIVELY DIRECTED TASK b1 C Notice to or direction for action to be taken by POS application.
            48-13 RFID DATA LLVAR ans..99 C Data received from RFID transponder.
            48-14 PIN ENCRYPTION METHODOLOGY ans2 C Used to identify the type of encryption methodology.
            48-15, 48-32 RESERVED FOR ANSI USE LLVAR ans..99 These are reserved for future use.
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the Heartland system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR HEARTLAND USE LLVAR ans..99 F
         */
        // DE48-2 - Hardware/Software Config
        DE48_2_HardwareSoftwareConfig hardwareSoftwareConfig = new DE48_2_HardwareSoftwareConfig();
        hardwareSoftwareConfig.setHardwareLevel(acceptorConfig.getHardwareLevel());
        hardwareSoftwareConfig.setSoftwareLevel(acceptorConfig.getSoftwareLevel());
        hardwareSoftwareConfig.setOperatingSystemLevel(acceptorConfig.getOperatingSystemLevel());
        messageControl.setHardwareSoftwareConfig(hardwareSoftwareConfig);

        // DE48-4 (Sequence Number & Batch Number)
        if(!builder.getTransactionType().equals(TransactionType.Auth)) {
            int sequenceNumber = 0;
            if(!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
            }
            messageControl.setSequenceNumber(sequenceNumber);

            int batchNumber = builder.getBatchNumber();
            if (batchNumber == 0 && batchProvider != null) {
                batchNumber = batchProvider.getBatchNumber();
            }
            messageControl.setBatchNumber(batchNumber);
        }

        // DE48-5
        if(builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            messageControl.setShiftNumber(authBuilder.getShiftNumber());
        }
        if(builder instanceof ManagementBuilder){
            ManagementBuilder managementBuilder = (ManagementBuilder) builder;
            TransactionType transactionType = managementBuilder.getTransactionType();
            if(transactionType.equals(TransactionType.BatchClose)) {
                messageControl.setShiftNumber(managementBuilder.getShiftNumber());
            }
        }

        // 48-6 CLERK ID
        if(builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            messageControl.setClerkId(authBuilder.getClerkId());
        }

        // DE48-8 Customer Data
        DE48_8_CustomerData customerData = new DE48_8_CustomerData();
        if(builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;

            // postal code
            if(!(builder.getPaymentMethod() instanceof  eCheck) && authBuilder.getBillingAddress() != null) {
                    Address address = authBuilder.getBillingAddress();
                    customerData.set(DE48_CustomerDataType.PostalCode, address.getPostalCode());
            }
            setEcheckData(builder, customerData);
        }

        // fleet data
        if(builder.getFleetData() != null) {
            FleetData fleetData = builder.getFleetData();
            if(builder instanceof AuthorizationBuilder) {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder) builder;
                if (!StringUtils.isNullOrEmpty(authBuilder.getTagData())) {
                    customerData.setEmvFlag(true);
                }
            }

            customerData.set(DE48_CustomerDataType.UnencryptedIdNumber, fleetData.getUserId());
            customerData.set(DE48_CustomerDataType.Vehicle_Number, fleetData.getVehicleNumber());
            customerData.set(DE48_CustomerDataType.VehicleTag, fleetData.getVehicleTag());
            customerData.set(DE48_CustomerDataType.DriverId_EmployeeNumber, fleetData.getDriverId());
            customerData.set(DE48_CustomerDataType.Odometer_Reading,fleetData.getOdometerReading());
            customerData.set(DE48_CustomerDataType.DriverLicense_Number, fleetData.getDriversLicenseNumber());
            customerData.set(DE48_CustomerDataType.WORKORDER_PONUMBER, fleetData.getWorkOrderPoNumber());
            customerData.set(DE48_CustomerDataType.EnteredData_Numeric, fleetData.getEnteredData());
            customerData.set(DE48_CustomerDataType.ServicePrompt, fleetData.getServicePrompt());
            customerData.set(DE48_CustomerDataType.JobNumber, fleetData.getJobNumber());
            customerData.set(DE48_CustomerDataType.Department, fleetData.getDepartment());
            customerData.set(DE48_CustomerDataType.ADDITIONALPROMPTDATA1,fleetData.getAdditionalPromptData1());
            customerData.set(DE48_CustomerDataType.ADDITIONALPROMPTDATA2,fleetData.getAdditionalPromptData2());
            customerData.set(DE48_CustomerDataType.EMPLOYEENUMBER,fleetData.getEmployeeNumber());
        }

        // cvn number
        if(builder.getPaymentMethod() instanceof ICardData) {
            ICardData card = (ICardData)builder.getPaymentMethod();
            IEncryptable encryption = null;
            if(builder.getPaymentMethod() instanceof IEncryptable) {
                encryption = (IEncryptable) builder.getPaymentMethod();
            }

            if(!StringUtils.isNullOrEmpty(card.getCvn())) {
                String cvn = card.getCvn();
                if(encryption != null && encryption.getEncryptionData() != null) {
                    cvn = StringUtils.padLeft(card.getCvn(), card.getCvn().length(), ' ');
                }
                customerData.set(DE48_CustomerDataType.CardPresentSecurityCode, cvn);
            }
        }

        // gift pin
        if(builder.getPaymentMethod() instanceof GiftCard) {
            GiftCard giftCard = (GiftCard)builder.getPaymentMethod();
            if(!StringUtils.isNullOrEmpty(giftCard.getPin())) {
                customerData.set(DE48_CustomerDataType.CardPresentSecurityCode, giftCard.getPin());
            }
        }

        if(customerData.getFieldCount() > 0) {
            messageControl.setCustomerData(customerData);
        }

        // DE48-11
        messageControl.setCardType(mapCardType(builder.getPaymentMethod()));

        // DE48-14
        if(builder.getPaymentMethod() instanceof IPinProtected && builder instanceof AuthorizationBuilder) {
            IPinProtected pinProtected = (IPinProtected)builder.getPaymentMethod();
            if(pinProtected.getPinBlock() != null) {
            DE48_14_PinEncryptionMethodology pinEncryptionMethodology = new DE48_14_PinEncryptionMethodology();
            pinEncryptionMethodology.setKeyManagementDataCode(DE48_KeyManagementDataCode.DerivedUniqueKeyPerTransaction_DUKPT);
            pinEncryptionMethodology.setEncryptionAlgorithmDataCode(DE48_EncryptionAlgorithmDataCode.TripleDES_3Keys);
            messageControl.setPinEncryptionMethodology(pinEncryptionMethodology);
        }
    }

        // DE48-33
        if(!(builder.getPaymentMethod() instanceof eCheck)) {
            if(acceptorConfig.hasPosConfiguration_MessageControl()) {
                DE48_33_PosConfiguration posConfiguration = new DE48_33_PosConfiguration();
                posConfiguration.setTimezone(posConfiguration.getTimezone());
                posConfiguration.setSupportsPartialApproval(acceptorConfig.getSupportsPartialApproval());
                posConfiguration.setSupportsReturnBalance(acceptorConfig.getSupportsReturnBalance());
                posConfiguration.setSupportsCashOver(acceptorConfig.getSupportsCashOver());
                posConfiguration.setMobileDevice(acceptorConfig.getMobileDevice());
                posConfiguration.setSupportWexAdditionalProducts(acceptorConfig.getSupportWexAdditionalProducts());
                posConfiguration.setSupportTerminalPurchaseRestriction(acceptorConfig.getSupportTerminalPurchaseRestriction());
                posConfiguration.setSupportVisaFleet2dot0(acceptorConfig.getSupportVisaFleet2dot0());
                messageControl.setPosConfiguration(posConfiguration);
            }
        }

        // DE48-34 // Message Configuration Fields
        // TODO: This needs to be pulled in from the config
        if(acceptorConfig.hasPosConfiguration_MessageData() && !isTimeRequest) {
            DE48_34_MessageConfiguration messageConfigData = new DE48_34_MessageConfiguration();
            messageConfigData.setPerformDateCheck(acceptorConfig.getPerformDateCheck());
            messageConfigData.setEchoSettlementData(acceptorConfig.getEchoSettlementData());
            messageConfigData.setIncludeLoyaltyData(acceptorConfig.getIncludeLoyaltyData());
            messageControl.setMessageConfiguration(messageConfigData);
        }

        // DE48-39 // Not a follow up message these should be defaults
        PriorMessageInformation priorMessageInformation = new PriorMessageInformation();
        if(builder.getPriorMessageInformation() != null) {
            priorMessageInformation = builder.getPriorMessageInformation();
        }
        else if(batchProvider != null && batchProvider.getPriorMessageData() != null) {
            priorMessageInformation = batchProvider.getPriorMessageData();
        }

        DE48_39_PriorMessageInformation pmi = new DE48_39_PriorMessageInformation();
        pmi.setResponseTime(priorMessageInformation.getResponseTime());
        pmi.setCardType(priorMessageInformation.getCardType());
        pmi.setMessageTransactionIndicator(priorMessageInformation.getMessageTransactionIndicator());
        pmi.setProcessingCode(priorMessageInformation.getProcessingCode());
        pmi.setStan(priorMessageInformation.getSystemTraceAuditNumber());

        if(!(builder.getPaymentMethod() instanceof eCheck)) {
            messageControl.setPriorMessageInformation(pmi);
        }

        // DE48-40 Addresses
        if(builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(authBuilder.getBillingAddress() != null) {
                DE48_Address billing = new DE48_Address();
                billing.setAddress(authBuilder.getBillingAddress());
                billing.setAddressUsage(DE48_AddressUsage.Billing);
                if(authBuilder.getAmount().equals(BigDecimal.ZERO) && !authBuilder.isAmountEstimated()) {
                    billing.setAddressType(DE48_AddressType.AddressVerification);
                }
                else {
                    billing.setAddressType(DE48_AddressType.StreetAddress);
                }
                messageControl.addAddress(billing);
            }

            if(authBuilder.getShippingAddress() != null) {
                DE48_Address shipping = new DE48_Address();
                shipping.setAddress(authBuilder.getShippingAddress());
                shipping.setAddressUsage(DE48_AddressUsage.Shipping);
                shipping.setAddressType(DE48_AddressType.StreetAddress);
                messageControl.addAddress(shipping);
            }
        }

        return messageControl;
    }

    private static <T extends TransactionBuilder<Transaction>> void setEcheckData(T builder, DE48_8_CustomerData customerData) {
        if(builder.getPaymentMethod() instanceof eCheck){
            eCheck check = (eCheck) builder.getPaymentMethod();
            customerData.set(DE48_CustomerDataType.DriverLicense_Number, check.getDriversLicenseNumber());
            customerData.set(DE48_CustomerDataType.DriverLicense_State_Province, check.getDriversLicenseState());
            customerData.set(DE48_CustomerDataType.DateofBirth,String.valueOf(check.getBirthYear()) + check.getBirthMonth() + check.getBirthDate());
        }
    }
    private  <T extends TransactionBuilder<Transaction>>  DE62_COO_2_VerificationType mapCheckVerificationType(T builder){
        AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
        eCheck check = (eCheck) authBuilder.getPaymentMethod();
        DE62_COO_2_VerificationType  de62Coo2VerificationType ;
        if(authBuilder.getRawMICRData() == null){
            de62Coo2VerificationType = DE62_COO_2_VerificationType.FORMATTED_MICR_DATA;
            if(check.getDriversLicenseNumber() != null && check.getDriversLicenseState()!=null){
                de62Coo2VerificationType = de62Coo2VerificationType.FORMATTED_MICR_DRIVER_LICENSE;
            }
        }
        else{
            de62Coo2VerificationType = DE62_COO_2_VerificationType.RAW_MICR_DATA;
            if(check.getDriversLicenseNumber() != null && check.getDriversLicenseState()!=null){
                de62Coo2VerificationType = de62Coo2VerificationType.RAW_MICR_DRIVER_LICENSE;
            }
        }

        return de62Coo2VerificationType;
    }

    private <T extends TransactionBuilder<Transaction>> DE62_CardIssuerData mapCardIssuerData(T builder) {
        // DE 62: Card Issuer Data - LLLVAR ans..999
        DE62_CardIssuerData cardIssuerData = new DE62_CardIssuerData();
        boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getVisaFleet2()!=null && acceptorConfig.getVisaFleet2();

        //F03
        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }
            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals("VisaFleet") && isVisaFleet2) {
                cardIssuerData.add(CardIssuerEntryTag.VISAFLEET2DOT0CARDPRESENTBYCARDHOLDER, acceptorConfig.getSupportVisaFleet2dot0().getValue());
            }
        }


        if(builder.getTransactionType().equals(TransactionType.StoreAndForward)) {
            String storeAndForwardFlag = "1";
            cardIssuerData.add(CardIssuerEntryTag.StoreAndForwardFlag, storeAndForwardFlag);
        }
        // unique device id
        if(!StringUtils.isNullOrEmpty(builder.getUniqueDeviceId())) {
            cardIssuerData.add(CardIssuerEntryTag.UniqueDeviceId, builder.getUniqueDeviceId());
        }
        else if(!StringUtils.isNullOrEmpty(uniqueDeviceId)) {
            cardIssuerData.add(CardIssuerEntryTag.UniqueDeviceId, uniqueDeviceId);
        }

        if(builder.getPaymentMethod() instanceof eCheck && builder instanceof AuthorizationBuilder){
            AuthorizationBuilder authBuilder = (AuthorizationBuilder) builder;
            eCheck check = (eCheck)builder.getPaymentMethod();
            DE62_COO_2_VerificationType verificationType = mapCheckVerificationType(authBuilder);
            String checkType =check.getCheckType().getValue();
            if(checkType != null){
                DE62_COO_checkType checkTp = DE62_COO_checkType.valueOf(checkType.toUpperCase());
                cardIssuerData.add(CardIssuerEntryTag.CheckInformation, checkTp.getValue() + verificationType.getValue());
            }

            DE103_Check_MICR_Data checkData = new DE103_Check_MICR_Data();

            checkData.setAccountNumber(check.getAccountNumber());
            checkData.setTransitNumber(check.getRoutingNumber());
            checkData.setSequenceNumber(check.getCheckNumber());

            if(authBuilder.getRawMICRData() != null) {
                cardIssuerData.add(CardIssuerEntryTag.CheckExpandedOrRawMICRData, authBuilder.getRawMICRData());
            }
            else if (checkData.toByteArray().length > 28) {
                cardIssuerData.add(CardIssuerEntryTag.CheckExpandedOrRawMICRData, checkData.toString());
            }
        }

        if (builder.getPaymentMethod().getPaymentMethodType().equals(PaymentMethodType.Ewic)) {
            if(builder.getEwicIssuingEntity() != null){cardIssuerData.add(CardIssuerEntryTag.EwicIssuingEntity, builder.getEwicIssuingEntity());}
            cardIssuerData.add(CardIssuerEntryTag.EWICMerchantId, ewicMerchantId);
        }
        // wex support
        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }

            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals(cardTypeWexFleet)) {
                cardIssuerData.add(CardIssuerEntryTag.FleetCards, "F00", "0401");
                if(builder.getTransactionType().equals(TransactionType.Refund)) {
                    cardIssuerData.add(CardIssuerEntryTag.IssuerSpecificTransactionMatchData, builder.getTransactionMatchingData().getElementData());
                }

                // purchase device sequence number
                if(builder.getFleetData() != null && builder.getFleetData().getPurchaseDeviceSequenceNumber() != null) {
                    cardIssuerData.add(CardIssuerEntryTag.FleetCards, "F01", builder.getFleetData().getPurchaseDeviceSequenceNumber());
                }
                else if(paymentMethod instanceof CreditTrackData) {
                    cardIssuerData.add(CardIssuerEntryTag.FleetCards, "F01", ((CreditTrackData) paymentMethod).getPurchaseDeviceSequenceNumber());
                }
            }
        }
        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }
            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals("MC")) {
                cardIssuerData.add(CardIssuerEntryTag.MASTERCARD_CIT_MIT_INDICATOR,"M207");
            }
        }

        // management builder related
        if(builder instanceof ManagementBuilder) {
            ManagementBuilder mb = (ManagementBuilder)builder;

            // IRR Issuer Reference Number
            if(!StringUtils.isNullOrEmpty(mb.getReferenceNumber())) {
                cardIssuerData.add(CardIssuerEntryTag.RetrievalReferenceNumber, mb.getReferenceNumber());
            }

            // NTE Terminal Error
            if(mb.isTerminalError()) {
                cardIssuerData.add(CardIssuerEntryTag.TerminalError, "Y");
            }

            if(mb.getTransactionType().equals(TransactionType.BatchClose) && mb.getBatchCloseType().equals(BatchCloseType.Forced)) {
                cardIssuerData.add(CardIssuerEntryTag.TerminalError, "Y");
            }
        }
        else {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(authBuilder.getEmvChipCondition() != null) {
                cardIssuerData.add(CardIssuerEntryTag.ChipConditionCode, mapChipCondition(authBuilder.getEmvChipCondition()));
            }
        }

        // catch all
        if(builder.getIssuerData() != null) {
            LinkedHashMap<CardIssuerEntryTag,String> issuerData = builder.getIssuerData();
            for(CardIssuerEntryTag tag: issuerData.keySet()) {
                cardIssuerData.add(tag, issuerData.get(tag));
            }
        }

        // put if there are entries
        if(cardIssuerData.getNumEntries() > 0) {
            return cardIssuerData;
        }
        return null;
    }
    private DE48_CardType mapCardType(IPaymentMethod paymentMethod) {
        // check to see if the original payment method is set
        paymentMethod = getPaymentMethodFromTransactionReference(paymentMethod);

        // evaluate and return
        if(paymentMethod instanceof DebitTrackData) {
            return DE48_CardType.PINDebitCard;
        }
        else if(paymentMethod instanceof Credit) {
            Credit card = (Credit)paymentMethod;
            if(card.getCardType().equals("Amex")) {
                return DE48_CardType.AmericanExpress;
            }
            else if(card.getCardType().equals("MC")) {
                return DE48_CardType.Mastercard;
            }
            else if(card.getCardType().equals("MCFleet")) {
                return DE48_CardType.MastercardFleet;
            }
            else if(card.getCardType().equals("WexFleet")) {
                return DE48_CardType.WEX;
            }
            else if(card.getCardType().equals("Visa") || card.getCardType().equals("VisaCorporate")) {
                return DE48_CardType.Visa;
            }
            else if(card.getCardType().equals("VisaFleet")) {
                return DE48_CardType.VisaFleet;
            }
            else if(card.getCardType().equals(cardTypeVisaReadyLink)) {
                return DE48_CardType.PINDebitCard;
            }
            else if(card.getCardType().equals("DinersClub")) {
                return DE48_CardType.DinersClub;
            }
            else if(card.getCardType().equals("Discover")) {
                return DE48_CardType.DiscoverCard;
            }
            else if(card.getCardType().equals("Jcb")) {
                return DE48_CardType.JCB;
            }
            else if (card.getCardType().equals("VoyagerFleet")) {
                return DE48_CardType.Voyager;
            }
            else if (card.getCardType().equals("VisaPurchasing")) {
                return DE48_CardType.VisaPurchasing;
            }
            else if (card.getCardType().equals("MCPurchasing")) {
                return DE48_CardType.MastercardPurchasing;
            }
            else if (card.getCardType().equals("PayPal")) {
                return DE48_CardType.PayPal;
            }

        }
        else if(paymentMethod instanceof GiftCard) {
            GiftCard card = (GiftCard)paymentMethod;

            if(card.getCardType().equals("ValueLink")) {
                return DE48_CardType.ValueLinkStoredValue;
            }
            else if(card.getCardType().equals("StoredValue")) {
                return DE48_CardType.SVSStoredValue;
            }
            else if(card.getCardType().equals("HeartlandGift")) {
                return DE48_CardType.HeartlandGiftCard_Proprietary;
            }
        }
        else if(paymentMethod instanceof EBT) {
        	EBT card = (EBT)paymentMethod;
            if (card.getEbtCardType().equals(EbtCardType.FoodStamp))
            {
                return DE48_CardType.EBTFoodStamps;
            }
            return DE48_CardType.EBTCash;
        }
        else if (paymentMethod instanceof Ewic) {
            return DE48_CardType.eWIC;
        }
        return null;
    }
    private String mapChipCondition(EmvChipCondition chipCondition) {
        switch(chipCondition){
            case ChipFailPreviousSuccess:
                return "1";
            case ChipFailPreviousFail:
                return "2";
            default:
                return null;
        }
    }

    private String formatExpiry(String shortExpiry) {
    	if(shortExpiry != null) {
    		return shortExpiry.substring(2, 4).concat(shortExpiry.substring(0, 2));
    	}
		return shortExpiry;
    }

    // check result & put to IBatchProvider if data collect
    private <T extends TransactionBuilder<Transaction>> String checkResponse(String responseCode, NetworkMessage request, NetworkMessage response, T builder) {
        ArrayList<String> successCodes = new ArrayList<>();
        successCodes.add("000");
        successCodes.add("400");
        successCodes.add("500");
        successCodes.add("501");
        successCodes.add("580");

        BigDecimal amount = request.getAmount(DataElementId.DE_004);
        TransactionType transactionType = null;
        PaymentMethodType paymentMethodType = null;
        if(builder != null) {
            transactionType = builder.getTransactionType();
            if(builder.getPaymentMethod() != null) {
                paymentMethodType = builder.getPaymentMethod().getPaymentMethodType();
            }
        }

        // report successes
        if(successCodes.contains(responseCode)) {
            if(builder != null && request.isDataCollect(paymentMethodType)) {
                String encodedRequest = null;

                // check if we need to build the implied data-collect
                if(transactionType.equals(TransactionType.Sale)) {
                    NetworkMessage impliedCapture = new NetworkMessage(Iso8583MessageType.CompleteMessage);
                    impliedCapture.setMessageTypeIndicator("1220");
                    impliedCapture.set(DataElementId.DE_003, request.getString(DataElementId.DE_003));
                    impliedCapture.set(DataElementId.DE_004, request.getString(DataElementId.DE_004));
                    impliedCapture.set(DataElementId.DE_007, request.getString(DataElementId.DE_007));
                    impliedCapture.set(DataElementId.DE_011, request.getString(DataElementId.DE_011));
                    impliedCapture.set(DataElementId.DE_012, request.getString(DataElementId.DE_012));
                    impliedCapture.set(DataElementId.DE_017, request.getString(DataElementId.DE_012).substring(0, 4));
                    impliedCapture.set(DataElementId.DE_018, request.getString(DataElementId.DE_018));
                    impliedCapture.set(DataElementId.DE_022, request.getDataElement(DataElementId.DE_022, DE22_PosDataCode.class));
                    impliedCapture.set(DataElementId.DE_024, request.getString(DataElementId.DE_024));
                    impliedCapture.set(DataElementId.DE_030, request.getString(DataElementId.DE_030));
                    impliedCapture.set(DataElementId.DE_038, response.getString(DataElementId.DE_038));
                    impliedCapture.set(DataElementId.DE_041, request.getString(DataElementId.DE_041));
                    impliedCapture.set(DataElementId.DE_042, request.getString(DataElementId.DE_042));
                    impliedCapture.set(DataElementId.DE_043, request.getString(DataElementId.DE_043));
                    impliedCapture.set(DataElementId.DE_048, request.getDataElement(DataElementId.DE_048, DE48_MessageControl.class));

                    // DE_062 Card Issuer Data
                    DE62_CardIssuerData requestIssuerData = request.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
                    if(requestIssuerData == null) {
                        requestIssuerData = new DE62_CardIssuerData();
                    }

                    DE62_CardIssuerData responseIssuerData = response.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
                    if(responseIssuerData != null) {
                        String ntsData = responseIssuerData.get(CardIssuerEntryTag.NTS_System);
                        if(ntsData != null) {
                            requestIssuerData.add(CardIssuerEntryTag.NTS_System, ntsData);
                        }
                    }

                    // DE_002 / DE_014 - PAN / EXP DATE
                    if(request.has(DataElementId.DE_002)) {
                        impliedCapture.set(DataElementId.DE_002, request.getString(DataElementId.DE_002));
                        impliedCapture.set(DataElementId.DE_014, request.getString(DataElementId.DE_014));
                    }
                    else if(request.has(DataElementId.DE_035)) {
                        CreditTrackData track = new CreditTrackData(request.getString(DataElementId.DE_035));
                        impliedCapture.set(DataElementId.DE_002, track.getPan());
                        impliedCapture.set(DataElementId.DE_014, track.getExpiry());
                    }
                    else {
                        CreditTrackData track = new CreditTrackData(request.getString(DataElementId.DE_045));
                        impliedCapture.set(DataElementId.DE_002, track.getPan());
                        impliedCapture.set(DataElementId.DE_014, track.getExpiry());
                    }
                    impliedCapture.set(DataElementId.DE_062, requestIssuerData);

                    // DE_025 - MESSAGE REASON CODE
                    if(paymentMethodType != null && paymentMethodType.equals(PaymentMethodType.Debit)) {
                        impliedCapture.set(DataElementId.DE_025, "1379");
                    }
                    else {
                        impliedCapture.set(DataElementId.DE_025, "1376");
                    }

                    // DE_056 - ORIGINAL TRANSACTION DATA
                    DE56_OriginalDataElements originalDataElements = new DE56_OriginalDataElements();
                    originalDataElements.setMessageTypeIdentifier("1200");
                    originalDataElements.setSystemTraceAuditNumber(request.getString(DataElementId.DE_011));
                    originalDataElements.setTransactionDateTime(request.getString(DataElementId.DE_012));
                    impliedCapture.set(DataElementId.DE_056, originalDataElements);

                    encodedRequest = encodeRequest(impliedCapture);
                    if(batchProvider != null) {
                        batchProvider.reportDataCollect(transactionType, paymentMethodType, amount, encodedRequest);
                    }
                }
                else if(!transactionType.equals(TransactionType.DataCollect)) {
                    encodedRequest = encodeRequest(request);
                    if(batchProvider != null) {
                        batchProvider.reportDataCollect(transactionType, paymentMethodType, amount, encodedRequest);
                    }
                }
                return encodedRequest;
            }

            // if there's a batch provider handle the batch close stuff
            //not supported


//            if ((responseCode.equals("500") || responseCode.equals("501")) && batchProvider != null) {
//                batchProvider.closeBatch(responseCode.equals("500"));
//            } else if (responseCode.equals("580")) {
//                if(batchProvider != null) {
//                    try {
//                        LinkedList<String> encodedRequests = batchProvider.getEncodedRequests();
//                        if (encodedRequests != null) {
//                            resentTransactions = new LinkedList<Transaction>();
//                            for (String encRequest : encodedRequests) {
//                                try {
//                                    NetworkMessage newRequest = decodeRequest(encRequest);
//                                    newRequest.setMessageTypeIndicator("1221");
//
//                                    Transaction resend = sendRequest(newRequest, null, new byte[2], new byte[8]);
//                                    resentTransactions.add(resend);
//                                } catch (ApiException exc) {
//                                   exc.printStackTrace();
//                                }
//                            }
//
//                            // resend the batch close
//                            request.setMessageTypeIndicator("1521");
//                            resentBatch = sendRequest(request, builder, new byte[2], new byte[8]);
//                        }
//                    } catch (ApiException exc) {
//                        exc.printStackTrace();
//                    }
//                }
//                return encodeRequest(request);
//            }
        }
        return null;
    }

    private String encodeRequest(NetworkMessage request) {
        byte[] encoded = Base64.encodeBase64(request.buildMessage());
        if(requestEncoder == null) {
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }
        return requestEncoder.encode(new String(encoded));
    }
    //not supported code

//    private NetworkMessage decodeRequest(String encodedStr) {
//        if(requestEncoder == null) {
//            requestEncoder = new PayrollEncoder(companyId, terminalId);
//        }
//
//        String requestStr = requestEncoder.decode(encodedStr);
//
//        byte[] decoded = Base64.decodeBase64(requestStr);
//        MessageReader mr = new MessageReader(decoded);
//
//        String mti = mr.readString(4);
//        byte[] buffer = mr.readBytes(decoded.length);
//        NetworkMessage request = NetworkMessage.parse(buffer, Iso8583MessageType.CompleteMessage);
//        request.setMessageTypeIndicator(mti);
//        return request;
//    }

    private <T extends TransactionBuilder<Transaction>> void validate(T builder) throws BuilderException, UnsupportedTransactionException {
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        getPaymentMethodFromTransactionReference(paymentMethod);

        TransactionType transactionType = builder.getTransactionType();

        if(paymentMethod instanceof EBT) {
            EBT ebtCard = (EBT)paymentMethod;
            if(ebtCard.getEbtCardType() == null) {
                throw new BuilderException("The card type must be specified for EBT transactions.");
            }

            // no refunds on cash benefit cards
            if(ebtCard.getEbtCardType() == EbtCardType.CashBenefit && transactionType.equals(TransactionType.Refund)) {
                throw new UnsupportedTransactionException("Refunds are not allowed for cash benefit cards.");
            }

            // no authorizations for ebt
            if(transactionType.equals(TransactionType.Auth)) {
                throw new UnsupportedTransactionException("Authorizations are not allowed for EBT cards.");
            }

            // no manual balance inquiry
            if(transactionType.equals(TransactionType.Balance) && !(paymentMethod instanceof ITrackData)) {
                throw new BuilderException("Track data must be used for EBT balance inquiries.");
            }
        }

        // WEX Specific
        if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals(cardTypeWexFleet)) {
            if(transactionType.equals(TransactionType.Refund)) {
                if (builder.getTransactionMatchingData() == null) {
                    throw new BuilderException("Transaction mapping data object required for WEX refunds.");
                } else {
                    TransactionMatchingData tmd = builder.getTransactionMatchingData();
                    if (StringUtils.isNullOrEmpty(tmd.getOriginalBatchNumber()) || StringUtils.isNullOrEmpty(tmd.getOriginalDate())) {
                        throw new BuilderException("Transaction Matching Data incomplete. Original batch number and date are required for WEX refunds.");
                    }
                }
            }
            FleetData fleetData = builder.getFleetData();
            if (fleetData == null && !(paymentMethod instanceof CreditTrackData) || ((fleetData != null && fleetData.getPurchaseDeviceSequenceNumber() == null) && (paymentMethod instanceof CreditTrackData && ((CreditTrackData) paymentMethod).getPurchaseDeviceSequenceNumber() == null))) {
                throw new BuilderException(WEX_SEQ_EXCEPTION_MESSAGE_STRING);
            }
        }

        if(builder instanceof ManagementBuilder) {
            ManagementBuilder mb = (ManagementBuilder)builder;
            TransactionReference reference = (TransactionReference)mb.getPaymentMethod();

            if(transactionType.equals(TransactionType.BatchClose) && batchProvider == null) {
                if(mb.getTransactionCount() == null || mb.getTotalCredits() == null || mb.getTotalDebits() == null) {
                    throw new BuilderException("When an IBatchProvider is not present, you must specify transaction count, total debits and total credits when calling batch close.");
                }

                if(mb.getBatchNumber() == 0) {
                    throw new BuilderException("When an IBatchProvider is not present, you must specify a batch and sequence number for a batch close.");
                }
            }

            if(transactionType.equals(TransactionType.Refund) && reference.getOriginalPaymentMethod() instanceof EBT) {
                    EBT ebtCard = (EBT)reference.getOriginalPaymentMethod();
                    // no refunds on cash benefit cards
                    if(ebtCard.getEbtCardType() == EbtCardType.CashBenefit && transactionType.equals(TransactionType.Refund)) {
                        throw new UnsupportedTransactionException("Refunds are not allowed for cash benefit cards.");
                    }
            }

            if(transactionType.equals(TransactionType.Reversal)) {
                if(StringUtils.isNullOrEmpty(reference.getOriginalProcessingCode())) {
                    throw new BuilderException("The original processing code should be specified when performing a reversal.");
                }

                // IRR for fleet reversals
                boolean isFallbackCodeNone = reference.getNtsData() != null && reference.getNtsData().getFallbackCode().equals(FallbackCode.None);
                    if(paymentMethod instanceof Credit && ((Credit) paymentMethod).isFleet() && StringUtils.isNullOrEmpty(mb.getReferenceNumber()) && isFallbackCodeNone) {
                        throw new BuilderException("Reference Number is required for fleet voids/reversals.");
                    }
            }
            boolean isTransactionTypeCreditVoid = transactionType.equals(TransactionType.Void) && paymentMethod instanceof Credit;
            // IRR for fleet reversals
                if( isTransactionTypeCreditVoid && ((Credit) paymentMethod).isFleet() && StringUtils.isNullOrEmpty(((ManagementBuilder) builder).getReferenceNumber())) {
                    throw new BuilderException("Reference Number is required for fleet voids/reversals.");
                }
        }
        else {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(paymentMethod!=null && paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit) && acceptorConfig.getAddress() == null) {
                throw new BuilderException("Address is required in acceptor config for Debit/EBT Transactions.");
            }

            if(paymentMethod instanceof EBT) {
                EBT card = (EBT)paymentMethod;
                if(card.getEbtCardType().equals(EbtCardType.FoodStamp) && authBuilder.getCashBackAmount() != null) {
                    throw new BuilderException("Cash back is not allowed for Food Stamp cards.");
                }
            }
        }
    }

    private static IPaymentMethod getPaymentMethodFromTransactionReference(IPaymentMethod paymentMethod) {
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            if(reference.getOriginalPaymentMethod() != null) {
                paymentMethod = reference.getOriginalPaymentMethod();
            }
        }
         return paymentMethod;
    }

        private EncryptedFieldMatrix getEncryptionField(IPaymentMethod paymentMethod, EncryptionType encryptionType){
        if(encryptionType.equals(EncryptionType.TDES)){
            if(paymentMethod instanceof ICardData){
                return EncryptedFieldMatrix.Pan;
            }
            else if(paymentMethod instanceof ITrackData) {
                TrackNumber trackType=((ITrackData)paymentMethod).getTrackNumber();
                if (trackType == TrackNumber.TrackOne)
                    return EncryptedFieldMatrix.Track1;
                else if (trackType == TrackNumber.TrackTwo)
                    return EncryptedFieldMatrix.Track2;
            }else if(paymentMethod instanceof GiftCard) {
                TrackNumber trackType=((GiftCard)paymentMethod).getTrackNumber();
                if (trackType == TrackNumber.TrackOne)
                    return EncryptedFieldMatrix.Track1;
                else if (trackType == TrackNumber.TrackTwo)
                    return EncryptedFieldMatrix.Track2;
            }
        }else if (encryptionType.equals(EncryptionType.TEP1)||encryptionType.equals(EncryptionType.TEP2)){
            if(paymentMethod instanceof ICardData) {
                String encryptedCvn = ((ICardData) paymentMethod).getCvn();
                if(!StringUtils.isNullOrEmpty(encryptedCvn))
                    return EncryptedFieldMatrix.CustomerDataCSV;
                else
                    return EncryptedFieldMatrix.CustomerData;
            }
        }
        return EncryptedFieldMatrix.CustomerData;
    }
}