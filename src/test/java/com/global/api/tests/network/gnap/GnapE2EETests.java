package com.global.api.tests.network.gnap;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
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

public class GnapE2EETests {

    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    //String tagData = "4f07a0000000041010500a4d61737465724361726457135413330089010434d22122019882803290000f5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a031901099b02e8009c01405f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0208409f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34033f00019f3501219f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
    private String tagData = "4f07a0000000041010500a4d617374657243617264" +
            "57134012002000060016D22122019882803290000F" +
            "5a085413330089010434820238008407a00000000410108e0a00000000000000001f00950500008080009a03" + DateTime.now().toString("yyMMdd") + "9b02e8009c01405f201a546573742f4361726420313020202020202020202020202020205f24032212315f25030401015f2a0201245f300202015f3401009f01060000000000019f02060000000006009f03060000000000009f0607a00000000410109f0702ff009f090200029f0d05b8508000009f0e0500000000009f0f05b8708098009f10120110a0800f22000065c800000000000000ff9f120a4d6173746572436172649f160f3132333435363738393031323334359f1a0201249f1c0831313232333334349f1e0831323334353637389f21030710109f26080631450565a30b759f2701809f330360f0c89f34033f00019f3501219f360200049f3704c6b1a04f9f3901059f4005f000a0b0019f4104000000869f4c0865c862608a23945a9f4e0d54657374204d65726368616e74";
    private GnapPosDetails posData;
    private TestUtil testUtil = TestUtil.getInstance();
    private CreditTrackData track;
    private CreditCardData manual;
    public GnapE2EETests() throws ConfigurationException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setEmvCapable(true);
        acceptorConfig.setPinPadSerialNumber("SERIAL02");
        acceptorConfig.setSupportsE2EEEncryption(true);
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
                .cardholderIDMethod(CardHolderIDMethod.Signature)
                .build();

        ServicesContainer.configureService(config);

        optionalData = OptionalData.builder()
                .terminalType(TerminalType.IntegratedSolutions)
                .employeeID(" ")
                .integratedHardwareList(IntegratedHardwareList.BBP)
                .pinPadComm(PinPadCommunication.RS32)
                .integratedPinpadVersionType(IntegratedPinpadVersionType.BBPOS)
                .paymentSolutionProviderCode(PaymentSolutionProviderCode.Tender_Retail)
                .base24TransactionModifier(Base24TransactionModifier.VoidTransactions)
                .integratedPinPadVersion("3031")
                .pinPadSerialNumber("00WA123456")
                .pOSVARCode("711")
                .pOSVersionNO("122334567")
                .paymentSolutionVersionNO("4218999")
                .cAPKKeyVersion("1200")
                .build();

        //-------------Visa
        track = new CreditTrackData();
        track.setValue(";4761739001010119=22122011758909689?");
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

    @Test
    public void test_1101_E2EE_CreditCardSwipedFinancialPurchase() throws ApiException {
        acceptorConfig.setSupportsE2EEEncryption(true);
        config.setAcceptorConfig(acceptorConfig);
        ServicesContainer.configureService(config);

        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();
        track.setEncryptionData(EncryptionData.add("BD00013210E00003","4D353431333333303038393031303031323D31373132313233203F0000000000"));
        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .optionalData(optionalData)
                .invoiceNumber("M100012")
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void test_1601_VisaEMV_E2EE_ChipInsertedOnlineRefund() throws ApiException {

        track=GnapTestCards.visaEmvTrack();
        track.setEncryptionData(EncryptionData.add("BD00013210E00001","344898177DE23F1BC3301C5365A5F21EE881B85D94F0BDF8"));
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
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
                .gnapProdSubFids(prodSubFids)
                .build();

        Transaction preResponse = track.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    // MC Online Card Present Manually Keyed Refund Request
    @Test
    public void test_1602_MCManual_KeyedOnlineRefund() throws ApiException {

        manual = GnapTestCards.MCManualCard();
        manual.setEncryptionData(EncryptionData.add("BD00013210E00001","02A4470117FFC16DEF044001F1C8B07248B28BC28D651570"));
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

        Transaction preResponse = manual.refund(new BigDecimal(15))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        testUtil.assertSuccess(preResponse);
    }

    @Test
    public void test_1801_UnionPayEMV_Purchase() throws ApiException {
        track=GnapTestCards.unipayEmvTrack();
        track.setEncryptionData(EncryptionData.add("BD00013210E00001","344898177DE23F1BC3301C5365A5F21EE881B85D94F0BDF8"));
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .unionPayOnlinePINDUKPTKSN("BD00013210E00001")
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .accountType(AccountType.Savings)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .sequenceNumber(testUtil.getSequenceNumber())
                .gnapProdSubFids(prodSubFids)
                .optionalData(optionalData)
                .build();

        Transaction response = track.charge(new BigDecimal(1))
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .withTagData(tagData)
                .execute();

        testUtil.assertSuccess(response);
    }

}
