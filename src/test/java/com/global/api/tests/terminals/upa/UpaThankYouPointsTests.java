package com.global.api.tests.terminals.upa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.upa.Entities.Enums.UpaSearchCriteria;
import com.global.api.terminals.upa.responses.UpaResponseHandler;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;


public class UpaThankYouPointsTests {
    private IDeviceInterface device;
    private static final String TEST_ECR_ID = "13";


    public UpaThankYouPointsTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.11.11"); // Update with actual test device IP
        config.setTimeout(90000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(config);
        Assertions.assertNotNull(device);
    }

    @Test
    public void TypSale_Success() throws ApiException {

        TerminalResponse response = device.sale(new BigDecimal("100"))
                .withEcrId(TEST_ECR_ID)
                .withClerkId(123)
                .execute();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Success", response.getStatus());
        Assertions.assertEquals("00", response.getDeviceResponseCode());

        UpaResponseHandler upaResponse = (UpaResponseHandler) response;
        Assertions.assertNotNull(upaResponse);
        Assertions.assertNotNull(upaResponse.getRedeemId());
        Assertions.assertNotNull(upaResponse.getRedeemStatus());
        Assertions.assertEquals("COMPLETE", upaResponse.getRedeemStatus().toUpperCase());
        Assertions.assertNotNull(upaResponse.getCurrencyAmountRedeemed());
        Assertions.assertNotNull(upaResponse.getPointsRedeemed());
        Assertions.assertNotNull(upaResponse.getDiscountAmountRedeemed());
    }


    @Test
    public void TypVoid_WithTerminalRefNumber() throws Exception {
        TerminalResponse saleResponse = device.sale(new BigDecimal("100"))
                .withEcrId(TEST_ECR_ID)
                .withClerkId(789)
                .execute();

        Assertions.assertNotNull(saleResponse, "Sale response should not be null");
        Assertions.assertEquals("Success", saleResponse.getStatus());

        Thread.sleep(5000);

        TerminalResponse voidResponse = device.voidTransaction()
                .withEcrId(TEST_ECR_ID)
                .withTerminalRefNumber(saleResponse.getTerminalRefNumber())
                .execute();

        Assertions.assertNotNull(voidResponse);
        Assertions.assertEquals("Success", voidResponse.getStatus());

        UpaResponseHandler upaResponse = (UpaResponseHandler) voidResponse;
        Assertions.assertNotNull(upaResponse);
        Assertions.assertNotNull(upaResponse.getVoidRedeemId());
        Assertions.assertNotNull(upaResponse.getVoidRedeemStatus());
        Assertions.assertEquals("COMPLETE", upaResponse.getVoidRedeemStatus().toUpperCase());
        Assertions.assertNotNull(upaResponse.getVoidCurrencyAmountRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidPointsRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidDiscountAmountRedeemed());
    }

    @Test
    public void TypVoid_WithTransactionId() throws Exception {
        TerminalResponse saleResponse = device.sale(new BigDecimal("100"))
                .withEcrId(TEST_ECR_ID)
                .withClerkId(321)
                .execute();

        Assertions.assertEquals("Success", saleResponse.getStatus());
        Thread.sleep(5000);

        TerminalResponse voidResponse = device.voidTransaction()
                .withEcrId(TEST_ECR_ID)
                .withTransactionId(saleResponse.getTransactionId())
                .execute();

        Assertions.assertNotNull(voidResponse);
        UpaResponseHandler upaResponse = (UpaResponseHandler) voidResponse;
        Assertions.assertNotNull(upaResponse);
        Assertions.assertNotNull(upaResponse.getVoidRedeemId());
        Assertions.assertNotNull(upaResponse.getVoidRedeemStatus());
        Assertions.assertNotNull(upaResponse.getVoidCurrencyAmountRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidPointsRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidDiscountAmountRedeemed());
    }

    @Test
    public void TypReversal_WithTerminalRefNumber() throws Exception {
        TerminalResponse saleResponse = device.sale(new BigDecimal("100"))
                .withEcrId(TEST_ECR_ID)
                .withClerkId(789)
                .execute();

        Assertions.assertNotNull(saleResponse);
        Assertions.assertEquals("Success", saleResponse.getStatus());
        Assertions.assertNotNull(saleResponse.getTerminalRefNumber());

        Thread.sleep(5000);

        TerminalResponse reversalResponse = device.reverse()
                .withTerminalRefNumber(saleResponse.getTerminalRefNumber())
                .withEcrId(TEST_ECR_ID)
                .execute();

        Assertions.assertNotNull(reversalResponse);
        Assertions.assertEquals("Success", reversalResponse.getStatus());

        UpaResponseHandler upaResponse = (UpaResponseHandler) reversalResponse;
        Assertions.assertNotNull(upaResponse);
        Assertions.assertNotNull(upaResponse.getVoidRedeemId());
        Assertions.assertNotNull(upaResponse.getVoidRedeemStatus());
        Assertions.assertEquals("COMPLETE", upaResponse.getVoidRedeemStatus().toUpperCase());
        Assertions.assertNotNull(upaResponse.getVoidCurrencyAmountRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidPointsRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidDiscountAmountRedeemed());
    }

