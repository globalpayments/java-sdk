package com.global.api.tests.terminals.diamondcloud;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceCloudService;
import com.global.api.services.DeviceService;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.diamond.DiamondCloudConfig;
import com.global.api.terminals.diamond.enums.DiamondCloudSearchCriteria;
import com.global.api.terminals.diamond.interfaces.DiamondHttpInterface;
import com.global.api.terminals.diamond.responses.DiamondCloudResponse;
import com.global.api.tests.gpapi.BaseGpApiTest;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import com.global.api.utils.JsonDoc;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiamondCloudTest extends BaseGpApiTest {

    IDeviceInterface _device;
    RandomIdProvider _requestIdProvider;
    RequestConsoleLogger _logManagementProvider;
    private String posID = "1342641186174645";

    public DiamondCloudTest() throws ApiException {
        _requestIdProvider = new RandomIdProvider();
        _logManagementProvider = new RequestConsoleLogger();

        DiamondCloudConfig diamondCloudConfig = new DiamondCloudConfig();
        diamondCloudConfig.setDeviceType(DeviceType.PAX_A920);
        diamondCloudConfig.setConnectionMode(ConnectionModes.DIAMOND_CLOUD);
        diamondCloudConfig.setRequestIdProvider(_requestIdProvider);
        diamondCloudConfig.setLogManagementProvider(_logManagementProvider);
        diamondCloudConfig.setTimeout(15);
        diamondCloudConfig.setIsvID("154F070E3E474AB98B00D73ED81AAA93");
        diamondCloudConfig.setSecretKey("8003672638");
        diamondCloudConfig.setRegion(Region.US.toString());
        diamondCloudConfig.setPosID(posID);

        _device = DeviceService.create(diamondCloudConfig);
        Assert.assertNotNull(_device);
    }

    private IDeviceInterface getEuDevice() throws ApiException {
        DiamondCloudConfig diamondCloudConfig = new DiamondCloudConfig();
        diamondCloudConfig.setDeviceType(DeviceType.PAX_A920);
        diamondCloudConfig.setConnectionMode(ConnectionModes.DIAMOND_CLOUD);
        diamondCloudConfig.setRequestIdProvider(_requestIdProvider);
        diamondCloudConfig.setLogManagementProvider(_logManagementProvider);
        diamondCloudConfig.setTimeout(15);
        diamondCloudConfig.setIsvID("154F070E3E474AB98B00D73ED81AAA93");
        diamondCloudConfig.setSecretKey("8003672638");
        diamondCloudConfig.setRegion(Region.EU.toString());
        diamondCloudConfig.setPosID(posID);

        IDeviceInterface device = DeviceService.create(diamondCloudConfig);
        Assert.assertNotNull(_device);
        return device;
    }

    @Test
    public void test_getHMACSHA256Hash() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        DiamondHttpInterface diamondHttpInterface = new DiamondHttpInterface(null);

        Method method = diamondHttpInterface.getClass().getDeclaredMethod("getHMACSHA256Hash", String.class, String.class);
        method.setAccessible(true);
        String actualHash = (String) method.invoke(diamondHttpInterface, "39AAA18DE37D00B89BA474E3E070F4511342641186174645", "8003672638");

        Assert.assertEquals("39b98ae28ea62b5be21d90c5c80124526fefefb306f3bab92419d264c33e8d29", actualHash);
    }

    @Test
    public void creditSale() throws ApiException {

        TerminalResponse response = _device
                .creditSale(new BigDecimal("2"))
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void getTransactionStatus() throws ApiException, IllegalAccessException {
        TerminalResponse response = (DiamondCloudResponse) _device.localDetailReport()
                .where(DiamondCloudSearchCriteria.ReferenceNumber, "ZYMD938VKW4")
                .execute();

        Assert.assertEquals("sale", response.getCommand());
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertEquals("ACCEPTED", response.getResponseCode());
    }

    @Test
    public void creditVoid() throws ApiException {
        TerminalResponse response = _device.creditVoid()
                .withTransactionId("Z9A58QWXA9N")
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void creditReturn() throws ApiException {
        TerminalResponse response = _device.creditRefund(new BigDecimal("1"))
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void linkedRefund() throws ApiException, InterruptedException {
        TerminalResponse response = _device.creditAuth(new BigDecimal("2.01"))
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());

        String trnsid = response.getTransactionId();

        Thread.sleep(15000);

        TerminalResponse refund = _device.refundById()
                .withTransactionId(trnsid)
                .execute();

        Assert.assertNotNull(refund);
        Assert.assertEquals("00", refund.getDeviceResponseCode());
        Assert.assertNotNull(refund.getTransactionId());
    }

    @Ignore("Tip adjust is not available on PAX devices")
    @Test
    public void tipAdjust() throws ApiException {
        TerminalResponse response = _device.tipAdjust(new BigDecimal("1.01"))
                .withAmount(new BigDecimal("5.01"))
                .withTransactionId("M3QR97ZDM8M")
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void authorize() throws ApiException {
        TerminalResponse response = _device.creditAuth(new BigDecimal("2"))
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void capture() throws ApiException {
        IDeviceInterface device = getEuDevice();

        TerminalResponse response = device.creditCapture(new BigDecimal("2"))
                .withTransactionId("Y7XEVE5YAB6")
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void cancelAuth() throws ApiException {
        IDeviceInterface device = getEuDevice();

        TerminalResponse response = device.deletePreAuth()
                .withTransactionId("DJKK7BY4MWV")
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void authIncreasing() throws ApiException {
        IDeviceInterface device = getEuDevice();

        TerminalResponse response = device.increasePreAuth(new BigDecimal(3))
                .withTransactionId("DJKK7BY4MWV")
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void ebtPurchase() throws ApiException {
        TerminalResponse response = _device.ebtPurchase(new BigDecimal(1))
                .withPaymentMethodType(PaymentMethodType.EBT)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void ebtBalance() throws ApiException {
        TerminalResponse response = _device.ebtBalance()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void ebtReturn() throws ApiException {
        TerminalResponse response = _device.ebtRefund(new BigDecimal("5.02"))
                .withPaymentMethodType(PaymentMethodType.EBT)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void giftBalance() throws ApiException {
        //Transaction type Balance for payment type not supported in EU
        TerminalResponse response = _device.giftBalance()
                .withCurrency(CurrencyType.Points)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void giftReload() throws ApiException {
        TerminalResponse response = _device.giftAddValue(new BigDecimal("1"))
                .withCurrency(CurrencyType.Points)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Ignore("This feature is not supported in EU region!")
    @Test
    public void giftRedeem() throws ApiException {
        TerminalResponse response = _device.giftSale(new BigDecimal("1"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(CurrencyType.Points)
                .execute();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void batchClose() throws ApiException {
        IDeviceInterface device = getEuDevice();

        TerminalResponse response = (TerminalResponse) device.batchClose();

        Assert.assertNotNull(response);
        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertNotNull(response.getTransactionId());
    }

    @Test
    public void saleStatusUrlResponse() throws ApiException {
        DiamondCloudConfig diamondCloudConfig = new DiamondCloudConfig();
        diamondCloudConfig.setDeviceType(DeviceType.PAX_A920);
        diamondCloudConfig.setConnectionMode(ConnectionModes.DIAMOND_CLOUD);
        diamondCloudConfig.setRequestIdProvider(_requestIdProvider);
        diamondCloudConfig.setLogManagementProvider(_logManagementProvider);
        diamondCloudConfig.setTimeout(15);
        diamondCloudConfig.setIsvID("154F070E3E474AB98B00D73ED81AAA93");
        diamondCloudConfig.setSecretKey("8003672638");
        diamondCloudConfig.setRegion(Region.EU.toString());
        diamondCloudConfig.setPosID(posID);

        DeviceCloudService service = new DeviceCloudService(diamondCloudConfig);
        String responseJson = "{\"IsvId\":\"154F070E3E474AB98B00D73ED81AAA93\",\"InvoiceId\":\"\",\"CloudTxnId\":\"XJ98K73YJ9N\",\"traceId\":\"\",\"followId\":\"\",\"Device\":\"1850747855_2\",\"PosId\":\"1342641186174645\",\"PaymentResponse\":{ \"PaymentResponse\":{ \"aosa\":null,\"applicationVersion\":\"1.6.2\",\"authorizationCode\":null,\"authorizationMessage\":\"000023\",\"authorizationMethod\":\"?\",\"authorizationType\":\"?\",\"cardBrandName\":\"MC CREDIT\",\"cardSource\":\"P\",\"cashbackAmount\":\"500\",\"currencyExchangeRate\":null,\"date\":\"2023.09.11\",\"dccCurrencyExponent\":null,\"dccText1\":null,\"dccText2\":null,\"errorMessage\":null,\"maskedCardNumber\":\"************0036\",\"merchantId\":\"888880000000373\",\"result\":\"1\",\"serverMessage\":null,\"slipNumber\":\"23\",\"terminalCurrency\":null,\"terminalId\":\"66677768\",\"terminalPrintingIndicator\":\"1\",\"time\":\"15:37:55\",\"tipAmount\":null,\"token\":null,\"transactionAmount\":\"1000\",\"transactionAmountInTerminalCurrency\":null,\"transactionCurrency\":\"EUR\",\"transactionTitle\":null,\"type\":\"1\",\"AC\":null,\"AID\":\"A0000000041010\",\"ATC\":null,\"TSI\":\"8000\",\"TVR\":\"0400000000\"},\"CloudInfo\":{\"Device\":\"1850747855_2\",\"TerminalType\":\"eService\",\"MqttClientId\":\"1bLU\",\"Command\":\"sale\",\"ApkVersion\":\"1.0.86.0629\",\"TerminalModel\":\"PAX_A920Pro\"},\"ResultId\":\"Zvx3hYCS9tXxfAVy\"}}";
        JsonDoc json = JsonDoc.parse(responseJson);
        TerminalResponse parsedResponse = service.parseResponse(responseJson);

        Assert.assertEquals(json.getStringOrNull("CloudTxnId"), parsedResponse.getTransactionId());
        Assert.assertEquals(json.get("PaymentResponse").get("PaymentResponse").getStringOrNull("authorizationCode"), parsedResponse.getAuthorizationCode());
        Assert.assertEquals(posID, parsedResponse.getTerminalRefNumber());
        Assert.assertEquals(json.get("PaymentResponse").get("CloudInfo").getStringOrNull("Command"), parsedResponse.getCommand());
    }

    @Test
    public void saleACKStatusUrlResponse() throws ApiException {

        DiamondCloudConfig diamondCloudConfig = new DiamondCloudConfig();
        diamondCloudConfig.setDeviceType(DeviceType.PAX_A920);
        diamondCloudConfig.setConnectionMode(ConnectionModes.DIAMOND_CLOUD);
        diamondCloudConfig.setRequestIdProvider(_requestIdProvider);
        diamondCloudConfig.setLogManagementProvider(_logManagementProvider);
        diamondCloudConfig.setTimeout(15);
        diamondCloudConfig.setIsvID("154F070E3E474AB98B00D73ED81AAA93");
        diamondCloudConfig.setSecretKey("8003672638");
        diamondCloudConfig.setRegion(Region.EU.toString());
        diamondCloudConfig.setPosID(posID);

        DeviceCloudService service = new DeviceCloudService(diamondCloudConfig);

        String responseJson = "{\"IsvId\":\"154F070E3E474AB98B00D73ED81AAA93\",\"InvoiceId\":\"\",\"CloudTxnId\":\"EXKX7WKV4QX\",\"traceId\":\"\",\"followId\":\"\",\"Device\":\"1850747855_2\",\"PosId\":\"1342641186174645\",\"PaymentResponse\":{\"PaymentResponse\":{\"resultCode\":\"T03\",\"hostMessage\":\"ACKNOWLEDGEEXKX7WKV4QX\",\"transactionId\":\"EXKX7WKV4QX\"},\"CloudInfo\":{\"Device\":\"1850747855_2\",\"TerminalType\":\"eService\",\"MqttClientId\":\"1bLU\",\"Command\":\"sale\",\"ApkVersion\":\"1.0.86.0629\",\"TerminalModel\":\"PAX_A920Pro\"},\"ResultId\":\"mNbmvleC3I63omNK\"}}";
        JsonDoc json = JsonDoc.parse(responseJson);
        TerminalResponse parsedResponse = service.parseResponse(responseJson);

        Assert.assertEquals(json.getStringOrNull("CloudTxnId"), parsedResponse.getTransactionId());
        Assert.assertEquals(json.get("PaymentResponse").get("CloudInfo").getStringOrNull("Command"), parsedResponse.getCommand());
        Assert.assertEquals(posID, parsedResponse.getTerminalRefNumber());
        Assert.assertEquals(json.get("PaymentResponse").get("PaymentResponse").getStringOrNull("resultCode"), parsedResponse.getResponseCode());
        Assert.assertEquals(json.get("PaymentResponse").get("PaymentResponse").getStringOrNull("hostMessage"), parsedResponse.getResponseText());
    }

    @Test
    public void creditSale_WithoutAmount() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditSale()
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void getTransactionStatus_IdNotFound() throws ApiException, IllegalAccessException {
        TerminalResponse response = (DiamondCloudResponse) _device.localDetailReport()
                .where(DiamondCloudSearchCriteria.ReferenceNumber, "A49KDND5W3Z")
                .execute();

        Assert.assertEquals("00", response.getDeviceResponseCode());
        Assert.assertEquals(null, response.getStatus());
    }

    @Test
    public void getTransactionStatus_NoId() throws ApiException {
        boolean errorFound = false;
        try {
            _device.localDetailReport()
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("referenceNumber cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void creditVoid_WithoutTransactionId() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditVoid()
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("transactionId cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void creditReturn_WithoutAmount() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditRefund()
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void authorize_WithoutAmount() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditAuth()
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void capture_WithoutAmount() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditCapture()
                    .withTransactionId("BWMNKQK6EB5")
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void capture_WithoutTransactionId() throws ApiException {
        boolean errorFound = false;
        try {
            _device.creditCapture(new BigDecimal("0.2"))
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("transactionId cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

    @Test
    public void authIncreasing_WithoutTransactionId() throws ApiException {
        boolean errorFound = false;
        try {
            _device.increasePreAuth(new BigDecimal(3))
                    .execute();
        } catch (BuilderException e) {
            errorFound = true;
            Assert.assertEquals("transactionId cannot be null for this transaction type.", e.getMessage());
        } finally {
            Assert.assertTrue(errorFound);
        }
    }

}