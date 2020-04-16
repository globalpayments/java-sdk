package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.abstractions.ITerminalResponse;
import com.global.api.terminals.ingenico.variables.PaymentMode;

import java.math.BigDecimal;

public class TerminalManageBuilder extends TerminalBuilder<TerminalManageBuilder> {
    private BigDecimal amount;
    private CurrencyType currency;
    private BigDecimal gratuity;
    private String currencyCode;
    private PaymentMode paymentMode;
    private String authCode;
    private String tableNumber;
    private String transactionId;
    
    public String getTableNumber() {
    	return tableNumber;
    }

    public String getCurrencyCode() {
    	return currencyCode;
    }
    
    public PaymentMode getPaymentMode() {
    	return paymentMode;
    }
    
    public String getAuthCode() {
    	return authCode;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    public CurrencyType getCurrency() {
        return currency;
    }
    public BigDecimal getGratuity() {
        return gratuity;
    }
    public String getTransactionId() {
        if(paymentMethod instanceof TransactionReference)
            return ((TransactionReference)paymentMethod).getTransactionId();
        return null;
    }
    
    public TerminalManageBuilder withCurrencyCode(String value) {
    	this.currencyCode = value;
    	return this;
    }
    
    public TerminalManageBuilder withTableNumber(String value) {
    	this.tableNumber = value;
    	return this;
    }
    
    public TerminalManageBuilder withAuthCode(String value) {
    	if(paymentMethod == null || !(paymentMethod instanceof TransactionReference))
            paymentMethod = new TransactionReference();
        ((TransactionReference)paymentMethod).setAuthCode(value);
        this.authCode = value;
        return this;
    }

    public TerminalManageBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }
    public TerminalManageBuilder withCurrency(CurrencyType value) {
        this.currency = value;
        return this;
    }
    public TerminalManageBuilder withGratuity(BigDecimal value) {
        this.gratuity = value;
        return this;
    }
    public TerminalManageBuilder withTransactionId(String value) {
        if(paymentMethod == null || !(paymentMethod instanceof TransactionReference))
            paymentMethod = new TransactionReference();
        ((TransactionReference)paymentMethod).setTransactionId(value);
        this.transactionId = value;
        return this;
    }

    public TerminalManageBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, paymentType);
    }

    @Override
    public ITerminalResponse execute(String configName) throws ApiException {
        super.execute(configName);

        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.manageTransaction(this);
    }

    public void setupValidations() {
    	this.validations.of(TransactionType.Capture).when("authCode").isNull().check("transactionId").isNotNull();
        this.validations.of(TransactionType.Void).check("transactionId").isNotNull();
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
        this.validations.of(TransactionType.Cancel).check("amount").isNotNull();
    }
}