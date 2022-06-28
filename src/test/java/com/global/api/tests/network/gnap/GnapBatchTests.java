package com.global.api.tests.network.gnap;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.network.entities.gnap.*;
import com.global.api.network.enums.gnap.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.GnapTestCards;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

public class GnapBatchTests {

    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private GnapMessageHeader gnapMessageHeader;
    private OptionalData optionalData;
    private GnapPosDetails posData;
    private TestUtil testUtil = TestUtil.getInstance();
    private GnapBatchTotal batchTotal;

    public GnapBatchTests() throws ConfigurationException {

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

        batchTotal=new GnapBatchTotal();
        batchTotal.setSignTotalSale(BatchTotalSign.Positive);
        batchTotal.setSignTotalRefund(BatchTotalSign.Positive);
        batchTotal.setSignTotalAdjustment(BatchTotalSign.Negative);


    }

    //Sample Online Refund Settlement Totals Scenarios
    @Test
    public void test_1601_OnlineRefundSettlementTotalsScenarios() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);

        batchTotal.setTotalSalesTransaction(10);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(8));
        batchTotal.setTotalRefundTransaction(4);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(12));
        batchTotal.setTotalAdjustmentTransaction(4);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(10));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    //Purchase & Purchase Void
    @Test
    public void ST_PVT() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);

        batchTotal.setTotalSalesTransaction(8);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(90));
        batchTotal.setTotalRefundTransaction(0);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(0));
        batchTotal.setTotalAdjustmentTransaction(8);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(90));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    //Return & Return Void
    @Test
    public void ST_RVT() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);

        batchTotal.setTotalSalesTransaction(0);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(0));
        batchTotal.setTotalRefundTransaction(8);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(90));
        batchTotal.setTotalAdjustmentTransaction(8);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(90));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    //pre auth & pre completion
    @Test
    public void ST_PCVT() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);

        batchTotal.setTotalSalesTransaction(8);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(166.3));
        batchTotal.setTotalRefundTransaction(6);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(16.3));
        batchTotal.setTotalAdjustmentTransaction(4);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(48.04));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    //pump pre auth & pre completion
    @Test
    public void ST_PCDT() throws ApiException {
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);

        batchTotal.setTotalSalesTransaction(8);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(228));
        batchTotal.setTotalRefundTransaction(0);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(0));
        batchTotal.setTotalAdjustmentTransaction(0);
        batchTotal.setSignTotalAdjustment(BatchTotalSign.Positive);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(0));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }


    @Test
    public void Zero_Amount_Settlement() throws ApiException, InterruptedException {
        Transaction sale1=Sale(BigDecimal.valueOf(60), GnapTestCards.testCard1_MSR());
        testUtil.assertSuccess(sale1);
        Transaction return1=Return(BigDecimal.valueOf(20), GnapTestCards.testCard6_MSR());
        testUtil.assertSuccess(return1);
        Transaction return2=Return(BigDecimal.valueOf(60), GnapTestCards.testCard5_MSR());
        testUtil.assertSuccess(return1);
        Transaction returnVoid=Void(BigDecimal.valueOf(60),BigDecimal.valueOf(40),return2);
        testUtil.assertSuccess(returnVoid);
        Transaction sale2=Sale(BigDecimal.valueOf(39), GnapTestCards.testCard13_MSR());
        testUtil.assertSuccess(sale2);
        Transaction saleVoid=Void(BigDecimal.valueOf(39),BigDecimal.valueOf(30),sale2);
        testUtil.assertSuccess(saleVoid);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));

        GnapBatchTotal batchTotal=new GnapBatchTotal();
        batchTotal.setTotalSalesTransaction(3);
        batchTotal.setSignTotalSale(BatchTotalSign.Positive);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(129));
        batchTotal.setTotalRefundTransaction(3);
        batchTotal.setSignTotalRefund(BatchTotalSign.Positive);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(120));
        batchTotal.setTotalAdjustmentTransaction(2);
        batchTotal.setSignTotalAdjustment(BatchTotalSign.Negative);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(21));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    @Test
    public void Negative_Amount_Settlement() throws ApiException, InterruptedException {
        Transaction sale=Sale(BigDecimal.valueOf(20), GnapTestCards.testCard2_MSR());
        testUtil.assertSuccess(sale);
        Transaction return1=Return(BigDecimal.valueOf(40), GnapTestCards.testCard1_MSR());
        testUtil.assertSuccess(return1);
        Transaction preAuth=PreAuth(BigDecimal.valueOf(17), GnapTestCards.testCard15_MSR());
        testUtil.assertSuccess(preAuth);
        Transaction preAuthComp=PreAuthComp(BigDecimal.valueOf(17),preAuth);
        testUtil.assertSuccess(preAuthComp);
        Transaction return2=Return(BigDecimal.valueOf(34), GnapTestCards.testCard12_MSR());
        testUtil.assertSuccess(return2);
        Transaction refundVoid=Void(BigDecimal.valueOf(40),BigDecimal.valueOf(40),return1);
        testUtil.assertSuccess(refundVoid);

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));

        GnapBatchTotal batchTotal=new GnapBatchTotal();
        batchTotal.setTotalSalesTransaction(2);
        batchTotal.setSignTotalSale(BatchTotalSign.Positive);
        batchTotal.setTotalSaleAmount(BigDecimal.valueOf(37));
        batchTotal.setTotalRefundTransaction(3);
        batchTotal.setSignTotalRefund(BatchTotalSign.Positive);
        batchTotal.setTotalRefundAmount(BigDecimal.valueOf(74));
        batchTotal.setTotalAdjustmentTransaction(1);
        batchTotal.setSignTotalAdjustment(BatchTotalSign.Negative);
        batchTotal.setTotalAdjustmentAmount(BigDecimal.valueOf(40));

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .dayTotals(batchTotal)
                .sequenceNumber(testUtil.settlement())
                .build();

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift,gnapData).execute();

        testUtil.assertSuccess(response);
    }

    public Transaction Sale(BigDecimal amount,CreditTrackData track) throws ApiException {
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));
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

        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

       return response;
    }

    public Transaction PreAuth(BigDecimal amount,CreditTrackData track) throws ApiException {

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
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

        Transaction response = track.authorize(amount)
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();
        testUtil.assertSuccess(response);

        return  response;

    }

    public Transaction PreAuthComp(BigDecimal amount,Transaction response) throws ApiException {

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));
        gnapMessageHeader.setMessageSubType(MessageSubType.StoreAndForwardTransactions);
        posData.setCardholderIDMethod(CardHolderIDMethod.Signature);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .sequenceNumber(testUtil.getSequenceNumber())
                .posConditionCode(POSConditionCode.PreAuthCompletionRequest)
                .gnapProdSubFids(prodSubFids).build();


        Transaction preAuthCom = response.preAuthCompletion(amount)
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        return  preAuthCom;
    }

    public Transaction Return(BigDecimal amount,CreditTrackData track) throws ApiException {
        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
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

        Transaction response = track.refund(amount)
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        return response;
    }

    public Transaction Void(BigDecimal originalAmount, BigDecimal amountDebit,Transaction preResponse) throws ApiException {

        gnapMessageHeader.setTransmissionNumber(String.format("%02d", testUtil.getTransmissionNo()));
        gnapMessageHeader.setCurrentDate(DateTime.now().toString("yyMMdd"));
        gnapMessageHeader.setCurrentTime(DateTime.now().toString("hhmmss"));
        gnapMessageHeader.setMessageSubType(MessageSubType.OnlineTransactions);
        posData.setCardholderIDMethod(CardHolderIDMethod.Unknown);
        GnapProdSubFids prodSubFids = GnapProdSubFids.builder()
                .pointOfServiceData(posData)
                .build();

        GnapRequestData gnapData = GnapRequestData.builder()
                .gnapMessageHeader(gnapMessageHeader)
                .languageCode(LanguageCode.English)
                .posConditionCode(POSConditionCode.StandAloneTerminal)
                .optionalData(optionalData)
                .gnapProdSubFids(prodSubFids)
                .sequenceNumber(testUtil.getSequenceNumber())
                .build();

        if(originalAmount.compareTo(amountDebit)!=0)
        preResponse.getTransactionReference().setOriginalApprovedAmount(amountDebit);

        Transaction response = preResponse.voidTransaction(originalAmount)
                .withCurrency("USD")
                .withGnapRequestData(gnapData)
                .execute();

        return response;
    }


}
