package com.global.api.builders;

import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.PhoneNumber;
import com.global.api.entities.enums.*;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.HashMap;

@Accessors(chain = true)
@Getter
@Setter
public class SecureBuilder<TResult> extends BaseBuilder<TResult> {

    protected BigDecimal amount;
    protected String currency;
    protected DateTime orderCreateDate;
    protected OrderTransactionType orderTransactionType;
    protected String orderId;
    protected String referenceNumber;
    protected Boolean addressMatchIndicator;
    protected Address shippingAddress;
    protected ShippingMethod shippingMethod;
    protected Boolean shippingNameMatchesCardHolderName;
    protected DateTime shippingAddressCreateDate;
    protected AgeIndicator shippingAddressUsageIndicator;
    protected BigDecimal giftCardAmount;
    protected Integer giftCardCount;
    protected String giftCardCurrency;
    protected String customerEmail;
    protected String deliveryEmail;
    protected DeliveryTimeFrame deliveryTimeframe;
    protected DateTime preOrderAvailabilityDate;
    protected PreOrderIndicator preOrderIndicator;
    protected ReorderIndicator reorderIndicator;
    protected String customerAccountId;
    protected AgeIndicator accountAgeIndicator;
    protected DateTime accountChangeDate;
    protected DateTime accountCreateDate;
    protected AgeIndicator accountChangeIndicator;
    protected DateTime passwordChangeDate;
    protected AgeIndicator passwordChangeIndicator;
    protected HashMap<PhoneNumberType, PhoneNumber> phoneList;
    protected String homeCountryCode;
    protected String homeNumber;
    protected String workCountryCode;
    protected String workNumber;
    protected String mobileCountryCode;
    protected String mobileNumber;
    protected DateTime paymentAccountCreateDate;
    protected AgeIndicator paymentAgeIndicator;
    protected Boolean previousSuspiciousActivity;
    protected SuspiciousAccountActivity suspiciousAccountActivity;
    protected Integer numberOfPurchasesInLastSixMonths;
    protected Integer numberOfTransactionsInLast24Hours;
    protected Integer numberOfAddCardAttemptsInLast24Hours;
    protected Integer numberOfTransactionsInLastYear;
    protected BrowserData browserData;
    protected String idempotencyKey;
    protected String priorAuthenticationData;
    protected PriorAuthenticationMethod priorAuthenticationMethod;
    protected String priorAuthenticationTransactionId;
    protected DateTime priorAuthenticationTimestamp;
    protected Integer maxNumberOfInstallments;
    protected DateTime recurringAuthorizationExpiryDate;
    protected Integer recurringAuthorizationFrequency;
    protected String customerAuthenticationData;
    protected CustomerAuthenticationMethod customerAuthenticationMethod;
    protected DateTime customerAuthenticationTimestamp;
    protected AuthenticationSource authenticationSource;
    protected IPaymentMethod paymentMethod;
    protected TransactionType transactionType;
    protected Address billingAddress;

    @Override
    public void setupValidations() {

    }

}
