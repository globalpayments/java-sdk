package com.global.api.terminals.genius;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.DeviceInterface;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.genius.builders.MitcManageBuilder;
import com.global.api.terminals.genius.enums.TransactionIdType;

import java.math.BigDecimal;

public class GeniusInterface extends DeviceInterface<GeniusController> {

    public GeniusInterface(GeniusController controller){
        super(controller);
    }

    @Override
    public MitcManageBuilder refundById(BigDecimal amount) throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale,null,TransactionType.Refund).withAmount(amount);
    }

    @Override
    public TerminalResponse getTransactionDetails(TransactionType transactionType, String transactionId, TransactionIdType transactionIdType) throws ApiException {
        return this._controller.processReport(transactionType, transactionId, transactionIdType);
    }

    @Override
    public TerminalManageBuilder Void() throws ApiException {
        return new MitcManageBuilder(TransactionType.Sale, PaymentMethodType.Credit, TransactionType.Void);
    }

    public MitcManageBuilder voidRefund() throws ApiException {
        return new MitcManageBuilder(TransactionType.Refund, PaymentMethodType.Credit ,TransactionType.Void);
    }
}
