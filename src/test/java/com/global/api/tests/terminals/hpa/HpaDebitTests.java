//package com.global.api.tests.terminals.hpa;
//
//import com.global.api.entities.enums.ConnectionModes;
//import com.global.api.entities.enums.DeviceType;
//import com.global.api.entities.enums.PaymentMethodType;
//import com.global.api.entities.exceptions.ApiException;
//import com.global.api.entities.exceptions.BuilderException;
//import com.global.api.services.DeviceService;
//import com.global.api.terminals.ConnectionConfig;
//import com.global.api.terminals.TerminalResponse;
//import com.global.api.terminals.abstractions.IDeviceInterface;
//import com.global.api.terminals.abstractions.IDeviceResponse;
//import com.global.api.terminals.messaging.IMessageSentInterface;
//
//import org.junit.After;
//import org.junit.Test;
//
//import java.math.BigDecimal;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//public class HpaDebitTests {
//    private static IDeviceInterface device;
//    private String expectedMessage = "";
//
//    public HpaDebitTests() throws ApiException {
//        ConnectionConfig deviceConfig = new ConnectionConfig();
//        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
//        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
//        deviceConfig.setIpAddress("10.12.220.39");
//        deviceConfig.setPort(12345);
//        deviceConfig.setTimeout(30000);
//        deviceConfig.setRequestIdProvider(new RandomIdProvider());
//
//        device = DeviceService.create(deviceConfig);
//        assertNotNull(device);
//
//        device.setOnMessageSent(new IMessageSentInterface() {
//            public void messageSent(String message) {
//                if(!expectedMessage.equals(""))
//                    assertEquals(expectedMessage, message);
//            }
//        });
//        device.openLane();
//    }
//
//    @After
//    public void waitAndReset() throws Exception {
//        Thread.sleep(3000);
//        device.reset();
//    }
//
//    @Test
//    public void debitSale() throws ApiException {
//        TerminalResponse response = device.debitSale(new BigDecimal("10"))
//                .withAllowDuplicates(true)
//                .execute();
//        assertNotNull(response);
//        assertEquals("00", response.getResponseCode());
//    }
//
//    @Test(expected = BuilderException.class)
//    public void debitSaleNoAmount() throws ApiException {
//    	device.debitSale().execute();
//    }
//
//    @Test
//    public void debitRefund() throws ApiException {
//        TerminalResponse response = device.debitRefund(new BigDecimal("10")).execute();
//        assertNotNull(response);
//        assertEquals("00", response.getResponseCode());
//    }
//
//    @Test(expected = BuilderException.class)
//    public void debitRefund_NoAmount() throws ApiException {
//    	device.debitRefund().execute();
//    }
//    
//    @Test
//    public void debitStartCard() throws ApiException {
//        IDeviceResponse response = device.startCard(PaymentMethodType.Debit);
//        assertNotNull(response);
//        assertEquals("00", response.getDeviceResponseCode());
//    }
//}
