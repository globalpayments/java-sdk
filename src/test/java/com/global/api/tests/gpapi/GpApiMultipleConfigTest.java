package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GpApiMultipleConfigTest extends BaseGpApiTest {

    @Test
    public void MultipleConfig() throws ApiException {
        GpApiConfig config = new GpApiConfig();
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);
        config.setEnableLogging(true);

        String firstConfig = "firstConfig";
        ServicesContainer.configureService(config, firstConfig);

        TransactionSummaryPaged transactionsFirstResponse =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .execute(firstConfig);

        assertNotNull(transactionsFirstResponse);
        assertEquals(1, transactionsFirstResponse.getResults().size());

        GpApiConfig config2 = new GpApiConfig();
        config2
                .setAppId("AzcKJwI7SzGGtd9IXCEir5VFPZ6kU8kH")
                .setAppKey("xv1bZxbRxFQtzhAo");
        config2.setEnableLogging(true);

        String secondConfig = "secondConfig";
        ServicesContainer.configureService(config2, secondConfig);

        TransactionSummaryPaged transactionsSecondResponse =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .execute(secondConfig);

        assertNotNull(transactionsSecondResponse);
        assertEquals(1, transactionsSecondResponse.getResults().size());
        assertNotEquals(config.getAccessTokenInfo().getAccessToken(), config2.getAccessTokenInfo().getAccessToken());

        TransactionSummaryPaged thirdResponse =
                ReportingService
                        .findTransactionsPaged(1, 1)
                        .execute(firstConfig);

        assertNotNull(thirdResponse);
        assertEquals(1, thirdResponse.getResults().size());
        assertNotEquals(thirdResponse, transactionsSecondResponse);
        assertEquals(transactionsFirstResponse.getResults().get(0).getTransactionId(), thirdResponse.getResults().get(0).getTransactionId());
    }
}
