package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Product;
import com.global.api.entities.enums.PaymentMethodFunction;
import com.global.api.entities.enums.StatusChangeReason;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.payFac.*;
import com.global.api.paymentMethods.CreditCardData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class PayFacBuilder<TResult> extends BaseBuilder<TResult> {
    private TransactionType transactionType;
    private TransactionModifier transactionModifier;
    // Primary Bank Account Information - Optional. Used to add a bank account to which funds can be settled
    private BankAccountData bankAccountData;
    // User/Merchant Personal Data
    private UserPersonalData userPersonalData;
    private CreditCardData creditCardInformation;
    private String description;
    private List<Product> productData;
    private List<Person> personsData;
    private int page;
    private int pageSize;
    private PaymentStatistics paymentStatistics;
    private StatusChangeReason statusChangeReason;
    private UserReference userReference;
    private HashMap<String, PaymentMethodFunction> paymentMethodsFunctions;
    private String idempotencyKey;

    public PayFacBuilder(TransactionType type) {
        this.transactionType = type;
        this.transactionModifier = TransactionModifier.None;
    }

    public PayFacBuilder(TransactionType type, TransactionModifier modifier) {
        if (modifier == null) {
            modifier = TransactionModifier.None;
        }

        this.transactionType = type;
        this.transactionModifier = modifier;
    }

    @Override
    public TResult execute(String configName) throws ApiException {

        if (configName == null) {
            configName = "default";
        }

        super.execute(configName);

        var client = ServicesContainer.getInstance().getPayFac(configName);

        switch (transactionModifier) {
            case Merchant:
                return client.processBoardingUser(this);

            default:
                break;
        }

        return client.processPayFac(this);
    }

    @Override
    public void setupValidations() {
        validations
                .of(EnumSet.of(TransactionType.Fetch, TransactionType.Edit))
                .with(TransactionModifier.Merchant)
                .check("userReference").propertyOf(String.class, "userId").isNotNull();
    }

    public PayFacBuilder<TResult> withBankAccountData(BankAccountData bankAccountData, PaymentMethodFunction paymentMethodFunction) {
        this.bankAccountData = bankAccountData;

        if (paymentMethodFunction != null) {
            if (paymentMethodsFunctions == null) {
                paymentMethodsFunctions = new HashMap<>();
            }
            paymentMethodsFunctions.put(bankAccountData.getAccountType(), paymentMethodFunction);
        }
        return this;
    }

    public PayFacBuilder<TResult> withDescription(String description) {
        this.description = description;
        return this;
    }

    public PayFacBuilder<TResult> withProductData(List<Product> productData) {
        this.productData = productData;
        return this;
    }

    public PayFacBuilder<TResult> withPersonsData(List<Person> personsData) {
        this.personsData = personsData;
        return this;
    }

    public PayFacBuilder<TResult> withUserReference(UserReference userReference) {
        this.userReference = userReference;
        return this;
    }

    public PayFacBuilder<TResult> withModifier(TransactionModifier transactionModifier) {
        this.transactionModifier = transactionModifier;
        return this;
    }

    public PayFacBuilder<TResult> withPaymentStatistics(PaymentStatistics paymentStatistics) {
        this.paymentStatistics = paymentStatistics;
        return this;
    }

    public PayFacBuilder<TResult> withStatusChangeReason(StatusChangeReason statusChangeReason) {
        this.statusChangeReason = statusChangeReason;
        return this;
    }


    // Set the gateway paging criteria for the report
    public PayFacBuilder<TResult> withPaging(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public PayFacBuilder<TResult> withIdempotencyKey(String value) {
        this.idempotencyKey = value;
        return this;
    }

    public PayFacBuilder<TResult> withUserPersonalData(UserPersonalData userPersonalData) {
        this.userPersonalData = userPersonalData;
        return this;
    }

    public PayFacBuilder<TResult> withCreditCardData(CreditCardData creditCardInformation, PaymentMethodFunction paymentMethodFunction) {
        this.creditCardInformation = creditCardInformation;

        if (paymentMethodFunction != null) {
            if (paymentMethodsFunctions == null) {
                paymentMethodsFunctions = new HashMap<>();
            }
            paymentMethodsFunctions.put(creditCardInformation.getCardType(), paymentMethodFunction);
        }
        return this;
    }

}