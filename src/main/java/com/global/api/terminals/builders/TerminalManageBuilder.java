package com.global.api.terminals.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.terminals.DeviceController;
import com.global.api.terminals.TerminalResponse;

import java.math.BigDecimal;
import java.util.EnumSet;

public class TerminalManageBuilder extends TerminalBuilder<TerminalManageBuilder> {
    private BigDecimal amount;
    private CurrencyType currency;
    private BigDecimal gratuity;
    private String transactionId;

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

    public TerminalResponse execute(String configName) throws ApiException {
        super.execute(configName);

        DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
        return device.manageTransaction(this);
    }

    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Void)).check("transactionId").isNotNull();
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
    }
}
