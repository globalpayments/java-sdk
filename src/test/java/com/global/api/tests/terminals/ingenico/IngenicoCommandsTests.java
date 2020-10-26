package com.global.api.tests.terminals.ingenico;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

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
		config.setDeviceType(DeviceType.INGENICO_L3000);
		config.setConnectionMode(ConnectionModes.SERIAL);
		config.setPort("7");
		config.setDataBits(DataBits.Seven);
		config.setParity(Parity.Even);
		config.setStopBits(StopBits.One);
		config.setBaudRate(BaudRate.r9600);
		config.setTimeout(65000);
		device = DeviceService.create(config);
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
				System.out.println(String.format("Code: %s, Message: %s", code, message));
				assertNotNull(code, message);
			}
		});

//		try {
//			ITerminalResponse response = device.sale(new BigDecimal("6.18")).withPaymentMode(PaymentMode.APPLICATION)
//					.withReferenceNumber(01).withCurrencyCode("826").execute();
//		} catch (Exception e) {
//			if (e.getMessage().contains("Terminal did not respond")) {
//				Thread.sleep(5000);
//
//				IDeviceResponse duplic = device.duplicate();
//				assertNotNull(duplic);
//			}
//		}

		ITerminalResponse response = device.sale(new BigDecimal("6.18")).withPaymentMode(PaymentMode.APPLICATION)
				.withReferenceNumber(01).withCurrencyCode("826").execute();
//
//		assertNotNull(response);
	}
	
	@Test
	public void refund() throws ApiException {
		ITerminalResponse response = device.refund(new BigDecimal("6.18"))
				.withPaymentMode(PaymentMode.MAILORDER)
				.withReferenceNumber(01).withCurrencyCode("826").execute();
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

	@Test
	public void payAtTable() {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
