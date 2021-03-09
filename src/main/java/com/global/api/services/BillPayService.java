package com.global.api.services;

import java.math.BigDecimal;
import java.util.List;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.HostedPaymentData;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.billing.ConvenienceFeeResponse;
import com.global.api.entities.billing.LoadSecurePayResponse;
import com.global.api.entities.enums.BillingLoadType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.IPaymentMethod;

public class BillPayService {
    /// <summary>
    /// Returns the fee for the given payment method and amount
    /// </summary>
    /// <param name="paymentMethod">The payment method that will be used to make the
    /// charge against</param>
    /// <param name="amount">The total amount to be charged</param>
    /// <param name="configName">The name of the registered configuration to
    /// retrieve. This defaults to 'default'</param>
    /// <returns></returns>
    public BigDecimal calculateConvenienceAmount(IPaymentMethod paymentMethod, BigDecimal amount) throws ApiException {
        return calculateConvenienceAmount(paymentMethod, amount, "default");
    }

    public BigDecimal calculateConvenienceAmount(IPaymentMethod paymentMethod, BigDecimal amount, String configName)
            throws ApiException {
        BillingResponse response = new BillingBuilder(TransactionType.Fetch)
            .withPaymentMethod(paymentMethod)
            .withAmount(amount)
            .execute(configName);

        return ((ConvenienceFeeResponse) response).getConvenienceFee();
    }

    /// <summary>
    /// Loads one or more bills for a specific customer and returns an identifier that can be used by the customer to retrieve their bills
    /// </summary>
    /// <param name="hostedPaymentData">The payment data to be hosted</param>
    /// <param name="configName">The name of the registered configuration to retrieve. This defaults to 'default'</param>
    /// <returns></returns>
    public LoadSecurePayResponse loadHostedPayment(HostedPaymentData hostedPaymentData) throws ApiException {
        return loadHostedPayment(hostedPaymentData, "default");
    }
    public LoadSecurePayResponse loadHostedPayment(HostedPaymentData hostedPaymentData, String configName)
            throws ApiException {
        BillingResponse response = new BillingBuilder(TransactionType.Create)
            .withBillingLoadType(BillingLoadType.SECURE_PAYMENT)
            .withHostedPaymentData(hostedPaymentData)
            .execute(configName);

        return ((LoadSecurePayResponse) response);
    }

    /// <summary>
    /// Loads one or more bills for one or many customers
    /// </summary>
    /// <param name="bills">The collection of bills to load</param>
    /// <param name="configName">The name of the registered configuration to retrieve. This defaults to 'default'</param>
    public void loadBills(List<Bill> bills) throws ApiException {
        loadBills(bills, "default");
    }
    public void loadBills(List<Bill> bills, String configName) throws ApiException {
        int maxBillsPerUpload = 1000;
        int billCount = bills.size();
        int numberOfCalls = billCount < maxBillsPerUpload ? 1 : billCount / maxBillsPerUpload;

        for (int i = 0; i < numberOfCalls; i++) {
            // skipped bills from previous uploads
            int fromIndex = i * maxBillsPerUpload;
            // limit bills to `maxBillsPerUpload`
            int toIndex = fromIndex + (billCount < maxBillsPerUpload ? billCount : maxBillsPerUpload);
            List<Bill> currentSetOfBills = bills.subList(fromIndex, toIndex);

            new BillingBuilder(TransactionType.Create)
                .withBillingLoadType(BillingLoadType.BILLS)
                .withBills(currentSetOfBills)
                .execute(configName);
        }
    }

    /// <summary>
    /// Removes all bills that have been loaded and have not been committed
    /// </summary>
    /// <param name="configName">The name of the registered configuration to
    /// retrieve. This defaults to 'default'</param>
    /// <returns></returns>
    public BillingResponse clearBills() throws ApiException {
        return clearBills("default");
    }

    public BillingResponse clearBills(String configName) throws ApiException {
        return new BillingBuilder(TransactionType.Delete)
            .withBillingLoadType(BillingLoadType.BILLS)
            .clearPreloadedBills()
            .execute(configName);
    }

    /// <summary>
    /// Commits all bills that have been preloaded
    /// </summary>
    /// <param name="configName">The name of the registered configuration to retrieve. This defaults to 'default'</param>
    /// <returns></returns>
    public BillingResponse commitPreloadedBills() throws ApiException {
        return commitPreloadedBills("default");
    }
    public BillingResponse commitPreloadedBills(String configName) throws ApiException {
        return new BillingBuilder(TransactionType.Activate)
            .withBillingLoadType(BillingLoadType.BILLS)
            .commitPreloadedBills()
            .execute(configName);
    }
}
