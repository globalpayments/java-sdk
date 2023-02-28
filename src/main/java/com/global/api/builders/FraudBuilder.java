package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.PhoneNumber;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.var;
import org.joda.time.DateTime;

import java.math.BigDecimal;

public class FraudBuilder<TResult> extends SecureBuilder<TResult> {

    public FraudBuilder(TransactionType type) {
        setTransactionType(type);
//        setAuthenticationSource(AuthenticationSource.Browser);
    }

    @Override
    public TResult execute(String configName) throws ApiException {
        super.execute(configName);

        var client = ServicesContainer.getInstance().getFraudCheckClient(configName);
        return (TResult) client.processFraud(this);
    }

    @Override
    public void setupValidations() {
        validations
                .of(TransactionType.RiskAssess)
                .check("paymentMethod").isNotNull();
    }

    public FraudBuilder<TResult> WithAmount(BigDecimal value) {
        setAmount(value);
        return this;
    }

    public FraudBuilder<TResult> WithCurrency(String value) {
        setCurrency(value);
        return this;
    }

    public FraudBuilder<TResult> WithAuthenticationSource(AuthenticationSource value) {
        setAuthenticationSource(value);
        return this;
    }

    public FraudBuilder<TResult> WithOrderCreateDate(DateTime value) {
        setOrderCreateDate(value);
        return this;
    }

    public FraudBuilder<TResult> WithReferenceNumber(String referenceNumber) {
        setReferenceNumber(referenceNumber);
        return this;
    }

    public FraudBuilder<TResult> WithAddressMatchIndicator(Boolean value) {
        setAddressMatchIndicator(value);
        return this;
    }

    public FraudBuilder<TResult> WithAddress(Address address) {
        return WithAddress(address, AddressType.Billing);
    }

    public FraudBuilder<TResult> WithAddress(Address address, AddressType type) {
        if (type.equals(AddressType.Billing)) {
            setBillingAddress(address);
        } else {
            setShippingAddress(address);
        }
        return this;
    }

    public FraudBuilder<TResult> WithGiftCardAmount(BigDecimal giftCardAmount) {
        setGiftCardAmount(giftCardAmount);
        return this;
    }

    public FraudBuilder<TResult> WithGiftCardCount(Integer giftCardCount) {
        setGiftCardCount(giftCardCount);
        return this;
    }

    public FraudBuilder<TResult> WithGiftCardCurrency(String giftCardCurrency) {
        setGiftCardCurrency(giftCardCurrency);
        return this;
    }

    public FraudBuilder<TResult> WithDeliveryEmail(String deliveryEmail) {
        setDeliveryEmail(deliveryEmail);
        return this;
    }

    public FraudBuilder<TResult> WithDeliveryTimeFrame(DeliveryTimeFrame deliveryTimeframe) {
        setDeliveryTimeframe(deliveryTimeframe);
        return this;
    }

    public FraudBuilder<TResult> WithShippingMethod(ShippingMethod shippingMethod) {
        setShippingMethod(shippingMethod);
        return this;
    }

    public FraudBuilder<TResult> WithShippingNameMatchesCardHolderName(Boolean shippingNameMatchesCardHolderName) {
        setShippingNameMatchesCardHolderName(shippingNameMatchesCardHolderName);
        return this;
    }

