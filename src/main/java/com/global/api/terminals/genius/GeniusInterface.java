package com.global.api.terminals.genius;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.genius.builders.MitcManageBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;
import com.global.api.terminals.messaging.IMessageSentInterface;

import java.math.BigDecimal;

public class GeniusInterface extends DeviceInterface {

    private final GeniusController controller;

    public GeniusInterface(GeniusController _controller){
        super();
        controller = _controller;
    }

    @Override
    public TerminalAuthBuilder creditSale(BigDecimal amount) throws ApiException {
        return sale(amount);
    }
    @Override
    public TerminalAuthBuilder creditRefund(BigDecimal amount) throws ApiException {
        return refund(amount);
    }
    public MitcManageBuilder refundById(BigDecimal amount) throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale,null,TransactionType.Refund).withAmount(amount);
    }

    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        return this.controller.processReport(transactionType, transactionId, transactionIdType);
    }
    @Override
    public TerminalManageBuilder creditVoid() throws ApiException {
        return Void();
    }
    public TerminalManageBuilder debitVoid() throws ApiException {
        return Void().withPaymentMethodType(PaymentMethodType.Debit);
    }
    @Override
    public TerminalManageBuilder Void() throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale, PaymentMethodType.Credit, TransactionType.Void);
    }

    public MitcManageBuilder voidRefund() throws ApiException {
        return new MitcManageBuilder(TransactionType.Refund, PaymentMethodType.Credit ,TransactionType.Void);
    }
    @Override
    public TerminalAuthBuilder debitSale(BigDecimal amount) throws ApiException {
        return sale(amount).withPaymentMethodType(PaymentMethodType.Debit);
    }

    @Override
    public void sendReady() throws ApiException {

    }

    @Override
    public void setOnMessageSent(IMessageSentInterface onMessageSent) {

    }

    @Override
    public void setOnMessageReceived(IMessageSentInterface onMessageReceived) {
        //Intentional left blank
    }

    @Override
    public void dispose() {

    }
}
