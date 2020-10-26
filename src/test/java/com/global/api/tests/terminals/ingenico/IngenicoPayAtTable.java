package com.global.api.tests.terminals.ingenico;

import java.math.BigDecimal;

import org.junit.Test;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.ingenico.pat.PATRequest;
import com.global.api.terminals.ingenico.variables.PATPaymentMode;
import com.global.api.terminals.ingenico.variables.PATRequestType;
import com.global.api.terminals.ingenico.variables.PATResponseType;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;

public class IngenicoPayAtTable {
	private IDeviceInterface device;
	ConnectionConfig config = new ConnectionConfig();

	public IngenicoPayAtTable() throws ApiException {
		config.setDeviceType(DeviceType.INGENICO_L3000);
		config.setConnectionMode(ConnectionModes.PAY_AT_TABLE);
		config.setPort("18101");
		config.setTimeout(60000);
		device = DeviceService.create(config);
	}

	@Test
	public void payAtTableHandling() throws ApiException, InterruptedException {
		device.setOnPayAtTableRequest(new IPayAtTableRequestInterface() {
			public void onPayAtTableRequest(final PATRequest payAtTableRequest) {
				payAtTableResponse(payAtTableRequest);
			}
		});

		Thread.sleep(3000 * 10000);
	}

	private void payAtTableResponse(PATRequest payAtTableRequest) {
		try {
			PATRequestType requestType = payAtTableRequest.getRequestType();
			if (requestType == PATRequestType.TABLE_LOCK) {
				device.payAtTableResponse().withPATResponseType(PATResponseType.CONF_OK)
						.withPATPaymentMode(PATPaymentMode.NO_ADDITIONAL_MSG)
						.withAmount(new BigDecimal("123"))
						.withCurrencyCode("826")
						.execute();
			} else if (requestType == PATRequestType.RECEIPT_MESSAGE) {
				device.payAtTableResponse().withXML("C:\\Users\\steven.tan\\Desktop\\\\PAY@TABLE SAMPLE RESPONSE\\receiptrequestsample.xml").execute();
			} else if (requestType == PATRequestType.TRANSACTION_OUTCOME) {
				device.payAtTableResponse().withPATResponseType(PATResponseType.CONF_OK)
						.withPATPaymentMode(PATPaymentMode.NO_ADDITIONAL_MSG).withAmount(new BigDecimal(6.18))
						.withCurrencyCode("826")
						.execute();
			} else if (requestType == PATRequestType.ADDITIONAL_MESSAGE) {
				return;
			} else if (requestType == PATRequestType.SPLITSALE_REPORT) {
				device.payAtTableResponse().withPATResponseType(PATResponseType.CONF_OK)
						.withPATPaymentMode(PATPaymentMode.NO_ADDITIONAL_MSG).withAmount(new BigDecimal(6.18))
						.withCurrencyCode("826")
						.execute();
			} else if (requestType == PATRequestType.TABLE_UNLOCK) {
				return;
			} else if (requestType == PATRequestType.TABLE_LIST) {
				device.payAtTableResponse().withXML(
						"C:\\Users\\steven.tan\\Desktop\\PAY@TABLE SAMPLE RESPONSE\\tablelistsample.xml")
						.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
