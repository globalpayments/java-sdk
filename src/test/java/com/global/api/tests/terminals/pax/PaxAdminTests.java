//package com.global.api.tests.terminals.pax;
//
//import com.global.api.entities.enums.ConnectionModes;
//import com.global.api.entities.enums.DeviceType;
//import com.global.api.entities.exceptions.ApiException;
//import com.global.api.entities.exceptions.MessageException;
//import com.global.api.services.DeviceService;
//import com.global.api.terminals.ConnectionConfig;
//import com.global.api.terminals.abstractions.IDeviceInterface;
//import com.global.api.terminals.abstractions.IDeviceResponse;
//import com.global.api.terminals.abstractions.IInitializeResponse;
//import com.global.api.terminals.abstractions.ISignatureResponse;
//import com.global.api.terminals.abstractions.ITerminalResponse;
//import com.global.api.terminals.ingenico.variables.PaymentMode;
//import com.global.api.terminals.messaging.IMessageSentInterface;
////import com.global.api.tests.terminals.hpa.RandomIdProvider;
//
//import org.junit.Ignore;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//import static org.junit.Assert.assertEquals;
//
//import java.math.BigDecimal;
//
//public class PaxAdminTests {
//    private IDeviceInterface device;
//    private ConnectionConfig deviceConfig = new ConnectionConfig();
//
//    public PaxAdminTests() throws ApiException {
//        
//        deviceConfig.setDeviceType(DeviceType.INGENICO);
//        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP_SERVER);
////        deviceConfig.setIpAddress("10.12.220.172");
//        deviceConfig.setPort("18101");
//        device = DeviceService.create(deviceConfig);
////        deviceConfig.setRequestIdProvider(new RandomIdProvider());
//        
//        
////        assertNotNull(device);
//    }
//    
//    @Test
//    public void testConnection() {
//    	try {
//    		device = DeviceService.create(deviceConfig);
//    		if (device != null) {
//    			device.dispose();
//    		}
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
//    }
//    
//    @Test
//    public void Sale() {
//    	try {
//    		device.setOnMessageSent(new IMessageSentInterface() {
//
//				public void messageSent(String message) {
//					assertNotNull(null);
//					assertTrue(message.startsWith("test"));
//				}
//    			
//    		});
//			ITerminalResponse response = device.Sale(new BigDecimal("15"))
//					.withPaymentMode(PaymentMode.APPLICATION)
//					.withCurrencyCode("826")
//					.withReferenceNumber(01)
//					.execute();
//			
//			assertNotNull(response);
//		} catch (ApiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//    }
//    
//    @Test
//    public void Refund() {
//    	try {
//			ITerminalResponse response = device.Refund(new BigDecimal("15"))
//					.withPaymentMode(PaymentMode.APPLICATION)
//					.withCurrencyCode("826")
//					.withReferenceNumber(01)
//					.execute();
//			
//			assertNotNull(response);
//		} catch (ApiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//    }
//    
//    @Test
//    public void PreAuth() {
//    	ITerminalResponse response;
//		try {
//			response = device.Authorize(new BigDecimal("15"))
//					.withPaymentMode(PaymentMode.APPLICATION)
//					.withCustomerCode("826")
//					.withReferenceNumber(01)
//					.execute();
//			
//			assertNotNull(response);
//		} catch (ApiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//
////    @Test
////    public void initialize() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A00[FS]1.35[FS][ETX]"));
////            }
////        });
////
////        IInitializeResponse response = device.initialize();
////        assertNotNull(response);
////        assertEquals("OK", response.getDeviceResponseText());
////        assertNotNull(response.getSerialNumber());
////    }
////
////    @Test(expected = MessageException.class)
////    public void cancel() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A14[FS]1.35[FS][ETX]"));
////            }
////        });
////
////        device.cancel();
////    }
////
////    @Test
////    public void reset() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A16[FS]1.35[FS][ETX]"));
////            }
////        });
////
////        IDeviceResponse response = device.reset();
////        assertNotNull(response);
////        assertEquals("OK", response.getDeviceResponseText());
////    }
////
////    @Test @Ignore
////    public void reboot() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A26[FS]1.35[FS][ETX]"));
////            }
////        });
////
////        IDeviceResponse response = device.reboot();
////        assertNotNull(response);
////        assertEquals("OK", response.getDeviceResponseText());
////    }
////
////    @Test
////    public void getSignature() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A08[FS]1.35[FS]0[FS][ETX]"));
////            }
////        });
////
////        ISignatureResponse response = device.getSignatureFile();
////        assertNotNull(response);
////        assertEquals("OK", response.getDeviceResponseText());
////        assertNotNull(response.getSignatureData());
////    }
////
////    @Test
////    public void promptForSignature() throws ApiException {
////        device.setOnMessageSent(new IMessageSentInterface() {
////            public void messageSent(String message) {
////                assertNotNull(message);
////                assertTrue(message.startsWith("[STX]A20"));
////            }
////        });
////
////        ISignatureResponse response = device.promptForSignature();
////        assertNotNull(response);
////        assertEquals("OK", response.getDeviceResponseText());
////        assertNotNull(response.getSignatureData());
////    }
//}