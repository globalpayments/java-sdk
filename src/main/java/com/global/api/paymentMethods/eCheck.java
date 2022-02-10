package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class eCheck implements IPaymentMethod, IChargable, ITokenizable, IAuthable, IVerifiable, IRefundable {
    private String accountNumber;
    private AccountType accountType;
    private boolean achVerify;
    private String bankName;
    private int birthYear;
    private String checkHolderName;
    private String checkName;
    private String checkNumber;
    private CheckType checkType;
    private boolean checkVerify;
    private String driversLicenseNumber;
    private String driversLicenseState;
    private EntryMethod entryMode = EntryMethod.Manual;
    private String micrNumber;
    private PaymentMethodType paymentMethodType = PaymentMethodType.ACH;
    private String phoneNumber;
    private String routingNumber;
    private SecCode secCode;
    private String ssnLast4;
    @Accessors(chain = false) private String token;
    private boolean checkGuarantee;
    private String checkReference;
    private String merchantNotes;
    private Address bankAddress;

    @Override
    public AuthorizationBuilder authorize() {
        return authorize(null, false);
    }

    @Override
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, false);
    }

    @Override
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimated) {
        return
                new AuthorizationBuilder(TransactionType.Auth, this)
                        .withAmount(amount);
    }

    @Override
    public AuthorizationBuilder charge() {
        return charge(null);
    }

    @Override
    public AuthorizationBuilder charge(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Sale, this)
                        .withAmount(amount);
    }

    @Override
    public AuthorizationBuilder verify() {
        return
                new AuthorizationBuilder(TransactionType.Verify, this)
                        .withRequestMultiUseToken(true);
    }

    @Override
    public AuthorizationBuilder refund() {
        return refund(null);
    }

    @Override
    public AuthorizationBuilder refund(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Refund, this)
                        .withAmount(amount);
    }

    @Override
    public String tokenize(String configName) throws ApiException {
        throw new NotImplementedException();
    }

    @Override
    public String tokenize(boolean validateCard, String configName) throws ApiException {
        throw new NotImplementedException();
    }

    @Override
    public boolean updateTokenExpiry(String configName) throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    @Override
    public boolean deleteToken(String configName) {
        try {
            new ManagementBuilder(TransactionType.TokenDelete)
                    .withPaymentMethod(this)
                    .execute(configName);
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }
}