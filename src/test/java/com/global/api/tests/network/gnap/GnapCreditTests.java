package com.global.api.tests.network.gnap;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.network.entities.gnap.*;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.network.enums.gnap.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.testdata.GnapTestCards;
import org.junit.Test;
import com.global.api.entities.enums.AccountType;

import java.math.BigDecimal;

public class GnapCreditTests {
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    private GnapPosDetails posData;
    private TestUtil testUtil = TestUtil.getInstance();
    private CreditTrackData track;
    private CreditCardData manual;
    private Address billingAdd;

    public GnapCreditTests() throws ConfigurationException {

        billingAdd=new Address("A1520 MAIN","YM5X1B1");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setEmvCapable(true);
        //acceptorConfig.setPinPadSerialNumber("SERIAL02");
        //acceptorConfig.setDeviceType("9.");

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

//Credit Card Swiped Financial Transaction Request Message
    @Test
    public void test_1101_CreditCardSwipedFinancialPurchase() throws ApiException {

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

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Credit Card Manual Entry Financial Transaction Request Message
    @Test
    public void test_1102_CreditCardManualEntryFinancialPurchase() throws ApiException {

        acceptorConfig.setPinCapability(PINCapability.NoPINEntryCapability);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English).posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(new BigDecimal(3))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Credit Card Manual Entry Financial Transaction Request Message with CVV
    @Test
    public void test_1103_CreditCardManualEntryFinancialPurchaseWithCVV() throws ApiException {

        acceptorConfig.setPinCapability(PINCapability.NoPINEntryCapability);
        posData.setCardholderIDMethod(CardHolderIDMethod.PIN);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .cardPresenceIndicator(CardIdentifierPresenceIndicator.CVDORCIDValueIsPresent)
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English).posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(BigDecimal.valueOf(1.35))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1106_VisaMSR_PreAuthorizationCancellation() throws ApiException {

        track = GnapTestCards.VisaTrack2();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.SelfServiceTerminal);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preAuthResponse = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);
    }

