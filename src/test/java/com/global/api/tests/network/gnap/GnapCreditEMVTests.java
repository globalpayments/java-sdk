package com.global.api.tests.network.gnap;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.gnap.*;
import com.global.api.network.enums.gnap.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.testdata.GnapTestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

public class GnapCreditEMVTests {
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    private String tagData = "4f07a0000000041010500a4d617374657243617264" +
            "57134012002000060016D22122019882803290000F" +
            "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01005f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34030403029f3501229f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
    private GnapPosDetails posData;
    private TestUtil testUtil = TestUtil.getInstance();
    private CreditTrackData track;
    public GnapCreditEMVTests() throws ConfigurationException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setEmvCapable(true);
//        acceptorConfig.setPinPadSerialNumber("SERIAL02");
        acceptorConfig.setDeviceType("9.");

        // gateway config
        config = new NetworkGatewayConfig(Target.GNAP);
        config.setPrimaryEndpoint("scctesta.globalpaycan.com");
        config.setPrimaryPort(443);
        config.setSecondaryEndpoint("mcctesta.globalpaycan.com");
        config.setSecondaryPort(443);
        config.setTerminalId("711SDKT1");

        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);


        gnapMessageHeader = GnapMessageHeader.builder()
                .transmissionNumber(String.format("%02d", testUtil.getTransmissionNo()))
                .messageSubType(MessageSubType.OnlineTransactions)
                .transactionCode(TransactionCode.Purchase)
                .build();

        posData = GnapPosDetails.builder()
                .cardHolderPresentIndicator(CardHolderPresentIndicator.CardHolderIsPresent)
                .cardPresentIndicator(CardPresentIndicator.CardPresent)
                .transactionStatusIndicator(TransactionStatusIndicator.NormalRequest)
                .transactionSecurityIndicator(TransactionSecurityIndicator.NoSecurityConcern)
                .cardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.NotACATTransaction)
                .cardholderIDMethod(CardHolderIDMethod.PIN)
                .build();

        ServicesContainer.configureService(config);

        optionalData = OptionalData.builder()
                .terminalType(TerminalType.IntegratedSolutions)
                .integratedHardwareList(IntegratedHardwareList.BBP)
                .pinPadComm(PinPadCommunication.RS32)
                .integratedPinpadVersionType(IntegratedPinpadVersionType.BBPOS)
                .paymentSolutionProviderCode(PaymentSolutionProviderCode.Tender_Retail)
                .base24TransactionModifier(Base24TransactionModifier.OtherTransaction)
                .integratedPinPadVersion("3031")
                .pinPadSerialNumber("00WA123456")
                .pOSVARCode("711")
                .pOSVersionNO("123456789")
                .paymentSolutionVersionNO("123456789")
                .cAPKKeyVersion("1200")
                .tLSCiphers("0123456789")
                .build();

        //-------------Visa
        track = GnapTestCards.visaEmvTrack();
//        //-------------MasterCard
//        track.setValue(";5413330089020029=2512201062980790?");
//        //-------------Discover
//        track.setValue(";6011005612796527=2512201062980790?");
//        //-------------UnionPay
//        track.setValue(";6210948000000029=22122011758909689?");
//        //-------------Amex
//        track.setValue(";372700699251018=22122011758909689?");

        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);


    }

