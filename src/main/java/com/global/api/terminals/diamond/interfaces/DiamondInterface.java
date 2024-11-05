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
public class DiamondInterface extends DeviceInterface {
    private final DiamondController controller;

    public DiamondInterface(DiamondController controller) {
        this.controller = controller;
    }

    @Override
    public TerminalManageBuilder tipAdjust(BigDecimal tipAmount) {
        return (new TerminalManageBuilder(TransactionType.Edit, PaymentMethodType.Credit))
                .withGratuity(tipAmount);
    }

    @Override
    public TerminalAuthBuilder giftSale(BigDecimal amount) throws ApiException {
        return sale(amount).withPaymentMethodType(PaymentMethodType.Gift);
    }

    @Override
    public TerminalAuthBuilder giftAddValue(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.AddValue, PaymentMethodType.Gift)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder giftBalance() throws ApiException {
        return balance();
    }

    @Override
    public TerminalAuthBuilder ebtBalance() throws ApiException {
        return balance().withPaymentMethodType(PaymentMethodType.EBT);
    }

    @Override
    public TerminalAuthBuilder ebtPurchase(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Sale, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalAuthBuilder ebtRefund(BigDecimal amount) throws ApiException {
        return refund(amount).withPaymentMethodType(PaymentMethodType.EBT);
    }

    @Override
    public TerminalReportBuilder localDetailReport() throws ApiException {
        return new TerminalReportBuilder(TerminalReportType.LocalDetailReport);
    }

    @Override
    public TerminalManageBuilder deletePreAuth() {
        return (new TerminalManageBuilder(TransactionType.Delete, PaymentMethodType.Credit))
                .withTransactionModifier(TransactionModifier.DeletePreAuth);
    }

    @Override
    public TerminalManageBuilder increasePreAuth(BigDecimal amount) {
        return (new TerminalManageBuilder(TransactionType.Auth, PaymentMethodType.Credit))
                .withTransactionModifier(TransactionModifier.Incremental)
                .withAmount(amount);
    }

    @Override
    public IBatchCloseResponse batchClose() throws ApiException {
        return (IBatchCloseResponse) new TerminalAuthBuilder(TransactionType.BatchClose)
                .execute();
    }

    @Override
    public TerminalAuthBuilder creditAuth(BigDecimal amount) throws ApiException {
        return new TerminalAuthBuilder(TransactionType.Auth, PaymentMethodType.Credit)
                .withAmount(amount);
    }

    @Override
    public TerminalManageBuilder creditCapture(BigDecimal amount) throws ApiException {
        return capture(amount);
    }

    @Override
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return refund(amount);
    }

    @Override
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return sale(amount);
    }

    @Override
    public TerminalAuthBuilder creditAuth() throws ApiException {
        return authorize(null);
    }

    @Override
    public TerminalManageBuilder creditCapture() throws ApiException {
        return capture();
    }

    @Override
    public TerminalAuthBuilder creditRefund() throws ApiException {
        return refund();
    }

    @Override
    public TerminalAuthBuilder creditSale() throws ApiException {
        return sale(null);
    }

    @Override
    public TerminalManageBuilder creditVoid() throws ApiException {
        return Void();
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
