package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.tests.terminals.hpa.RequestIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxEbtTests {
    private IDeviceInterface device;
    private String rec_message;

    public PaxEbtTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_S300);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RequestIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void ebtFoodstampPurchase() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]01[FS]1000[FS][US][US][US]F[US][US]1[FS]1[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtPurchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.FoodStamps)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitPurchase() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]01[FS]1000[FS][US][US][US]C[US][US]1[FS]2[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtPurchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtPurchaseWithCashback() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]01[FS]1000[US][US]1000[FS][US][US][US]C[US][US]1[FS]1[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtPurchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .withAllowDuplicates(true)
                .withCashBack(new BigDecimal("10"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtVoucherPurchase() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]01[FS]1000[FS][US][US][US]V[US][US]1[FS]3[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtPurchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.Voucher)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtFoodstampBalanceInquiry() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]23[FS][FS][US][US][US]F[FS]5[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtBalance()
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitsBalanceInquiry() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]23[FS][FS][US][US][US]C[FS]6[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtBalance()
                .withCurrency(CurrencyType.CashBenefits)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtBalanceInquiryWithVoucher() throws ApiException {
        device.ebtBalance().withCurrency(CurrencyType.Voucher).execute();
    }

    @Test
    public void ebtFoodStampRefund() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]02[FS]1000[FS][US][US][US]F[FS]9[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtRefund(new BigDecimal("10"))
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitRefund() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]02[FS]1000[FS][US][US][US]F[FS]10[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtRefund(new BigDecimal("10"))
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtRefundAllowDup() throws ApiException {
        device.ebtRefund().withAllowDuplicates(true).execute();
    }

    @Test
    public void ebtCashBenefitWithdrawal() throws ApiException {
        rec_message = "[STX]T04[FS]1.35[FS]07[FS]1000[FS][US][US][US]C[FS]12[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.ebtWithdrawal(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtBenefitWithdrawalAllowDup() throws ApiException {
        device.ebtWithdrawal(new BigDecimal("10")).withAllowDuplicates(true).execute();
    }
}
