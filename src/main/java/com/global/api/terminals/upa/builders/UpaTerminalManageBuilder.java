package com.global.api.terminals.upa.builders;

import java.math.BigDecimal;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalManageBuilder;

public class UpaTerminalManageBuilder extends TerminalManageBuilder {
    public UpaTerminalManageBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, paymentType);
    }
    public UpaTerminalManageBuilder withAmount(BigDecimal value) {
        this.amount = value;
        return this;
    }

    @Override
    public void setupValidations() {
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
        this.validations.of(TransactionType.Capture).check("terminalRefNumber").isNotNull();
        this.validations.of(TransactionType.Void).when("terminalRefNumber").isNull().check("transactionId").isNotNull();
    }
}
