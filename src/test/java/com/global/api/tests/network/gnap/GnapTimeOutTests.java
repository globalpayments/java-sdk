package com.global.api.tests.network.gnap;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.gnap.*;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.network.enums.gnap.*;
import com.global.api.network.enums.gnap.CardType;
import com.global.api.network.enums.gnap.TerminalType;
import com.global.api.network.enums.gnap.TransactionCode;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.testdata.GnapTestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

public class GnapTimeOutTests {

    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    private GnapPosDetails posData;
    private TestUtil testUtil = TestUtil.getInstance();
    private CreditTrackData track;
    private CreditCardData manual;
    private DebitTrackData trackData;
    private Address shippingAdd;
    private Address billingAdd;
    private Customer cardHolder;
    String tagData = "4f07a0000000041010500a4d617374657243617264" + "57134012002000060016D22122019882803290000F" + "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01005f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34030403029f3501229f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
    public GnapTimeOutTests() throws ConfigurationException {

        billingAdd=new Address("A1520 MAIN","YM5X1B1");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setEmvCapable(true);
        acceptorConfig.setPinPadSerialNumber("SERIAL02");
        acceptorConfig.setDeviceType("9.");
        //acceptorConfig.setSupportsE2EEEncryption(true);

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
                .build();

        posData = GnapPosDetails.builder()
                .cardHolderPresentIndicator(CardHolderPresentIndicator.CardHolderIsPresent)
                .cardPresentIndicator(CardPresentIndicator.CardPresent)
                .transactionStatusIndicator(TransactionStatusIndicator.NormalRequest)
                .transactionSecurityIndicator(TransactionSecurityIndicator.NoSecurityConcern)
                .cardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.NotACATTransaction)
                .cardholderIDMethod(CardHolderIDMethod.Signature)
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
        track = new CreditTrackData();
        track.setValue(";4761739001010119=22122011758909689?");
//        //-------------MasterCard
//        track.setValue(";5413330089020029=2512201062980790?");
//        //-------------Discover
//        track.setValue(";6011005612796527=2512201062980790?");
        //-------------UnionPay
        // track.setValue(";6210948000000029=22122011758909689?");
//        //-------------Amex
//        track.setValue(";372700699251018=22122011758909689?");

        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);

        //--------------Visa manual
        manual = new CreditCardData();
        manual.setNumber("4761739001010119");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCvn("123");
        manual.setCardPresent(true);
        manual.setReaderPresent(true);

        //        //--------------Amex manual
