package com.global.api.tests.terminals.ingenico;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.global.api.entities.enums.BaudRate;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DataBits;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.Parity;
import com.global.api.entities.enums.StopBits;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.abstractions.ITerminalResponse;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import com.global.api.terminals.ingenico.variables.ReceiptType;
import com.global.api.terminals.ingenico.variables.ReportTypes;
import com.global.api.terminals.ingenico.variables.TaxFreeType;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public class IngenicoCommandsTests {
	private IDeviceInterface device;
	ConnectionConfig config = new ConnectionConfig();

	public IngenicoCommandsTests() throws ApiException {
//		config.setDeviceType(DeviceType.INGENICO_L3000);
//		config.setConnectionMode(ConnectionModes.SERIAL);
//		config.setPort("1");
//		config.setBaudRate(BaudRate.r9600);
//		config.setDataBits(DataBits.Seven);
//		config.setStopBits(StopBits.One);
//		config.setParity(Parity.Even);
//		config.setTimeout(8000);
//		device = DeviceService.create(config);
	}

	@Test
	public void testConnection() throws ApiException {
		String test = "01700000001100000000000000000000000000000000000000000000000000000008260000000000";
		byte[] res = test.getBytes(StandardCharsets.UTF_8);
		
		IngenicoTerminalResponse r = new IngenicoTerminalResponse(res, ParseFormat.Transaction);
		ITerminalResponse t = (ITerminalResponse)r;
		int i = 0;
	}

	@Test
	public void sale() throws ApiException, InterruptedException {
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});

		device.setOnBroadcastMessageReceived(new IBroadcastMessageInterface() {
			public void broadcastReceived(String code, String message) {
				assertNotNull(code, message);
			}
		});

		ITerminalResponse response = device.sale(new BigDecimal("100")).withPaymentMode(PaymentMode.APPLICATION)
				.withReferenceNumber(01).withCurrencyCode("826").withCashBack(new BigDecimal("90")).execute();

//		if (response != null) {
//			Thread.sleep(5000);
			
//			ITerminalResponse response2 = device.sale(new BigDecimal("100")).withPaymentMode(PaymentMode.APPLICATION)
//					.withReferenceNumber(01).withCurrencyCode("826").withCashBack(new BigDecimal("90")).execute();
			
//		}
		assertNotNull(response);
	}

	@Test
	public void reverse() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			device.setOnBroadcastMessageReceived(new IBroadcastMessageInterface() {
				public void broadcastReceived(String code, String message) {
					assertNotNull(code, message);
				}
			});

			IDeviceResponse response = device.reverse(new BigDecimal("6.18")).withReferenceNumber(1)
					.withTransactionId("1234").execute();

			assertNotNull(response);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void completion() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			device.setOnBroadcastMessageReceived(new IBroadcastMessageInterface() {
				public void broadcastReceived(String code, String message) {
					assertNotNull(code, message);
				}
			});

			ITerminalResponse response = device.capture(new BigDecimal("15")).withReferenceNumber(01)
					.withCurrencyCode("826").withAuthCode("").execute();

			assertNotNull(response);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void taxFreeCashRefund() throws ApiException {
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});

		device.setOnBroadcastMessageReceived(new IBroadcastMessageInterface() {
			public void broadcastReceived(String code, String message) {
				assertNotNull(code, message);
			}
		});

		ITerminalResponse response = device.refund(new BigDecimal("30")).withPaymentMode(PaymentMode.APPLICATION)
				.withReferenceNumber(01).withCurrencyCode("826").withTaxFree(TaxFreeType.CASH).execute();

		assertNotNull(response);
	}

	@Test
	public void taxFreeCreditRefund() throws ApiException {
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});

		device.setOnBroadcastMessageReceived(new IBroadcastMessageInterface() {
			public void broadcastReceived(String code, String message) {
				assertNotNull(code, message);
			}
		});

		ITerminalResponse response = device.refund(new BigDecimal("30")).withPaymentMode(PaymentMode.APPLICATION)
				.withReferenceNumber(01).withCurrencyCode("826").withTaxFree(TaxFreeType.CREDIT).execute();

		assertNotNull(response);
	}

	@Test
	public void verify() throws ApiException {
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});

		ITerminalResponse response = device.verify().withReferenceNumber(1).execute();
		assertNotNull(response);
	}

	@Test
	public void duplicate() throws ApiException {
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});

		IDeviceResponse response = device.duplicate();
		assertNotNull(response);
	}

	@Test
	public void ticket() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			ITerminalReport xml = device.getLastReceipt(ReceiptType.TICKET).execute();
			assertNotNull(xml.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void banking() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			ITerminalReport banking = device.getReport(ReportTypes.BANKING).execute();
			assertNotNull(banking.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void eod() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			ITerminalReport eod = device.getReport(ReportTypes.EOD).execute();
			assertNotNull(eod.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void reboot() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			IDeviceResponse reboot = device.reboot();
			assertNotNull(reboot);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void posIdentifer() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			IInitializeResponse init = device.initialize();
			assertNotNull(init);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void terminalStatus() {
		try {
			device.setOnMessageSent(new IMessageSentInterface() {
				public void messageSent(String message) {
					assertNotNull(message);
				}
			});

			IDeviceResponse response = device.getTerminalStatus();
			assertNotNull(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
