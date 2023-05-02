package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Product;
import com.global.api.entities.enums.*;
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
    private HashMap<AddressType, Address> addresses;
    private String accountNumber;

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

        if (client.hasBuiltInMerchantManagementService()) {
            return client.processBoardingUser(this);
        }

        return client.processPayFac(this);
    }

    @Override
    public void setupValidations() {
        validations
                .of(TransactionType.Create)
                .with(TransactionModifier.Merchant)
                .check("userPersonalData").isNotNull();

        validations
                .of(EnumSet.of(TransactionType.Fetch, TransactionType.Edit))
                .with(TransactionModifier.Merchant)
                .check("userReference").propertyOf(String.class, "userId").isNotNull();

        validations
                .of(TransactionType.EditAccount)
                .check("accountNumber").isNotNull();
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

    public PayFacBuilder<TResult> withAddress(Address value, AddressType type) {
        if (type == null) {
            type = AddressType.Billing;
        }

        if (addresses == null) {
            addresses = new HashMap<>();
        }

        addresses.put(type, value);

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

    public PayFacBuilder<TResult> withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

}