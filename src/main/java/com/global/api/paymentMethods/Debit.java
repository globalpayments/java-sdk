package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;

import java.math.BigDecimal;

public abstract class Debit implements IPaymentMethod, IPrePayable, IRefundable, IReversable, IChargable, IEncryptable, IPinProtected {
    private EncryptionData encryptionData;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Debit;
    private String pinBlock;

    public EncryptionData getEncryptionData() {
        return encryptionData;
    }
    public void setEncryptionData(EncryptionData encryptionData) {
        this.encryptionData = encryptionData;
    }
    public String getPinBlock() {
        return pinBlock;
    }
    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }
    public PaymentMethodType getPaymentMethodType() { return paymentMethodType; }

    public AuthorizationBuilder addValue() { return addValue(null); }
    public AuthorizationBuilder addValue(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.AddValue, this).withAmount(amount);
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this).withAmount(amount);
    }

    public AuthorizationBuilder refund() { return refund(null); }
    public AuthorizationBuilder refund(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Refund, this).withAmount(amount);
    }

    public AuthorizationBuilder reverse() { return reverse(null); }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reversal, this).withAmount(amount);
    }
}
