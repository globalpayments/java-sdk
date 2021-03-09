package com.global.api.builders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.global.api.ServicesContainer;
import com.global.api.entities.Customer;
import com.global.api.entities.HostedPaymentData;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.enums.BillingLoadType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IBillingProvider;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.utils.StringUtils;

public class BillingBuilder extends TransactionBuilder<BillingResponse> {
    private List<Bill> bills;
    private BillingLoadType billingLoadType;
    private HostedPaymentData hostedPaymentData;
    private String orderId;
    private boolean commitBills;
    private boolean clearBills;
    private Customer customer;
    private BigDecimal amount;

    public BillingBuilder(TransactionType transactionType) {
        super(transactionType);

        billingLoadType = BillingLoadType.NONE;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public BillingLoadType getBillingLoadType() {
        return billingLoadType;
    }

    public HostedPaymentData getHostedPaymentData() {
        return hostedPaymentData;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean getCommitBills() {
        return commitBills;
    }

    public boolean getClearBills() {
        return clearBills;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BillingBuilder withOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public BillingBuilder commitPreloadedBills() {
        this.commitBills = true;
        return this;
    }

    public BillingBuilder clearPreloadedBills() {
        this.clearBills = true;
        return this;
    }

    public BillingBuilder withBillingLoadType(BillingLoadType billingLoadType) {
        this.billingLoadType = billingLoadType;
        return this;
    }

    public BillingBuilder withBills(Bill ... bills) {
        this.bills = Arrays.asList(bills);
        return this;
    }
    
    public BillingBuilder withBills(List<Bill> bills) {
        this.bills = bills;
        return this;
    }

    public BillingBuilder withHostedPaymentData(HostedPaymentData hostedPaymentData) {
        this.hostedPaymentData = hostedPaymentData;
        return this;
    }

    public BillingBuilder withPaymentMethod(IPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;

        if ((paymentMethod instanceof EBTCardData)
                && !StringUtils.isNullOrEmpty(((EBTCardData) paymentMethod).getSerialNumber())) {
            transactionModifier = TransactionModifier.Voucher;
        }

        if ((paymentMethod instanceof CreditCardData) && ((CreditCardData) paymentMethod).getMobileType() != null) {
            transactionModifier = TransactionModifier.EncryptedMobile;
        }

        return this;
    }

    public BillingBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public BillingBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public BillingResponse execute(String configName) throws ApiException {
        super.execute(configName);

        IBillingProvider client = ServicesContainer.getInstance().getBillingClient(configName);
        return client.processBillingRequest(this);
    }

    @Override
    public void setupValidations() {
        // 
    }
}
