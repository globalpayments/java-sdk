package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.ReasonCode;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;
import java.util.EnumSet;

public class ManagementBuilder extends TransactionBuilder<Transaction> {
    private BigDecimal amount;
    private BigDecimal authAmount;
    private String currency;
    private String description;
    private BigDecimal gratuity;
    private String orderId;
    private String payerAuthenticationResponse;
    private String poNumber;
    private ReasonCode reasonCode;
    private BigDecimal taxAmount;
    private TaxType taxType;

    public BigDecimal getAmount() {
        return amount;
    }
    public BigDecimal getAuthAmount() {
        return authAmount;
    }
    public String getAuthorizationCode() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getAuthCode();
        return null;
    }
    public String getClientTransactionId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getClientTransactionId();
        return null;
    }
    public String getCurrency() {
        return currency;
    }
    public String getDescription() {
        return description;
    }
    public BigDecimal getGratuity() {
        return gratuity;
    }
    public String getOrderId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getOrderId();
        return null;
    }
    public String getPayerAuthenticationResponse() {
        return payerAuthenticationResponse;
    }
    public String getPoNumber() {
        return poNumber;
    }
    public ReasonCode getReasonCode() {
        return reasonCode;
    }
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    public TaxType getTaxType() {
        return taxType;
    }
    public String getTransactionId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getTransactionId();
        return null;
    }

    public ManagementBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }
    public ManagementBuilder withAuthAmount(BigDecimal value) {
        this.authAmount = value;
        return this;
    }
    public ManagementBuilder withCurrency(String value) {
        this.currency = value;
        return this;
    }
    public ManagementBuilder withDescription(String value) {
        this.description = value;
        return this;
    }
    public ManagementBuilder withGratuity(BigDecimal value) {
        this.gratuity = value;
        return this;
    }
    public ManagementBuilder withPayerAuthenticationResponse(String value) {
        payerAuthenticationResponse = value;
        return this;
    }
    public ManagementBuilder withPaymentMethod(IPaymentMethod value) {
        this.paymentMethod = value;
        if(paymentMethod instanceof TransactionReference)
            this.orderId = ((TransactionReference) paymentMethod).getOrderId();
        return this;
    }
    public ManagementBuilder withPoNumber(String value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.poNumber = value;
        return this;
    }
    public ManagementBuilder withReasonCode(ReasonCode value) {
        this.reasonCode = value;
        return this;
    }
    public ManagementBuilder withTaxAmount(BigDecimal value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.taxAmount = value;
        return this;
    }
    public ManagementBuilder withTaxType(TaxType value) {
        this.transactionModifier = TransactionModifier.LevelII;
        this.taxType = value;
        return this;
    }
    private ManagementBuilder withModifier(TransactionModifier value) {
        this.transactionModifier = value;
        return this;
    }

    public ManagementBuilder(TransactionType type) {
        super(type, null);
    }

    @Override
    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway gateway = ServicesContainer.getInstance().getGateway(configName);
        return gateway.manageTransaction(this);
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Edit, TransactionType.Hold, TransactionType.Release))
                .check("paymentMethod").isClass(TransactionReference.class);

        this.validations.of(TransactionType.Edit).with(TransactionModifier.LevelII)
                .check("taxType").isNotNull();

        this.validations.of(TransactionType.Refund)
                .when("amount").isNotNull()
                .check("currency").isNotNull();

        this.validations.of(TransactionType.VerifySignature)
                .check("payerAuthenticationResponse").isNotNull()
                .check("amount").isNotNull()
                .check("currency").isNotNull()
                .check("orderId").isNotNull();
    }
}