    //    Mastercard Zero Dollar Pre-Authorization Completion
    @Test
    public void test_1107_MastercardZeroDollarPreAuthorizationCompletion() throws ApiException {
        track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.ElectronicCashRegisterInterfaceIntegrated)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(5))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(0))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Credit Purchase Void
    @Test
    public void AX_MSR_SV_14() throws ApiException {

        track=GnapTestCards.testCard13_MSR();
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

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        gnapData.setInvoiceNumber("V100012");
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());


        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Credit Purchase Void Manual
    @Test
    public void test_1115_Manual_CashPointCreditPurchaseVoidTransaction() throws ApiException {

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction preResponse = manual.charge(new BigDecimal(3))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapData.setInvoiceNumber("V100012");
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        optionalData.setEmployeeID("03");
        gnapData.setOptionalData(optionalData);

        Transaction response = preResponse.voidTransaction(new BigDecimal(3))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Credit Merchandise Return Void MSR
    @Test
    public void MC_MSR_RV_08() throws ApiException {

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

        Transaction preResponse = track.refund(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Credit Merchandise Return Void Manual
    @Test
    public void VI_MNL_RV11() throws ApiException {

        manual =GnapTestCards.testCard2_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData)
                .build();

        Transaction preResponse = manual.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Credit Merchandise Return
    @Test
    public void VI_MSR_RE_09() throws ApiException {

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

        Transaction response = track.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

    }

    //    Cash-Point Request Message - Credit Merchandise Return
    @Test
    public void MC_MNL_RE_06() throws ApiException {

        manual =GnapTestCards.testCard6_MNL();

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

        Transaction response = manual.refund(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //    EMV Request Message - Credit Online Purchase
    @Test
    public void test_1119_EMVOnlinePurchaseFallback() throws ApiException {
        track.setEntryMethod(EntryMethod.TechnicalFallback);

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.charge(new BigDecimal(1.35))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Telephone Authorized Purchase
    @Test
    public void VI_RP_01() throws ApiException {

        manual.setNumber("4501161107217214");
        manual.setExpYear(2025);
        manual.setExpMonth(07);

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .approvalCode("123456")
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(new BigDecimal(6.01))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    // Fully approved transaction with Ledger balance passed back from issuer
    @Test
    public void test_1501_MCMSR_LedgerBalancePassedPartialAuth() throws ApiException {
         track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids gnapProdSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(2))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    //Fully approved transaction with Ledger balance and available balance pass back from issuer
    @Test
    public void test_1502_MCMSR_LedgerAndAvailableBalancePassedPartialAuth() throws ApiException {
         track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids gnapProdSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(2))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    //Partially approved transaction with Original amount pass back from issuer

    @Test
    public void test_1503_MCMSR_PartialApproveWithriginalAmount() throws ApiException {
         track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids gnapProdSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(9.99))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    //Partially approved transaction with Original amount and Ledger Balance pass back from issuer
    @Test
    public void test_1504_MCMSR_PartialApproveWithOriginalAndLedgerBalance() throws ApiException {
         track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids gnapProdSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(9.99))
                .withGnapRequestData(gnapData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    /*   Partially approved transaction with Original amount and Ledger and Available Balances pass back from issuer
    (only Original amount and Ledger balance is pass back to terminal)*/
    @Test
    public void test_1505_MCMSR_PartialApprovalWithOnlyOriginalAmountAndLedgerBalancePassedBack() throws ApiException {
         track = GnapTestCards.MCTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids gnapProdSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .languageCode(LanguageCode.English)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(9.99))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Visa Online Card Present Manually Keyed Refund Request
    @Test
    public void test_1601_VisaManual_KeyedOnlineRefund() throws ApiException {

        manual = GnapTestCards.visaManualCard();

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

        Transaction preResponse = manual.refund(new BigDecimal(4))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    @Test
    public void test_1601_VisaMSR_KeyedOnlineRefund() throws ApiException {

        track = GnapTestCards.VisaTrack2();

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

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    // MC Online Card Present Manually Keyed Refund Request
    @Test
    public void test_1602_MCManual_KeyedOnlineRefund() throws ApiException {

        manual = GnapTestCards.MCManualCard();

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

        Transaction preResponse = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    // Discover Online Card Present Manually Keyed Refund Request
    @Test
    public void test_1603_DiscoverManual_KeyedOnlineRefund() throws ApiException {

        manual = GnapTestCards.discoverManualCard();

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

        Transaction preResponse = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Contact MSR Card Present Online Refund FULL Amount VOID (Visa)
    @Test
    public void test_1607_VisaMSR_ContactMSRCardPresentOnlineRefundFULLAmountVOID() throws ApiException {
        track = GnapTestCards.VisaTrack2();

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

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
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

    //Contact Manual Card Present Online Refund FULL Amount VOID (Visa)
    @Test
    public void test_1607_VisaManual_ContactManualCardPresentOnlineRefundFULLAmountVOID() throws ApiException {

        manual = GnapTestCards.visaManualCard();

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

        Transaction response = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
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

    //Contact MSR Card Present Online Refund FULL Amount VOID (Visa)
    @Test
    public void test_1608_VisaMSR_OnlineRefundPartialAmountVoid() throws ApiException {

        track = GnapTestCards.VisaTrack2();

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

    }

    //Contact Manual Card Present Online Refund Partial Amount VOID (Visa)
    @Test
    public void test_1608_VisaManual_OnlineRefundPartialAmountVoid() throws ApiException {

        manual = GnapTestCards.visaManualCard();

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

        Transaction response = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        response.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(9));

        Transaction preResponse = response.voidTransaction(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Visa MSR Online Refund Request
    @Test
    public void test_1611_VisaMSR_OnlineRefundRequest() throws ApiException {
        track = GnapTestCards.VisaTrack2();

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

        Transaction preResponse = track.refund(new BigDecimal(2))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Online Purchase Full void Request Message
    @Test
    public void MC_MSR_SV_08() throws ApiException {
        track=GnapTestCards.testCard6_MSR();
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

        Transaction preResponse = track.charge(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Manually Keyed Online Purchase Full void Request Message
    @Test
    public void test_1702_Manual_OnlinePurchaseVoidTransaction() throws ApiException {

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

        Transaction preResponse = manual.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Manually Keyed Online Purchase Request Message - Discover
    @Test
    public void DI_MNL_SA_15() throws ApiException {
        manual=GnapTestCards.testCard15_MNL();
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

        Transaction preResponse = manual.charge(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    //Manually Keyed Online Purchase Full void Request Message - Discover
    @Test
    public void DI_MSR_SV_16() throws ApiException {

        track=GnapTestCards.testCard15_MSR();

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = track.charge(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Manually Keyed Partial void Request Message - Visa
    @Test
    public void test_1707_ManuallyKeyedPartialVoidTransaction() throws ApiException {

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = manual.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        preResponse.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(0.9));
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    // Online Purchase Partial void Request Message - Discover
    @Test
    public void test_1708_Discover_ManualOnlinePurchasePartialVoid() throws ApiException {

        manual = GnapTestCards.discoverManualCard();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = manual.charge(new BigDecimal(0.9))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        preResponse.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(0.9));
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Full Online Purchase void Timeout Reversal Request Message
    @Test
    public void MC_MSR_SV_08_TO() throws ApiException {
        track = GnapTestCards.testCard6_MSR();

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = track.charge(new BigDecimal(12.08))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.reverse(new BigDecimal(12.08))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Partial Online Purchase void Timeout Reversal Request Message
    @Test
    public void test_1711_PartialOnlinePurchaseVoidTimeoutReversal() throws ApiException {

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

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        preResponse.getTransactionReference().setOriginalApprovedAmount(BigDecimal.valueOf(0.9));
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //working
    @Test
    public void test_1801_UnionPayMSR_Purchase() throws ApiException {
        track = GnapTestCards.UnionPayTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //working
    @Test
    public void test_1801_UnionPayManual_Purchase() throws ApiException {
        manual = GnapTestCards.UnionPayManual();

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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }


    @Test
    public void test_1802_UnionPayMSRPurchase_Cancellation() throws ApiException {

        track = GnapTestCards.UnionPayTrack2();

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        optionalData.setEmployeeID("03");
        gnapData.setOptionalData(optionalData);

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapData.setInvoiceNumber("V100012");
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1803_UnionPayMSR_AuthorizationPurchase() throws ApiException {
        track = GnapTestCards.UnionPayTrack2();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.SelfServiceTerminal);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(5))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1804_UnionPayMSR_PreAuthorizationCompletion() throws ApiException {

        track = GnapTestCards.UnionPayTrack2();

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData).build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(45))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(4))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1805_UnionPayMSR_PreAuthorizationCancellation() throws ApiException {

        track = GnapTestCards.UnionPayTrack2();

        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.SelfServiceTerminal);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
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

        Transaction preAuthResponse = track.authorize(new BigDecimal(4))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }


    @Test
    public void test_1806_UnionPay_ManualRefund() throws ApiException {

        manual = GnapTestCards.UnionPayManual();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    @Test
    public void test_JcbManual_Purchase() throws ApiException {
        manual = GnapTestCards.JCBManual();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English).posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(new BigDecimal(3))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_JcbMSR_Purchase() throws ApiException {
        track=GnapTestCards.JCBTrack2();

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

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_JcbMSR_Preauthorization() throws ApiException {
        track=GnapTestCards.JCBTrack2();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.SelfServiceTerminal);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.ElectronicCashRegisterInterfaceIntegrated)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_JcbMSR_PreauthCompletion() throws ApiException{
        track=GnapTestCards.JCBTrack2();

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.SelfServiceTerminal);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.ElectronicCashRegisterInterfaceIntegrated)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preAuthResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_JcbMSR_CreditPurchaseVoid() throws ApiException {
        track=GnapTestCards.JCBTrack2();

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        optionalData.setEmployeeID("03");
        gnapData.setOptionalData(optionalData);

        Transaction preResponse = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setInvoiceNumber("V100012");
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_JcbMSR_Return() throws ApiException {
        track=GnapTestCards.JCBTrack2();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .invoiceNumber("R10032")
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .build();

        optionalData.setBase24TransactionModifier(Base24TransactionModifier.VoidTransactions);
        gnapData.setOptionalData(optionalData);

        Transaction response = track.refund(new BigDecimal(100))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void MC_MNL_SA_06() throws ApiException {

        manual =GnapTestCards.testCard6_MNL();

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

        Transaction response = manual.charge(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void VI_MSR_SA_09() throws ApiException {

        track=GnapTestCards.testCard2_MSR();

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
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void VI_MNL_SV_11() throws ApiException {
        manual = GnapTestCards.testCard2_MNL();

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

        Transaction preResponse = track.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void AX_MNL_SA_12() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();
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

        Transaction response = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void DI_MNL_RE_15() throws ApiException {

        manual=GnapTestCards.testCard15_MNL();

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

        Transaction response = manual.refund(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void DI_MSR_RV_16() throws ApiException {

        track=GnapTestCards.testCard15_MSR();

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

        Transaction preResponse = track.refund(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }


    @Test
    public void AX_MSR_RV_14() throws ApiException {

        track=GnapTestCards.testCard13_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = track.refund(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void AX_MNL_RE_12() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = manual.refund(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void MC_MSR_SA_06() throws ApiException {

        track =GnapTestCards.testCard6_MSR();

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

        Transaction response = track.charge(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MNL_SV_08() throws ApiException {
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

        Transaction preResponse = manual.charge(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_SA_09() throws ApiException {

        manual=GnapTestCards.testCard2_MNL();

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

        Transaction response = manual.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MSR_SV_11() throws ApiException {
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
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void AX_MSR_SA_12() throws ApiException {

        track=GnapTestCards.testCard13_MSR();
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

        Transaction response = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MNL_SV_14() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();
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

        Transaction preResponse = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        gnapData.setInvoiceNumber("V100012");
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());


        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MSR_SA_15() throws ApiException {
        track=GnapTestCards.testCard15_MSR();

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

        Transaction preResponse = track.charge(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }
    @Test
    public void DI_MNL_SV_16() throws ApiException {

        manual=GnapTestCards.testCard15_MNL();
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = manual.charge(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MSR_RE_06() throws ApiException {

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MNL_RV_08() throws ApiException {

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

        Transaction preResponse = manual.refund(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.06))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_RE_09() throws ApiException {

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

        Transaction response = manual.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);

    }
    @Test
    public void VI_MSR_RV11() throws ApiException {

        track=GnapTestCards.testCard2_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData)
                .build();

        Transaction preResponse = track.refund(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MSR_RE_12() throws ApiException {

        track=GnapTestCards.testCard13_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MNL_RV_14() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .approvalCode("12501R")
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = manual.refund(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MSR_RE_15() throws ApiException {

        track=GnapTestCards.testCard15_MSR();

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

        Transaction response = track.refund(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MNL_RV_16() throws ApiException {

        manual=GnapTestCards.testCard15_MNL();

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

        Transaction preResponse = manual.refund(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setOptionalData(optionalData);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction response = preResponse.voidTransaction(new BigDecimal(2.15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //--------------------------------------Pump Transactions----------------------------------------
    //Note:POS condition code always 27 for pay at pump transactions.
    @Test
    public void DI_MNL_PA_05() throws ApiException {

        manual=GnapTestCards.testCard15_MNL();

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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MSR_PA_05() throws ApiException {

        track=GnapTestCards.testCard15_MSR();

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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MNL_PC_06() throws ApiException {
        manual= GnapTestCards.testCard15_MNL();

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

        Transaction preAuthResponse = manual.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DI_MSR_PC_06() throws ApiException {
        track= GnapTestCards.testCard15_MSR();

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

        Transaction preAuthResponse = track.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_PA_07() throws ApiException {
        manual=GnapTestCards.testCard1_MNL();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.authorize(new BigDecimal(40.20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MSR_PA_07() throws ApiException {
        track=GnapTestCards.testCard1_MSR();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(40.20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_PC_08() throws ApiException {
        manual= GnapTestCards.testCard1_MNL();
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

        Transaction preAuthResponse = manual.authorize(new BigDecimal(40.20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(40))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MSR_PC_08() throws ApiException {
        track= GnapTestCards.testCard1_MSR();
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

        Transaction preAuthResponse = track.authorize(new BigDecimal(40.20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(40))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MNL_PA_09() throws ApiException {
        manual=GnapTestCards.testCard5_MNL();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MSR_PA_09() throws ApiException {
        track=GnapTestCards.testCard5_MSR();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MNL_PC_10() throws ApiException {
        manual= GnapTestCards.testCard5_MNL();
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

        Transaction preAuthResponse = manual.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MSR_PC_10() throws ApiException {
        track= GnapTestCards.testCard5_MSR();
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

        Transaction preAuthResponse = track.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthResponse);

        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setOptionalData(optionalData);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MNL_PA_11() throws ApiException {
        manual=GnapTestCards.testCard13_MNL();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MSR_PA_11() throws ApiException {
        track=GnapTestCards.testCard13_MSR();
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MNL_PC_13() throws ApiException {

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
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preResponse = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }
    @Test
    public void AX_MSR_PC_13() throws ApiException {

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
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction preResponse = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }
    //--------------------Pump end------------------------------------------
    @Test
    public void MC_RP_02() throws ApiException {

        manual.setNumber("5194419000000007");
        manual.setExpYear(2025);
        manual.setExpMonth(07);

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .approvalCode("78945RT")
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = manual.charge(new BigDecimal(6.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_RP_03() throws ApiException {

        manual=GnapTestCards.testCard1_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction response = manual.charge(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MSR_RP_03() throws ApiException {

        track=GnapTestCards.testCard1_MSR();

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
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MNL_RP_04() throws ApiException {

        manual=GnapTestCards.testCard6_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction response = manual.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_MSR_RP_04() throws ApiException {

        track=GnapTestCards.testCard6_MSR();

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

        Transaction response = track.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DC_MNL_RP_05() throws ApiException {

        manual=GnapTestCards.testCard15_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction response = manual.charge(new BigDecimal(180))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void DC_MSR_RP_05() throws ApiException {

        track=GnapTestCards.testCard15_MSR();

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

        Transaction response = track.charge(new BigDecimal(180))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MNL_RP_06() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction response = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MSR_RP_06() throws ApiException {

        track=GnapTestCards.testCard13_MSR();

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

        Transaction response = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MNL_RPV_07() throws ApiException {

        manual=GnapTestCards.testCard1_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction preResponse = manual.charge(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void VI_MSR_RPV_07() throws ApiException {

        track=GnapTestCards.testCard1_MSR();

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
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(6.03))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void MC_MNL_RPV_08() throws ApiException {

        manual=GnapTestCards.testCard6_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction preResponse = manual.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void MC_MSR_RPV_08() throws ApiException {

        track=GnapTestCards.testCard6_MSR();

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

        Transaction preResponse = track.charge(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void AX_MNL_RPV_09() throws ApiException {

        manual=GnapTestCards.testCard13_MNL();

        gnapMessageHeader.setTransactionCode(TransactionCode.TelephoneAuthPurchase);
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
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

        Transaction preResponse = manual.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void AX_MSR_RPV_09() throws ApiException {

        track=GnapTestCards.testCard13_MSR();

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

        Transaction preResponse = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        optionalData.setEmployeeID("03");
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preResponse.voidTransaction(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);


    }
    @Test
    public void VI_SA_01() throws ApiException {

        manual.setNumber("4761739001010119");
        manual.setExpYear(2019);
        manual.setExpMonth(12);

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

        Transaction response = manual.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void MC_SA_02() throws ApiException {

        manual.setNumber("5413330089604111");
        manual.setExpYear(2019);
        manual.setExpMonth(12);

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

        Transaction response = manual.charge(new BigDecimal(2.09))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }
    @Test
    public void AX_MSR_SV_08_TO() throws ApiException {
        track = GnapTestCards.testCard13_MSR();

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData).build();

        Transaction preResponse = track.charge(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.TimeoutReversalOnlineTransaction);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));

        Transaction response = preResponse.reverse(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }




    //Pre-Auth & completion

    @Test
    public void DI_MSR_PA_01() throws ApiException {
        track=GnapTestCards.testCard15_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void DI_MSR_PC_03() throws ApiException {
        track=GnapTestCards.testCard15_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void DI_MNL_PA_01() throws ApiException {
        manual=GnapTestCards.testCard15_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void DI_MNL_PC_03() throws ApiException {
        manual=GnapTestCards.testCard15_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(17))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MSR_PA_04() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void VI_MSR_PC_05() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MSR_PCV_06() throws ApiException {
        track =GnapTestCards.testCard1_MSR();

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

        Transaction preResponse = track.refund(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    // pre-auth full void
    @Test
    public void VI_MSR_PCV_07() throws ApiException {
        track=GnapTestCards.testCard1_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MNL_PA_04() throws ApiException {
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

        Transaction response = manual.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void VI_MNL_PC_05() throws ApiException {
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

        Transaction response = manual.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MNL_PCV_06() throws ApiException {
        manual =GnapTestCards.testCard1_MNL();

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

        Transaction preResponse = manual.refund(new BigDecimal(4.0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    // pre-auth full void
    @Test
    public void VI_MNL_PCV_07() throws ApiException {
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

        Transaction response = manual.authorize(new BigDecimal(4.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void MC_MSR_PA_08() throws ApiException {
        track=GnapTestCards.testCard5_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    //pre-auth full void
    @Test
    public void MC_MSR_PV_09() throws ApiException {
        track=GnapTestCards.testCard5_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void MC_MSR_PC_14() throws ApiException {
        track=GnapTestCards.testCard6_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void MC_MSR_PCV_15() throws ApiException {
        track =GnapTestCards.testCard5_MSR();

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

        Transaction preResponse = track.refund(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }


    @Test
    public void MC_MNL_PA_08() throws ApiException {
        manual=GnapTestCards.testCard5_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    //pre-auth full void
    @Test
    public void MC_MNL_PV_09() throws ApiException {
        manual=GnapTestCards.testCard5_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(20))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(0))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void MC_MNL_PC_14() throws ApiException {
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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = manual.authorize(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void MC_MNL_PCV_15() throws ApiException {
        manual =GnapTestCards.testCard5_MNL();

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

        Transaction preResponse = manual.refund(new BigDecimal(4.13))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }


    @Test
    public void AX_MSR_PA_16() throws ApiException {
        track=GnapTestCards.testCard13_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void AX_MSR_PC_18() throws ApiException {
        track=GnapTestCards.testCard13_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void AX_MNL_PA_16() throws ApiException {
        manual=GnapTestCards.testCard13_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void AX_MNL_PC_18() throws ApiException {
        manual=GnapTestCards.testCard13_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(39))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MSR_PA_19() throws ApiException {
        track=GnapTestCards.testCard2_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void VI_MSR_PC_21() throws ApiException {
        track=GnapTestCards.testCard2_MSR();

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
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MNL_PA_19() throws ApiException {
        manual=GnapTestCards.testCard2_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }

    @Test
    public void VI_MNL_PC_21() throws ApiException {
        manual=GnapTestCards.testCard2_MNL();

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

        Transaction response = manual.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);

        Transaction preAuthCom = response.preAuthCompletion(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preAuthCom);

    }

    @Test
    public void VI_MSR_SA() throws ApiException {
        track = GnapTestCards.testCard2_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        optionalData.setTerminalType(TerminalType.StandAloneOrSemiIntegratedSolutions);
        optionalData.setIntegratedHardwareList(IntegratedHardwareList.Unknown);
        optionalData.setTerminalSoftwareVersion("nnnn");
        optionalData.setPinPadModel(PinPadModel.PINPad810);
        optionalData.setTerminalSerialNumber("000JA123456");
        optionalData.setPinPadOSVersion("011A0");
        optionalData.setPinPadSerialNumber("SERIAL02");
        optionalData.setPinPadSoftwareVersion("523e");
        optionalData.setTerminalSoftwareVersion("");

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.charge(new BigDecimal(5))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);
    }
    @Test
    public void VI_MSR_PA() throws ApiException {
        track=GnapTestCards.testCard2_MSR();

        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        optionalData.setTerminalType(TerminalType.StandAloneOrSemiIntegratedSolutions);
        optionalData.setIntegratedHardwareList(IntegratedHardwareList.Unknown);
        optionalData.setTerminalSoftwareVersion("nnnn");
        optionalData.setPinPadModel(PinPadModel.PINPad810);
        optionalData.setTerminalSerialNumber("000JA123456");
        optionalData.setPinPadOSVersion("011A0");
        optionalData.setPinPadSerialNumber("SERIAL02");
        optionalData.setPinPadSoftwareVersion("523e");
        optionalData.setTerminalSoftwareVersion("");

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.authorize(new BigDecimal(19))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

    }


}