    @Test
    public void TypReversal_WithTransactionId() throws Exception {
        TerminalResponse saleResponse = device.sale(new BigDecimal("100"))
                .withEcrId(TEST_ECR_ID)
                .withClerkId(999)
                .execute();

        Assertions.assertNotNull(saleResponse);
        Assertions.assertEquals("Success", saleResponse.getStatus());
        Assertions.assertNotNull(saleResponse.getTerminalRefNumber());

        Thread.sleep(5000);

        TerminalResponse reverseResponse = device.reverse()
                .withTransactionId(saleResponse.getTransactionId())
                .withEcrId(TEST_ECR_ID)
                .execute();

        Assertions.assertNotNull(reverseResponse);
        Assertions.assertEquals("Success", reverseResponse.getStatus());

        UpaResponseHandler upaResponse = (UpaResponseHandler) reverseResponse;
        Assertions.assertNotNull(upaResponse);
        Assertions.assertNotNull(upaResponse.getVoidRedeemId());
        Assertions.assertNotNull(upaResponse.getVoidRedeemStatus());
        Assertions.assertEquals("COMPLETE", upaResponse.getVoidRedeemStatus().toUpperCase());
        Assertions.assertNotNull(upaResponse.getVoidCurrencyAmountRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidPointsRedeemed());
        Assertions.assertNotNull(upaResponse.getVoidDiscountAmountRedeemed());
    }


    @Test
    public void GetBatchDetails_SummaryReport() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.EcrId, TEST_ECR_ID)
                .and(UpaSearchCriteria.ReportType, "summary")
                .and(UpaSearchCriteria.ReportSubType, "1")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void GetBatchDetails_DetailReport() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.EcrId, TEST_ECR_ID)
                .and(UpaSearchCriteria.ReportType, "detail")
                .and(UpaSearchCriteria.ReportSubType, "1")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void GetBatchDetails_BothReports() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.BothReports, "1")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void GetBatchDetails_WithClerkFilter() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.EcrId, TEST_ECR_ID)
                .and(UpaSearchCriteria.ReportType, "summary")
                .and(UpaSearchCriteria.ReportSubType, "2")
                .and(UpaSearchCriteria.ClerkId, "123")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void GetBatchDetails_WithPreviousBatch() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.EcrId, TEST_ECR_ID)
                .and(UpaSearchCriteria.ReportType, "detail")
                .and(UpaSearchCriteria.ReportSubType, "1")
                .and(UpaSearchCriteria.PreviousBatchReport, "1")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void GetBatchDetails_WithAllClerkFilter() throws ApiException, IllegalAccessException {
        ITerminalReport report = device.getBatchDetailsReport()
                .where(UpaSearchCriteria.EcrId, TEST_ECR_ID)
                .and(UpaSearchCriteria.ReportType, "detail")
                .and(UpaSearchCriteria.ReportSubType, "3")
                .execute();

        // Assert
        Assertions.assertNotNull(report);
        Assertions.assertEquals("Success", report.getStatus());
    }

    @Test
    public void TypReverseWithInvalidTerminalRefNumber_ShouldThrowGatewayException() {
        ApiException ex = Assertions.assertThrows(ApiException.class, () -> {
            device.reverse()
                    .withTerminalRefNumber("23")
                    .withEcrId("12")
                    .execute();
        });
        Assertions.assertEquals("Unexpected Device Response : ERR011 - [tranNo:23]-INVALID LENGTH", ex.getMessage());
    }

    @Test
    public void TypVoidWithoutRefNumber_ShouldThrowsGatewayException() {
        ApiException ex = Assertions.assertThrows(ApiException.class, () -> {
            device.voidTransaction()
                    .withEcrId(TEST_ECR_ID)
                    .execute();
        });
        Assertions.assertEquals("Unexpected Device Response : VOID003 - NO TRANNO OR REFERENCENUMBER SUPPLIED", ex.getMessage());
    }
}
