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
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.testdata.GnapTestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class GnapDebitTests {
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    String tagData = "4f07a0000000041010500a4d617374657243617264" + "57134012002000060016D22122019882803290000F" + "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01005f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34030403029f3501229f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
    GnapPosDetails posData;
    DebitTrackData track ;
    TestUtil testUtil=new TestUtil();
    public GnapDebitTests() throws ConfigurationException, IOException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setEmvCapable(true);
        //acceptorConfig.setPinPadSerialNumber("SERIAL02");
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
                .terminalId("711SDKT1")
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

          track=GnapTestCards.interacEMVTrack();
    }

    // 1 EMV Request Message – Interac Debit Online Purchase
    @Test
    public void DE_CT_SA_02() throws ApiException {

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

        Transaction response = track.charge(new BigDecimal(2.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

// 2   EMV Request Message – Interac Debit Online Purchase
    @Test
    public void test_1102_InteracEMV_OnlinePurchase_Unattened() throws ApiException {
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Checking)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //EMV Request Message - Debit Pre-Authorization Purchase
    @Test
    public void DE_CT_PA_01() throws ApiException {
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);
        GnapProdSubFids prodSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response=track.authorize(new BigDecimal(40) )
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //EMV Request Message – Interac Debit Pre-Authorization Completion
    @Test
    public void DE_CT_PC_02() throws ApiException{
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.PreAuthorizationRequest);
        posData.setCardholderActivatedTerminalIndicator(CardholderActivatedTerminalIndicator.AutomatedDispensingMachineWithPIN);
        posData.setCardholderIDMethod(CardHolderIDMethod.UnattendedTerminal);

        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.UnattendedTerminalUnableToRetainCard)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData).gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction preAuthResponse=track.authorize(new BigDecimal(40) )
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();
        //testUtil.assertSuccess(preAuthResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setPosConditionCode(POSConditionCode.PreAuthCompletionRequest);
        posData.setTransactionStatusIndicator(TransactionStatusIndicator.NormalRequest);
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());

        Transaction response = preAuthResponse.preAuthCompletion(new BigDecimal(30))
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .withCurrency("USD")
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Request Message - Debit Purchase Void
    @Test
    public void DE_CT_SV_04() throws ApiException {
        GnapProdSubFids gnapProdSubFids=GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData=GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(gnapProdSubFids)
                .build();

        Transaction response=track.charge(new BigDecimal(2.02))
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .withCurrency("USD")
                .execute();

          //testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction voidResponse=response.voidTransaction(new BigDecimal(2.02))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(voidResponse);
    }

    //Debit Card Contact EMV Return Transaction Request Message
    @Test
    public void DE_CT_RE_02() throws ApiException {
        tagData= "4f07a0000000041010500a4d617374657243617264" + "57134012002000060016D22122019882803290000F" + "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01205f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34030403029f3501229f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(2.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
        }

    //Cash-Point Request Message – Interac Debit Return/Refund Void
    @Test
    public void DE_CT_RV_04() throws ApiException {
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids).build();

        Transaction response = track.refund(new BigDecimal(2.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        //testUtil.assertSuccess(response);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapData.setSequenceNumber(testUtil.getSequenceNumber());
        Transaction voidResponse=response.voidTransaction(new BigDecimal(2.04))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(voidResponse);

    }

    @Test
    public void test_1314_DebitCardContactEMVReturnTransaction() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.ElectronicCashRegisterInterfaceIntegrated)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

    //Cash-Point Debit Purchase Request Message
    //need to check on previous transaction and response
    @Test
    public void test_Concentrator_reversal_DebitPurchase() throws ApiException {
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

        Transaction preResponse = track.charge(new BigDecimal(25))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);

        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        gnapData.setInvoiceNumber("001");
        gnapData.setAccountType(AccountType.Savings);
        gnapData.setOptionalData(optionalData);

        Transaction response = preResponse.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData(tagData)
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

}
