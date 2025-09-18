package com.global.api.tests.terminals.upa;

import com.global.api.entities.PrintData;
import com.global.api.entities.ScanData;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.UpaConfigContent;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.SummaryResponse;
import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaSafReportDataType;
import com.global.api.terminals.upa.Entities.TokenInfo;
import com.global.api.terminals.upa.UpaInterface;
import com.global.api.terminals.upa.responses.UpaTransactionResponse;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.terminals.upa.subgroups.UpaSafReportParams;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaAdminTests {
    UpaInterface device;

    public UpaAdminTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.51.94");
        config.setTimeout(30_000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setRequestLogger(new RequestConsoleLogger());

        device = (UpaInterface) DeviceService.create(config);
        assertNotNull(device);
        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void test01_Ping() throws ApiException {
        IDeviceResponse response = device.ping();
        runBasicTests(response);
    }

    @Test
    public void test02_cancel() throws ApiException {
        IDeviceResponse response = device.cancel();
        runBasicTests(response);
    }

    /**
     * -----------------------------Line Items Test case start ----------------------------------------------------------------------
     */
    @Test
    public void LineItemDisplay() throws ApiException {
        runBasicTests(device.lineItem("Line Item #1", "10.00"));
        runBasicTests(device.lineItem("Line Item #2", "11.00"));
    }

    @Test
    public void test03_lineItems() throws ApiException {
        runBasicTests(device.lineItem("Line Item 1", "111.11"));
        runBasicTests(device.lineItem("Line Item 2", null));
        runBasicTests(device.lineItem("Line Item 3", "333.33"));

        try {
            device.cancel();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test03_lineItems_leftSideTextMandatory() {
        ApiException leftSideTextMandatory = assertThrows(ApiException.class, () -> device.lineItem(null, "null"));
        assertEquals("Left-side text is required.", leftSideTextMandatory.getMessage());
    }

    @Test
    public void test03_lineItems_rightSideTextLimit() {
        ApiException rightSideTextLimit = assertThrows(ApiException.class, () -> device.lineItem("Line Item ", "1111.1111111111"));
        assertEquals("Right-side text has 10 char limit.", rightSideTextLimit.getMessage());
    }

    /**
     * -------------------------------Line Items Test case end ----------------------------------------------------------------------
     */

    @Test
    public void test04_reboot() throws ApiException {
        runBasicTests(device.reboot());
    }

    @Test
    public void test05_sendReady() throws ApiException {
        device.sendReady();
    }

    @Test
    public void test06_reset() throws ApiException {
        runBasicTests(device.reset());
    }

    /**
     * ---------------------------------SAF test case start------------------------------------------------------------------
     */
    @Test
    public void test_sendSAF() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    /**
     * This test assumes there is one SAF transaction stored in the device prior to runtime.
     * The SAF transaction should be an $85.00 sale, which results in a $55.00 partial
     * authorization.
     */
    @Test
    public void test_sendSAF_partial_auth() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));

        Map<SummaryType, SummaryResponse> approvedTransactions = response.getApproved();
        assertNotNull(approvedTransactions);

        // transaction record specifics:
        if (!approvedTransactions.isEmpty()) {
            SummaryResponse summaryResponse = (SummaryResponse) approvedTransactions.values().toArray()[0];
            if (!summaryResponse.getTransactions().isEmpty()) {
                TransactionSummary transRecord = summaryResponse.getTransactions().get(0);
                assertEquals(new BigDecimal("55.00"), transRecord.getAuthorizedAmount());
                assertEquals(new BigDecimal("85.00"), transRecord.getRequestAmount());
                assertNotNull(transRecord.getMaskedCardNumber());
            } else {
                fail("No transactions found in SummaryResponse.");
            }
        } else {
            fail("No approved transactions found.");
        }

    }

    @Test
    public void test_sendSAF_safReferenceNumber() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    @Test
    public void test_getSafReportInBackground() throws ApiException {
        UpaSafReportParams params = new UpaSafReportParams();
        params.setDataType(UpaSafReportDataType.REPORT_DATA);
        params.setBackgroundTask(true);
        ISAFResponse response = device.safSummaryReportInBackground(params);
        assertNotNull(response);
    }

    @Test
    public void test_getSafReport() throws ApiException {
        ISAFResponse response = device.safSummaryReport("", "ReturnData");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    @Test
    public void test_getSafReport_safReferenceNumber_onlyPrint() throws ApiException {
        ISAFResponse response = device.safSummaryReport("Print", "");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    @Test
    public void test_getSafReport_safReferenceNumber_PrintAndReturnData() throws ApiException {
        ISAFResponse response = device.safSummaryReport("Print", "ReturnData");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    @Test
    public void test_deleteSaf_withReferenceNo() throws ApiException {
        ISAFResponse response = device.safDelete("P0000022", "");
        runBasicTests(response);
    }

    @Test
    public void test_deleteSaf_withReferenceNo_And_TransactionNo() throws ApiException {
        ISAFResponse response = device.safDelete("P0000013", "0080");
        runBasicTests(response);
    }

    /**
     * ----------------------------------Get Signature test case start-----------------------------------------------------------------------
     */
    @Test
    public void test_getSignature() throws ApiException {
        SignatureData data = new SignatureData();
        data.setPrompt1("Please sign");
        data.setDisplayOption(1);

        ISignatureResponse response = device.getSignatureFile(data);
        assertNotNull(response.getSignatureData());
        runBasicTests(response);

        Path resourcesDir = Paths.get("src", "test", "resources");
        String filePath = resourcesDir.toFile().getAbsolutePath() + "\\image.jpg";

        try {
            saveSignatureImage(response.getSignatureData(), filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_getSignature_withPrompt2() throws ApiException {
        SignatureData data = new SignatureData();
        data.setPrompt1("Please sign");
        data.setPrompt2("and confirm");
        data.setDisplayOption(1);

        ISignatureResponse response = device.getSignatureFile(data);
        assertNotNull(response.getSignatureData());
        runBasicTests(response);

        try {
            saveSignatureImage(response.getSignatureData(), "C:\\Temp\\image18.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ----------------------------------Register POS test case start-----------------------------------------------------------------------
     */
    @Test
    public void test_registerPOS() throws ApiException {
        RegisterPOS data = new RegisterPOS();
        data.setAppName("com.global.testapp");
        data.setLaunchOrder(1);
        data.setRemove(false);
        IDeviceResponse response = device.registerPOS(data);
        runBasicTests(response);
    }

    @Test
    public void test_registerPOS_O1() throws ApiException {
        RegisterPOS data = new RegisterPOS();
        data.setAppName("com.global.testapp");
        data.setLaunchOrder(1);
        data.setRemove(true);
        IDeviceResponse response = device.registerPOS(data);
        runBasicTests(response);
    }

    /**
     * Package Name of Application is required for Register POS command.
     */
    @Test
    public void test_registerPOS_packageNameRequired_Exception() {
        RegisterPOS data = new RegisterPOS();
        data.setLaunchOrder(1);
        data.setRemove(true);
        ApiException packageNameRequiredException = assertThrows(ApiException.class, () -> device.registerPOS(data));
        assertEquals("The package name of the application is required.", packageNameRequiredException.getMessage());
    }

    /**
     * ----------------------------------Print Data test case start-----------------------------------------------------------------------
     */
    @Test
    public void test_receipt() throws ApiException, IOException {
        Path resourcesDir = Paths.get("src", "test", "resources", "images");
        String filePath = resourcesDir.toFile().getAbsolutePath() + "\\verifoneTest.jpg";
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];
            int content = fis.read(bytes);
            if(content > 0) {
                String base64Image = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);

                PrintData data = new PrintData();
                data.setContent(base64Image);
                data.setLine1("Printing");
                data.setLine2("Please Wait...");

                IDeviceResponse response = device.print(data);
                runBasicTests(response);
            }
            else fail("No data returned");
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * ----------------------------GetParams Test Case--------------------------
     */
    @Test
    public void test_getParams() throws ApiException {
        String getParams = device.getParams();
        assertNotNull(getParams);
    }

    /**
     * Image content for PrintData command is mandatory.
     */
    @Test
    public void test_receipt_ImageDataShouldNotBeNull() {
        PrintData data = new PrintData();
        data.setLine1("Printing");
        data.setLine2("Please Wait...");
        ApiException imageDataNotNullException = assertThrows(ApiException.class, () ->
                device.print(data));
        assertEquals("The image data cannot be null or empty.", imageDataNotNullException.getMessage());

    }


    /**
     * Only TCP_IP mode supported for UPA.
     */
    @Test
    public void test_unsupportedConnectionMode() {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.2.96");
        config.setTimeout(450000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.HTTP);

        ConfigurationException configurationException = assertThrows(ConfigurationException.class, () -> device = (UpaInterface) DeviceService.create(config));
        assertEquals("Unsupported connection mode.", configurationException.getMessage());
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    /**
     * used to save the signature data in image format
     */
    public static void saveSignatureImage(byte[] signatureData, String imgFileName) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(imgFileName)))) {
            outputStream.write(signatureData);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clearDataLake() throws ApiException {
        IDeviceResponse response = device.clearDataLake();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void setTimeZone() throws ApiException {
        TimeZone curTimeZone = TimeZone.getDefault();
        IDeviceResponse response = device.setTimeZone(curTimeZone);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void getParams() throws ApiException {
        ArrayList<String> paramsList = new ArrayList<>(Arrays.asList("TerminalLanguage", "PinBypassIsSupported"));
        IDeviceResponse response = device.getParams(paramsList);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testSetDebugLevel() throws ApiException {
        DebugLevel[] debugLevels = {DebugLevel.PACKETS, DebugLevel.DATA, DebugLevel.MESSAGE};
        IDeviceResponse response = device.setDebugLevel(debugLevels, DebugLogsOutput.FILE);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testGetDebugLevel() throws ApiException {
        IDeviceResponse response = device.getDebugLevel();
        assertNotNull(response);
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testGetDebugInfo() throws ApiException {
        UpaTransactionResponse response = (UpaTransactionResponse) device.getDebugInfo(LogFile.DEBUGLOG1);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testBroadcastConfiguration() throws ApiException {
        IDeviceResponse response = device.broadcastConfiguration(false);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void ReturnToIdle() throws ApiException {
        IDeviceResponse response = device.returnToIdle();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void LoadUDDataFile() throws ApiException {
        UDData udData = new UDData();
        udData.setFileType(UDFileType.HTML5);
        udData.setSlot((short) 2);
        udData.setFileName("PIA.html");
        IDeviceResponse response = device.loadUDDataFile(udData);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void removeUDDataFile() throws ApiException {
        UDData udData = new UDData();
        udData.setFileType(UDFileType.HTML5);
        udData.setSlot(1);
        IDeviceResponse response = device.removeUDDataFile(udData);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void scan() throws ApiException {
        ScanData scanData = new ScanData();
        scanData.setHeader("SCAN");
        scanData.setPrompt1("SCAN QR CODE");
        scanData.setPrompt2("ALIGN THE QR CODE WITHIN THE FRAME TO SCAN");
        scanData.setDisplayOption(DisplayOption.NO_SCREEN_CHANGE);
        scanData.setTimeout(26);
        device.setOnMessageSent(Assert::assertNotNull);

        IDeviceResponse response = device.Scan(scanData);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void printData() throws ApiException {
        PrintData printData = new PrintData();
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "download.png").toString();
        printData.setFilePath(filePath);
        printData.setLine1("Printing...");
        printData.setLine2("Please Wait...");
        printData.setDisplayOption(DisplayOption.NO_SCREEN_CHANGE);
        device.setOnMessageSent(Assert::assertNotNull);
        IDeviceResponse response = device.Print(printData);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void availableBatches() throws ApiException {
        IBatchReportResponse response = device.findBatches();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void executeUDDataFile() throws ApiException {
        UDData udData = new UDData();
        udData.setFileType(UDFileType.HTML5);
        udData.setSlot(1);
        device.setOnMessageSent(Assert::assertNotNull);
        IDeviceResponse response = device.executeUDDataFile(udData);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void injectUDDataFile() throws IOException, ApiException {
        UDData udData = new UDData();
        udData.setFileType(UDFileType.HTML5);
        udData.setFileName("UDDataFile.html");
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "UDDataFile.html").toString();
        udData.setFilePath(filePath);
        device.setOnMessageSent(Assert::assertNotNull);
        IDeviceResponse response = device.injectUDDataFile(udData);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void getConfigContent() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaTransactionResponse response = (UpaTransactionResponse) device.getConfigContents(TerminalConfigType.ContactTerminalConfiguration);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("GetConfigContents", response.getCommand());
        assertNotNull(response.getConfigContent().getConfigType());
        assertNotNull(response.getConfigContent().getFileContent());
        assertEquals(response.getConfigContent().getLength(), response.getConfigContent().getFileContent().length());
    }

    @Test
    public void getAppInfo() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaTransactionResponse response = (UpaTransactionResponse) device.getAppInfo();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("GetAppInfo", response.getCommand());
        assertFalse(response.getDeviceSerialNum().isEmpty());
        assertFalse(response.getAppVersion().isEmpty());
        assertFalse(response.getOsVersion().isEmpty());
        assertFalse(response.getEmvSdkVersion().isEmpty());
        assertFalse(response.getCtlsSdkVersion().isEmpty());
    }

    @Test
    public void test_communicationCheck() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaTransactionResponse response = (UpaTransactionResponse) device.communicationCheck();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("CommunicationCheck", response.getCommand());
    }

    @Test
    public void test_logOn() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaTransactionResponse response = (UpaTransactionResponse) device.logOn();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("Logon", response.getCommand());
    }

    @Test
    public void test_injectCarouselLogo() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "brand_logo_test.png").toString();
        UDData udData = new UDData();
        udData.setFileName("brand_logo_test.png");
        udData.setFilePath(filePath);
        UpaTransactionResponse response = (UpaTransactionResponse) device.injectCarouselLogo(udData);

        assertNotNull(response);
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void test_injectCarouselLogo_shouldThrowException_whenFileNameDoesNotStartWithBrandLogo() {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "test.html").toString();
        UDData udData = new UDData();
        udData.setFileName("test.html");
        udData.setFilePath(filePath);
        MessageException exception = assertThrows(MessageException.class, () ->  device.injectCarouselLogo(udData));

        assertEquals("FileName must start with 'brand_logo_'.", exception.getMessage());
    }

    @Test
    public void test_injectCarouselLogo_shouldThrowException_whenFileExtensionIsInvalid() {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "brand_logo_test.html").toString();
        UDData udData = new UDData();
        udData.setFileName("brand_logo_test.html");
        udData.setFilePath(filePath);
        MessageException exception = assertThrows(MessageException.class, () ->  device.injectCarouselLogo(udData));

        assertEquals("FileName must have a valid extension (.jpg, .jpeg, .bmp, .png, .gif).", exception.getMessage());
    }

    @Test
    public void test_removeCarouselLogo() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "brand_logo_test.png").toString();
        UDData udData = new UDData();
        udData.setFileName("brand_logo_test.png");
        udData.setFilePath(filePath);
        UpaTransactionResponse response = (UpaTransactionResponse) device.removeCarouselLogo(udData);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void test_removeCarouselLogo_shouldThrowException_whenFileNameHasNoExtension() {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "brand_logo_test.png").toString();
        UDData udData = new UDData();
        udData.setFileName("brand_logo_test");
        udData.setFilePath(filePath);
        MessageException exception = assertThrows(MessageException.class, () -> device.removeCarouselLogo(udData));

        assertEquals("FileName must include a file extension and must not contain a file path.", exception.getMessage());
    }

    @Test
    public void test_removeCarouselLogo_shouldThrowException_whenFileNameContainsFilePath() {
        device.setOnMessageSent(Assert::assertNotNull);
        String currentDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(currentDirectory, "src", "test", "java", "com", "global", "api", "tests", "terminals", "upa", "fileExamples", "brand_logo_test.png").toString();
        UDData udData = new UDData();
        udData.setFileName(filePath + "brand_logo_test.png");
        udData.setFilePath(filePath);
        MessageException exception = assertThrows(MessageException.class, () -> device.removeCarouselLogo(udData));

        assertEquals("FileName must include a file extension and must not contain a file path.", exception.getMessage());
    }

    @Test
    public void test_manageToken_shouldUpdateTokenExpirationDateAndReturnSuccessStatus() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        String token = "Kbl04e1ddX6JVyjp55ZO0011";
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setExpiryMonth("12");
        tokenInfo.setExpiryYear("2026");

        UpaTransactionResponse response = (UpaTransactionResponse) device.manageToken(tokenInfo);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void test_manageToken_shouldThrowException_whenExpiryMonthIsInvalid() {
        device.setOnMessageSent(Assert::assertNotNull);
        String token = "Kbl04e1ddX6JVyjp55ZO0011";
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setExpiryMonth("14");
        tokenInfo.setExpiryYear("2026");

        MessageException exception = assertThrows(MessageException.class, () -> device.manageToken(tokenInfo));

        assertEquals("ExpiryMonth must be an integer between 1 and 12.", exception.getMessage());
    }

    @Test
    public void test_manageToken_shouldThrowException_whenExpiryYearInvalid() {
        device.setOnMessageSent(Assert::assertNotNull);
        String token = "Kbl04e1ddX6JVyjp55ZO0011";
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setExpiryMonth("07");
        tokenInfo.setExpiryYear("17885");

        MessageException exception = assertThrows(MessageException.class, () -> device.manageToken(tokenInfo));

        assertEquals("ExpiryYear must be a 4-digit positive integer greater than 1999.", exception.getMessage());
    }

    @Test
    public void test_manageToken_shouldThrowException_whenTokenIsInvalid() {
        device.setOnMessageSent(Assert::assertNotNull);
        String token = "Kbl04e1ddX6JVyjp55ZO0011hF23G3445";
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setExpiryMonth("07");
        tokenInfo.setExpiryYear("2026");

        MessageException exception = assertThrows(MessageException.class, () -> device.manageToken(tokenInfo));

        assertEquals("Token must be alphanumeric and between 1 and 24 characters in length.", exception.getMessage());
    }

    @Test
    public void test_getLastEod() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaTransactionResponse response = (UpaTransactionResponse) device.getLastEod();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("GetLastEOD", response.getCommand());
    }

    @Test
    public void test_deleteSaf() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();

        runBasicTests(response);

        Map<SummaryType, SummaryResponse> declinedTransactions = response.getDeclined();
        assertNotNull(declinedTransactions);

        // transaction record specifics:
        SummaryResponse summaryResponse = (SummaryResponse) declinedTransactions.values().toArray()[0];
        TransactionSummary transRecord = summaryResponse.getTransactions().get(0);
        ISAFResponse safDeleteResponse = device.safDelete(transRecord.getTransactionId(), transRecord.getSafReferenceNumber());
        runBasicTests(safDeleteResponse);
    }
    @Test
    public void saveConfigFile() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaConfigContent upaConfigContent =new UpaConfigContent();
        upaConfigContent.setConfigType(TerminalConfigType.ContactTerminalConfiguration);
        upaConfigContent.setFileContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TerminalConfiguration>\n" +
                "    <ContactTerminalConfiguration>\n" +
                "        <TerminalType>Contact</TerminalType>\n" +
                "        <TerminalCapabilities>...</TerminalCapabilities>\n" +
                "    </ContactTerminalConfiguration>\n" +
                "</TerminalConfiguration>");
        upaConfigContent.setLength(13321);
        upaConfigContent.setReinitialize(Reinitialize.ReinitializeApplication);

        UpaTransactionResponse response = (UpaTransactionResponse) device.saveConfigFile(upaConfigContent);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("SaveConfigFile", response.getCommand());
    }

    @Test
    public void saveConfigFile_NullMissingField() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);
        UpaConfigContent upaConfigContent =new UpaConfigContent();
        upaConfigContent.setConfigType(TerminalConfigType.ContactTerminalConfiguration);
        upaConfigContent.setLength(13321);
        upaConfigContent.setReinitialize(Reinitialize.ReinitializeApplication);

        ApiException exception = assertThrows(ApiException.class, () -> {
            // Simulate device failure or invalid response
            device.saveConfigFile(upaConfigContent);
        });

        assertEquals("Invalid UpaConfigContent: null or missing required fields.", exception.getMessage());
    }

    @Test
    public void setLogoCarouselInterval() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);

        UpaTransactionResponse response = (UpaTransactionResponse) device.setLogoCarouselInterval(9,false);

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("SetLogoCarouselInterval", response.getCommand());
    }

    @Test
    public void setLogoCarouselIntervalMoreIntervalTime() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);

        ApiException exception = assertThrows(ApiException.class, () -> {
            // Simulate device failure or invalid response
            device.setLogoCarouselInterval(11,false);
        });

        assertEquals("Interval time must be between 0 to 9 seconds.", exception.getMessage());
    }
    @Test
    public void getBatteryPercentage() throws ApiException {
        device.setOnMessageSent(Assert::assertNotNull);

        UpaTransactionResponse response = (UpaTransactionResponse) device.getBatteryPercentage();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getStatus());
        assertEquals("GetBatteryPercentage", response.getCommand());
        assertNotNull(response.getBatteryPercentage());
    }

    /**
     * This test simulates a scenario where the device fails to respond correctly.
     * It should throw an ApiException with a specific message.
     */
    @Test
    public void getBatteryPercentage_shouldThrowException_whenDeviceFails() {
        device.setOnMessageSent(Assert::assertNotNull);

        ApiException exception = assertThrows(ApiException.class, () -> {
            // Simulate device failure or invalid response
            device.getBatteryPercentage();
        });

        assertEquals("Unable to connect with device.", exception.getMessage());
    }
}