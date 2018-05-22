package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;

import java.math.BigDecimal;

public abstract class EBT implements IPaymentMethod, IBalanceable, IChargable, IRefundable, IPinProtected {
    private String pinBlock;

    public PaymentMethodType getPaymentMethodType() { return PaymentMethodType.EBT; }
    public String getPinBlock() {
        return pinBlock;
    }
    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this).withAmount(amount);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(InquiryType.Foodstamp);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType inquiry) {
        return new AuthorizationBuilder(TransactionType.Balance, this)
                .withBalanceInquiryType(inquiry)
                .withAmount(new BigDecimal("0"));
    }

    public AuthorizationBuilder benefitWithdrawal() { return benefitWithdrawal(null); }
    public AuthorizationBuilder benefitWithdrawal(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.BenefitWithdrawal, this)
                .withAmount(amount).withCashBack(new BigDecimal(0));
    }

    public AuthorizationBuilder refund() { return refund(null); }
    public AuthorizationBuilder refund(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Refund, this).withAmount(amount);
    }
}
