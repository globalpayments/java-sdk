package com.global.api.terminals.genius.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.genius.GeniusController;
import lombok.Getter;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MitcManageBuilder extends TerminalManageBuilder {
    public TransactionType followOnTransactionType;
    public boolean receipt = false;
    @Getter
    public boolean allowDuplicates = false;
    public TransactionType originalTransType;

    public MitcManageBuilder(TransactionType originalTransType, PaymentMethodType paymentType, TransactionType followOnTransType) {
        super(originalTransType, paymentType);

        followOnTransactionType = followOnTransType;
        this.originalTransType = originalTransType;
    }

    @Override
    public MitcManageBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }


    public MitcManageBuilder withAllowDuplicates(boolean value) {
        allowDuplicates = value;
        return this;
    }

    public MitcManageBuilder withReceipt(boolean value) {
        receipt = value;
        return this;
    }

    @Override
    public TerminalResponse execute(String configName) throws ApiException {
        GeniusController device = (GeniusController) ServicesContainer.getInstance().getDeviceController(configName);
        try {
            return device.manageTransaction(this);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
