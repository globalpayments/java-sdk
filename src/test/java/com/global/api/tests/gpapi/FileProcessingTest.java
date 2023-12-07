package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.FileProcessor;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.FileProcessingService;
import com.global.api.tests.fileprocessing.FileProcessingClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class FileProcessingTest extends BaseGpApiTest {

    @BeforeClass
    public static void ClassInitialize() throws ConfigurationException {

        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setAppId("fWkEqBHQNyLrWCAtp1vCWDbo10kf5jr6");
        gpApiConfig.setAppKey("EkOH93AQKuGlj8Ty");
        gpApiConfig.setCountry("US");
        gpApiConfig.setChannel(Channel.CardPresent);
        gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");

        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        gpApiConfig.setStatusUrl("https://eo9faqlbl8wkwmx.m.pipedream.net/");
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());
        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig);

    }

    @Test
    public void createUploadUrl() throws Exception {

        FileProcessingService fileProcessingService = new FileProcessingService();

        FileProcessor response = fileProcessingService.initiate();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("INITIATED", response.getResponseMessage());
        assertNotNull(response.getUploadUrl());

        FileProcessingClient client = new FileProcessingClient(response.getUploadUrl());

        String fileToUploadName = "202310261147CreateToken-TokenizationFPR-new.csv.encrypted.txt";
        URL fileAsResource = FileProcessingTest.class.getClassLoader().getResource(fileToUploadName);
        File fileToUpload = new File(fileAsResource.toURI());
        String filePathInLocalMachine = fileToUpload.getAbsolutePath();

        boolean result = client.uploadFile(filePathInLocalMachine);
        assertTrue(result);

        FileProcessor fp = new FileProcessingService().getDetails(response.getResourceId());

        assertEquals("SUCCESS", fp.getResponseCode());
        assertEquals("INITIATED", fp.getStatus());
    }

    @Test
    public void getFileUploadDetails() throws ApiException {
        String resourceId = "FPR_971edc6eb0944d8d890dcba7a2a41bea";
        FileProcessingService fileProcessingService = new FileProcessingService();
        FileProcessor response = fileProcessingService.getDetails(resourceId);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(Integer.parseInt(response.getTotalRecordCount()) > 0);
        assertNotNull(response.getFilesUploaded().get(0).getUrl());
    }

    @Test
    public void getFileUploadDetails_withoutResourceId() throws ApiException {
        try {
            FileProcessingService fileProcessingService = new FileProcessingService();
            fileProcessingService.getDetails(null);
            fail("It should throw an exception.");
        } catch (BuilderException ex) {
            assertEquals("resourceId cannot be null for this transaction type.", ex.getMessage());
        }
    }

}
