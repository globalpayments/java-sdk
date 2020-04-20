package com.global.api.tests.terminals.ingenico;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
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
		config.setDeviceType(DeviceType.INGENICO);
		config.setConnectionMode(ConnectionModes.TCP_IP_SERVER);
		config.setPort("18101");
		config.setTimeout(65000);
		device = DeviceService.create(config);
		device.setOnMessageSent(new IMessageSentInterface() {
			public void messageSent(String message) {
				assertNotNull(message);
			}
		});
	}

	@Test
	public void sale() throws ApiException {
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

		ITerminalResponse response = device.sale(new BigDecimal("15")).withPaymentMode(PaymentMode.MAILORDER)
				.withReferenceNumber(01).withCurrencyCode("826").execute();

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

			 IDeviceResponse response = device.reverse(new BigDecimal("6.18"))
				.withReferenceNumber(1).withTransactionId("1234").execute();

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

			ITerminalReport xml = device.getLastReceipt(ReceiptType.SPLITR).execute();
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
}
