package com.global.api.tests.ci.upa_mitc;

import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.upa.UpaInterface;
import com.global.api.tests.utils.citesting.CiTestingHarness;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaMicTests {

    private static final String MITC_UPA_APP_ID = "6l8Xr23kHL9tGmAtXUvCEXKskvF7aLGq";
    private static final String MITC_UPA_APP_KEY = "z0ApiLDfXrKmrlNa";

    private static final CiTestingHarness ciTestingHarness = new CiTestingHarness(
            "https://apis.sandbox.globalpay.com/ucp",
            CiTestingHarness.CacheMode.Locked,
            "UpaMicTests"
    );

    private static GpApiConfig getGpApiConfig() {
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("90916726");

        GpApiConfig gatewayConfig = new GpApiConfig();
        gatewayConfig.setAppId(MITC_UPA_APP_ID);
        gatewayConfig.setAppKey(MITC_UPA_APP_KEY);
        gatewayConfig.setChannel(Channel.CardPresent);
        gatewayConfig.setCountry("US");
        gatewayConfig.setDeviceCurrency("USD");
        gatewayConfig.setEnableLogging(true);
        gatewayConfig.setAccessTokenInfo(accessTokenInfo);
        gatewayConfig.setServiceUrl(ciTestingHarness.getTestingUrl());
        return gatewayConfig;
    }

    private static UpaInterface createDevice(String testKey) throws ApiException {
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setGatewayConfig(getGpApiConfig());
        connectionConfig.setDeviceType(DeviceType.UPA_DEVICE);
        connectionConfig.setConnectionMode(ConnectionModes.MEET_IN_THE_CLOUD);
        connectionConfig.setTimeout(30000);
        connectionConfig.setRequestIdProvider(ciTestingHarness.createRequestIdProvider(testKey));
        connectionConfig.setRequestLogger(new RequestConsoleLogger());

        UpaInterface device = (UpaInterface) DeviceService.create(connectionConfig);
        device.setEcrId("12");
        return device;
    }

    @Test
    public void CreditSale() throws ApiException {
        UpaInterface device = createDevice("CreditSale");
        BigDecimal amount = ciTestingHarness.generateRandomBigDecimal(
                "CreditSale_amount", new BigDecimal("1"), new BigDecimal("10"), 2);

        TerminalResponse response = device.sale(amount).execute();

        assertNotNull(response);
        assertMitcUpaResponse(response);
    }

    @Test
    public void CreditSale_WithZeroTip() throws ApiException {
        UpaInterface device = createDevice("CreditSale_WithZeroTip");
        BigDecimal amount = ciTestingHarness.generateRandomBigDecimal(
                "CreditSale_WithZeroTip_amount", new BigDecimal("1"), new BigDecimal("10"), 2);

        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal(0))
                .execute();

        assertNotNull(response);
        assertMitcUpaResponse(response);
    }

    private void assertMitcUpaResponse(TerminalResponse response) {
        assertEquals("COMPLETE", response.getStatus());
        assertEquals("COMPLETE", response.getDeviceResponseText());
        assertEquals("00", response.getDeviceResponseCode());
    }
}
