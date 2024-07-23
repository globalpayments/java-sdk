package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public abstract class Ewic implements  IPaymentMethod, IBalanceable, IChargable, IPinProtected,IEncryptable{
    @Getter
    private PaymentMethodType paymentMethodType = PaymentMethodType.Debit;
    @Getter @Setter
    private String pinBlock;
    @Getter @Setter
    private EncryptionData encryptionData;
    @Getter @Setter
    private String encryptedPan;
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
    public AuthorizationBuilder storeAndForward( BigDecimal amount){
        return new AuthorizationBuilder(TransactionType.StoreAndForward,this)
                .withAmount(amount);
    }
}
