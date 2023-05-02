package com.global.api.services;

import com.global.api.builders.PayFacBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.User;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.enums.UserType;
import com.global.api.entities.payFac.UserReference;
import com.global.api.terminals.abstractions.IDisposable;
import lombok.var;

public class PayFacService implements IDisposable {

    public PayFacBuilder<User> createMerchant() {
        return
                new PayFacBuilder<User>(TransactionType.Create)
                        .withModifier(TransactionModifier.Merchant);
    }

    public PayFacBuilder<User> getMerchantInfo(String merchantId) {
        var userReference = new UserReference();
        userReference.setUserId(merchantId);
        userReference.setUserType(UserType.MERCHANT);

        return
                new PayFacBuilder<User>(TransactionType.Fetch)
                        .withModifier(TransactionModifier.Merchant)
                        .withUserReference(userReference);
    }

    public PayFacBuilder<Transaction> createAccount() {
        return new PayFacBuilder<>(TransactionType.CreateAccount);
    }

    public PayFacBuilder<Transaction> editAccount() {
        return new PayFacBuilder<>(TransactionType.EditAccount);
    }

    @Override
    public void dispose() {
    }

}