//        manual.setValue("M372700699251018=12250?");
//        //--------------MasterCard manual
//        manual.setValue("M5413330089020029=2512?");
//        //--------------MasterCard manual
//        manual.setValue("M6210948000000029=2512?");

    }

    //Cash-Point Debit Purchase Request Message
    //need to check on previous transaction and response
    @Test
    public void test_1301_Concentrator_reversal_DebitPurchase() throws ApiException {

        track=GnapTestCards.VisaTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber()).optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
        gnapMessageHeader.setMessageSubType(MessageSubType.ConcentratorReversal);

        Transaction timeoutResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }


    //Financial Transaction Timeout Reversal Message
    @Test
    public void test_1302_FinancialTransaction_TimeoutReversalMessage() throws ApiException {

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .cardType(CardType.Credit)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber()).optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        Transaction timeoutResponse= track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }

    //Online Refund VOID Timeout Reversals

    //FULL Online Refund VOID Timeout Request Message Example (Visa)
    @Test
    public void test_1601_VisaMSR_FULLOnlineRefundVOIDTimeoutRequest() throws ApiException {

        track = GnapTestCards.VisaTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber()).optionalData(optionalData)
                .optionalData(optionalData).build();

        gnapData.setGnapProdSubFids(prodSubFids);

        Transaction response = track.refund(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction preResponse = response.voidTransaction(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.ConcentratorReversal);
        Transaction timeoutResponse = response.reverse(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }

    //Partial Online Refund VOID Timeout Request Message Example (Visa)
    @Test
    public void test_1602_VisaMSR_PartialOnlineRefundVOIDTimeoutRequest() throws ApiException {

        track = GnapTestCards.VisaTrack2();

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

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(9));
        Transaction preResponse = response.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(9));
        Transaction timeoutResponse = response.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void DE_MSR_PA_03_TO() throws ApiException {
        track=GnapTestCards.testCard15_MSR();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
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

        Transaction timeoutResponse = track.authorize(new BigDecimal(12.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void DE_MNL_PA_03_TO() throws ApiException {
        manual=GnapTestCards.testCard15_MNL();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.authorize(new BigDecimal(12.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MSR_PA_04_TO() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
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

        Transaction timeoutResponse = track.authorize(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MNL_PA_04_TO() throws ApiException {
        manual=GnapTestCards.testCard1_MNL();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.authorize(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MSR_PA_06_TO() throws ApiException {
        track=GnapTestCards.testCard6_MSR();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
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

        Transaction timeoutResponse = track.authorize(new BigDecimal(60))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MNL_PA_06_TO() throws ApiException {
        manual=GnapTestCards.testCard6_MNL();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.authorize(new BigDecimal(60))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MSR_PC_08_TO() throws ApiException {
        track= GnapTestCards.testCard6_MSR();
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

        Transaction preAuthResponse = track.authorize(new BigDecimal(60))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction timeoutResponse = preAuthResponse.preAuthCompletion(new BigDecimal(32))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MNL_PC_08_TO() throws ApiException {
        manual= GnapTestCards.testCard6_MNL();
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

        Transaction preAuthResponse = manual.authorize(new BigDecimal(60))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction timeoutResponse = preAuthResponse.preAuthCompletion(new BigDecimal(60))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MSR_PA_09_TO() throws ApiException {
        track=GnapTestCards.testCard13_MSR();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
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

        Transaction timeoutResponse = track.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MNL_PA_09_TO() throws ApiException {
        manual=GnapTestCards.testCard13_MNL();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MSR_SC_10_TO() throws ApiException {

        track= GnapTestCards.testCard13_MSR();
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

        Transaction response = track.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction timeoutResponse = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MNL_SC_10_TO() throws ApiException {

        manual= GnapTestCards.testCard13_MNL();
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

        Transaction response = manual.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preResponse = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    @Test
    public void VI_MSR_SA_01_TO() throws ApiException {

        track=GnapTestCards.testCard2_MSR();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = track.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MSR_RP_02_TO() throws ApiException {

        track=GnapTestCards.testCard1_MSR();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);

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

        Transaction timeoutResponse = track.charge(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MSR_RV_03_TO() throws ApiException {

        track = GnapTestCards.testCard2_MSR();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber()).optionalData(optionalData)
                .optionalData(optionalData).build();

        gnapData.setGnapProdSubFids(prodSubFids);

        Transaction response = track.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        Transaction timeoutResponse = response.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MSR_SV_04_TO() throws ApiException {
        track= GnapTestCards.testCard2_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

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

        Transaction preResponse = track.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction timeoutResponse = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void DE_MNL_SA_03_TO() throws ApiException {
        manual=GnapTestCards.testCard15_MNL();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

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

        Transaction timeoutResponse = manual.charge(new BigDecimal(12.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void DE_MSR_SA_03_TO() throws ApiException {
        track=GnapTestCards.testCard15_MSR();

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

        Transaction timeoutResponse = track.charge(new BigDecimal(12.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MNL_RE_04_TO() throws ApiException {

        manual=GnapTestCards.testCard1_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction timeoutResponse = manual.refund(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void VI_MSR_RE_04_TO() throws ApiException {

        track=GnapTestCards.testCard1_MSR();

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

        Transaction timeoutResponse = track.refund(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void VI_MNL_SA_05_TO() throws ApiException {

        manual=GnapTestCards.testCard1_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.charge(new BigDecimal(12.05))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MSR_SA_05_TO() throws ApiException {

        track=GnapTestCards.testCard1_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = track.charge(new BigDecimal(12.05))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MNL_SA_06_TO() throws ApiException {

        manual =GnapTestCards.testCard6_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.charge(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MSR_SA_06_TO() throws ApiException {

        track =GnapTestCards.testCard6_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = track.charge(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MNL_SV_08_TO() throws ApiException {
        manual=GnapTestCards.testCard6_MNL();
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

        Transaction preResponse = manual.charge(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);

        Transaction timeoutResponse = preResponse.voidTransaction(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void MC_MSR_SV_08_TO() throws ApiException {
        track=GnapTestCards.testCard6_MSR();
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

        Transaction preResponse = track.charge(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);

        Transaction timeoutResponse = preResponse.voidTransaction(new BigDecimal(12.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MNL_SA_09_TO() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MSR_SA_09_TO() throws ApiException {

        track=GnapTestCards.testCard13_MSR();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction timeoutResponse = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MNL_SV_11_TO() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        optionalData.setEmployeeID("03");
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setInvoiceNumber("V100012");
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());


        Transaction timeoutResponse = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void AX_MSR_SV_11_TO() throws ApiException {

        track=GnapTestCards.testCard13_MSR();
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        optionalData.setEmployeeID("03");
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setInvoiceNumber("V100012");
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());


        Transaction timeoutResponse = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_MNL_PA_13_TO() throws ApiException {
        manual=GnapTestCards.testCard1_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction timeoutResponse = manual.authorize(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void VI_MSR_PA_13_TO() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction timeoutResponse = track.authorize(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void VI_MNL_PC_14_TO() throws ApiException {
        manual=GnapTestCards.testCard1_MNL();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = manual.authorize(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction timeoutResponse = response.preAuthCompletion(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void VI_MSR_PC_14_TO() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction timeoutResponse = response.preAuthCompletion(new BigDecimal(12.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void DE_CT_PA_11_TO() throws ApiException {
        trackData=GnapTestCards.interacEMVTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .accountType(AccountType.Savings)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction timeoutResponse = trackData.authorize(new BigDecimal(12.11))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void DE_CT_PC_12_TO() throws ApiException {
        trackData= GnapTestCards.interacEMVTrack();
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .accountType(AccountType.Savings)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction preAuthResponse = trackData.authorize(new BigDecimal(12.11))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        //testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalStoreAndForwardTransaction);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction timeoutResponse = preAuthResponse.preAuthCompletion(new BigDecimal(12.11))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_CT_PA_04_TO() throws ApiException {
        trackData=GnapTestCards.interacEMVTrack();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
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

        Transaction timeoutResponse = trackData.authorize(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();
        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void DE_CT_SA_01_TO() throws ApiException {
        trackData=GnapTestCards.interacEMVTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .accountType(AccountType.Savings)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction timeoutResponse = trackData.charge(new BigDecimal(12.01))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }
    @Test
    public void VI_CT_RE_04_TO() throws ApiException {

        track=GnapTestCards.visaEmvTrack();

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

        Transaction timeoutResponse = track.refund(new BigDecimal(12.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);

    }
    @Test
    public void MC_CT_SA_07_TO() throws ApiException {
        track=GnapTestCards.visaEmvTrack();
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .accountType(AccountType.Savings)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction timeoutResponse = track.charge(new BigDecimal(12.01))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(timeoutResponse);
    }


}