    public FraudBuilder<TResult> WithPreOrderIndicator(PreOrderIndicator preOrderIndicator) {
        setPreOrderIndicator(preOrderIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithPreOrderAvailabilityDate(DateTime preOrderAvailabilityDate) {
        setPreOrderAvailabilityDate(preOrderAvailabilityDate);
        return this;
    }

    public FraudBuilder<TResult> WithReorderIndicator(ReorderIndicator reorderIndicator) {
        setReorderIndicator(reorderIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithOrderTransactionType(OrderTransactionType orderTransactionType) {
        setOrderTransactionType(orderTransactionType);
        return this;
    }

    public FraudBuilder<TResult> WithCustomerAccountId(String customerAccountId) {
        setCustomerAccountId(customerAccountId);
        return this;
    }

    public FraudBuilder<TResult> WithAccountAgeIndicator(AgeIndicator ageIndicator) {
        setAccountAgeIndicator(ageIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithAccountCreateDate(DateTime accountCreateDate) {
        setAccountCreateDate(accountCreateDate);
        return this;
    }

    public FraudBuilder<TResult> WithAccountChangeDate(DateTime accountChangeDate) {
        setAccountChangeDate(accountChangeDate);
        return this;
    }

    public FraudBuilder<TResult> WithAccountChangeIndicator(AgeIndicator accountChangeIndicator) {
        setAccountChangeIndicator(accountChangeIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithPasswordChangeDate(DateTime passwordChangeDate) {
        setPasswordChangeDate(passwordChangeDate);
        return this;
    }

    public FraudBuilder<TResult> WithPasswordChangeIndicator(AgeIndicator passwordChangeIndicator) {
        setPasswordChangeIndicator(passwordChangeIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithPhoneNumber(String phoneCountryCode, String number, PhoneNumberType type) {
        var phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode(phoneCountryCode);
        phoneNumber.setNumber(number);
        //setPhoneList[type](phoneNumber);
        switch (type) {
            case Home:
                setHomeNumber(number);
                setHomeCountryCode(phoneCountryCode);
                break;
            case Work:
                setWorkNumber(number);
                setWorkCountryCode(phoneCountryCode);
                break;
            case Mobile:
                setMobileNumber(number);
                setMobileCountryCode(phoneCountryCode);
                break;
            default:
                break;
        }

        return this;
    }

    public FraudBuilder<TResult> WithPaymentAccountCreateDate(DateTime paymentAccountCreateDate) {
        setPaymentAccountCreateDate(paymentAccountCreateDate);
        return this;
    }

    public FraudBuilder<TResult> WithPaymentAccountAgeIndicator(AgeIndicator paymentAgeIndicator) {
        setPaymentAgeIndicator(paymentAgeIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithPreviousSuspiciousActivity(Boolean previousSuspiciousActivity) {
        setPreviousSuspiciousActivity(previousSuspiciousActivity);
        return this;
    }

    public FraudBuilder<TResult> WithNumberOfPurchasesInLastSixMonths(Integer numberOfPurchasesInLastSixMonths) {
        setNumberOfPurchasesInLastSixMonths(numberOfPurchasesInLastSixMonths);
        return this;
    }

    public FraudBuilder<TResult> WithNumberOfTransactionsInLast24Hours(Integer numberOfTransactionsInLast24Hours) {
        setNumberOfTransactionsInLast24Hours(numberOfTransactionsInLast24Hours);
        return this;
    }

    public FraudBuilder<TResult> WithNumberOfTransactionsInLastYear(Integer numberOfTransactionsInLastYear) {
        setNumberOfTransactionsInLastYear(numberOfTransactionsInLastYear);
        return this;
    }

    public FraudBuilder<TResult> WithNumberOfAddCardAttemptsInLast24Hours(Integer numberOfAddCardAttemptsInLast24Hours) {
        setNumberOfAddCardAttemptsInLast24Hours(numberOfAddCardAttemptsInLast24Hours);
        return this;
    }

    public FraudBuilder<TResult> WithShippingAddressCreateDate(DateTime shippingAddressCreateDate) {
        setShippingAddressCreateDate(shippingAddressCreateDate);
        return this;
    }

    public FraudBuilder<TResult> WithShippingAddressUsageIndicator(AgeIndicator shippingAddressUsageIndicator) {
        setShippingAddressUsageIndicator(shippingAddressUsageIndicator);
        return this;
    }

    public FraudBuilder<TResult> WithPriorAuthenticationMethod(PriorAuthenticationMethod priorAuthenticationMethod) {
        setPriorAuthenticationMethod(priorAuthenticationMethod);
        return this;
    }

    public FraudBuilder<TResult> WithPriorAuthenticationTransactionId(String priorAuthencitationTransactionId) {
        setPriorAuthenticationTransactionId(priorAuthencitationTransactionId);
        return this;
    }

    public FraudBuilder<TResult> WithPriorAuthenticationTimestamp(DateTime priorAuthenticationTimestamp) {
        setPriorAuthenticationTimestamp(priorAuthenticationTimestamp);
        return this;
    }

    public FraudBuilder<TResult> WithPriorAuthenticationData(String priorAuthenticationData) {
        setPriorAuthenticationData(priorAuthenticationData);
        return this;
    }

    public FraudBuilder<TResult> WithMaxNumberOfInstallments(Integer maxNumberOfInstallments) {
        setMaxNumberOfInstallments(maxNumberOfInstallments);
        return this;
    }

    public FraudBuilder<TResult> WithRecurringAuthorizationFrequency(Integer recurringAuthorizationFrequency) {
        setRecurringAuthorizationFrequency(recurringAuthorizationFrequency);
        return this;
    }

    public FraudBuilder<TResult> WithRecurringAuthorizationExpiryDate(DateTime recurringAuthorizationExpiryDate) {
        setRecurringAuthorizationExpiryDate(recurringAuthorizationExpiryDate);
        return this;
    }

    public FraudBuilder<TResult> WithCustomerAuthenticationData(String customerAuthenticationData) {
        setCustomerAuthenticationData(customerAuthenticationData);
        return this;
    }

    public FraudBuilder<TResult> WithCustomerAuthenticationTimestamp(DateTime customerAuthenticationTimestamp) {
        setCustomerAuthenticationTimestamp(customerAuthenticationTimestamp);
        return this;
    }

    public FraudBuilder<TResult> WithCustomerAuthenticationMethod(CustomerAuthenticationMethod customerAuthenticationMethod) {
        setCustomerAuthenticationMethod(customerAuthenticationMethod);
        return this;
    }

    public FraudBuilder<TResult> WithIdempotencyKey(String value) {
        setIdempotencyKey(value);
        return this;
    }

    public FraudBuilder<TResult> WithBrowserData(BrowserData value) {
        setBrowserData(value);
        return this;
    }

    public FraudBuilder<TResult> WithPaymentMethod(IPaymentMethod value) {
        setPaymentMethod(value);
        return this;
    }

}
