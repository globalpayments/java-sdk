package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public abstract class EBT implements IPaymentMethod, IBalanceable, IChargable, IRefundable, IPinProtected, IAuthable, IEncryptable {
    protected EbtCardType ebtCardType;
    private String pinBlock;
    public String cardHolderName;
    @Getter @Setter
    private EncryptionData encryptionData;
    @Getter @Setter
    private String encryptedPan;

    public EbtCardType getEbtCardType() {
        return ebtCardType;
    }
    public void setEbtCardType(EbtCardType ebtCardType) {
        this.ebtCardType = ebtCardType;
    }
    public PaymentMethodType getPaymentMethodType() { return PaymentMethodType.EBT; }
    public String getPinBlock() {
        return pinBlock;
    }
    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }
    public String getCardHolderName() {
        return cardHolderName;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public AuthorizationBuilder authorize() { return authorize(null, false); }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, false);
    }
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimated) {
        return new AuthorizationBuilder(TransactionType.Auth, this)
                .withAmount(amount)
                .withAmountEstimated(isEstimated);
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

    public AuthorizationBuilder fileAction() {
        return new AuthorizationBuilder(TransactionType.FileAction, this);
    }

    public AuthorizationBuilder offlineDecline() { return offlineDecline(null); }
    public AuthorizationBuilder offlineDecline(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Decline, this).withAmount(amount);
    }

}
