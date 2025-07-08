package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxEbtTests {
    private final IDeviceInterface device;

    public PaxEbtTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void ebtFoodStampPurchase() throws ApiException {
        TerminalResponse response = device.purchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.FoodStamps)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitPurchase() throws ApiException {
        TerminalResponse response = device.purchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtPurchaseWithCashback() throws ApiException {
        TerminalResponse response = device.purchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .withAllowDuplicates(true)
                .withCashBack(new BigDecimal("10"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtVoucherPurchase() throws ApiException {
        TerminalResponse response = device.purchase(new BigDecimal("10"))
                .withCurrency(CurrencyType.Voucher)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtFoodStampBalanceInquiry() throws ApiException {
        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitsBalanceInquiry() throws ApiException {
        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withCurrency(CurrencyType.CashBenefits)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtBalanceInquiryWithVoucher() throws ApiException {
        device.balance()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withCurrency(CurrencyType.Voucher)
                .execute();
    }

    @Test
    public void ebtFoodStampRefund() throws ApiException {
        TerminalResponse response = device.refund(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtCashBenefitRefund() throws ApiException {
        TerminalResponse response = device.refund(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withCurrency(CurrencyType.FoodStamps)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtRefundAllowDup() throws ApiException {
        device.refund()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withAllowDuplicates(true)
                .execute();
    }

    @Test
    public void ebtCashBenefitWithdrawal() throws ApiException {
        TerminalResponse response = device.withdrawal(new BigDecimal("10"))
                .withCurrency(CurrencyType.CashBenefits)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtBenefitWithdrawalAllowDup() throws ApiException {
        device.withdrawal(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
    }
}
