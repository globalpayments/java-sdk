package com.global.api.terminals.diamond.interfaces;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.enums.TerminalReportType;
import com.global.api.terminals.diamond.DiamondController;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiamondInterface extends DeviceInterface<DiamondController> {
    public DiamondInterface(DiamondController controller) {
        super(controller);
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal tipAmount) {
        return (new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit))
                .withGratuity(tipAmount);
    }

    @Override
    public TerminalReportBuilder localDetailReport() {
        return new TerminalReportBuilder(TerminalReportType.LocalDetailReport);
    }

    @Override
    public TerminalManageBuilder deletePreAuth() {
        return new TerminalManageBuilder(TransactionType.Delete, PaymentMethodType.Credit)
                .withTransactionModifier(TransactionModifier.DeletePreAuth);
    }

    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withTransactionModifier(TransactionModifier.Incremental)
                .withAmount(amount);
    }

    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        return (IBatchCloseResponse) new TerminalAuthBuilder(TransactionType.BatchClose)
                .execute();
    }

    @Override
    public TerminalManageBuilder refundById(BigDecimal amount) {
        return new TerminalManageBuilder(TransactionType.Refund, PaymentMethodType.Credit).withAmount(amount);
    }

    @Override
    public TerminalManageBuilder refundById() {
        return new TerminalManageBuilder(TransactionType.Refund, PaymentMethodType.Credit);
    }
}