//  Credit Card Contact EMV Financial Transaction Request Message
    @Test
    public void VI_CT_SA_09() throws ApiException {
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Request Message - Credit Purchase
    @Test
    public void VI_CTL_SA_10() throws ApiException {

        track= GnapTestCards.visaEmvTrack();
        track.setEntryMethod(EntryMethod.EmvContactlessCard);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(2.10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    // EMV Request Message - Credit Pre-Authorization
    @Test
    public void test_1103_EMV_CreditPreAuthorization() throws ApiException {

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(80))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //    EMV Request Message - AMEX Credit Pre-Authorization
    @Test
    public void VI_CT_PA_14() throws ApiException {

        track=GnapTestCards.visaEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

    }

    //EMV Request Message - AMEX Credit Pre-Authorization Completion
    //Pre-Auth Completion Final Amount Lower than Original Pre-Auth Amount
    @Test
    public void VI_CT_PC_15() throws ApiException {
        track=GnapTestCards.visaEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();
        //testUtil.assertSuccess(response);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction preAuthResponse = response.preAuthCompletion(new BigDecimal(15))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);
    }

    //EMV Request Message - AMEX Credit Pre-Authorization Completion
    @Test
    public void test_1106_AmexAFDCompletionAmountEqualToOriginalAmountPreAuth() throws ApiException {
        track=GnapTestCards.amexEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthresponse = track.authorize(new BigDecimal(80))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthresponse);

        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction response = preAuthresponse.preAuthCompletion(new BigDecimal(80))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //EMV Request Message - Credit Purchase Void
    @Test
    public void VI_CT_SV_11() throws ApiException {

        track=GnapTestCards.visaEmvTrack();
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();
       //testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //EMV Request Message - Credit Return/Refund
    @Test
    public void VI_CT_RE_09() throws ApiException {
        tagData= "4f07a0000000041010500a4d617374657243617264" + "57134012002000060016D22122019882803290000F" + "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01205f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34030403029f3501229f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
        track=GnapTestCards.visaEmvTrack();

        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //EMV Request Message - Credit Return/Refund Void
    @Test
    public void VI_CT_RV_11() throws ApiException {
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        //testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //  Credit Card Contact EMV Pre-auth Completion Transaction Request Message
    @Test
    public void test_1115_CreditCardContactEMVPreAuthCompletionTransaction() throws ApiException {
        track=GnapTestCards.visaEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction preResponse = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

   // EMV Request Message - Credit Pre-Authorization // Original Visa Pre-Authorization Transaction
    @Test
    public void test_1116_OriginalVisaContactEMVPreAuthorization() throws ApiException {
        track=GnapTestCards.visaEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids).build();

        Transaction response = track.authorize(new BigDecimal(1.35))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

    }

    // EMV Request Message - Credit Pre-Authorization
    // Original Discover Contact EMV Pre-Authorization Transaction
    @Test
    public void test_1117_OriginalDiscoverContactEMVPreAuthorization() throws ApiException {
        track=GnapTestCards.discoverEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids).build();

        Transaction response=track.authorize(new BigDecimal(1.35))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    //    Initial Card Present EMV Offline PIN Pre-Auth Request Message - Visa
    @Test
    public void test_1118_InitialCardPresentEMVOfflinePINPreAuthRequestMessageVisa() throws ApiException {
        track=GnapTestCards.visaEmvTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //EMV Cash Advance
    @Test
    public void test_1119_EMVCashAdvanceCreditOnlinePurchase () throws ApiException{

        gnapMessageHeader.setTransactionCode(TransactionCode.EmvCashAdvance);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids).build();

        Transaction response = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }


    //   11 end ----------------------------------------------

    //Start Visa Chapter 17
    //Online Purchase Partial void Request Message
    //Visa
    @Test
    public void test_1701_Visa_EMVOnlinePurchasePartialVoid() throws ApiException {
        track=GnapTestCards.VisaTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Discover EMV Chip Inserted Online Purchase Request Message
    @Test
    public void test_1702_Discover_EMVChipInsertedOnlinePurchase() throws ApiException {
        track=GnapTestCards.discoverEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Discover Online Purchase Partial void Request Message
    //nw
    @Test
    public void test_1703_Discover_OnlinePurchasePartialVoid() throws ApiException {
        track=GnapTestCards.discoverEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Visa EMV Chip Inserted Online Purchase Request Message
    @Test
    public void test_1704_Visa_EMVChipInsertedOnlinePurchase()throws ApiException {
        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Online Purchase Partial void Request Message - visa
    @Test
    public void test_1705_Visa_EMVOnlinePurchasePartialVoid()throws ApiException {
        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        preResponse.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(0.9));

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Discover Online Purchase Partial void Request Message
    @Test
    public void test_1706_Discover_EMVOnlinePurchasePartialVoid()throws ApiException {
        track=GnapTestCards.DiscoverTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        preResponse.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(0.9));
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }



    @Test
    public void test_1313_EMVRequestMessageCreditRefund() throws ApiException {
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //EMV Cash Advance
    @Test
    public void test_EMVCashAdvanceCreditOnlinePurchase () throws ApiException{

        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids).build();

        Transaction response = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //EMV Request Message - Credit Return/Refund Void

    @Test
    public void test_EMVCreditReturnOrRefundVoid() throws ApiException {
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber((String.format("%02d", testUtil.getTransmissionNo())));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response=preResponse.voidTransaction(new BigDecimal(15))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //Financial Transaction Timeout Reversal Message
    @Test
    public void test_FinancialTransaction_TimeoutReversalMessage() throws ApiException {

        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }



    // start chapter 16 refund processing

    //Visa Online EMV Card Present Refund Request Example
    @Test
    public void test_1601_VisaEMV_ChipInsertedOnlineRefund() throws ApiException {
        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //MC Online EMV Card Present Refund Request Example
    @Test
    public void test_1602_MCEMV_ChipInsertedOnlineRefund() throws ApiException {
        track=GnapTestCards.MCEMVTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Discover Online EMV Card Present Refund Request
    @Test
    public void test_1603_DiscoverEMV_ChipInsertedOnlineRefund() throws ApiException {
        track=GnapTestCards.discoverEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Contact EMV Card Present Online Refund FULL Amount VOID (Visa)
    @Test
    public void test_1604_VisaEMV_ContactEMVCardPresentOnlineRefundFULLAmountVOID() throws ApiException {
        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction preResponse = response.voidTransaction(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    //Contact EMV Card Present Online Refund FULL Amount VOID (MC)
    @Test
    public void test_1605_MCEMV_ContactEMVCardPresentOnlineRefundFULLAmountVOID() throws ApiException {
        track=GnapTestCards.MCTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preResponse = response.voidTransaction(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    //Contact EMV Card Present Online Refund FULL Amount VOID (Discover)
    @Test
    public void test_1606_DiscoverEMV_ContactEMVCardPresentOnlineRefundFULLAmountVOID() throws ApiException {
        track=GnapTestCards.DiscoverTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preResponse = response.voidTransaction(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    //Online Refund Partial Amount VOID

    //Sample Visa Messaging Online Refund Partial Amount VOID
    @Test
    public void test_1607_VisaEMV_MessagingOnlineRefundPartialAmountVOID() throws ApiException {

        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(90));

        Transaction preResponse = response.voidTransaction(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    //Sample MC Messaging Online Refund Partial Amount VOID
    @Test
    public void test_1608_MCEMV_MessagingOnlineRefundPartialAmountVOID() throws ApiException {

        track=GnapTestCards.MCEMVTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(90));

        Transaction preResponse = response.voidTransaction(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    //Sample Discover Messaging Online Refund Partial Amount VOID
    @Test
    public void test_1609_DiscoverEMV_MessagingOnlineRefundPartialAmountVOID() throws ApiException {

        track=GnapTestCards.discoverEmvTrack();
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(90));

        Transaction preResponse = response.voidTransaction(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

    }

    // end chapter 16

    //Unionpay Transaction start
    //UnionPay EMV Purchase
    @Test
    public void test_1801_UnionPayEMV_Purchase() throws ApiException {
        track=GnapTestCards.unipayEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1802_UnionPayEMV_PurchaseCancellation() throws ApiException {

        track=GnapTestCards.unipayEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        optionalData.setEmployeeID("03");
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setInvoiceNumber("V100012");
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);

    }

    @Test
    public void test_1803_UnionPayEMV_AuthorizationPurchase() throws ApiException {
        track=GnapTestCards.unipayEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(5))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1804_UnionPayEMV_PreAuthorizationCompletion() throws ApiException {
        track=GnapTestCards.unipayEmvTrack();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(45))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(45))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1805_UnionPayEMV_PreAuthorizationCancellation() throws ApiException {

        track=GnapTestCards.unipayEmvTrack();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(4))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1806_UnionPayEMV_RefundTransaction() throws ApiException {
        track=GnapTestCards.unipayEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }


    @Test
    public void VI_CT_RP_03() throws ApiException {

        track=GnapTestCards.visaEmvTrack();

        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .approvalCode("ASD456")
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void VI_CT_RPV_07() throws ApiException {

        track=GnapTestCards.visaEmvTrack();

        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);
        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .approvalCode("ASD456")
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = track.charge(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);


    }

}
