package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.RemittanceReferenceType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

import java.math.BigDecimal;
import java.util.EnumSet;

public class BankPaymentBuilder extends BaseBuilder<Transaction> {

    @Getter @Setter private BigDecimal amount;
    @Getter @Setter private IPaymentMethod paymentMethod;
    @Getter @Setter private String currency;
    @Getter @Setter private TransactionType transactionType;
    @Getter @Setter private TransactionModifier transactionModifier = TransactionModifier.None;
    @Getter @Setter private String description;
    @Getter @Setter private String orderId;
    @Getter @Setter private String timestamp;
    @Getter @Setter private String remittanceReferenceValue;
    @Getter @Setter private RemittanceReferenceType remittanceReferenceType;

    public BankPaymentBuilder(TransactionType transactionType, IPaymentMethod paymentMethod) {
        this.transactionType = transactionType;

        if (paymentMethod != null) {
            this.withPaymentMethod(paymentMethod);
        }
    }

    @Override
    public Transaction execute() throws ApiException {
        return execute("default");
    }

    @Override
    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        var client = ServicesContainer.getInstance().getOpenBankingClient(configName);

        return client.processOpenBanking(this);
    }

    public String serialize() throws ApiException {
        return serialize("default");
    }

    public String serialize(String configName) throws ApiException {
        transactionModifier = TransactionModifier.HostedRequest;
        super.execute(configName);

        var client = ServicesContainer.getInstance().getOpenBankingClient(configName);

        if (client.supportsHostedPayments()) {
            return client.serializeRequest(this);
        }

        throw new UnsupportedTransactionException("Your current gateway does not support hosted payments.");
    }

    public BankPaymentBuilder withCurrency(String value) {
        currency = value;
        return this;
    }

    public BankPaymentBuilder withAmount(BigDecimal value) {
        amount = value;
        return this;
    }

    public BankPaymentBuilder withDescription(String value) {
        description = value;
        return this;
    }

    public BankPaymentBuilder withPaymentMethod(IPaymentMethod value) {
        paymentMethod = value;
        return this;
    }

    public BankPaymentBuilder withOrderId(String value) {
        orderId = value;
        return this;
    }

    public BankPaymentBuilder withModifier(TransactionModifier value) {
        transactionModifier = value;
        return this;
    }

    public BankPaymentBuilder withTimeStamp(String value) {
        timestamp = value;
        return this;
    }

    public BankPaymentBuilder withRemittanceReference(RemittanceReferenceType remittanceReferenceType, String remittanceReferenceValue) {
        this.remittanceReferenceType = remittanceReferenceType;
        this.remittanceReferenceValue = remittanceReferenceValue;
        return this;
    }

    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Sale))
                .check("paymentMethod").isNotNull()
                .check("amount").isNotNull()
                .check("currency").isNotNull();
    }
}