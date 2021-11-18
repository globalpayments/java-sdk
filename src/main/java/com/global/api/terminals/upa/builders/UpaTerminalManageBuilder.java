package com.global.api.terminals.upa.builders;

import java.util.EnumSet;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalManageBuilder;

public class UpaTerminalManageBuilder extends TerminalManageBuilder {
    public UpaTerminalManageBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, paymentType);
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Capture, TransactionType.Void)).check("terminalRefNumber").isNotNull();
        this.validations.of(PaymentMethodType.Gift).check("currency").isNotNull();
    }
}
