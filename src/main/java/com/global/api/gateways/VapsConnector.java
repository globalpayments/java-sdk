package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.*;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.*;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.entities.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.GatewayConnectorConfig;
import com.global.api.network.elements.*;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.terminals.DeviceMessage;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.*;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class VapsConnector extends GatewayConnectorConfig {
    private AcceptorConfig acceptorConfig;
    private IBatchProvider batchProvider;
    private CharacterSet characterSet = CharacterSet.ASCII;
    private String companyId;
    private ConnectionType connectionType;
    private String merchantType;
    private MessageType messageType;
    private String nodeIdentification;
    private ProtocolType protocolType;
    private IRequestEncoder requestEncoder;
    private IStanProvider stanProvider;
    private String terminalId;
    private String uniqueDeviceId;
    private LinkedList<Transaction> resentTransactions;
    private Transaction resentBatch;
    private NetworkProcessingFlag processingFlag;

    private boolean lrcFailure;

    private static final String DISCOVER = "Discover";
    private static final String CASH_AT_CHECKOUT_EXCEPTION = "The Cash at Checkout amount requested must be less than or equal to $120.00.";
    private static final String SUPPORTS_DISCOVER_ONLY_EXCEPTION = "Cash At Checkout supported for Discover card only.";
    private static final int AMEX_DISCOVER_CVV_LENGTH =4;
    private static final int MC_VISA_CVV_LENGTH =3;
    private static final String FEE_AMOUNT_SUPPORTS_EBT_CASH_BENEFITS_ONLY = "For EBT,Fee Amount supported for Cash Benefits card only";


    BatchSummary summary = new BatchSummary();

    private String SVS_VERSION = "0036";

    public void setAcceptorConfig(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
    }
    public void setBatchProvider(IBatchProvider batchProvider) {
        this.batchProvider = batchProvider;
        if(this.batchProvider != null && this.batchProvider.getRequestEncoder() != null) {
            requestEncoder = batchProvider.getRequestEncoder();
        }
    }
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    public void setNodeIdentification(String nodeIdentification) {
        this.nodeIdentification = nodeIdentification;
    }
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
    public void setRequestEncoder(IRequestEncoder requestEncoder) {
        this.requestEncoder = requestEncoder;
    }
    public void setStanProvider(IStanProvider provider) {
        this.stanProvider = provider;
    }
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    public void setUniqueDeviceId(String uniqueDeviceId) {
        this.uniqueDeviceId = uniqueDeviceId;
    }
    public NetworkProcessingFlag getProcessingFlag() {
        return processingFlag;
    }
    public void setProcessingFlag(NetworkProcessingFlag processingFlag) {
        this.processingFlag = processingFlag;
    }

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    @Override
    public boolean supportsOpenBanking() {
        return false;
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        validate(builder);

        // TODO: These should come from the builder somehow
        byte[] orgCorr1 = new byte[2];
        byte[] orgCorr2 = new byte[8];

        IPaymentMethod paymentMethod = null;
        PaymentMethodType paymentMethodType = null;
        NetworkMessage request = new NetworkMessage();
        paymentMethod = builder.getPaymentMethod();
        if(paymentMethod != null) {
            paymentMethodType = builder.getPaymentMethod().getPaymentMethodType();
        }
        TransactionType transactionType = builder.getTransactionType();
        boolean isPosSiteConfiguration = transactionType.equals(TransactionType.PosSiteConfiguration);
        boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getVisaFleet2()!=null && acceptorConfig.getVisaFleet2();
        boolean isDiscoverCashAtCheckout = acceptorConfig.getSupportsCashAtCheckout() != null && builder.getCashAtCheckoutAmount() != null && builder.getTransactionType().equals(TransactionType.Sale);
        Iso4217_CurrencyCode currencyCode = Iso4217_CurrencyCode.USD;
        EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
        if(!StringUtils.isNullOrEmpty(builder.getCurrency())) {
            currencyCode = builder.getCurrency().equalsIgnoreCase("USD") ? Iso4217_CurrencyCode.USD : Iso4217_CurrencyCode.CAD;
        }

        // if null set to false so there is always a value for this connector
        if(builder.isAmountEstimated() == null) {
            builder.withAmountEstimated(false);
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
        if(paymentMethod instanceof ICardData) {
            ICardData card = (ICardData)builder.getPaymentMethod();
            String token = card.getTokenizationData();
            if (token == null) {
                // DE 2: Primary Account Number (PAN) - LLVAR // 1100, 1200, 1220, 1300, 1310, 1320, 1420
                request.set(DataElementId.DE_002, card.getNumber());

                // DE 14: Date, Expiration - n4 (YYMM) // 1100, 1200, 1220, 1420
                request.set(DataElementId.DE_014, formatExpiry(card.getShortExpiry()));
            }

            // set data codes
            DE22_CardDataInputMode cardDataInputMode = acceptorConfig.getCardDataInputMode();
            if(cardDataInputMode != null){
                dataCode.setCardDataInputMode(cardDataInputMode);
            } else {
                dataCode.setCardDataInputMode(card.isReaderPresent() ? DE22_CardDataInputMode.KeyEntry : DE22_CardDataInputMode.Manual);
            }

            dataCode.setCardHolderPresence(card.isCardPresent() ? DE22_CardHolderPresence.CardHolder_Present : DE22_CardHolderPresence.CardHolder_NotPresent);
            dataCode.setCardPresence(card.isCardPresent() ? DE22_CardPresence.CardPresent : DE22_CardPresence.CardNotPresent);
            if(!StringUtils.isNullOrEmpty(card.getCvn())) {
                dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.OnCard_SecurityCode);
            }
            if(card instanceof EBTCardData) {
                if (tagData != null) {
                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactEmv);
                } else if (builder.getEmvChipCondition() != null) {
                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);
                }
            }
        }
        else if(paymentMethod instanceof ITrackData) {
            ITrackData card = (ITrackData)builder.getPaymentMethod();
            String token = card.getTokenizationData();

            // put the track data
            if(transactionType.equals(TransactionType.Refund) && (!paymentMethodType.equals(PaymentMethodType.Debit) && !paymentMethodType.equals(PaymentMethodType.EBT))) {
                if(paymentMethod instanceof IEncryptable && ((IEncryptable)paymentMethod).getEncryptionData() != null &&
                        !acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES)) {
                    request.set(DataElementId.DE_002, ((IEncryptable)card).getEncryptedPan());
                }
                else {
                    request.set(DataElementId.DE_002, card.getPan());
                }
                request.set(DataElementId.DE_014, card.getExpiry());
            }
            else if(card.getTrackNumber().equals(TrackNumber.TrackTwo) && token == null) {
                // DE 35: Track 2 Data - LLVAR ns.. 37
                request.set(DataElementId.DE_035, card.getTrackData());
            }
            else if(card.getTrackNumber().equals(TrackNumber.TrackOne) &&  token == null) {
                // DE 45: Track 1 Data - LLVAR ans.. 76
                request.set(DataElementId.DE_045, card.getTrackData());
            }
            else {
                if(card instanceof IEncryptable && ((IEncryptable) card).getEncryptionData() != null) {
                    EncryptionData encryptionData = ((IEncryptable) card).getEncryptionData();
                    if(encryptionData.getTrackNumber().equals("1") && token == null) {
                        // DE 45: Track 1 Data - LLVAR ans.. 76
                        request.set(DataElementId.DE_045, card.getValue());
                    }
                    else if (encryptionData.getTrackNumber().equals("2") && token == null) {
                        // DE 35: Track 2 Data - LLVAR ns.. 37
                        request.set(DataElementId.DE_035, card.getValue());
                    }
                }
            }

            // set data codes
            if(paymentMethodType.equals(PaymentMethodType.Credit) || paymentMethodType.equals(PaymentMethodType.Debit) || paymentMethodType.equals(PaymentMethodType.EBT)) {
                dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                dataCode.setCardPresence(DE22_CardPresence.CardPresent);
                if(tagData != null) {
                    DE22_CardDataInputMode cardDataInputMode = acceptorConfig.getCardDataInputMode();
                    if(cardDataInputMode != null){
                        dataCode.setCardDataInputMode(cardDataInputMode);
                    }
                    else if(tagData.isContactlessMsd()){
                        dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                    }
                    else {
                        if (card.getEntryMethod().equals(EntryMethod.Proximity)) {
                            dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessEmv);
                        } else dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactEmv);
                    }
                }
                else {
                    DE22_CardDataInputMode cardDataInputMode = acceptorConfig.getCardDataInputMode();
                    if(cardDataInputMode != null){
                        dataCode.setCardDataInputMode(cardDataInputMode);
                    }
                    else if(card.getEntryMethod() != null && card.getEntryMethod().equals(EntryMethod.Proximity)) {
                        dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                    }
                    else {
                        if(builder.getEmvChipCondition() != null) {
                            dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);
                        } else if(card instanceof EBTTrackData){
                            dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
                        }
                        else dataCode.setCardDataInputMode(DE22_CardDataInputMode.UnalteredTrackData);
                    }
                }
           }
        }
        else if(paymentMethod instanceof GiftCard) {
            GiftCard giftCard = (GiftCard)paymentMethod;

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
                //request.set(DataElementId.DE_014, giftCard.getExpiry());
            }

            // set data codes
            if(!StringUtils.isNullOrEmpty(giftCard.getPin())) {
                dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
            }
            else {
                dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.NotAuthenticated);
            }
            dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
            dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
            dataCode.setCardPresence(DE22_CardPresence.CardPresent);
        }

        if(paymentMethod instanceof IPinProtected) {
            // address validation for acceptorConfig
            if(acceptorConfig.getAddress() == null && (paymentMethodType.equals(PaymentMethodType.Debit) || paymentMethodType.equals(PaymentMethodType.EBT))) {
                throw new BuilderException("The address in the acceptor config cannot be null for PIN based transactions.");
            }

            String pinBlock = ((IPinProtected)paymentMethod).getPinBlock();
            if(!StringUtils.isNullOrEmpty(pinBlock)) {
                // DE 52: Personal Identification Number (PIN) Data - b8
                request.set(DataElementId.DE_052, StringUtils.bytesFromHex(pinBlock.substring(0, 16)));

                // DE 53: Security Related Control Information - LLVAR an..48
                request.set(DataElementId.DE_053, pinBlock.substring(16));

                // set the data code
                dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
            }
            else if(tagData != null && tagData.isOfflinePin()) {
                // set the data code
                dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
            }
        }

        // DE 1: Secondary Bitmap - b8 // M (AUTO GENERATED IN NetworkMessage)

        if (!isPosSiteConfiguration) {
            // DE 3: Processing Code - n6 (n2: TRANSACTION TYPE, n2: ACCOUNT TYPE 1, n2: ACCOUNT TYPE 2) // M 1100, 1200, 1220, 1420
            if(!transactionType.equals(TransactionType.FileAction)) {
                DE3_ProcessingCode processingCode = mapProcessingCode(builder);
                request.set(DataElementId.DE_003, processingCode);
            }

            String functionCode=mapFunctionCode(builder);
            // DE 4: Amount, Transaction - n12 // C 1100, 1200, 1220, 1420
            if(functionCode.equals("100")) {
                request.set(DataElementId.DE_004, StringUtils.toDecimal(builder.getAmount(), 12));
            }else{
                if (isDiscoverCashAtCheckout){
                    request.set(DataElementId.DE_004, StringUtils.toDecimal((builder.getCashAtCheckoutAmount().add(builder.getAmount())), 12));
                }else {
                    request.set(DataElementId.DE_004, StringUtils.toNumeric(builder.getAmount(), 12));
                }
            }
            // DE 7: Date and Time, Transmission - n10 (MMDDhhmmss) // C
            request.set(DataElementId.DE_007, DateTime.now(DateTimeZone.UTC).toString("MMddhhmmss"));
        }

        // DE 11: System Trace Audit Number (STAN) - n6 // M
        int stan = builder.getSystemTraceAuditNumber();
        if(stan == 0 && stanProvider != null) {
            stan = stanProvider.generateStan();
        }
        request.set(DataElementId.DE_011, StringUtils.padLeft(stan, 6, '0'));

        // DE 12: Date and Time, Transaction - n12 (YYMMDDhhmmss)
        String timestamp = builder.getTimestamp();
        if(StringUtils.isNullOrEmpty(timestamp)) {
            timestamp = DateTime.now().toString("yyMMddhhmmss");
        }
        request.set(DataElementId.DE_012, timestamp);

        // DE 15: Date, Settlement - n6 (YYMMDD) // C
        // DE 17: Date, Capture - n4 (MMDD) // C

        // DE 18: Merchant Type - n4 // C 1100, 1200, 1220, 1300, 1320, 1420 (Same as MCC Code - Add to config since will be same for all transactions)
        request.set(DataElementId.DE_018, merchantType);

        // DE 19: Country Code, Acquiring Institution - n3 (ISO 3166) // C Config value perhaps? Same for each message
        //request.set(DataElementId.DE_019, "840");

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
            dataCode.setCardDataInputCapability(acceptorConfig.getCardDataInputCapability());
            dataCode.setCardHolderAuthenticationCapability(acceptorConfig.getCardHolderAuthenticationCapability());
            dataCode.setCardCaptureCapability(acceptorConfig.isCardCaptureCapability());
            dataCode.setOperatingEnvironment(acceptorConfig.getOperatingEnvironment());
            dataCode.setCardHolderAuthenticationEntity(acceptorConfig.getCardHolderAuthenticationEntity());
            dataCode.setCardDataOutputCapability(acceptorConfig.getCardDataOutputCapability());
            dataCode.setTerminalOutputCapability(acceptorConfig.getTerminalOutputCapability());
            dataCode.setPinCaptureCapability(acceptorConfig.getPinCaptureCapability());
            request.set(DataElementId.DE_022, dataCode);
        }

        // DE 23: Card Sequence Number - n3 // C 1100, 1120, 1200, 1220, 1420 (Applies to EMV cards if the sequence number is returned from the terminal)

        // DE 24: Function Code - n3 // M
        request.set(DataElementId.DE_024, mapFunctionCode(builder));

        // DE 25: Message Reason Code - n4 // C 1100, 1120, 1200, 1220, 1300, 1320, 1420, 16XX, 18XX

        // DE 28: Date, Reconciliation - n6 (YYMMDD)
        if(transactionType.equals(TransactionType.BatchClose)){
            String date =  DateTime.now().toString("yyMMdd");
            request.set(DataElementId.DE_028,date);
        }
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
        if(StringUtils.isNullOrEmpty(companyIdValue)) {
            companyIdValue = companyId;
        }
        request.set(DataElementId.DE_041, StringUtils.padRight(companyIdValue, 8, ' '));

        // DE 42: Card Acceptor Identification Code - ans15
        request.set(DataElementId.DE_042, StringUtils.padRight(terminalId, 15, ' '));

        /* DE 43: Card Acceptor Name/Location - LLVAR ans.. 99
            43.1 NAME-STREET-CITY ans..83 Name\street\city\
            43.2 POSTAL-CODE ans10
            43.3 REGION ans3 Two letter state/province code for the United States and Canada. Refer to the GlobalPayments Integrator’s Guide.
            43.4 COUNTRY-CODE a3 See A.30.1 ISO 3166-1: Country Codes, p. 809.
         */
        if(acceptorConfig.getAddress() != null && !(transactionType.equals(TransactionType.PosSiteConfiguration))) {
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
        if(!isPosSiteConfiguration) {
            EbtCardType card = null;
            if (paymentMethod instanceof EBT) {
                EBT ebtCard = (EBT) paymentMethod;
                card = ebtCard.getEbtCardType();
            }
            if (paymentMethodType.equals(paymentMethodType.Debit) || ((paymentMethodType.equals(PaymentMethodType.EBT) && card.equals(EbtCardType.CashBenefit)) || paymentMethodType.equals(paymentMethodType.Credit))) {

                if (builder.getFeeAmount() != null && !isDiscoverCashAtCheckout) {
                    DE46_FeeAmounts feeAmounts = new DE46_FeeAmounts();
                    feeAmounts.setFeeTypeCode(builder.getFeeType());
                    feeAmounts.setCurrencyCode(currencyCode);
                    feeAmounts.setAmount(builder.getFeeAmount());
                    feeAmounts.setReconciliationCurrencyCode(currencyCode);
                    request.set(DataElementId.DE_046, feeAmounts);
                }
            }
            else if (paymentMethodType.equals(PaymentMethodType.EBT) && card.equals(EbtCardType.FoodStamp) && builder.getFeeAmount() != null){
                throw new UnsupportedOperationException(FEE_AMOUNT_SUPPORTS_EBT_CASH_BENEFITS_ONLY);
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
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the GlobalPayments system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
         */
        // DE48-5
//        messageControl.setShiftNumber(builder.getShiftNumber());

        if (!isPosSiteConfiguration) {
            DE48_MessageControl messageControl = mapMessageControl(builder);
            request.set(DataElementId.DE_048, messageControl);
        }
        // DE 49: Currency Code, Transaction - n3
        if(!currencyCode.equals(Iso4217_CurrencyCode.USD) && builder.getAmount()!=null) {
            request.set(DataElementId.DE_049, currencyCode.getValue());
        }else if (paymentMethod instanceof GiftCard){
            if(currencyCode!=null && builder.getAmount()!=null) {
                request.set(DataElementId.DE_049, currencyCode.getValue());
            }
        }
        // DE 50: Currency Code, Reconciliation - n3

        /* DE 54: Amounts, Additional - LLVAR ans..120
            54.1 ACCOUNT TYPE, ADDITIONAL AMOUNTS n2 Positions 3 and 4 or positions 5 and 6 of the processing code data element.
            54.2 AMOUNT TYPE, ADDITIONAL AMOUNTS n2 Identifies the purpose of the transaction amounts.
            54.3 CURRENCY CODE, ADDITIONAL AMOUNTS n3 Use DE 49 codes.
            54.4 AMOUNT, ADDITIONAL AMOUNTS x + n12 See Use of the Terms Credit and Debit under Table 1-2 Transaction Processing, p. 61.
         */
        if(builder.getCashBackAmount() != null || transactionType.equals(TransactionType.BenefitWithdrawal)) {
            DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
            if(paymentMethod instanceof GiftCard) {
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashCardAccount, currencyCode, builder.getCashBackAmount());
                amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashCardAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
            }
            else if(paymentMethod instanceof EBT) {
                if(transactionType.equals(TransactionType.BenefitWithdrawal)) {
                    amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getAmount());
                }
                else {
                    amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getCashBackAmount());
                    amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
                }
            }
            else if(paymentMethod instanceof Debit) {
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.PinDebitAccount, currencyCode, builder.getCashBackAmount());
                amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.PinDebitAccount, currencyCode, builder.getAmount().subtract(builder.getCashBackAmount()));
            }
            if(amountsAdditional.size() > 0) {
                request.set(DataElementId.DE_054, amountsAdditional);
            }
        }
        else if (paymentMethod instanceof Credit) {
            Credit card = (Credit) paymentMethod;
            DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
            if (builder.getProductData() != null) {
                DE63_ProductData productData = builder.getProductData().toDataElement();
                productData.setCardType(card.getCardType());

                if (card.getCardType().equals("VisaFleet") && (isVisaFleet2)) {
                    if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                        amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelWithTax());
                        amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelAmount());
                    } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel)) {
                        amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                    }
                    if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                        amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelAmount());
                        amountsAdditional.put(DE54_AmountTypeCode.GROSSFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelWithTax());
                    } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                        amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                        amountsAdditional.put(DE54_AmountTypeCode.GROSSFUELPRICE, DE3_AccountType.Unspecified, currencyCode,new BigDecimal(0));
                    }
                    if(productData.getDiscount()!=null){
                        amountsAdditional.put(DE54_AmountTypeCode.AmountDiscount, DE3_AccountType.Unspecified, currencyCode,productData.getDiscount());
                    }
                    if(productData.getSalesTax()!=null){
                        amountsAdditional.put(DE54_AmountTypeCode.AmountTax, DE3_AccountType.Unspecified, currencyCode,productData.getSalesTax());
                    }
                    request.set(DataElementId.DE_054, amountsAdditional);
                }
            }else if (isDiscoverCashAtCheckout){
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.Unspecified, currencyCode, getCashAtCheckoutAmount(builder,card));
                request.set(DataElementId.DE_054, amountsAdditional);
            }
        }
        // DE 55: Integrated Circuit Card (ICC) Data - LLLVAR b..512
        if(tagData != null) {
            // I have a feeling this will need to come back... leaving it for now
            if(!StringUtils.isNullOrEmpty(tagData.getCardSequenceNumber())) {
                String cardSequenceNumber = StringUtils.padLeft(tagData.getCardSequenceNumber(), 3, '0');
                request.set(DataElementId.DE_023, cardSequenceNumber);
            }
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
        if(builder.getProductData() != null) {
            DE63_ProductData productData = builder.getProductData().toDataElement();
            request.set(DataElementId.DE_063, productData);
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
        // DE 116: eWIC Overflow Data - LLLVAR ansb..999
        // DE 117: eWIC Data - LLLVAR ansb..999
        // DE 123: Reconciliation Totals - LLLVAR ans..999
        // DE 124: Sundry Data - LLLVAR ans..999
        // DE 125: Extended Response Data 1 - LLLVAR ans..999
        // DE 126: Extended Response Data 2 - LLLVAR ans..999

        // DE 127: Forwarding Data - LLLVAR ans..999
        DE127_ForwardingData forwardingData = new DE127_ForwardingData();
        String encryptedPan = null;
        if(paymentMethod instanceof IEncryptable) {
            EncryptionData encryptionData = ((IEncryptable)paymentMethod).getEncryptionData();
            if((paymentMethod instanceof CreditTrackData) && transactionType.equals(TransactionType.Refund)) {
                encryptedPan = ((IEncryptable) paymentMethod).getEncryptedPan();
            }
            if(encryptionData != null) {
                EncryptionType encryptionType=acceptorConfig.getSupportedEncryptionType();
                if(encryptedPan != null && encryptionType.equals(EncryptionType.TDES)){
                    encryptionData.setKtb(encryptedPan);
                }
                EncryptedFieldMatrix encryptedField=getEncryptionField(paymentMethod,encryptionType, transactionType);
                if(encryptionType.equals(EncryptionType.TDES)){
                    forwardingData.setServiceType(acceptorConfig.getServiceType());
                    forwardingData.setOperationType(acceptorConfig.getOperationType());
                }
                forwardingData.setEncryptedField(encryptedField);

                // check for encrypted cid
                if(paymentMethod instanceof ICardData) {
                    String encryptedCvn = ((ICardData) paymentMethod).getCvn();
                    if(!StringUtils.isNullOrEmpty(encryptedCvn)) {
                        forwardingData.addEncryptionData(encryptionType, encryptionData,encryptedCvn);
                    } else{
                        forwardingData.addEncryptionData(encryptionType, encryptionData);
                    }
                }else{
                    forwardingData.addEncryptionData(encryptionType, encryptionData);
                }

                request.set(DataElementId.DE_127, forwardingData);
            }
        }
        if (paymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) paymentMethod;
            if (cardData != null) {
                String tokenizationData = cardData.getTokenizationData();
                if (tokenizationData != null) {
                    setTokenizationData(forwardingData, cardData,null, null, tokenizationData);
                    request.set(DataElementId.DE_127, forwardingData);
                }
            }
        }
        if (paymentMethod instanceof ITrackData) {
            ITrackData trackData = (ITrackData) paymentMethod;
            if (trackData != null) {
                String tokenizationData = trackData.getTokenizationData();
                if (tokenizationData != null) {
                    setTokenizationData(forwardingData,null, trackData, null, tokenizationData);
                    request.set(DataElementId.DE_127, forwardingData);
                }
            }
        }
        return sendRequest(request, builder, orgCorr1, orgCorr2);
    }
    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        validate(builder);

        // TODO: These should come from the builder somehow
        byte[] orgCorr1 = new byte[2];
        byte[] orgCorr2 = new byte[8];

        NetworkMessage request = new NetworkMessage();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        TransactionType transactionType = builder.getTransactionType();
        boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getVisaFleet2()!=null && acceptorConfig.getVisaFleet2();
        Iso4217_CurrencyCode currencyCode = Iso4217_CurrencyCode.USD;
        EmvData tagData = EmvUtils.parseTagData(builder.getTagData(), isEnableLogging());
        if(!StringUtils.isNullOrEmpty(builder.getCurrency())) {
            currencyCode = builder.getCurrency().equalsIgnoreCase("USD") ? Iso4217_CurrencyCode.USD : Iso4217_CurrencyCode.CAD;
        }

        BigDecimal transactionAmount = builder.getAmount();
        if(transactionAmount == null && paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference)paymentMethod;
            transactionAmount = transactionReference.getOriginalApprovedAmount();
        }

        // MTI
        String mti = mapMTI(builder);
        request.setMessageTypeIndicator(mti);

        // pos data code
        DE22_PosDataCode dataCode = new DE22_PosDataCode();

        // DE 1: Secondary Bitmap - b8 // M
        request.set(DataElementId.DE_001, new byte[8]); // TODO: This should be better

        // DE 2: Primary Account Number (PAN) - LLVAR // 1100, 1200, 1220, 1300, 1310, 1320, 1420
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference)paymentMethod;

            // Original Card Data
            if(transactionReference.getOriginalPaymentMethod() != null) {
                IPaymentMethod originalPaymentMethod = transactionReference.getOriginalPaymentMethod();
                PaymentMethodType paymentMethodType = originalPaymentMethod.getPaymentMethodType();

                if(originalPaymentMethod instanceof ICardData) {
                    ICardData cardData = (ICardData) originalPaymentMethod;
                    String token = cardData.getTokenizationData();
                    if( token == null) {
                        // DE 2: PAN & DE 14 Expiry
                        request.set(DataElementId.DE_002, cardData.getNumber());
                        request.set(DataElementId.DE_014, formatExpiry(cardData.getShortExpiry()));
                    }

                    // Data codes
                    dataCode.setCardDataInputMode(cardData.isReaderPresent() ? DE22_CardDataInputMode.KeyEntry : DE22_CardDataInputMode.Manual);
                    dataCode.setCardHolderPresence(cardData.isCardPresent() ? DE22_CardHolderPresence.CardHolder_Present : DE22_CardHolderPresence.CardHolder_NotPresent);
                    dataCode.setCardPresence(cardData.isCardPresent() ? DE22_CardPresence.CardPresent : DE22_CardPresence.CardNotPresent);
                }
                else if (originalPaymentMethod instanceof ITrackData) {
                    ITrackData track = (ITrackData)originalPaymentMethod;

                    String token = track.getTokenizationData();
                    if (token == null) {
                        if (track instanceof IEncryptable && ((IEncryptable) track).getEncryptionData() != null) {
                            //EncryptionData encryptionData = ((IEncryptable) track).getEncryptionData();
//                        if(encryptionData.getTrackNumber().equals("1")) {
//                            request.set(DataElementId.DE_045, track.getValue());
//                        }
//                        else if (encryptionData.getTrackNumber().equals("2")) {
//                            request.set(DataElementId.DE_035, track.getValue());
//                        }
                            // DE 2: PAN & DE 14 Expiry
                            if (!acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES)) {
                                request.set(DataElementId.DE_002, ((IEncryptable) track).getEncryptedPan());
                            }
                            request.set(DataElementId.DE_014, track.getExpiry());
                        } else {
                            // DE 2: PAN & DE 14 Expiry
                            request.set(DataElementId.DE_002, track.getPan());
                            request.set(DataElementId.DE_014, track.getExpiry());
                        }
                    }

                    // set data codes
                    if(paymentMethodType.equals(PaymentMethodType.Credit) || paymentMethodType.equals(PaymentMethodType.Debit)) {
                        dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                        dataCode.setCardPresence(DE22_CardPresence.CardPresent);

                        if(tagData != null) {
                            if(tagData.isContactlessMsd()){
                                dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                            }
                            else {
                                if (track.getEntryMethod().equals(EntryMethod.Proximity)) {
                                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessEmv);
                                } else dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactEmv);
                            }
                        }
                        else {
                            if(track.getEntryMethod().equals(EntryMethod.Proximity)) {
                                dataCode.setCardDataInputMode(DE22_CardDataInputMode.ContactlessMsd);
                            }
                            else {
                                if(builder.getEmvChipCondition() != null) {
                                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe_Fallback);
                                }
                                else dataCode.setCardDataInputMode(DE22_CardDataInputMode.UnalteredTrackData);
                            }
                        }
                    }
                }
                else if (originalPaymentMethod instanceof GiftCard) {
                    GiftCard gift = (GiftCard)originalPaymentMethod;

                    // DE 35 / DE 45
                    if(gift.getValueType()!=null && gift.getValueType().equals("TrackData")) {
                        if(gift.getTrackNumber().equals(TrackNumber.TrackTwo)) {
                            request.set(DataElementId.DE_035, gift.getTrackData());
                        }
                        else {
                            request.set(DataElementId.DE_045, gift.getTrackData());
                        }
                    }
                    else {
                        // DE 2: PAN & DE 14 Expiry
                        request.set(DataElementId.DE_002, gift.getNumber());
                        if (acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES)) {
                        request.set(DataElementId.DE_014, gift.getExpiry());
                        }
                    }

                    // set data codes
                    if(!StringUtils.isNullOrEmpty(gift.getPin())) {
                        dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.PIN);
                    }
                    else {
                        dataCode.setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod.NotAuthenticated);
                    }
                    dataCode.setCardDataInputMode(DE22_CardDataInputMode.MagStripe);
                    dataCode.setCardHolderPresence(DE22_CardHolderPresence.CardHolder_Present);
                    dataCode.setCardPresence(DE22_CardPresence.CardPresent);
                }
            }
        }

        boolean isNeitherBatchCloseNorTimeRequest = !transactionType.equals(TransactionType.BatchClose) && !transactionType.equals(TransactionType.TimeRequest);

        // DE 3: Processing Code - n6 (n2: TRANSACTION TYPE, n2: ACCOUNT TYPE 1, n2: ACCOUNT TYPE 2) // M 1100, 1200, 1220, 1420
        if(isNeitherBatchCloseNorTimeRequest) {
            DE3_ProcessingCode processingCode = mapProcessingCode(builder);
            request.set(DataElementId.DE_003, processingCode);

            // DE 4: Amount, Transaction - n12 // C 1100, 1200, 1220, 1420
            request.set(DataElementId.DE_004, StringUtils.toNumeric(transactionAmount, 12));

            // DE 7: Date and Time, Transmission - n10 (MMDDhhmmss) // C
            request.set(DataElementId.DE_007, DateTime.now(DateTimeZone.UTC).toString("MMddhhmmss"));
        }

        // DE 11: System Trace Audit Number (STAN) - n6 // M
        int stan = builder.getSystemTraceAuditNumber();
        if(stan == 0 && stanProvider != null) {
            stan = stanProvider.generateStan();
        }
        request.set(DataElementId.DE_011, StringUtils.padLeft(stan, 6, '0'));

        // DE 12: Date and Time, Transaction - n12 (YYMMDDhhmmss)
        String timestamp = builder.getTimestamp();
        if(StringUtils.isNullOrEmpty(timestamp)) {
            timestamp = DateTime.now().toString("yyMMddhhmmss");
        }
        request.set(DataElementId.DE_012, timestamp);

        // DE 15: Date, Settlement - n6 (YYMMDD) // C
        // DE 17: Date, Capture - n4 (MMDD) // C
        if(transactionType.equals(TransactionType.Capture) || transactionType.equals(TransactionType.PreAuthCompletion)) {
            String captureTimestamp = timestamp.substring(2, 6);

            if(paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;
                if(reference.getOriginalPaymentMethod() instanceof EBT) {
                    request.set(DataElementId.DE_017, captureTimestamp);
                }
            } else if(paymentMethod instanceof EBT) {
                request.set(DataElementId.DE_017, captureTimestamp);
            }
        }

        // DE 18: Merchant Type - n4 // C 1100, 1200, 1220, 1300, 1320, 1420 (Same as MCC Code - Add to config since will be same for all transactions)
        request.set(DataElementId.DE_018, merchantType);

        // DE 19: Country Code, Acquiring Institution - n3 (ISO 3166) // C Config value perhaps? Same for each message
        //request.set(DataElementId.DE_019, "840");

        if(isNeitherBatchCloseNorTimeRequest) {
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
            dataCode.setCardDataInputCapability(acceptorConfig.getCardDataInputCapability());
            dataCode.setCardHolderAuthenticationCapability(acceptorConfig.getCardHolderAuthenticationCapability());
            dataCode.setCardCaptureCapability(acceptorConfig.isCardCaptureCapability());
            dataCode.setOperatingEnvironment(acceptorConfig.getOperatingEnvironment());
            dataCode.setCardHolderAuthenticationEntity(acceptorConfig.getCardHolderAuthenticationEntity());
            dataCode.setCardDataOutputCapability(acceptorConfig.getCardDataOutputCapability());
            dataCode.setTerminalOutputCapability(acceptorConfig.getTerminalOutputCapability());
            dataCode.setPinCaptureCapability(acceptorConfig.getPinCaptureCapability());

            if(paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference)paymentMethod;

                String originalPosDataCode = reference.getPosDataCode();
                if(!StringUtils.isNullOrEmpty(originalPosDataCode)) {
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
        if(transactionType.equals(TransactionType.BatchClose)){
            String date = DateTime.now().toString("yyMMdd");
            request.set(DataElementId.DE_028, date);
        }

        /* DE 30: Amounts, Original - n24
            30.1 ORIGINAL AMOUNT, TRANSACTION n12 A copy of amount, transaction (DE 4) from the original transaction.
            30.2 ORIGINAL AMOUNT, RECONCILIATION n12 A copy of amount, reconciliation (DE 5) from the original transaction. Since DE 5 is not used, this element will contain all zeros.
         */
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference)paymentMethod;
            if(reference.getOriginalAmount() != null) {
                BigDecimal amount = builder.getAmount();
                if(amount != null) {
                    DE30_OriginalAmounts originalAmounts = new DE30_OriginalAmounts();

                    if(!AmountUtils.areEqual(amount, reference.getOriginalAmount())) {
                        originalAmounts.setOriginalTransactionAmount(reference.getOriginalAmount());
                        request.set(DataElementId.DE_030, originalAmounts);
                    }
                    else if(reference.getOriginalPaymentMethod() instanceof Debit
                            && (transactionType.equals(TransactionType.PreAuthCompletion) || transactionType.equals(TransactionType.Capture))) {
                        originalAmounts.setOriginalTransactionAmount(reference.getOriginalAmount());
                        request.set(DataElementId.DE_030, originalAmounts);

                        // TODO: Exclude Data-Collects for Sale/Refund
                    }
                    else if(reference.getOriginalPaymentMethod() instanceof Credit) {
                        Credit credit = (Credit)reference.getOriginalPaymentMethod();
                        if(credit.getCardType().equals("WexFleet") && transactionType.equals(TransactionType.Capture)) {
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
        //request.set(DataElementId.DE_037, builder.getClientTransactionId());

        // DE 38: Approval Code - anp6
        request.set(DataElementId.DE_038, builder.getAuthorizationCode());

        // DE 39: Action Code - n3

        // DE 41: Card Acceptor Terminal Identification Code - ans8
        String companyIdValue = builder.getCompanyId();
        if(StringUtils.isNullOrEmpty(companyIdValue)) {
            companyIdValue = companyId;
        }
        request.set(DataElementId.DE_041, StringUtils.padRight(companyIdValue, 8, ' '));

        // DE 42: Card Acceptor Identification Code - ans15
        request.set(DataElementId.DE_042, StringUtils.padRight(terminalId, 15, ' '));

        /* DE 43: Card Acceptor Name/Location - LLVAR ans.. 99
            43.1 NAME-STREET-CITY ans..83 Name\street\city\
            43.2 POSTAL-CODE ans10
            43.3 REGION ans3 Two letter state/province code for the United States and Canada. Refer to the GlobalPayments Integrator’s Guide.
            43.4 COUNTRY-CODE a3 See A.30.1 ISO 3166-1: Country Codes, p. 809.
         */
        if(acceptorConfig.getAddress() != null) {
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
        if (paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            if (reference.getFeeAmount() != null) {
                request.set(DataElementId.DE_046, reference.getFeeAmount());
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
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the GlobalPayments system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
         */
        DE48_MessageControl messageControl = mapMessageControl(builder);
        request.set(DataElementId.DE_048, messageControl);

        // DE 49: Currency Code, Transaction - n3
        //String currencyCode=Iso4217_CurrencyCode.valueOf(builder.getCurrency()).getValue();
        if(!currencyCode.equals(Iso4217_CurrencyCode.USD) && transactionAmount!=null){
            request.set(DataElementId.DE_049,currencyCode.getValue());
        }else if (paymentMethod instanceof TransactionReference){
            IPaymentMethod orignalPaymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            if(orignalPaymentMethod instanceof GiftCard && transactionAmount!=null) {
                request.set(DataElementId.DE_049, currencyCode.getValue());
            }
        }
        // DE 50: Currency Code, Reconciliation - n3
        if(transactionType.equals(TransactionType.BatchClose) && !currencyCode.equals(Iso4217_CurrencyCode.USD)) {
            if(!StringUtils.isNullOrEmpty(builder.getCurrency())) {
                if(builder.getCurrency().equalsIgnoreCase("CAD")) {
                    request.set(DataElementId.DE_050, Iso4217_CurrencyCode.CAD);
                }
            }
        }

        // DE 52: Personal Identification Number (PIN)
        if(paymentMethod instanceof TransactionReference) {
            IPaymentMethod originalPaymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            if(transactionType.equals(TransactionType.Refund)) {
                String pinBlock = null;
                if(originalPaymentMethod instanceof EBT){
                 pinBlock = ((EBT)originalPaymentMethod).getPinBlock();
                } else if(originalPaymentMethod instanceof Debit){
                 pinBlock = ((Debit)originalPaymentMethod).getPinBlock();
                }
                if(!StringUtils.isNullOrEmpty(pinBlock)) {
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
        if(builder.getCashBackAmount() != null && (isReversal(transactionType) || transactionType.equals(TransactionType.PreAuthCompletion))) {
            if(transactionAmount == null) {
                transactionAmount = BigDecimal.ZERO;
            }

            DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
            if(paymentMethod.getPaymentMethodType().equals(PaymentMethodType.EBT)) {
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.CashBenefitAccount, currencyCode, builder.getCashBackAmount());
                amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.CashBenefitAccount, currencyCode, transactionAmount.subtract(builder.getCashBackAmount()));
            }
            else if(paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)) {
                amountsAdditional.put(DE54_AmountTypeCode.AmountCash, DE3_AccountType.PinDebitAccount, currencyCode, builder.getCashBackAmount());
                amountsAdditional.put(DE54_AmountTypeCode.AmountGoodsAndServices, DE3_AccountType.PinDebitAccount, currencyCode, transactionAmount.subtract(builder.getCashBackAmount()));
            }
            request.set(DataElementId.DE_054, amountsAdditional);
        }
        else if (paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference) paymentMethod;
            if (reference.getOriginalPaymentMethod() instanceof Credit) {
                Credit card = (Credit) reference.getOriginalPaymentMethod();
                DE54_AmountsAdditional amountsAdditional = new DE54_AmountsAdditional();
                if (builder.getProductData() != null) {
                    DE63_ProductData productData = builder.getProductData().toDataElement();

                    if (card.getCardType().equals("VisaFleet") && (isVisaFleet2)) {
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelWithTax());
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getNonFuelAmount());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                            amountsAdditional.put(DE54_AmountTypeCode.NETNONFUELPRICE, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                        }
                        if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.Fuel) || acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.FuelAndNonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelAmount());
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSFUELPRICE, DE3_AccountType.Unspecified, currencyCode, productData.getFuelWithTax());
                        } else if (acceptorConfig.getSupportVisaFleet2dot0().equals(PurchaseType.NonFuel)) {
                            amountsAdditional.put(DE54_AmountTypeCode.NETFUELPRICE, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                            amountsAdditional.put(DE54_AmountTypeCode.GROSSFUELPRICE, DE3_AccountType.Unspecified, currencyCode, new BigDecimal(0));
                        }
                        if(productData.getDiscount()!=null){
                            amountsAdditional.put(DE54_AmountTypeCode.AmountDiscount, DE3_AccountType.Unspecified, currencyCode,productData.getDiscount());
                        }
                        if(productData.getSalesTax()!=null){
                            amountsAdditional.put(DE54_AmountTypeCode.AmountTax, DE3_AccountType.Unspecified, currencyCode,productData.getSalesTax());
                        }
                        request.set(DataElementId.DE_054, amountsAdditional);
                    }
                }
            }
        }
        // DE 55: Integrated Circuit Card (ICC) Data - LLLVAR b..512
        if(!StringUtils.isNullOrEmpty(builder.getTagData())) {
            if(!StringUtils.isNullOrEmpty(tagData.getCardSequenceNumber())) {
                String cardSequenceNumber = StringUtils.padLeft(tagData.getCardSequenceNumber(), 3, '0');
                request.set(DataElementId.DE_023, cardSequenceNumber);
            }
            if(!(transactionType.equals(TransactionType.PreAuthCompletion)||transactionType.equals(TransactionType.Capture))||
                    (mapCardType(paymentMethod,builder.getTransactionType()).equals(DE48_CardType.WEX)
                            && (transactionType.equals(TransactionType.PreAuthCompletion)||transactionType.equals(TransactionType.Capture))))
            request.set(DataElementId.DE_055, tagData.getSendBuffer());
        }

        /* DE 56: Original Data Elements - LLVAR n..35
            56.1 Original message type identifier n4
            56.2 Original system trace audit number n6
            56.3 Original date and time, local transaction n12
            56.4 Original acquiring institution identification code LLVAR n..11
         */
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference)paymentMethod;

            // check that we have enough
            if(!StringUtils.isNullOrEmpty(reference.getMessageTypeIndicator())
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
        if(isNeitherBatchCloseNorTimeRequest) {
            DE62_CardIssuerData cardIssuerData = mapCardIssuerData(builder);
            request.set(DataElementId.DE_062, cardIssuerData);
        }
        else if((builder.getBatchCloseType() != null && builder.getBatchCloseType().equals(BatchCloseType.Forced)) || builder.isForceToHost()) {
            DE62_CardIssuerData cardIssuerData = mapCardIssuerData(builder);
            request.set(DataElementId.DE_062, cardIssuerData);
        }

        // DE 63: Product Data - LLLVAR ans…999
        if(builder.getProductData() != null) {
            DE63_ProductData productData = builder.getProductData().toDataElement();
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;
                if (reference.getOriginalPaymentMethod() instanceof Credit) {
                    Credit card = (Credit) reference.getOriginalPaymentMethod();
                    if (card.getCardType() != null) {
                        productData.setCardType(card.getCardType());
                    }
                }
            }
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
                DE123_ReconciliationTotals totals = new DE123_ReconciliationTotals();

                Integer transactionCount = builder.getTransactionCount();
                BigDecimal totalDebits = builder.getTotalDebits();
                BigDecimal totalCredits = builder.getTotalCredits();

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
                }

                // Debits & Credits
                totals.setTotalDebits(transactionCount, totalDebits);
                totals.setTotalCredits(totalCredits);

                request.set(DataElementId.DE_123, totals);
            }

            // DE 124: Sundry Data - LLLVAR ans..999
            // DE 125: Extended Response Data 1 - LLLVAR ans..999
            // DE 126: Extended Response Data 2 - LLLVAR ans..999

            // DE 127: Forwarding Data - LLLVAR ans..999
            DE127_ForwardingData forwardingData = new DE127_ForwardingData();
            if (paymentMethod instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference) paymentMethod;
                String card =null;
                if(reference.getOriginalPaymentMethod() instanceof GiftCard) {
                    card = ((GiftCard) reference.getOriginalPaymentMethod()).getCardType();
                }
            if(reference.getOriginalPaymentMethod() != null && reference.getOriginalPaymentMethod() instanceof IEncryptable) {
                EncryptionData encryptionData = ((IEncryptable) reference.getOriginalPaymentMethod()).getEncryptionData();
                String encryptedPan = null;
                String encryptedKTB = null;
                if (encryptionData != null && encryptionData.getEncryptedKTB() != null) {
                    encryptedKTB = encryptionData.getEncryptedKTB();
                }
                boolean nonOriginalTransactions = (!("ValueLink").equals(card) && !("StoredValue").equals(card)) && (transactionType.equals(TransactionType.Capture) || transactionType.equals(TransactionType.PreAuthCompletion) || transactionType.equals(TransactionType.Reversal) || transactionType.equals(TransactionType.Void)
                        || transactionType.equals(TransactionType.Refund));

                if (nonOriginalTransactions) {
                    if ((reference.getOriginalPaymentMethod() instanceof Credit || ((reference.getOriginalPaymentMethod() instanceof Debit
                            || reference.getOriginalPaymentMethod() instanceof EBTTrackData) || reference.getOriginalPaymentMethod() instanceof GiftCard
                            && !(transactionType.equals(TransactionType.Refund))) ||
                            reference.getOriginalPaymentMethod() instanceof EBTCardData)) {
                        encryptedPan = ((IEncryptable) reference.getOriginalPaymentMethod()).getEncryptedPan();
                    }
                }
                if (encryptionData != null) {

                    EncryptionType encryptionType=acceptorConfig.getSupportedEncryptionType();
                    if(encryptedKTB!=null && ((reference.getOriginalPaymentMethod() instanceof Debit || reference.getOriginalPaymentMethod() instanceof EBTTrackData
                            || reference.getOriginalPaymentMethod() instanceof GiftCard) && transactionType.equals(TransactionType.Refund))){
                        encryptionData.setKtb(encryptedKTB);
                    }
                    else if(encryptedPan != null && encryptionType.equals(EncryptionType.TDES)){
                        encryptionData.setKtb(encryptedPan);
                    }
                    if(encryptionType.equals(EncryptionType.TDES)){
                        forwardingData.setServiceType(acceptorConfig.getServiceType());
                        forwardingData.setOperationType(acceptorConfig.getOperationType());
                    }
                    EncryptedFieldMatrix encryptedField=getEncryptionField(((TransactionReference) paymentMethod).getOriginalPaymentMethod(),encryptionType, transactionType);
                    forwardingData.setEncryptedField(encryptedField);

                        // check for encrypted cid -- THIS MAY NOT BE VALID FOR FOLLOW ON TRANSACTIONS WHERE THE CVN SHOULD NOT BE STORED
                    if (reference.getOriginalPaymentMethod() instanceof ICardData) {
                        String encryptedCvn = ((ICardData) reference.getOriginalPaymentMethod()).getCvn();
                        if (!StringUtils.isNullOrEmpty(encryptedCvn)) {
                            forwardingData.addEncryptionData(encryptionType, encryptionData,encryptedCvn);
                        } else {
                            forwardingData.addEncryptionData(encryptionType, encryptionData);
                        }
                    } else {
                        forwardingData.addEncryptionData(encryptionType, encryptionData);
                    }
                        request.set(DataElementId.DE_127, forwardingData);
                    }
                }
                if (reference.getOriginalPaymentMethod() instanceof ICardData) {
                    ICardData cardData = (ICardData) reference.getOriginalPaymentMethod();
                    if (cardData != null) {
                        String tokenizationData = cardData.getTokenizationData();
                        if (tokenizationData != null) {
                            setTokenizationData(forwardingData, cardData, null, null, tokenizationData);
                            request.set(DataElementId.DE_127, forwardingData);
                        }
                    }
                }
                if (reference.getOriginalPaymentMethod() instanceof ITrackData) {
                    ITrackData trackData = (ITrackData) reference.getOriginalPaymentMethod();
                    if (trackData != null) {
                        String tokenizationData = trackData.getTokenizationData();
                        if (tokenizationData != null) {
                            setTokenizationData(forwardingData, null, trackData, null, tokenizationData);
                            request.set(DataElementId.DE_127, forwardingData);
                        }
                    }
                }
            }

        return sendRequest(request, builder, orgCorr1, orgCorr2);
    }
    public Transaction resubmitTransaction(ResubmitBuilder builder) throws ApiException {
        if(StringUtils.isNullOrEmpty(builder.getTransactionToken())){
            throw new BuilderException("The transaction token cannot be null for resubmitted transactions.");
        }
        String currency = builder.getCurrency();

        // get the original request/implied capture
        NetworkMessage request = this.decodeRequest(builder.getTransactionToken());
        switch(builder.getTransactionType()) {
            case BatchClose: {
                request.setMessageTypeIndicator("1521");

                if(builder.isForceToHost()) {
                    request.set(DataElementId.DE_025, DE25_MessageReasonCode.Forced_AuthCapture);
                }
                if(!StringUtils.isNullOrEmpty(currency)) {
                     if(currency.equalsIgnoreCase("CAD")) {
                        request.set(DataElementId.DE_050, Iso4217_CurrencyCode.CAD);
                    }
                }
                //DE 28
                request.set(DataElementId.DE_028,DateTime.now().toString("yyMMdd"));
            } break;
            case DataCollect:
            case Refund:
            case Sale: {
                request.setMessageTypeIndicator("1221");

                    if(builder.isForceToHost() && builder.getTransactionType().equals(TransactionType.DataCollect)){
                    request.set(DataElementId.DE_025,"1381");
                }
                // STAN
                if(builder.getSystemTraceAuditNumber() != 0) {
                    request.set(DataElementId.DE_011, StringUtils.padLeft(builder.getSystemTraceAuditNumber(), 6, '0'));
                }

                // Transaction Time
                if(!StringUtils.isNullOrEmpty(builder.getTimestamp())) {
                    request.set(DataElementId.DE_012, builder.getTimestamp());
                }

                // Function Code
                if(builder.getTransactionType().equals(TransactionType.Sale) || builder.getTransactionType().equals(TransactionType.Refund)) {
                    request.set(DataElementId.DE_024, "200");
                }

                // Message Reason Code
                if(builder.isForceToHost()) {
                    request.set(DataElementId.DE_025, DE25_MessageReasonCode.Forced_AuthCapture);
                }

                // Auth Code
                if(!StringUtils.isNullOrEmpty(builder.getAuthCode())){
                    request.set(DataElementId.DE_038, builder.getAuthCode());
                }

                // Message Control
                DE48_MessageControl messageControl = request.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);
                if(builder.hasMessageControlData()) {
                    // Batch Number
                    if(builder.getBatchNumber() != 0) {
                        messageControl.setBatchNumber(builder.getBatchNumber());
                    }

                    // Sequence Number
                    if(builder.getSequenceNumber() != 0) {
                        messageControl.setSequenceNumber(builder.getSequenceNumber());
                    }
                    request.set(DataElementId.DE_048, messageControl);
                }

                // DE 56
                if(!(builder.getPaymentMethod() instanceof Debit || builder.getPaymentMethod() instanceof EBT) && builder.getTransactionType().equals(TransactionType.Refund)) {
                    request.remove(DataElementId.DE_056);
                }

                // NTS Data
                DE62_CardIssuerData issuerData = request.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
                if(issuerData == null) {
                    issuerData = new DE62_CardIssuerData();
                }

                if(builder.getNtsData() != null) {
                    issuerData.add(CardIssuerEntryTag.NTS_System, builder.getNtsData().toString());
                    request.set(DataElementId.DE_062, issuerData);
                }
                if(!StringUtils.isNullOrEmpty(currency)) {
                    if(currency.equalsIgnoreCase("CAD")) {
                        request.set(DataElementId.DE_050, Iso4217_CurrencyCode.CAD);
                    }
                }
//                DE 127
                if (builder.getPaymentMethod() instanceof Debit || builder.getPaymentMethod() instanceof EBT
                || builder.getPaymentMethod() instanceof GiftCard) {
                    DE127_ForwardingData forwardingData = new DE127_ForwardingData();

                    IPaymentMethod paymentMethod = builder.getPaymentMethod();
                    if (paymentMethod instanceof IEncryptable) {
                        String encryptedPan = null;
                        EncryptionData encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
                        encryptedPan = ((IEncryptable) paymentMethod).getEncryptedPan();
                        if (encryptionData != null) {
                            EncryptionType encryptionType = acceptorConfig.getSupportedEncryptionType();
                            if (encryptedPan != null && encryptionType.equals(EncryptionType.TDES)) {
                                encryptionData.setKtb(encryptedPan);
                            }
                            if (encryptionType.equals(EncryptionType.TDES)) {
                                forwardingData.setServiceType(acceptorConfig.getServiceType());
                                forwardingData.setOperationType(acceptorConfig.getOperationType());
                            }
                            forwardingData.setEncryptedField(EncryptedFieldMatrix.Pan);
                            forwardingData.addEncryptionData(encryptionType, encryptionData);
                            request.set(DataElementId.DE_127, forwardingData);
                        }
                    }
                }
            }
            break;
            default:
                throw new UnsupportedTransactionException("Only data collect or batch close transactions can be resubmitted");
        }

        return sendRequest(request, builder, new byte[2], new byte[8]);
    }
    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        throw new UnsupportedTransactionException("VAPS does not support reporting.");
    }
    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException("VAPS does not support hosted payments.");
    }

    private IDeviceMessage buildMessage(byte[] message, byte[] orgCorr1, byte[] orgCorr2) {
        return buildMessage(message, orgCorr1, orgCorr2, false);
    }
    private IDeviceMessage buildMessage(byte[] message, byte[] orgCorr1, byte[] orgCorr2, Boolean isKeepAlive) {
        int messageLength = message.length + 32;

        // build the header
        NetworkMessageBuilder buffer = new NetworkMessageBuilder()
                .append(messageLength, 2) // EH.1: Total Tran Length
                .append(isKeepAlive ? NetworkTransactionType.KeepAlive : NetworkTransactionType.Transaction) // EH.2: ID (Transaction or Keep Alive)
                .append(0 ,2) // EH.3: Reserved
                .append(messageType) // EH.4: Type Message
                .append(characterSet) // EH.5: Character Set
                .append(0) // EH.6: Response Code
                .append(0) // EH.7: Response Code Origin
                .append(processingFlag); // EH.8: Processing Flag

        // EH.9: Protocol Type
        if(protocolType.equals(ProtocolType.Async)) {
            if (messageType.equals(MessageType.GlobalPayments_POS_8583) || messageType.equals(MessageType.GlobalPayments_NTS)) {
                buffer.append(0x07);
            }
            else {
                buffer.append(protocolType);
            }
        }
        else {
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

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        IDeviceMessage keepAlive = buildMessage(new byte[0], new byte[2], new byte[8], true);
        byte[] responseBuffer = send(keepAlive);
        MessageReader mr = new MessageReader(responseBuffer);

        // parse the header
        NetworkMessageHeader header = NetworkMessageHeader.parse(mr.readBytes(30));
        if(!header.getResponseCode().equals(NetworkResponseCode.Success)) {
            GatewayException exc = new GatewayException(
                    String.format("Unexpected response from gateway: %s %s", header.getResponseCode().toString(), header.getResponseCodeOrigin().toString()),
                    header.getResponseCode().toString(),
                    header.getResponseCodeOrigin().toString());
            throw exc;
        } else {
            return header;
        }
    }

    private <T extends TransactionBuilder<Transaction>> Transaction sendRequest(NetworkMessage request, T builder, byte[] orgCorr1, byte[] orgCorr2) throws ApiException {
        byte[] sendBuffer = request.buildMessage();
        if(isEnableLogging()) {
            System.out.println("Request Breakdown:\r\n" + request.toString());
        }
        IDeviceMessage message = buildMessage(sendBuffer, orgCorr1, orgCorr2, false);
        TransactionType transactionType = null;

        try {
            if(builder != null) {
                transactionType = builder.getTransactionType();
                this.setSimulatedHostErrors(builder.getSimulatedHostErrors());
            }
            byte[] responseBuffer = send(message);

            String functionCode = request.getString(DataElementId.DE_024);
            String messageReasonCode = request.getString(DataElementId.DE_025);
            String processingCode = request.getString(DataElementId.DE_003);
            String stan = request.getString(DataElementId.DE_011);

            PriorMessageInformation priorMessageInformation = new PriorMessageInformation();
            IPaymentMethod paymentMethod = builder != null ? builder.getPaymentMethod() : null;
            if(paymentMethod != null) {
                DE48_CardType cardType = mapCardType(paymentMethod, transactionType);
                if(cardType != null) {
                    priorMessageInformation.setCardType(cardType.getValue());
                }
            }

//            priorMessageInformation.setResponseTime(); // TODO: Need to get this from the send message
            priorMessageInformation.setFunctionCode(functionCode);
            priorMessageInformation.setMessageReasonCode(messageReasonCode);
            priorMessageInformation.setMessageTransactionIndicator(request.getMessageTypeIndicator());
            priorMessageInformation.setProcessingCode(processingCode);
            priorMessageInformation.setSystemTraceAuditNumber(stan);
            priorMessageInformation.setProcessingHost(currentHost);

            Transaction response = mapResponse(responseBuffer, request, builder);
            response.setMessageInformation(priorMessageInformation);
            if(batchProvider != null) {
                batchProvider.setPriorMessageData(priorMessageInformation);
            }

            // check to see if we need to send a completion
            if(paymentMethod != null && transactionType != null) {
                if(stanProvider == null && builder.getFollowOnStan() == null) {
                    return response;
                }

                // get the original payment method
                IPaymentMethod originalPaymentMethod = paymentMethod;
                if(originalPaymentMethod instanceof TransactionReference) {
                    originalPaymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
                }

                // check for Debit or EBT
                if(originalPaymentMethod instanceof Debit || originalPaymentMethod instanceof EBT) {
                    ArrayList<String> successCodes = new ArrayList<String>();
                    successCodes.add("000");
                    successCodes.add("002");

                    if((transactionType.equals(TransactionType.Sale) || transactionType.equals(TransactionType.Refund)) && !StringUtils.isNullOrEmpty(response.getTransactionToken())) {
                        if(!successCodes.contains(response.getResponseCode())) {
                            return response;
                        }

                        Integer followOnStan = builder.getFollowOnStan();
                        if (followOnStan == null) {
                            return response;
                        } else {
                        NetworkMessage impliedCapture = decodeRequest(response.getTransactionToken());
                        impliedCapture.set(DataElementId.DE_011, StringUtils.padLeft(followOnStan, 6, '0'));
                        impliedCapture.set(DataElementId.DE_012, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")));
                        impliedCapture.set(DataElementId.DE_025, DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement);


                            if(originalPaymentMethod != null && originalPaymentMethod instanceof IEncryptable) {

                                DE127_ForwardingData forwardingData = new DE127_ForwardingData();
                                EncryptionType encryptionType=acceptorConfig.getSupportedEncryptionType();

                                if(encryptionType.equals(EncryptionType.TDES)) {
                                    EncryptionData encryptionData = ((IEncryptable) originalPaymentMethod).getEncryptionData();
                                    if (encryptionData != null) {
                                        String track = ((IEncryptable) originalPaymentMethod).getEncryptedPan();
                                        if (track != null) {
                                            encryptionData.setKtb(track);
                                        }
                                        if (encryptionType.equals(EncryptionType.TDES)) {
                                            forwardingData.setServiceType(acceptorConfig.getServiceType());
                                            forwardingData.setOperationType(acceptorConfig.getOperationType());
                                        }
                                        EncryptedFieldMatrix encryptedField = getEncryptionField(originalPaymentMethod, encryptionType, TransactionType.Capture);
                                        forwardingData.setEncryptedField(encryptedField);
                                        forwardingData.addEncryptionData(encryptionType, encryptionData);
                                        impliedCapture.set(DataElementId.DE_127, forwardingData);
                                    }
                                }
                            }

                        Transaction dataCollectResponse = sendRequest(impliedCapture, null, orgCorr1, orgCorr2);
                        response.setPreAuthCompletion(dataCollectResponse);
                    }
                  }
                    else if(transactionType.equals(TransactionType.Capture) && messageReasonCode != null) {
                        Integer followOnStan = builder.getFollowOnStan();
                        if (followOnStan == null) {
                            return response;
                        } else if (messageReasonCode.equals(DE25_MessageReasonCode.AuthCapture.getValue())) {
                            request.set(DataElementId.DE_011, StringUtils.padLeft(followOnStan, 6, '0'));
                            request.set(DataElementId.DE_012, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhhmmss")));
                            request.set(DataElementId.DE_025, DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement);

                            Transaction dataCollectResponse = sendRequest(request, builder, orgCorr1, orgCorr2);
                            response.setPreAuthCompletion(dataCollectResponse);
                        }
                    }
                }
                else if(originalPaymentMethod instanceof GiftCard) {
                    /*
                    Removed As this was deemed not needed by the issuer
                    Leaving the code for now... in case they change their minds
                    */
//                    if(((GiftCard) originalPaymentMethod).getCardType().equals("ValueLink")) {
//                        if(transactionType.equals(TransactionType.Capture) && messageReasonCode != null) {
//                            // check for the right MRC
//                            if(messageReasonCode.equals(DE25_MessageReasonCode.AuthCapture.getValue())) {
//                                // make sure it's a capture and not a preAuthCompletion
//                                request.set(DataElementId.DE_025, DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement);
//                                Transaction dataCollectResponse = sendRequest(request, builder, orgCorr1, orgCorr2);
//                                dataCollectResponse.setPreAuthCompletion(response);
//                                return dataCollectResponse;
//                            }
//                        }
//                    }
                }
            }

            return response;
        }
        catch(GatewayException exc) {
            String transactionToken = checkResponse(null, request, null, builder);
            exc.setTransactionToken(transactionToken);
            exc.setMessageTypeIndicator(request.getMessageTypeIndicator());
            exc.setProcessingCode(request.getString(DataElementId.DE_003));
            exc.setTransmissionTime(request.getString(DataElementId.DE_007));
            exc.setPosDataCode(request.getString(DataElementId.DE_022));
            exc.setHost(currentHost.getValue());
            throw exc;
        }
    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(byte[] buffer, NetworkMessage request, T builder) throws GatewayException {
        Transaction result = new Transaction();
        MessageReader mr = new MessageReader(buffer);

        // parse the header
        NetworkMessageHeader header = NetworkMessageHeader.parse(mr.readBytes(30));
        if(!header.getResponseCode().equals(NetworkResponseCode.Success)) {
            GatewayException exc = new GatewayException(
                    String.format("Unexpected response from gateway: %s %s", header.getResponseCode().toString(), header.getResponseCodeOrigin().toString()),
                    header.getResponseCode().toString(),
                    header.getResponseCodeOrigin().toString());
            throw exc;
        }
        else {
            result.setResponseCode("000");
            result.setResponseMessage(header.getResponseCodeOrigin().toString());

            // parse the message
            if(!header.getMessageType().equals(MessageType.NoMessage)) {
                String messageTransactionIndicator = mr.readString(4);
                NetworkMessage message = NetworkMessage.parse(mr.readBytes(buffer.length), Iso8583MessageType.CompleteMessage);
                message.setMessageTypeIndicator(messageTransactionIndicator);

                // log out the breakdown
                if(isEnableLogging()) {
                    System.out.println("\r\nResponse Breakdown:\r\n" + message.toString());
                }

                DE3_ProcessingCode processingCode = message.getDataElement(DataElementId.DE_003, DE3_ProcessingCode.class);
                DE44_AdditionalResponseData additionalResponseData = message.getDataElement(DataElementId.DE_044, DE44_AdditionalResponseData.class);
                DE48_MessageControl messageControl = message.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);
                DE54_AmountsAdditional additionalAmounts = message.getDataElement(DataElementId.DE_054, DE54_AmountsAdditional.class);
                DE62_CardIssuerData cardIssuerData = message.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);

                result.setAuthorizedAmount(message.getAmount(DataElementId.DE_004));
                result.setHostResponseDate(message.getDate(DataElementId.DE_012, new SimpleDateFormat("yyMMddhhmmss")));
                result.setReferenceNumber(message.getString(DataElementId.DE_037));
                String authCode = message.getString(DataElementId.DE_038);

                String responseCode = message.getString(DataElementId.DE_039);
                String responseText = DE39_ActionCode.getDescription(responseCode);
                if(!StringUtils.isNullOrEmpty(responseCode)) {
                    if(additionalResponseData != null) {
                        result.setAdditionalResponseCode(additionalResponseData.getActionReasonCode().toString());
                        responseText += String.format(" - %s: %s", additionalResponseData.getActionReasonCode().toString(), additionalResponseData.getTextMessage());
                    }

                    result.setResponseCode(responseCode);
                    result.setResponseMessage(responseText);

                    // Issuer Data
                    if(cardIssuerData != null) {
                        for (DE62_2_CardIssuerEntry entry : cardIssuerData.getCardIssuerEntries().values()) {
                            result.setIssuerData(entry.getIssuerTag(), entry.getIssuerEntry());
                        }
                    }

                    if(builder != null) {
                        String transactionToken = checkResponse(responseCode, request, message, builder);
                        result.setTransactionToken(transactionToken);
                    }
                }

                // EMV response
                byte[] emvResponse = message.getByteArray(DataElementId.DE_055);
                if(emvResponse != null){
                    EmvData emvData = EmvUtils.parseTagData(StringUtils.hexFromBytes(emvResponse), isEnableLogging());
                    result.setEmvIssuerResponse(emvData.getAcceptedTagData());
                }

                // Transaction Reference
                if(builder != null) {
                    TransactionReference reference = new TransactionReference();
                    reference.setAuthCode(authCode);

                    // original data elements
                    reference.setMessageTypeIndicator(request.getMessageTypeIndicator());
                    reference.setOriginalApprovedAmount(message.getAmount(DataElementId.DE_004));
                    reference.setOriginalProcessingCode(request.getString(DataElementId.DE_003));
                    reference.setSystemTraceAuditNumber(request.getString(DataElementId.DE_011));
                    reference.setOriginalTransactionTime(request.getString(DataElementId.DE_012));
                    reference.setPosDataCode(request.getString(DataElementId.DE_022));
                    reference.setAcquiringInstitutionId(request.getString(DataElementId.DE_032));
                    reference.setFeeAmount(request.getString(DataElementId.DE_046));

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

                    if (messageControl != null ) {
                        reference.setSequenceNumber(messageControl.getSequenceNumber());
                    }

                    // card issuer data
                    if (cardIssuerData != null) {
                        reference.setNtsData(cardIssuerData.get("NTS"));
                        result.setReferenceNumber(cardIssuerData.get("IRR"));
                        reference.setMastercardBanknetRefNo(cardIssuerData.get(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber));
                        reference.setMastercardBanknetSettlementDate(cardIssuerData.get(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate));
                    }

                    // authorization builder
                    if (builder instanceof AuthorizationBuilder) {
                        AuthorizationBuilder authBuilder = (AuthorizationBuilder) builder;
                        reference.setOriginalAmount(authBuilder.getAmount());
                        reference.setOriginalEmvChipCondition(authBuilder.getEmvChipCondition());
                    }
                    // management builder
                    else if (builder instanceof ManagementBuilder) {
                        ManagementBuilder managementBuilder = (ManagementBuilder) builder;
                        reference.setOriginalAmount(managementBuilder.getAmount());
                    }
                    else if(builder instanceof ResubmitBuilder) {
                        reference.setOriginalAmount(request.getAmount(DataElementId.DE_004));
                    }

                    // transaction reference
                    IPaymentMethod paymentMethod = builder.getPaymentMethod();
                    if (paymentMethod != null) {
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
                    }
                    result.setTransactionReference(reference);

                    // balance amounts
                    if(additionalAmounts != null) {
                        final DE3_AccountType fromAccountType = processingCode.getFromAccount();
                        final DE3_AccountType toAccountType = processingCode.getToAccount();

                        // build the list of account types to check
                        ArrayList<DE3_AccountType> accountTypes = new ArrayList<DE3_AccountType>() {{
                            add(fromAccountType);
                            if(!toAccountType.equals(fromAccountType)) {
                                add(toAccountType);
                            }
                        }};

                        // account type 60 is generic and the response can contain 60, 65 or 66 we need to check all
                        if(fromAccountType.equals(DE3_AccountType.CashCardAccount) || toAccountType.equals(DE3_AccountType.CashCardAccount)) {
                            accountTypes.add(DE3_AccountType.CashCard_CashAccount);
                            accountTypes.add(DE3_AccountType.CashCard_CreditAccount);
                        }

                        result.setBalanceAmount(additionalAmounts.getAmount(accountTypes, DE54_AmountTypeCode.AccountLedgerBalance));
                        result.setAvailableBalance(additionalAmounts.getAmount(accountTypes, DE54_AmountTypeCode.AccountAvailableBalance));
                    }

                    // batch summary
                    if(builder.getTransactionType().equals(TransactionType.BatchClose)) {
                        summary.setResponseCode(responseCode);
                        summary.setResentTransactions(resentTransactions);
                        summary.setResentBatchClose(resentBatch);
                        summary.setTransactionToken(result.getTransactionToken());

                        if(messageControl != null) {
                            summary.setBatchId(messageControl.getBatchNumber());
                            summary.setSequenceNumber(messageControl.getSequenceNumber() + "");
                        }

                        DE123_ReconciliationTotals reconciliationTotals = message.getDataElement(DataElementId.DE_123, DE123_ReconciliationTotals.class);
                        if(reconciliationTotals != null) {
                            int transactionCount = 0;
                            BigDecimal totalAmount = new BigDecimal(0);
                            for(DE123_ReconciliationTotal total: reconciliationTotals.getTotals()) {
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
                        result.setBatchSummary(summary);
                    }
                }
            }
        }

        return result;
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
        switch(builder.getTransactionType()) {
            case Auth:
            case Balance:
            case Decline:
            case Verify: {
                mtiValue += "1";
            } break;
            case Activate:
            case AddValue:
            case BenefitWithdrawal:
            case Capture:
            case CashOut:
            case PreAuthCompletion:
            case Refund:
            case Sale: {
                mtiValue += "2";
            } break;
            case LoadReversal:
            case Reversal:
            case Void:{
                mtiValue += "4";
            } break;
            case BatchClose: {
                mtiValue += "5";
            } break;
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
            case LoadReversal:
            case Reversal:
            case PosSiteConfiguration:
            case Decline:
            case Void: {
                mtiValue += "2";
            } break;
            case Refund: {
                IPaymentMethod paymentMethod = builder.getPaymentMethod();
                if(paymentMethod instanceof TransactionReference) {
                    paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
                }

                if(paymentMethod instanceof Debit || paymentMethod instanceof EBT) {
                    mtiValue += "0";
                }
                else if(paymentMethod instanceof GiftCard) {
                    String cardType = ((GiftCard) paymentMethod).getCardType();
                    if(("ValueLink").equals(cardType)|| ("StoredValue").equals(cardType)|| ("GlobalPaymentsGift").equals(cardType)) {
                        mtiValue += "0";
                    }
                    else mtiValue += "2";
                }
                else mtiValue += "2";
            } break;
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
            2 GlobalPayments system
            3 GlobalPayments system repeat
            4 POS application or GlobalPayments system
            5 Reserved for GlobalPayments use
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
        IPaymentMethod paymentMethod = builder.getPaymentMethod();

        if(shouldUseOriginalProcessingCode(paymentMethod, type)) {
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
                if(paymentMethod instanceof Credit) {
                    Credit card = (Credit)paymentMethod;
                    if(card.getCardType().equals("VisaReadyLink")) {
                        processingCode.setTransactionType(DE3_TransactionType.LoadValue);
                    }
                    else {
                        processingCode.setTransactionType(DE3_TransactionType.Deposit);
                    }
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.Deposit);
                }
            } break;
            case Auth: {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                if((authBuilder.getAmount().equals(BigDecimal.ZERO) && authBuilder.getBillingAddress() != null && !authBuilder.isAmountEstimated())
                   || isAccountVerified(authBuilder)){
                    processingCode.setTransactionType(DE3_TransactionType.AddressOrAccountVerification);
                    processingCode.setToAccount(DE3_AccountType.Unspecified);
                    processingCode.setFromAccount(DE3_AccountType.Unspecified);
                    return processingCode;
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
                }
            } break;
            case Balance: {
                if(builder.getPaymentMethod() instanceof EBT) {
                    processingCode.setTransactionType(DE3_TransactionType.BalanceInquiry);
                }
                else if(builder.getPaymentMethod() instanceof GiftCard) {
                    GiftCard gift = (GiftCard)builder.getPaymentMethod();
                    if(gift.getCardType().equals("ValueLink")) {
                        processingCode.setTransactionType(DE3_TransactionType.BalanceInquiry);
                    }
                    else processingCode.setTransactionType(DE3_TransactionType.AvailableFundsInquiry);
                }
                else processingCode.setTransactionType(DE3_TransactionType.AvailableFundsInquiry);
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
            case Sale: {
                AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                if(authBuilder.getCashBackAmount() != null
                        || (authBuilder.getCashAtCheckoutAmount() != null && paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals(DISCOVER)) && acceptorConfig.getSupportsCashAtCheckout() != null) {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndServiceWithCashDisbursement);
                }
                else {
                    processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
                }
            } break;
            case Verify: {
                processingCode.setTransactionType(DE3_TransactionType.BalanceInquiry);
            } break;
            case Decline:
            default: {
                processingCode.setTransactionType(DE3_TransactionType.GoodsAndService);
            }
        }

        // check for an original payment method
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
            if(card.isFleet()) {
                accountType = DE3_AccountType.FleetAccount;
            }
            else if(card.getCardType().equals("VisaReadyLink")) {
                accountType = DE3_AccountType.PinDebitAccount;
            } else if ((builder instanceof AuthorizationBuilder) && (card.getCardType().equals(DISCOVER)) &&
                    (type.equals(TransactionType.Sale))) {
                AuthorizationBuilder authorizationBuilder = (AuthorizationBuilder) builder;
                if (authorizationBuilder.getCashAtCheckoutAmount() != null) {
                    accountType = DE3_AccountType.Unspecified;
                } else accountType = DE3_AccountType.CreditAccount;
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

        switch (type) {
            case Activate:
            case AddValue:
            case Refund: {
                processingCode.setToAccount(accountType);
                processingCode.setFromAccount(DE3_AccountType.Unspecified);
            } break;
            default: {
                processingCode.setFromAccount(accountType);
                processingCode.setToAccount(DE3_AccountType.Unspecified);
            }
        }

        return processingCode;
    }
    private <T extends TransactionBuilder<Transaction>> String mapFunctionCode(T builder) {
        TransactionType type = builder.getTransactionType();
        TransactionModifier modifier = builder.getTransactionModifier();

        switch(type) {
            case Activate:
            case AddValue:
            case BenefitWithdrawal:
            case CashOut:
            case Refund:
            case Sale: {
                return "200";
            }
            case PreAuthCompletion:
            case Capture: {
                ManagementBuilder managementBuilder = (ManagementBuilder)builder;

                TransactionReference reference = null;
                if(managementBuilder.getPaymentMethod() != null && managementBuilder.getPaymentMethod() instanceof TransactionReference) {
                    reference = (TransactionReference)managementBuilder.getPaymentMethod();
                }

                // check for ready link and pre-auth completion
                if(reference != null) {
                    IPaymentMethod originalPaymentMethod = reference.getOriginalPaymentMethod();
                    if (originalPaymentMethod instanceof Credit) {
                        String cardType = ((Credit) originalPaymentMethod).getCardType();
                        if (cardType.equals("VisaReadyLink") && type.equals(TransactionType.PreAuthCompletion)) {
                            return "200";
                        }
                    }
                }

                BigDecimal amount = managementBuilder.getAmount();
                if(amount == null && reference != null) {
                    amount = reference.getOriginalAmount();
                }

                if(amount != null) {
                    if(reference != null) {
                        BigDecimal compareAmount = reference.getOriginalAmount();
                        if(reference.isUseAuthorizedAmount() && reference.getOriginalApprovedAmount() != null) {
                            compareAmount = reference.getOriginalApprovedAmount();
                        }

                        if(amount.compareTo(compareAmount) == 0) {
                            return "201";
                        }
                        return "202";
                    }
                }
                return "201";
            }
            case Auth: {
                if(modifier.equals(TransactionModifier.Offline)) {
                    return "190";
                }

                if(builder instanceof AuthorizationBuilder) {
                    AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
                    if(authBuilder.isAmountEstimated()) {
                        return "101";
                    }
                    else if((authBuilder.getAmount().equals(BigDecimal.ZERO) && authBuilder.getBillingAddress() != null) || isAccountVerified(authBuilder)) {
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
                ManagementBuilder managementBuilder = (ManagementBuilder)builder;
                if(managementBuilder.getBatchCloseType() == null || managementBuilder.getBatchCloseType().equals(BatchCloseType.Forced)) {
                    return "572";
                }
                return "570";
            }
            case LoadReversal:
            case Reversal: {
                ManagementBuilder managementBuilder = (ManagementBuilder)builder;

                if(managementBuilder.getAmount() != null) {
                    if(managementBuilder.getPaymentMethod() != null && managementBuilder.getPaymentMethod() instanceof TransactionReference) {
                        TransactionReference reference = (TransactionReference)managementBuilder.getPaymentMethod();

                        if(reference.isPartialApproval() && !managementBuilder.isCustomerInitiated()) {
                            return "401";
                        }
                        return "400";
                    }
                }
                return "400";
            }
            case Void: {
                return "441";
            }
            case FileAction: {
                return "388";
            }
            case TimeRequest: {
                return "641";
            }
            case PosSiteConfiguration:{
                return "692";
            }
            case Decline:{
                return "190";
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
        if(transactionType.equals(TransactionType.BatchClose)) {
            if(builder.isForceToHost()) {
                reasonCode = DE25_MessageReasonCode.Forced_AuthCapture;
            }
        }
        else if(transactionType.equals(TransactionType.Capture)) {
            if(authorizerCode != null && authorizerCode.equals(AuthorizerCode.Voice_Authorized)) {
                reasonCode = DE25_MessageReasonCode.VoiceCapture;
            }else if (authorizerCode != null && authorizerCode.equals(AuthorizerCode.Terminal_Authorized)) {
                reasonCode = DE25_MessageReasonCode.StandInCapture;
            }
            else if(fallbackCode != null) {
                switch (fallbackCode) {
                    case CouldNotCommunicateWithHost:
                    case Received_IssuerTimeout:
                    case Received_IssuerUnavailable:
                    case Received_SystemMalfunction:
                        break;
                    default: {
                        if(builder.isForceToHost()) {
                            reasonCode = DE25_MessageReasonCode.Forced_AuthCapture;
                        }
                        else {
                            reasonCode = DE25_MessageReasonCode.AuthCapture;
                        }
                    }
                }
            }else if (builder.isForceToHost()) {
                reasonCode = DE25_MessageReasonCode.Forced_AuthCapture;
            }
            else {
                reasonCode = DE25_MessageReasonCode.AuthCapture;
            }
        }
        if(transactionType.equals(TransactionType.PreAuthCompletion)) {
            reasonCode = DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement;
        }
        else if(isReversal(transactionType) || transactionType.equals(TransactionType.Void)) {
            boolean partial = false;
            if(paymentMethod != null) {
                partial = paymentMethod.isPartialApproval();
            }

            if(builder.isForceToHost()) {
                if(isReversal(transactionType)) {
                    reasonCode = DE25_MessageReasonCode.Forced_AuthCapture;
                }
                else {
                    reasonCode = partial ? DE25_MessageReasonCode.ForceVoid_PartialApproval : DE25_MessageReasonCode.ForceVoid_ApprovedTransaction;
                }
            }
            else if(fallbackCode != null) {
                switch (fallbackCode) {
                    case Received_IssuerTimeout:
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
                        }
                        else {
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
        else if(transactionType.equals(TransactionType.Refund)) {
            if(builder.isForceToHost()) {
                reasonCode = DE25_MessageReasonCode.Forced_AuthCapture;
            }
            else if (originalPaymentMethod instanceof Debit || originalPaymentMethod instanceof EBT) {
                reasonCode = DE25_MessageReasonCode.PinDebit_EBT_Acknowledgement;
            }
        }

        return reasonCode;
    }
    private <T extends TransactionBuilder<Transaction>> DE48_MessageControl mapMessageControl(T builder) throws BatchFullException, BuilderException {
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
            48-33 POS CONFIGURATION LLVAR ans..99 C Values that indicate to the GlobalPayments system capabilities and configuration of the POS application.
            48-34 MESSAGE CONFIGURATION LLVAR ans..99 C Information regarding the POS originating message and the host generated response message.
            48-35 NAME 1 LLVAR ans..99 D
            48-36 NAME 2 LLVAR ans..99 D
            48-37 SECONDARY ACCOUNT NUMBER LLVAR ans..28 C Second Account Number for manually entered transactions requiring 2 account numbers.
            48-38 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
            48-39 PRIOR MESSAGE INFORMATION LLVAR ans..99 C Information regarding the status of the prior message sent by the POS.
            48-40, 48-49 ADDRESS 1 THROUGH ADDRESS 10 LLVAR ans..99 D One or more types of addresses.
            48-50, 48-64 RESERVED FOR GlobalPayments USE LLVAR ans..99 F
         */
        // DE48-2 - Hardware/Software Config
        DE48_2_HardwareSoftwareConfig hardwareSoftwareConfig = new DE48_2_HardwareSoftwareConfig();
        hardwareSoftwareConfig.setHardwareLevel(acceptorConfig.getHardwareLevel());
        hardwareSoftwareConfig.setSoftwareLevel(acceptorConfig.getSoftwareLevel());
        hardwareSoftwareConfig.setOperatingSystemLevel(acceptorConfig.getOperatingSystemLevel());
        messageControl.setHardwareSoftwareConfig(hardwareSoftwareConfig);

        // DE48-4 (Sequence Number & Batch Number)
        if(!builder.getTransactionType().equals(TransactionType.Auth) && !isTimeRequest) {
            int sequenceNumber = 0;

            if(!builder.getTransactionType().equals(TransactionType.BatchClose)) {
                sequenceNumber = builder.getSequenceNumber();
                if (builder.getPaymentMethod() != null) {
                    IPaymentMethod paymentMethod = builder.getPaymentMethod();
                    if (paymentMethod instanceof TransactionReference) {
                        TransactionReference reference = (TransactionReference) builder.getPaymentMethod();
                        if(reference.getSequenceNumber()!=null && reference.getOriginalPaymentMethod() instanceof Debit) {
                            sequenceNumber = reference.getSequenceNumber();
                        }
                    }
                }
                if (sequenceNumber == 0 && batchProvider != null) {
                    sequenceNumber = batchProvider.getSequenceNumber();
                }
            }
            messageControl.setSequenceNumber(sequenceNumber);

            int batchNumber = builder.getBatchNumber();
            if (builder.getPaymentMethod() != null) {
                IPaymentMethod paymentMethod = builder.getPaymentMethod();
                if (paymentMethod instanceof TransactionReference) {
                    TransactionReference reference = (TransactionReference) builder.getPaymentMethod();
                    if(reference.getBatchNumber()!=null)
                        batchNumber = reference.getBatchNumber();
                }
            }
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
            if(authBuilder.getBillingAddress() != null) {
                Address address = authBuilder.getBillingAddress();
                customerData.set(DE48_CustomerDataType.PostalCode, address.getPostalCode());
            }
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
            if (builder.getPaymentMethod() instanceof Credit && ((Credit) builder.getPaymentMethod()).getCardType().equals("WexFleet")) {
                FleetData wexFleetData = builder.getFleetData();
                if (wexFleetData != null) {
                    if (getWexFleetPromptCount(wexFleetData) > 6){
                        throw new UnsupportedOperationException("WEX has only six possible prompts per transaction.");
                    }
                    if (builder.getPaymentMethod() instanceof CreditTrackData) {
                        String trackData = ((CreditTrackData) builder.getPaymentMethod()).getTrackData();
                        String ISONumber = trackData.substring(3, 5);
                        if (ISONumber.equals(00) && fleetData.getDriverId() == null) {
                            throw new BuilderException("Driver ID must not be null.");
                        }
                    }
                    customerData.set(DE48_CustomerDataType.EnteredData_Numeric, restrictWexPromptToMaxLength(wexFleetData.getEnteredData(), 12, DE48_CustomerDataType.EnteredData_Numeric));
                    customerData.set(DE48_CustomerDataType.Department, restrictWexPromptToMaxLength(wexFleetData.getDepartment(), 12, DE48_CustomerDataType.Department));
                    customerData.set(DE48_CustomerDataType.DriverId_EmployeeNumber, validateWexDriverIDLength(wexFleetData.getDriverId(), 10));
                    customerData.set(DE48_CustomerDataType.HubometerNumber, restrictWexPromptToMaxLength(wexFleetData.getHubometerNumber(), 9, DE48_CustomerDataType.HubometerNumber));
                    customerData.set(DE48_CustomerDataType.JobNumber, restrictWexPromptToMaxLength(wexFleetData.getJobNumber(), 12, DE48_CustomerDataType.JobNumber));
                    customerData.set(DE48_CustomerDataType.MaintenanceNumber, restrictWexPromptToMaxLength(wexFleetData.getMaintenanceNumber(), 12, DE48_CustomerDataType.MaintenanceNumber));
                    customerData.set(DE48_CustomerDataType.Odometer_Reading, restrictWexPromptToMaxLength(wexFleetData.getOdometerReading(), 9, DE48_CustomerDataType.Odometer_Reading));
                    customerData.set(DE48_CustomerDataType.TrailerHours_ReferHours, restrictWexPromptToMaxLength(wexFleetData.getTrailerReferHours(), 6, DE48_CustomerDataType.TrailerHours_ReferHours));
                    customerData.set(DE48_CustomerDataType.TrailerNumber, restrictWexPromptToMaxLength(wexFleetData.getTrailerNumber(), 12, DE48_CustomerDataType.EnteredData_Numeric));
                    customerData.set(DE48_CustomerDataType.TripNumber, restrictWexPromptToMaxLength(wexFleetData.getTripNumber(), 12, DE48_CustomerDataType.TripNumber));
                    customerData.set(DE48_CustomerDataType.UnitNumber, restrictWexPromptToMaxLength(wexFleetData.getUnitNumber(), 12, DE48_CustomerDataType.UnitNumber));
                    customerData.set(DE48_CustomerDataType.UnencryptedIdNumber, restrictWexPromptToMaxLength(wexFleetData.getUserId(), 12, DE48_CustomerDataType.UnencryptedIdNumber));
                    customerData.set(DE48_CustomerDataType.Vehicle_Number, restrictWexPromptToMaxLength(wexFleetData.getVehicleNumber(), 8, DE48_CustomerDataType.Vehicle_Number));
                    customerData.set(DE48_CustomerDataType.WORKORDER_PONUMBER, restrictWexPromptToMaxLength(fleetData.getWorkOrderPoNumber(), 12, DE48_CustomerDataType.WORKORDER_PONUMBER));
                    customerData.set(DE48_CustomerDataType.ServicePrompt, fleetData.getServicePrompt());
                }

            } else {
                DE48_CardType cardType = mapCardType(builder.getPaymentMethod(), builder.getTransactionType());
                customerData.set(DE48_CustomerDataType.UnencryptedIdNumber, fleetData.getUserId());
                if (cardType != null && cardType.getValue().trim().equals("VT")) {
                    customerData.set(DE48_CustomerDataType.Vehicle_Number_Code3, fleetData.getVehicleNumber());
                    customerData.set(DE48_CustomerDataType.Id_Number_Code3, fleetData.getIdNumber());
                    customerData.set(DE48_CustomerDataType.DriverId_EmployeeNumber, fleetData.getDriverId());
                    customerData.set(DE48_CustomerDataType.Odometer_Reading, fleetData.getOdometerReading());
                }
                else if(cardType != null && cardType.getValue().trim().equals("VF")){
                    customerData.set(DE48_CustomerDataType.Vehicle_Number_Code3, fleetData.getVehicleNumber() == null ? fleetData.getVehicleNumber() : StringUtils.padLeft(fleetData.getVehicleNumber(),6,'0'));
                    customerData.set(DE48_CustomerDataType.Id_Number_Code3, fleetData.getIdNumber() == null ? fleetData.getIdNumber() : StringUtils.padLeft(fleetData.getIdNumber(),6,'0'));
                    customerData.set(DE48_CustomerDataType.DriverId_EmployeeNumber, fleetData.getDriverId() == null ? fleetData.getDriverId() : StringUtils.padLeft(fleetData.getDriverId(),6,'0'));
                    customerData.set(DE48_CustomerDataType.Odometer_Reading, fleetData.getOdometerReading() == null ? fleetData.getOdometerReading() : StringUtils.padLeft(fleetData.getOdometerReading(),6,'0'));
                }
                else {
                    customerData.set(DE48_CustomerDataType.Vehicle_Number, fleetData.getVehicleNumber());
                    customerData.set(DE48_CustomerDataType.DriverId_EmployeeNumber, fleetData.getDriverId());
                    customerData.set(DE48_CustomerDataType.Odometer_Reading, fleetData.getOdometerReading());
                }
                customerData.set(DE48_CustomerDataType.VehicleTag, fleetData.getVehicleTag());
                customerData.set(DE48_CustomerDataType.DriverLicense_Number, fleetData.getDriversLicenseNumber());
                customerData.set(DE48_CustomerDataType.WORKORDER_PONUMBER, fleetData.getWorkOrderPoNumber());
                customerData.set(DE48_CustomerDataType.TrailerHours_ReferHours, fleetData.getTrailerReferHours());
                customerData.set(DE48_CustomerDataType.EnteredData_Numeric, fleetData.getEnteredData());
                customerData.set(DE48_CustomerDataType.ServicePrompt, fleetData.getServicePrompt());
                customerData.set(DE48_CustomerDataType.JobNumber, fleetData.getJobNumber());
                customerData.set(DE48_CustomerDataType.Department, fleetData.getDepartment());
                customerData.set(DE48_CustomerDataType.TripNumber, fleetData.getTripNumber());
                customerData.set(DE48_CustomerDataType.UnitNumber, fleetData.getUnitNumber());
                customerData.set(DE48_CustomerDataType.MaintenanceNumber, fleetData.getMaintenanceNumber());
                customerData.set(DE48_CustomerDataType.TrailerNumber, fleetData.getTrailerNumber());
                customerData.set(DE48_CustomerDataType.HubometerNumber, fleetData.getHubometerNumber());
                customerData.set(DE48_CustomerDataType.ADDITIONALPROMPTDATA1, fleetData.getAdditionalPromptData1());
                customerData.set(DE48_CustomerDataType.ADDITIONALPROMPTDATA2, fleetData.getAdditionalPromptData2());
                customerData.set(DE48_CustomerDataType.EMPLOYEENUMBER, fleetData.getEmployeeNumber());
                customerData.set(DE48_CustomerDataType.Id_Number_Code3, fleetData.getIdNumber());
            }
        }

        // cvn number
        if(builder.getPaymentMethod() instanceof ICardData) {
            ICardData card = (ICardData) builder.getPaymentMethod();
            if (acceptorConfig.isSupportE3Encryption() && builder.getPaymentMethod() instanceof CreditCardData
            &&  !StringUtils.isNullOrEmpty(card.getCvn())) {
                String cardType = card.getCardType();
                String cvn;
                if (cardType.equals(CardType.AMEX) || cardType.equalsIgnoreCase("Discover")) {
                    cvn = StringUtils.padLeft(' ', AMEX_DISCOVER_CVV_LENGTH, ' ');
                    customerData.set(DE48_CustomerDataType.CardPresentSecurityCode, cvn);
                }
                else if (cardType.equalsIgnoreCase("MC") || cardType.equalsIgnoreCase("Visa")) {
                    cvn = StringUtils.padLeft(' ', MC_VISA_CVV_LENGTH, ' ');
                    customerData.set(DE48_CustomerDataType.CardPresentSecurityCode, cvn);
                }
            } else {
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
        messageControl.setCardType(mapCardType(builder.getPaymentMethod(), builder.getTransactionType()));

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
        if(acceptorConfig.hasPosConfiguration_MessageControl() && !isTimeRequest) {
            DE48_33_PosConfiguration posConfiguration = new DE48_33_PosConfiguration();
            posConfiguration.setTimezone(posConfiguration.getTimezone());
            posConfiguration.setSupportsPartialApproval(acceptorConfig.getSupportsPartialApproval());
            posConfiguration.setSupportsReturnBalance(acceptorConfig.getSupportsReturnBalance());
            posConfiguration.setSupportsCashAtCheckOut(acceptorConfig.getSupportsCashAtCheckout());
            posConfiguration.setMobileDevice(acceptorConfig.getMobileDevice());
            posConfiguration.setSupportWexAvailableProducts(acceptorConfig.getSupportWexAvailableProducts());
            posConfiguration.setSupportTerminalPurchaseRestriction(acceptorConfig.getSupportTerminalPurchaseRestriction());
            posConfiguration.setSupportVisaFleet2dot0(acceptorConfig.getSupportVisaFleet2dot0());
            messageControl.setPosConfiguration(posConfiguration);
        }

        // DE48-34 // Message Configuration Fields
        if(acceptorConfig.hasPosConfiguration_MessageData() && !isTimeRequest) {
            DE48_34_MessageConfiguration messageConfigData = new DE48_34_MessageConfiguration();
            messageConfigData.setPerformDateCheck(acceptorConfig.getPerformDateCheck());
            messageConfigData.setEchoSettlementData(acceptorConfig.getEchoSettlementData());
            messageConfigData.setIncludeLoyaltyData(acceptorConfig.getIncludeLoyaltyData());
            messageConfigData.setTransactionGroupId(acceptorConfig.getTransactionGroupId());
            messageConfigData.setIncrementalSupportIndicator(acceptorConfig.getIncrementalSupportIndicator());
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
        messageControl.setPriorMessageInformation(pmi);

        // DE48-40 Addresses
        if(builder instanceof AuthorizationBuilder) {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(authBuilder.getBillingAddress() != null && !isAccountVerified(authBuilder)) {
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
    private <T extends TransactionBuilder<Transaction>> DE62_CardIssuerData mapCardIssuerData(T builder) {
        // DE 62: Card Issuer Data - LLLVAR ans..999
        DE62_CardIssuerData cardIssuerData = new DE62_CardIssuerData();
        boolean isVisaFleet2 = acceptorConfig.getSupportVisaFleet2dot0() != null && acceptorConfig.getVisaFleet2()!=null && acceptorConfig.getVisaFleet2();

        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }
            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals("VisaFleet") && isVisaFleet2) {
                cardIssuerData.add(CardIssuerEntryTag.VISAFLEET2DOT0CARDPRESENTBYCARDHOLDER, acceptorConfig.getSupportVisaFleet2dot0().getValue());
            }
        }
        // unique device id
        if(!StringUtils.isNullOrEmpty(builder.getUniqueDeviceId())) {
            cardIssuerData.add(CardIssuerEntryTag.UniqueDeviceId, builder.getUniqueDeviceId());
        }
        else if(!StringUtils.isNullOrEmpty(uniqueDeviceId)) {
            cardIssuerData.add(CardIssuerEntryTag.UniqueDeviceId, uniqueDeviceId);
        }

        // pos config
        if(acceptorConfig.hasPosConfiguration_IssuerData()) {
            cardIssuerData.add(CardIssuerEntryTag.NTS_POS_Capability, acceptorConfig.getPosConfigForIssuerData());
        }

        // wex support
        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }

            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals("WexFleet")) {

                cardIssuerData.add(CardIssuerEntryTag.Wex_SpecVersionSupport, "0401");

                if(builder.getTransactionType().equals(TransactionType.Refund)) {
                    cardIssuerData.add(CardIssuerEntryTag.IssuerSpecificTransactionMatchData, builder.getTransactionMatchingData().getElementData());
                }

                // purchase device sequence number
                if(builder.getFleetData() != null && builder.getFleetData().getPurchaseDeviceSequenceNumber() != null) {
                    cardIssuerData.add(CardIssuerEntryTag.Wex_PurchaseDeviceSequenceNumber, builder.getFleetData().getPurchaseDeviceSequenceNumber());
                }
                else if(paymentMethod instanceof CreditTrackData) {
                    cardIssuerData.add(CardIssuerEntryTag.Wex_PurchaseDeviceSequenceNumber, ((CreditTrackData) paymentMethod).getPurchaseDeviceSequenceNumber());
                }
            }
        }

        //IMC
        if(builder.getPaymentMethod() != null) {
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof TransactionReference) {
                paymentMethod = ((TransactionReference) paymentMethod).getOriginalPaymentMethod();
            }
            if(paymentMethod instanceof Credit && ((Credit) paymentMethod).getCardType().equals("MC")) {
                if(builder.getCitMitIndicator()!=null) {
                    cardIssuerData.add(CardIssuerEntryTag.MASTERCARD_CIT_MIT_INDICATOR, builder.getCitMitIndicator().getValue());
                }
            }
            //G00 for SVS
            if(paymentMethod instanceof GiftCard && ((GiftCard) paymentMethod).getCardType().equals("StoredValue")) {
                cardIssuerData.add(CardIssuerEntryTag.SVSVersion, SVS_VERSION);
            }
        }

        // NTE
        if(builder.isTerminalError()) {
            cardIssuerData.add(CardIssuerEntryTag.TerminalError, "Y");
        }

        // management builder related
        if(builder instanceof ManagementBuilder) {
            ManagementBuilder mb = (ManagementBuilder)builder;

            // IRR Issuer Reference Number
            if(!StringUtils.isNullOrEmpty(mb.getReferenceNumber())) {
                cardIssuerData.add(CardIssuerEntryTag.RetrievalReferenceNumber, mb.getReferenceNumber());
            }

            // Forced Batch Close
            if(mb.getTransactionType().equals(TransactionType.BatchClose) && (mb.getBatchCloseType().equals(BatchCloseType.Forced) || mb.isForceToHost())) {
                cardIssuerData.add(CardIssuerEntryTag.TerminalError, "Y");
            }

            // Transaction reference
            if(mb.getPaymentMethod() instanceof TransactionReference) {
                TransactionReference reference = (TransactionReference)mb.getPaymentMethod();

                // NTS Specific Data
                if(reference.getNtsData() != null) {
                    if(!mb.getTransactionType().equals(TransactionType.Void) && !isReversal(mb.getTransactionType())) {
                        cardIssuerData.add(CardIssuerEntryTag.NTS_System, reference.getNtsData().toString());
                    }
                }

                // original payment method
                if(reference.getOriginalPaymentMethod() != null) {
                    if(reference.getOriginalPaymentMethod() instanceof CreditCardData || reference.getOriginalPaymentMethod() instanceof
                    EBTCardData) {
                        cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator,"0");
                    }
                    else if (reference.getOriginalPaymentMethod() instanceof ITrackData) {
                        ITrackData track = (ITrackData) reference.getOriginalPaymentMethod();

                        if(track.getTrackNumber() != null) {
                            String nsiValue = track.getTrackNumber().equals(TrackNumber.TrackTwo) ? "2" : "1";
                            cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator, nsiValue);
                        }
                    }
                    else if (reference.getOriginalPaymentMethod() instanceof GiftCard) {
                        GiftCard giftCard = (GiftCard) reference.getOriginalPaymentMethod();

                        if(giftCard.getTrackNumber() != null) {
                            String nsiValue = giftCard.getTrackNumber().equals(TrackNumber.TrackTwo) ? "2" : "1";
                            cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator, nsiValue);
                        } else {
                            cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator, "0");
                        }
                    }
                }
                // Mastercard Banknet Reference Number
                if (reference.getMastercardBanknetRefNo() != null){
                    cardIssuerData.add(CardIssuerEntryTag.NTS_MastercardBankNet_ReferenceNumber,reference.getMastercardBanknetRefNo());
                }
                // Mastercard Banknet Settlement Date
                if (reference.getMastercardBanknetSettlementDate() != null){
                    cardIssuerData.add(CardIssuerEntryTag.NTS_MastercardBankNet_SettlementDate,reference.getMastercardBanknetSettlementDate());
                }
            }
        }
        else {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(authBuilder.getEmvChipCondition() != null) {
                cardIssuerData.add(CardIssuerEntryTag.ChipConditionCode, mapChipCondition(authBuilder.getEmvChipCondition()));
            }

            // Refund Logic
            if(authBuilder.getTransactionType().equals(TransactionType.Refund)) {
                IPaymentMethod paymentMethod = authBuilder.getPaymentMethod();

                // NTS
                if(paymentMethod instanceof Credit) {
                    cardIssuerData.add(CardIssuerEntryTag.NTS_System, "08 00");
                }

                // NSI
                if(!( paymentMethod instanceof Debit || paymentMethod instanceof EBT || paymentMethod instanceof GiftCard)) {
                    if(paymentMethod instanceof ITrackData) {
                        ITrackData track = (ITrackData)paymentMethod;

                        String nsiValue = track.getTrackNumber().equals(TrackNumber.TrackTwo) ? "2" : "1";
                        cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator, nsiValue);
                    }
                    else {
                        cardIssuerData.add(CardIssuerEntryTag.SwipeIndicator,"0");
                    }
                }
            }
            IPaymentMethod paymentMethod = builder.getPaymentMethod();
            if(paymentMethod instanceof Credit) {
                Credit card = (Credit) paymentMethod;
                if (card.getCardType() != null && card.getCardType().equals("MC")) {
                    // IAU(Mastercard UCAF Data)
                    String ucafData = authBuilder.getMasterCardUCAFData();
                    if (authBuilder.getTransactionType().equals(TransactionType.Auth) && ucafData != null) {
                        cardIssuerData.add(CardIssuerEntryTag.MastercardUCAFData, ucafData);
                    }
                    // IME(Mastercard E-comm indicator)
                    DE62_IME_EcommerceData masterCardEComIndicator = authBuilder.getEcommerceData();
                    if ((authBuilder.getTransactionType().equals(TransactionType.Auth) || authBuilder.getTransactionType().equals(TransactionType.Sale)) &&
                            (masterCardEComIndicator != null && masterCardEComIndicator.getDe62ImeSubfield1() != null)) {
                        cardIssuerData.add(CardIssuerEntryTag.MastercardECommerceIndicators, masterCardEComIndicator.getDe62ImeSubfield1().getValue());
                    }
                    String masterCardDSRPCryptogram = authBuilder.getMasterCardDSRPCryptogram();
                    if(masterCardDSRPCryptogram != null){
                        cardIssuerData.add(CardIssuerEntryTag.MastercardDSRPCryptogram,StringUtils.padLeft(masterCardDSRPCryptogram, 32, ' '));
                    }
                    String masterCardRemoteCommAcceptor = authBuilder.getMasterCardRemoteCommAcceptor();
                    if(masterCardRemoteCommAcceptor != null){
                        cardIssuerData.add(CardIssuerEntryTag.MastercardRemoteCommerceAcceptorIdentifier,masterCardRemoteCommAcceptor);
                    }
                    String masterCard3DSCryptogram = authBuilder.getMastercard3DSCryptogram();
                    if(masterCard3DSCryptogram != null){
                        cardIssuerData.add(CardIssuerEntryTag.Mastercard3DSCryptogram, StringUtils.padLeft(masterCard3DSCryptogram, 32, ' '));
                    }
                }
            }
                //IAD (Visa CAVV data for e-comm transaction)
            if(paymentMethod instanceof Credit) {
                Credit card = (Credit) paymentMethod;
                if (card.getCardType()!=null && (card.getCardType().equals("Visa") || card.getCardType().equals("Discover") || card.getCardType().equals("Amex"))) {
                    String cardIssuerAuthenticationData = authBuilder.getCardIssuerAuthenticationData();
                    if (cardIssuerAuthenticationData != null) {
                        cardIssuerData.add(CardIssuerEntryTag.CardIssuerAuthenticationData, StringUtils.padLeft(cardIssuerAuthenticationData, 40, ' '));
                    }
                }
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
    private DE48_CardType mapCardType(IPaymentMethod paymentMethod, TransactionType transactionType) {
        // check to see if the original payment method is set
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference transactionReference = (TransactionReference)paymentMethod;
            if(transactionReference.getOriginalPaymentMethod() != null) {
                paymentMethod = transactionReference.getOriginalPaymentMethod();
            }
        }

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
            else if(card.getCardType().equals("Visa")) {
                return DE48_CardType.Visa;
            }
            else if(card.getCardType().equals("VisaFleet")) {
                return DE48_CardType.VisaFleet;
            }
            else if(card.getCardType().equals("VisaReadyLink")) {
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
            else if(card.getCardType().equals("VoyagerFleet")) {
                return DE48_CardType.Voyager;
            }
            else if(card.getCardType().equals("FuelmanFleet")) {
                return DE48_CardType.FleetCorFuelmanPlus;
            }
            else if(card.getCardType().equals("FleetWide")) {
                return DE48_CardType.FleetCorFleetwide;
            }else if(card.getCardType().equals("UnionPay")) {
                return DE48_CardType.DiscoverCard;
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
            else if(card.getCardType().equals("GlobalPaymentsGift")) {
                return DE48_CardType.GlobalPaymentsGiftCard_Proprietary;
            }
        }
        else if(paymentMethod instanceof EBT) {
            EBT ebt = (EBT)paymentMethod;
            if(ebt.getEbtCardType().equals(EbtCardType.CashBenefit)) {
                return DE48_CardType.EBTCash;
            }
            else return DE48_CardType.EBTFoodStamps;
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
        ArrayList<String> successCodes = new ArrayList<String>();
        successCodes.add("000");
        successCodes.add("002");
        successCodes.add("400");
        successCodes.add("500");
        successCodes.add("501");
        successCodes.add("580");

        BigDecimal amount = request.getAmount(DataElementId.DE_004);
        if(response != null) {
            BigDecimal partialAmount = response.getAmount(DataElementId.DE_004);
            if (responseCode.equals("002")) {
                amount = partialAmount;
                request.set(DataElementId.DE_004, StringUtils.toNumeric(amount, 12));
            }
        }
        TransactionType transactionType = null;
        IPaymentMethod paymentMethod = null;
        PaymentMethodType paymentMethodType = null;
        if(builder != null) {
            transactionType = builder.getTransactionType();
            paymentMethod = builder.getPaymentMethod();
            if(paymentMethod != null) {
                paymentMethodType = paymentMethod.getPaymentMethodType();
            }
        }

        String encodedRequest = null;
        if(builder != null && request.isDataCollect(paymentMethodType)) {
            // check if we need to build the implied data-collect
            if(transactionType.equals(TransactionType.Sale) || transactionType.equals(TransactionType.Refund)
                    ||transactionType.equals(TransactionType.AddValue)) {
                NetworkMessage impliedCapture = buildImpliedCapture(request, response, paymentMethod);

                encodedRequest = encodeRequest(impliedCapture);
                if(batchProvider != null && successCodes.contains(responseCode)) {
                    batchProvider.reportDataCollect(transactionType, paymentMethodType, amount, encodedRequest);
                }
            }
            else if(!transactionType.equals(TransactionType.DataCollect)) {
                encodedRequest = encodeRequest(request);
                if(batchProvider != null && successCodes.contains(responseCode)) {
                    batchProvider.reportDataCollect(transactionType, paymentMethodType, amount, encodedRequest);
                }
            }
        }

        // report successes
        if(successCodes.contains(responseCode)) {
            // if there's a batch provider handle the batch close stuff
            if ((responseCode.equals("500") || responseCode.equals("501")) && batchProvider != null) {
                batchProvider.closeBatch(responseCode.equals("500"));
            } else if (responseCode.equals("580")) {
                if(batchProvider != null) {
                    try {
                        LinkedList<String> encodedRequests = batchProvider.getEncodedRequests();
                        if (encodedRequests != null) {
                            resentTransactions = new LinkedList<Transaction>();
                            for (String encRequest : encodedRequests) {
                                try {
                                    NetworkMessage newRequest = decodeRequest(encRequest);
                                    newRequest.setMessageTypeIndicator("1221");

                                    Transaction resend = sendRequest(newRequest, null, new byte[2], new byte[8]);
                                    resentTransactions.add(resend);
                                } catch (ApiException exc) {
                                    /* NOM NOM */
                                    // TODO: this should be reported
                                }
                            }

                            // resend the batch close
                            request.setMessageTypeIndicator("1521");
                            resentBatch = sendRequest(request, builder, new byte[2], new byte[8]);
                        }
                    } catch (ApiException exc) {
                        /* NOM NOM */
                        // TODO: this should be reported
                    }
                }
                encodedRequest = encodeRequest(request);
            }
        }

        // Tokenize that which has not already.
        if(StringUtils.isNullOrEmpty(encodedRequest)) {
            // check for pan data and replace it with the truncated track
            if(builder != null && builder.getPaymentMethod() instanceof ITrackData) {
                ITrackData track = (ITrackData)builder.getPaymentMethod();
                if(request.has(DataElementId.DE_035) || request.has(DataElementId.DE_045)) {
                    if(!StringUtils.isNullOrEmpty(track.getTruncatedTrackData())) {
                        request.set(track.getTrackNumber().equals(TrackNumber.TrackTwo) ? DataElementId.DE_035 : DataElementId.DE_045, track.getTruncatedTrackData());
                    }
                }
            }
            encodedRequest = encodeRequest(request);
        }
        return encodedRequest;
    }

    private NetworkMessage buildImpliedCapture(NetworkMessage request, NetworkMessage response, IPaymentMethod paymentMethod) {
        String authCode = null;
        String ntsData = null;
        String requestAmount = request.getString(DataElementId.DE_004);
        boolean isPartial = false;

        if(response != null) {
            String responseCode = response.getString(DataElementId.DE_039);
            if(responseCode.equals("002")) {
                isPartial = true;
            }
            else if(responseCode.equals("000")) {
                String responseAmount = response.getString(DataElementId.DE_004);
                if(!requestAmount.equals(responseAmount)) {
                    isPartial = true;
                    requestAmount = responseAmount;
                }
            }

            authCode = !StringUtils.isNullOrEmpty(response.getString(DataElementId.DE_038)) ? response.getString(DataElementId.DE_038) : request.getString(DataElementId.DE_038);

            DE62_CardIssuerData responseIssuerData = response.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
            if(responseIssuerData != null) {
                ntsData = responseIssuerData.get(CardIssuerEntryTag.NTS_System);
            }
        }

        return buildImpliedCapture(request, requestAmount, isPartial, authCode, ntsData, paymentMethod);
    }
    private NetworkMessage buildImpliedCapture(NetworkMessage request, String amount, boolean isPartial, String authCode, String ntsData, IPaymentMethod paymentMethod) {
        PaymentMethodType paymentMethodType = null;
        if(paymentMethod != null) {
            paymentMethodType = paymentMethod.getPaymentMethodType();
        }

        NetworkMessage impliedCapture = new NetworkMessage(Iso8583MessageType.CompleteMessage);
        impliedCapture.setMessageTypeIndicator("1220");
        impliedCapture.set(DataElementId.DE_003, request.getString(DataElementId.DE_003));
        impliedCapture.set(DataElementId.DE_004, amount);
        impliedCapture.set(DataElementId.DE_007, request.getString(DataElementId.DE_007));
        impliedCapture.set(DataElementId.DE_011, request.getString(DataElementId.DE_011));
        impliedCapture.set(DataElementId.DE_012, request.getString(DataElementId.DE_012));
        impliedCapture.set(DataElementId.DE_018, request.getString(DataElementId.DE_018));
        impliedCapture.set(DataElementId.DE_022, request.getDataElement(DataElementId.DE_022, DE22_PosDataCode.class));
        impliedCapture.set(DataElementId.DE_024, isPartial ? "202" : "201");
        impliedCapture.set(DataElementId.DE_030, request.getString(DataElementId.DE_030));
        impliedCapture.set(DataElementId.DE_038, authCode);
        impliedCapture.set(DataElementId.DE_041, request.getString(DataElementId.DE_041));
        impliedCapture.set(DataElementId.DE_042, request.getString(DataElementId.DE_042));
        impliedCapture.set(DataElementId.DE_043, request.getString(DataElementId.DE_043));
        impliedCapture.set(DataElementId.DE_046, request.getString(DataElementId.DE_046));
        impliedCapture.set(DataElementId.DE_054, request.getString(DataElementId.DE_054));
        impliedCapture.set(DataElementId.DE_063, request.getDataElement(DataElementId.DE_063, DE63_ProductData.class));
        impliedCapture.set(DataElementId.DE_127, request.getString(DataElementId.DE_127));

        if (paymentMethodType != null) {
            if (paymentMethodType.equals(PaymentMethodType.EBT)) {
                impliedCapture.set(DataElementId.DE_017, request.getString(DataElementId.DE_012).substring(2, 6));
            }

            if (acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES) && (paymentMethodType.equals(PaymentMethodType.EBT) ||
                    paymentMethodType.equals(PaymentMethodType.Debit) || paymentMethodType.equals(PaymentMethodType.Credit)||
                    (paymentMethodType.equals(PaymentMethodType.Gift)))) {
                impliedCapture.set(DataElementId.DE_014, request.getString(DataElementId.DE_014));

                //DE127 changing field matrix and encrypted data as encrypted pan.
                DE127_ForwardingData forwardingData = new DE127_ForwardingData();
                if (paymentMethod instanceof IEncryptable) {
                    String encryptedPan = null;
                    EncryptionData encryptionData = ((IEncryptable) paymentMethod).getEncryptionData();
//                    For setting KTB value as encrypted data in manage Transaction
                    if(!paymentMethodType.equals(PaymentMethodType.Credit)){
                    encryptionData.setEncryptedKTB(encryptionData.getKtb());
                    }
                    encryptedPan = ((IEncryptable) paymentMethod).getEncryptedPan();
                    if (encryptionData != null) {
                        EncryptionType encryptionType = acceptorConfig.getSupportedEncryptionType();
                        if (encryptedPan != null && encryptionType.equals(EncryptionType.TDES)) {
                            encryptionData.setKtb(encryptedPan);
                        }
                        if (encryptionType.equals(EncryptionType.TDES)) {
                            forwardingData.setServiceType(acceptorConfig.getServiceType());
                            forwardingData.setOperationType(acceptorConfig.getOperationType());
                        }
                        forwardingData.setEncryptedField(EncryptedFieldMatrix.Pan);
                        forwardingData.addEncryptionData(encryptionType, encryptionData);
                        impliedCapture.set(DataElementId.DE_127, forwardingData);
                    }
                }

            }
        }

        // DE_048 Message Control
        DE48_MessageControl messageControl = request.getDataElement(DataElementId.DE_048, DE48_MessageControl.class);
        messageControl.setPinEncryptionMethodology(null);
        impliedCapture.set(DataElementId.DE_048, messageControl);

        // DE_062 Card Issuer Data
        DE62_CardIssuerData requestIssuerData = request.getDataElement(DataElementId.DE_062, DE62_CardIssuerData.class);
        if(requestIssuerData == null) {
            requestIssuerData = new DE62_CardIssuerData();
        }

        // NTS
        if(ntsData != null) {
            requestIssuerData.add(CardIssuerEntryTag.NTS_System, ntsData);
        }

        // NSI
        String nsi = requestIssuerData.get(CardIssuerEntryTag.SwipeIndicator);

        // DE_002 / DE_014 - PAN / EXP DATE
        if(request.has(DataElementId.DE_002)) {
            impliedCapture.set(DataElementId.DE_002, request.getString(DataElementId.DE_002));
            impliedCapture.set(DataElementId.DE_014, request.getString(DataElementId.DE_014));
            if(StringUtils.isNullOrEmpty(nsi)) {
                requestIssuerData.add(CardIssuerEntryTag.SwipeIndicator, "0");
            }
        }
        else {
            // get the track object
            ITrackData track = null;
            if(paymentMethod instanceof ITrackData) {
                track = (ITrackData)paymentMethod;
            }
            else if(request.has(DataElementId.DE_035)) {
                track = new CreditTrackData(request.getString(DataElementId.DE_035));
            }
            else {
                track = new CreditTrackData(request.getString(DataElementId.DE_045));
            }

            // get the encryptable object
            IEncryptable encryptable = null;
            if(track instanceof IEncryptable) {
                encryptable = (IEncryptable)track;
            }

            if(encryptable != null) {
                if(encryptable.getEncryptionData() != null && encryptable.getEncryptedPan() != null && !acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES)) {
                    impliedCapture.set(DataElementId.DE_002, encryptable.getEncryptedPan());
                }
                else {
                    impliedCapture.set(DataElementId.DE_002, track.getPan());
                }
            }
            else {
                impliedCapture.set(DataElementId.DE_002, track.getPan());
            }

            // expiry
            impliedCapture.set(DataElementId.DE_014, track.getExpiry());

            // NSI swipe indicator
            if(StringUtils.isNullOrEmpty(nsi)) {
                if(acceptorConfig.getSupportedEncryptionType().equals(EncryptionType.TDES)){
                    if(track.getTrackNumber() != null) {
                        String nsiValue = track.getTrackNumber().equals(TrackNumber.TrackTwo) ? "2" : "1";
                        requestIssuerData.add(CardIssuerEntryTag.SwipeIndicator, nsiValue);
                    }
                }else {
                    requestIssuerData.add(CardIssuerEntryTag.SwipeIndicator, request.has(DataElementId.DE_035) ? "2" : "1");
                }
            }
        }
        impliedCapture.set(DataElementId.DE_062, requestIssuerData); // DE_042 - ISSUER DATA

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

        return impliedCapture;
    }
    private String encodeRequest(NetworkMessage request) {
        lrcFailure = false;
        int encodeCount = 0;
        while(encodeCount++ < 3) {
            String encodedRequest = doEncoding(request);
//            if(lrcFailure && isEnableLogging()) {
//                System.out.println(String.format("[TOKEN TRACE]: framedRequest: %s", encodedRequest));
//            }
            if(TerminalUtilities.checkLRC(encodedRequest)) {
                return encodedRequest;
            }
//            else if(isEnableLogging()) {
//                lrcFailure = true;
//                System.out.println(String.format("[TOKEN LRC FAILURE]: attempt: %s %s", encodeCount, encodedRequest));
//            }
        }
        return null;
    }
    private String doEncoding(NetworkMessage request) {
        // base64 encode the message buffer
        byte[] encoded = Base64.encodeBase64(request.buildMessage());
        String encodedString = new String(encoded);
//        if(lrcFailure && isEnableLogging()) {
//            System.out.println(String.format("[TOKEN TRACE]: encodedString: %s", encodedString));
//        }

        // encrypt it
        if(requestEncoder == null) {
            if(isEnableLogging()) {
                System.out.println(String.format("[TOKEN TRACE]: %s %s", companyId, terminalId));
            }
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }
        String token = requestEncoder.encode(encodedString);
//        if(lrcFailure && isEnableLogging()) {
//            System.out.println(String.format("[TOKEN TRACE]: encryptedToken: %s", token));
//        }

        // build final token
        MessageWriter mw = new MessageWriter();
        mw.add(ControlCodes.STX);
        mw.addRange(token.getBytes());
        mw.add(ControlCodes.ETX);

        // generate the CRC
        mw.add(TerminalUtilities.calculateLRC(mw.toArray()));
        return new String(mw.toArray());
    }
    private NetworkMessage decodeRequest(String encodedStr) {
        if(requestEncoder == null) {
            requestEncoder = new PayrollEncoder(companyId, terminalId);
        }

        byte[] encodedBuffer = encodedStr.getBytes();
        MessageReader mr = new MessageReader(encodedBuffer);

        String valueToDecrypt = encodedStr;
        if(mr.peek() == ControlCodes.STX.getByte()) {
            mr.readCode(); // pop the STX off
            valueToDecrypt = mr.readToCode(ControlCodes.ETX);

            byte lrc = mr.readByte();
            if(lrc != TerminalUtilities.calculateLRC(encodedBuffer)) {
                // invalid token
            }
        }

        String requestStr = requestEncoder.decode(valueToDecrypt);
        byte[] decoded = Base64.decodeBase64(requestStr);

        mr = new MessageReader(decoded);
        String mti = mr.readString(4);
        byte[] buffer = mr.readBytes(decoded.length);
        NetworkMessage request = NetworkMessage.parse(buffer, Iso8583MessageType.CompleteMessage);
        request.setMessageTypeIndicator(mti);
        return request;
    }
    private boolean isReversal(TransactionType type) {
        return type.equals(TransactionType.Reversal) || type.equals(TransactionType.LoadReversal);
    }
    private boolean shouldUseOriginalProcessingCode(IPaymentMethod paymentMethod, TransactionType type) {
        if(paymentMethod instanceof TransactionReference) {
            paymentMethod = ((TransactionReference)paymentMethod).getOriginalPaymentMethod();
        }

        if(isReversal(type) || type.equals(TransactionType.PreAuthCompletion)) {
            return true;
        }
        else if (paymentMethod instanceof GiftCard) {
            GiftCard giftCard = (GiftCard)paymentMethod;
            return giftCard.getCardType().equals("ValueLink") && type.equals(TransactionType.Void);
        }
        else if (paymentMethod instanceof EBTCardData && ((EBTCardData) paymentMethod).getCardType().equals(EbtCardType.FoodStamp.toString())
                && type.equals(TransactionType.Capture)){
            return true;
        }
        return false;
    }

    private <T extends TransactionBuilder<Transaction>> void validate(T builder) throws BuilderException, UnsupportedTransactionException {
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        if(paymentMethod instanceof TransactionReference) {
            TransactionReference reference = (TransactionReference)paymentMethod;
            if(reference.getOriginalPaymentMethod() != null) {
                paymentMethod = reference.getOriginalPaymentMethod();
            }
        }

        TransactionType transactionType = builder.getTransactionType();

        if(paymentMethod instanceof GiftCard) {
            GiftCard giftCard = (GiftCard) paymentMethod;
            if (giftCard.getCardType() == null) {
                throw new BuilderException("The card type must be specified for Gift transactions.");
            }
        }

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
        if(paymentMethod instanceof Credit) {
            if(((Credit) paymentMethod).getCardType().equals("WexFleet")) {
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
                if(fleetData == null && !(paymentMethod instanceof CreditTrackData)) {
                    throw new BuilderException("The purchase device sequence number cannot be null for WEX transactions.");
                }
                else if((fleetData != null && fleetData.getPurchaseDeviceSequenceNumber() == null) && (paymentMethod instanceof CreditTrackData && ((CreditTrackData) paymentMethod).getPurchaseDeviceSequenceNumber() == null)) {
                    throw new BuilderException("The purchase device sequence number cannot be null for WEX transactions.");
                }
            }
        }

        if(builder instanceof ManagementBuilder) {
            ManagementBuilder mb = (ManagementBuilder)builder;
            TransactionReference reference = (TransactionReference)mb.getPaymentMethod();

            if(transactionType.equals(TransactionType.BatchClose)) {
                if(batchProvider == null) {
                    if(mb.getTransactionCount() == null || mb.getTotalCredits() == null || mb.getTotalDebits() == null) {
                        throw new BuilderException("When an IBatchProvider is not present, you must specify transaction count, total debits and total credits when calling batch close.");
                    }

                    if(mb.getBatchNumber() == 0) {
                        throw new BuilderException("When an IBatchProvider is not present, you must specify a batch and sequence number for a batch close.");
                    }
                }
            }

            if(transactionType.equals(TransactionType.Refund)) {
                if(reference.getOriginalPaymentMethod() instanceof EBT) {
                    EBT ebtCard = (EBT)reference.getOriginalPaymentMethod();
                    // no refunds on cash benefit cards
                    if(ebtCard.getEbtCardType() == EbtCardType.CashBenefit && transactionType.equals(TransactionType.Refund)) {
                        throw new UnsupportedTransactionException("Refunds are not allowed for cash benefit cards.");
                    }
                }
            }

            if(isReversal(transactionType)) {
                if(StringUtils.isNullOrEmpty(reference.getOriginalProcessingCode())) {
                    throw new BuilderException("The original processing code should be specified when performing a reversal.");
                }

                // IRR for fleet reversals
                if(paymentMethod instanceof Credit) {
                    if(((Credit) paymentMethod).isFleet() && StringUtils.isNullOrEmpty(mb.getReferenceNumber())) {
                        if(reference.getNtsData() != null && reference.getNtsData().getFallbackCode().equals(FallbackCode.None)) {
                            throw new BuilderException("Reference Number is required for fleet voids/reversals.");
                        }
                    }
                }
            }

            if(transactionType.equals(TransactionType.Void)){
                // IRR for fleet reversals
                if(paymentMethod instanceof Credit) {
                    if(((Credit) paymentMethod).isFleet() && StringUtils.isNullOrEmpty(((ManagementBuilder) builder).getReferenceNumber())) {
                        throw new BuilderException("Reference Number is required for fleet voids/reversals.");
                    }
                }
            }
        }
        else {
            AuthorizationBuilder authBuilder = (AuthorizationBuilder)builder;
            if(paymentMethod !=null && paymentMethod.getPaymentMethodType().equals(PaymentMethodType.Debit)) {
                if(acceptorConfig.getAddress() == null) {
                    throw new BuilderException("Address is required in acceptor config for Debit/EBT Transactions.");
                }
            }

            if(paymentMethod instanceof EBT) {
                EBT card = (EBT)paymentMethod;
                if(card.getEbtCardType().equals(EbtCardType.FoodStamp) && authBuilder.getCashBackAmount() != null) {
                    throw new BuilderException("Cash back is not allowed for Food Stamp cards.");
                }
            }
        }
    }
    private EncryptedFieldMatrix getEncryptionField(IPaymentMethod paymentMethod, EncryptionType encryptionType, TransactionType transactionType){
        String card = null;
        if(paymentMethod instanceof GiftCard){
            card = ((GiftCard) paymentMethod).getCardType();
        }
        if(encryptionType.equals(EncryptionType.TDES)){
            if(paymentMethod instanceof ICardData || (paymentMethod instanceof CreditTrackData && transactionType.equals(TransactionType.Refund))){
                return EncryptedFieldMatrix.Pan;
            }
            else if(paymentMethod instanceof ITrackData && !transactionType.equals(TransactionType.Capture) && !transactionType.equals(TransactionType.PreAuthCompletion) && !transactionType.equals(TransactionType.Void) && !transactionType.equals(TransactionType.Reversal)) {
                TrackNumber trackType=((ITrackData)paymentMethod).getTrackNumber();
                if (trackType == TrackNumber.TrackOne)
                    return EncryptedFieldMatrix.Track1;
                else if (trackType == TrackNumber.TrackTwo)
                    return EncryptedFieldMatrix.Track2;
            }else if(paymentMethod instanceof GiftCard
                    && ((("ValueLink").equals(card) || ("StoredValue").equals(card)) || (!transactionType.equals(TransactionType.Capture)
                    && !transactionType.equals(TransactionType.PreAuthCompletion)
                    && !transactionType.equals(TransactionType.Void) && !transactionType.equals(TransactionType.Reversal))))
            {
                TrackNumber trackType=((GiftCard)paymentMethod).getTrackNumber();
                if (trackType == TrackNumber.TrackOne)
                    return EncryptedFieldMatrix.Track1;
                else if (trackType == TrackNumber.TrackTwo)
                    return EncryptedFieldMatrix.Track2;
            }else{
                return EncryptedFieldMatrix.Pan;
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
    private void setTokenizationData(DE127_ForwardingData forwardingData, ICardData cardData, ITrackData trackData,GiftCard giftCard, String tokenizationData) {

        //Tokenization Operation type
        TokenizationOperationType tokenOperationType = acceptorConfig.getTokenizationOperationType();

        //Token data AccountNumber/Token
        forwardingData.setTokenOrAcctNum(tokenizationData);

        // Card Expiry
        if(cardData != null) {
            forwardingData.setExpiryDate(formatExpiry(cardData.getShortExpiry()));
        }
        if(trackData != null){
            forwardingData.setExpiryDate(trackData.getExpiry());
            if(tokenOperationType.equals(TokenizationOperationType.Tokenize)){
                forwardingData.setTokenOrAcctNum(trackData.getPan());
            }
        }
        if(giftCard != null){
            forwardingData.setExpiryDate(giftCard.getExpiry());
            if(tokenOperationType.equals(TokenizationOperationType.Tokenize)){
                forwardingData.setTokenOrAcctNum(giftCard.getPan());
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

        setTokenizationOperationType(forwardingData, tokenOperationType);
        forwardingData.addTokenizationData(tokenizationType);

    }
    private BigDecimal getCashAtCheckoutAmount(AuthorizationBuilder builder,Credit card){
        BigDecimal cashAtCheckoutAmount;
        float cashAtCheckoutLimit = 120F;
        if (card.getCardType().equals(DISCOVER)){
            if (builder.getCashAtCheckoutAmount().floatValue() <= cashAtCheckoutLimit){
                cashAtCheckoutAmount = builder.getCashAtCheckoutAmount();
            }else throw new UnsupportedOperationException(CASH_AT_CHECKOUT_EXCEPTION);
        }
        else throw new UnsupportedOperationException(SUPPORTS_DISCOVER_ONLY_EXCEPTION);

        return cashAtCheckoutAmount;
    }
    private boolean isAccountVerified(AuthorizationBuilder builder){
        if (builder.getAmount() != null && builder.getAmount().equals(BigDecimal.ZERO) && builder.isAvs()){
            return true;
        }else
            return false;
    }
    public static String restrictWexPromptToMaxLength(String wexPrompt, int maxLength, DE48_CustomerDataType de48CustomerDataType) throws BuilderException {
        if (!StringUtils.isNullOrEmpty(wexPrompt)) {
            if (wexPrompt.length() > maxLength)
                throw new BuilderException(de48CustomerDataType + " length should not exceed " + maxLength);
            return wexPrompt;
        }
        return wexPrompt;
    }
    private static String validateWexDriverIDLength(String wexDriverId,int length) throws BuilderException {
        if(!StringUtils.isNullOrEmpty(wexDriverId)){
            if(wexDriverId.length() != 4 && wexDriverId.length() != 6)
                throw new BuilderException("Driver Id length must be 4 or 6.");
            else
                return StringUtils.padRight(wexDriverId,length, ' ');
        }
        return wexDriverId;
    }
    private static int getWexFleetPromptCount(FleetData wexFleetData){
        int noOfPrompt =0;

        List<String> promptCode = new ArrayList<>();
        promptCode.add(wexFleetData.getEnteredData());
        promptCode.add(wexFleetData.getDepartment());
        promptCode.add(wexFleetData.getDriverId());
        promptCode.add(wexFleetData.getHubometerNumber());
        promptCode.add(wexFleetData.getJobNumber());
        promptCode.add(wexFleetData.getMaintenanceNumber());
        promptCode.add(wexFleetData.getOdometerReading());
        promptCode.add(wexFleetData.getTrailerReferHours());
        promptCode.add(wexFleetData.getTrailerNumber());
        promptCode.add(wexFleetData.getTripNumber());
        promptCode.add(wexFleetData.getUnitNumber());
        promptCode.add(wexFleetData.getUserId());
        promptCode.add(wexFleetData.getVehicleNumber());
        promptCode.add(wexFleetData.getWorkOrderPoNumber());
        promptCode.add(wexFleetData.getServicePrompt());

        noOfPrompt = Math.toIntExact(promptCode.stream().filter(Objects::nonNull).count());
        promptCode.clear();

        return noOfPrompt;
    }
}
