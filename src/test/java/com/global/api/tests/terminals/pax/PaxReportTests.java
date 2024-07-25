package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.pax.responses.LocalDetailReportResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxReportTests {

    private IDeviceInterface device;

    public PaxReportTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.228.130");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void local_detail_report_by_record_number() throws ApiException {
        Integer recordNumber = 00;

         LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
         .withPaxReportSearchCriteria(PaxSearchCriteriaType.RECORD_NUMBER, recordNumber)
        .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_reference_number() throws ApiException {
        String referenceNumber = "65961";

        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.REFERENCE_NUMBER, referenceNumber)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_terminal_reference_number() throws ApiException {
        String terminalRefNumber = "5";
        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.TERMINAL_REFERENCE_NUMBER,terminalRefNumber)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_transaction_type() throws ApiException {

        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.TRANSACTION_TYPE, PaxTxnType.AUTH)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_auth_code() throws ApiException {
        String authNumber = "123456";
        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.AUTH_CODE, authNumber)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_card_type() throws ApiException {

        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.CARD_TYPE, CardType.AMEX )
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_merchant_id() throws ApiException {
        String merchant_id ="12345";
        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.MERCHANT_ID, merchant_id)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }
    @Test
    public void local_detail_report_by_merchant_name() throws ApiException {
        String merchant_name ="CAS";
        LocalDetailReportResponse reportResponse = (LocalDetailReportResponse) device.localDetailReport()
                .withPaxReportSearchCriteria(PaxSearchCriteriaType.MERCHANT_ID, merchant_name)
                .execute("default");

        assertNotNull(reportResponse);
        assertEquals("000000", reportResponse.getDeviceResponseCode());
    }

}
