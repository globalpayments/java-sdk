package com.global.api.tests.terminals.upa;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.SummaryType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.SummaryResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.terminals.upa.subgroups.PrintData;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.terminals.upa.subgroups.SignatureData;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaAdminTests {
    IDeviceInterface device;

    public UpaAdminTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.2.82");
        config.setTimeout(450000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
//        config.setRequestLogger(new RequestFileLogger("AdminTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);
        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void test01_Ping() throws ApiException {
        IDeviceResponse response = device.ping();
        runBasicTests(response);
    }

    @Test
    public void test02_cancel() {
        try {
            device.cancel();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     -----------------------------Line Items Test case start ----------------------------------------------------------------------
     */
    @Test
    public void test03_lineItems() throws ApiException {
        runBasicTests(device.addLineItem("Line Item 1", "111.11"));
        runBasicTests(device.addLineItem("Line Item 2", null));
        runBasicTests(device.addLineItem("Line Item 3", "333.33"));

        try {
            device.cancel();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * For line item display left side text is mandatory & should not be null.
     * @throws ApiException
     */
    @Test
    public void test03_lineItems_leftSideTextMandatory() throws ApiException {
        ApiException leftSideTextMandatory = assertThrows(ApiException.class,() -> device.addLineItem(null,"null"));
        assertEquals("Left-side text is required.",leftSideTextMandatory.getMessage());
    }

    /**
     * For line item display right side text character should not exceed 10 length.
     * @throws ApiException
     */
    @Test
    public void test03_lineItems_rightSideTextLimit() throws ApiException {
        ApiException rightSideTextLimit = assertThrows(ApiException.class,() -> device.addLineItem("Line Item ","1111.1111111111"));
        assertEquals("Right-side text has 10 char limit.",rightSideTextLimit.getMessage());
    }

    /**
     -------------------------------Line Items Test case end ----------------------------------------------------------------------
     */

    @Test
    public void test04_reboot() throws ApiException {
        runBasicTests(device.reboot());
    }

    @Test
    public void test05_sendReady() throws ApiException {
        device.sendReady();
    }

    /**
     ---------------------------------SAF test case start------------------------------------------------------------------
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
     *
     * @throws ApiException
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
        SummaryResponse summaryResponse = (SummaryResponse) approvedTransactions.values().toArray()[0];
        TransactionSummary transRecord = summaryResponse.getTransactions().get(0);
        assertEquals(new BigDecimal("55.00"), transRecord.getAuthorizedAmount());
        assertEquals(new BigDecimal("85.00"), transRecord.getRequestAmount());
        assertNotNull(transRecord.getMaskedCardNumber());
    }

    @Test
    public void test_sendSAF_safReferenceNumber() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    @Test
    public void test_getSafReport() throws ApiException {
        ISAFResponse response = device.safSummaryReport("", "ReturnData");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
        assertNotNull(response.getApproved());
        assertNotNull(response.getDeclined());
        assertNotNull(response.getPending());
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
     ----------------------------------SAF test case end-----------------------------------------------------------------------
     */

    /**
     ----------------------------------Get Signature test case start-----------------------------------------------------------------------
     */
    @Test
    public void test_getSignature() throws ApiException {
        SignatureData data = new SignatureData();
        data.setPrompt1("Please sign");
        data.setDisplayOption(1);

        ISignatureResponse response = device.getSignatureFile(data);
        assertNotNull(response.getSignatureData());
        runBasicTests(response);

        try {
            saveSignatureImage(response.getSignatureData(), "C:\\Temp\\image16.png");
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
     ----------------------------------Get Signature test case end-----------------------------------------------------------------------
     */

    /**
     ----------------------------------Register POS test case start-----------------------------------------------------------------------
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
     * @throws ApiException
     */
    @Test
    public void test_registerPOS_packageNameRequired_Exception() throws ApiException {
        RegisterPOS data = new RegisterPOS();
        data.setLaunchOrder(1);
        data.setRemove(true);
        ApiException packageNameRequiredException = assertThrows(ApiException.class,() -> device.registerPOS(data));
        assertEquals("The package name of the application is required.",packageNameRequiredException.getMessage());
    }

    /**
     ----------------------------------Register POS test case end-----------------------------------------------------------------------
     */

    /**
     ----------------------------------Print Data test case start-----------------------------------------------------------------------
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
            String base64Image = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);

            PrintData data = new PrintData();
            data.setContent(base64Image);
            data.setLine1("Printing");
            data.setLine2("Please Wait...");
            IDeviceResponse response = device.printReceipt(data);
            runBasicTests(response);
        } finally {
            if(fis != null){
                fis.close();
            }
        }
    }

    /**
     * ----------------------------GetParams Test Case--------------------------
     */
    @Test
    public void test_getParams() throws ApiException{
        String getParams = device.getParams();
        assertNotNull(getParams);
    }

    /**
     * Image content for PrintData command is mandatory.
     * @throws ApiException
     */
    @Test
    public void test_receipt_ImageDataShouldNotBeNull() throws ApiException{
        PrintData data = new PrintData();
        data.setLine1("Printing");
        data.setLine2("Please Wait...");
        ApiException imageDataNotNullException = assertThrows(ApiException.class,() ->
                 device.printReceipt(data));
        assertEquals("The image data cannot be null or empty.",imageDataNotNullException.getMessage());

    }

    /**
     ----------------------------------Print Data test case end-----------------------------------------------------------------------
     */


    /**
     * Only TCP_IP mode supported for UPA.
     * @throws ConfigurationException
     */
    @Test
    public void test_unsupportedConnectionMode() throws ConfigurationException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.2.96");
        config.setTimeout(450000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.HTTP);

       ConfigurationException configurationException = assertThrows(ConfigurationException.class,() -> device = DeviceService.create(config));
        assertEquals("Unsupported connection mode.",configurationException.getMessage());
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

    /**
     used to save the signature data in image format
     */
    public static void saveSignatureImage(byte[] signatureData, String imgFileName) throws IOException
    {
        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(imgFileName))){
            outputStream.write(signatureData);
            outputStream.flush();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}


