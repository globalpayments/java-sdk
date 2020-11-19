package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.ISecure3dProvider;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ISecure3d;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import com.global.api.entities.MerchantDataCollection;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.enums.TransactionType;

import java.math.BigDecimal;

public class Secure3dBuilder extends BaseBuilder<ThreeDSecure> {
    private AgeIndicator accountAgeIndicator;
    private DateTime accountChangeDate;
    private DateTime accountCreateDate;
    private AgeIndicator accountChangeIndicator;
    private boolean addressMatchIndicator;
    private BigDecimal amount;
    private String applicationId;
    private AuthenticationSource authenticationSource = AuthenticationSource.Browser;
    private AuthenticationRequestType authenticationRequestType = AuthenticationRequestType.PaymentTransaction;
    private Address billingAddress;
    private BrowserData browserData;
    private ChallengeRequestIndicator challengeRequestIndicator;
    private String currency;
    private String customerAccountId;
    private String customerAuthenticationData;
    private CustomerAuthenticationMethod customerAuthenticationMethod;
    private DateTime customerAuthenticationTimestamp;
    private String customerEmail;
    private DecoupledFlowRequest decoupledFlowRequest;
    private Integer decoupledFlowTimeout;
    private String decoupledNotificationUrl;
    private String deliveryEmail;
    private DeliveryTimeFrame deliveryTimeframe;
    private String encodedData;
    private JsonDoc ephemeralPublicKey;
    private Integer giftCardCount;
    private String giftCardCurrency;
    private BigDecimal giftCardAmount;
    private String homeCountryCode;
    private String homeNumber;
    private Integer maxNumberOfInstallments;
    private Integer maximumTimeout;
    private MerchantDataCollection merchantData;
    private MessageCategory messageCategory = MessageCategory.PaymentAuthentication;
    private MerchantInitiatedRequestType merchantInitiatedRequestType;
    private MessageVersion messageVersion;
    private MethodUrlCompletion methodUrlCompletion;
    private String mobileCountryCode;
    private String mobileNumber;
    private Integer numberOfAddCardAttemptsInLast24Hours;
    private Integer numberOfPurchasesInLastSixMonths;
    private Integer numberOfTransactionsInLast24Hours;
    private Integer numberOfTransactionsInLastYear;
    private DateTime orderCreateDate;
    private String orderId;
    private OrderTransactionType orderTransactionType;
    private DateTime passwordChangeDate;
    private AgeIndicator passwordChangeIndicator;
    private DateTime paymentAccountCreateDate;
    private AgeIndicator paymentAgeIndicator;
    private String payerAuthenticationResponse;
    private IPaymentMethod paymentMethod;
    private DateTime preOrderAvailabilityDate;
    private PreOrderIndicator preOrderIndicator;
    private Boolean previousSuspiciousActivity;
    private String priorAuthenticationData;
    private PriorAuthenticationMethod priorAuthenticationMethod;
    private String priorAuthenticationTransactionId;
    private DateTime priorAuthenticationTimestamp;
    private DateTime recurringAuthorizationExpiryDate;
    private Integer recurringAuthorizationFrequency;
    private String referenceNumber;
    private ReorderIndicator reorderIndicator;
    private SdkInterface sdkInterface;
    private String sdkTransactionId;
    private SdkUiType[] sdkUiTypes;
    private Address shippingAddress;
    private DateTime shippingAddressCreateDate;
    private AgeIndicator shippingAddressUsageIndicator;
    private ShippingMethod shippingMethod;
    private Boolean shippingNameMatchesCardHolderName;
    private ThreeDSecure threeDSecure;
    private TransactionType transactionType;
    private WhitelistStatus whitelistStatus;
    private String workCountryCode;
    private String workNumber;

    public AgeIndicator getAccountAgeIndicator() {
        return accountAgeIndicator;
    }
    public DateTime getAccountChangeDate() {
        return accountChangeDate;
    }
    public DateTime getAccountCreateDate() {
        return accountCreateDate;
    }
    public AgeIndicator getAccountChangeIndicator() {
        return accountChangeIndicator;
    }
    public boolean isAddressMatchIndicator() {
        return addressMatchIndicator;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public AuthenticationSource getAuthenticationSource() {
        return authenticationSource;
    }
    public AuthenticationRequestType getAuthenticationRequestType() {
        return authenticationRequestType;
    }
    public Address getBillingAddress() {
        return billingAddress;
    }
    public BrowserData getBrowserData() {
        return browserData;
    }
    public ChallengeRequestIndicator getChallengeRequestIndicator() {
        return challengeRequestIndicator;
    }
    public String getCurrency() {
        return currency;
    }
    public String getCustomerAccountId() {
        return customerAccountId;
    }
    public String getCustomerAuthenticationData() {
        return customerAuthenticationData;
    }
    public CustomerAuthenticationMethod getCustomerAuthenticationMethod() {
        return customerAuthenticationMethod;
    }
    public DateTime getCustomerAuthenticationTimestamp() {
        return customerAuthenticationTimestamp;
    }
    public String getCustomerEmail() {
        return customerEmail;
    }
    public DecoupledFlowRequest getDecoupledFlowRequest() {
        return decoupledFlowRequest;
    }
    public void setDecoupledFlowRequest(DecoupledFlowRequest decoupledFlowRequest) {
        this.decoupledFlowRequest = decoupledFlowRequest;
    }
    public Integer getDecoupledFlowTimeout() {
        return decoupledFlowTimeout;
    }
    public void setDecoupledFlowTimeout(Integer decoupledFlowTimeout) {
        this.decoupledFlowTimeout = decoupledFlowTimeout;
    }
    public String getDecoupledNotificationUrl() {
        return decoupledNotificationUrl;
    }
    public void setDecoupledNotificationUrl(String decoupledNotificationUrl) {
        this.decoupledNotificationUrl = decoupledNotificationUrl;
    }
    public String getDeliveryEmail() {
        return deliveryEmail;
    }
    public DeliveryTimeFrame getDeliveryTimeframe() {
        return deliveryTimeframe;
    }
    public String getEncodedData() {
        return encodedData;
    }
    public JsonDoc getEphemeralPublicKey() {
        return ephemeralPublicKey;
    }
    public Integer getGiftCardCount() {
        return giftCardCount;
    }
    public String getGiftCardCurrency() {
        return giftCardCurrency;
    }
    public BigDecimal getGiftCardAmount() {
        return giftCardAmount;
    }
    public String getHomeCountryCode() {
        return homeCountryCode;
    }
    public String getHomeNumber() {
        return homeNumber;
    }
    public Integer getMaxNumberOfInstallments() {
        return maxNumberOfInstallments;
    }
    public Integer getMaximumTimeout() {
        return maximumTimeout;
    }
    public MerchantDataCollection getMerchantData() {
        return merchantData;
    }
    public MessageCategory getMessageCategory() {
        return messageCategory;
    }
    public MerchantInitiatedRequestType getMerchantInitiatedRequestType() {
        return merchantInitiatedRequestType;
    }
    public void setMerchantInitiatedRequestType(MerchantInitiatedRequestType merchantInitiatedRequestType) {
        this.merchantInitiatedRequestType = merchantInitiatedRequestType;
    }
    public MessageVersion getMessageVersion() {
        return messageVersion;
    }
    public MethodUrlCompletion getMethodUrlCompletion() {
        return methodUrlCompletion;
    }
    public String getMobileCountryCode() {
        return mobileCountryCode;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }
    public Integer getNumberOfAddCardAttemptsInLast24Hours() {
        return numberOfAddCardAttemptsInLast24Hours;
    }
    public Integer getNumberOfPurchasesInLastSixMonths() {
        return numberOfPurchasesInLastSixMonths;
    }
    public Integer getNumberOfTransactionsInLast24Hours() {
        return numberOfTransactionsInLast24Hours;
    }
    public Integer getNumberOfTransactionsInLastYear() {
        return numberOfTransactionsInLastYear;
    }
    public DateTime getOrderCreateDate() {
        return orderCreateDate;
    }
    public String getOrderId() {
        return orderId;
    }
    public OrderTransactionType getOrderTransactionType() {
        return orderTransactionType;
    }
    public DateTime getPasswordChangeDate() {
        return passwordChangeDate;
    }
    public AgeIndicator getPasswordChangeIndicator() {
        return passwordChangeIndicator;
    }
    public DateTime getPaymentAccountCreateDate() {
        return paymentAccountCreateDate;
    }
    public AgeIndicator getPaymentAgeIndicator() {
        return paymentAgeIndicator;
    }
    public String getPayerAuthenticationResponse() {
        return payerAuthenticationResponse;
    }
    public IPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public DateTime getPreOrderAvailabilityDate() {
        return preOrderAvailabilityDate;
    }
    public PreOrderIndicator getPreOrderIndicator() {
        return preOrderIndicator;
    }
    public Boolean getPreviousSuspiciousActivity() {
        return previousSuspiciousActivity;
    }
    public String getPriorAuthenticationData() {
        return priorAuthenticationData;
    }
    public PriorAuthenticationMethod getPriorAuthenticationMethod() {
        return priorAuthenticationMethod;
    }
    public String getPriorAuthenticationTransactionId() {
        return priorAuthenticationTransactionId;
    }
    public DateTime getPriorAuthenticationTimestamp() {
        return priorAuthenticationTimestamp;
    }
    public DateTime getRecurringAuthorizationExpiryDate() {
        return recurringAuthorizationExpiryDate;
    }
    public Integer getRecurringAuthorizationFrequency() {
        return recurringAuthorizationFrequency;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public ReorderIndicator getReorderIndicator() {
        return reorderIndicator;
    }
    public SdkInterface getSdkInterface() {
        return sdkInterface;
    }
    public String getSdkTransactionId() {
        return sdkTransactionId;
    }
    public SdkUiType[] getSdkUiTypes() {
        return sdkUiTypes;
    }
    public String getServerTransactionId() {
        if(threeDSecure != null) {
            return threeDSecure.getServerTransactionId();
        }
        return null;
    }
    public Address getShippingAddress() {
        return shippingAddress;
    }
    public DateTime getShippingAddressCreateDate() {
        return shippingAddressCreateDate;
    }
    public AgeIndicator getShippingAddressUsageIndicator() {
        return shippingAddressUsageIndicator;
    }
    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }
    public Boolean getShippingNameMatchesCardHolderName() {
        return shippingNameMatchesCardHolderName;
    }
    public ThreeDSecure getThreeDSecure() {
        return threeDSecure;
    }
    public TransactionType getTransactionType() {
        return transactionType;
    }
    public WhitelistStatus getWhitelistStatus() {
        return whitelistStatus;
    }
    public void setWhitelistStatus(WhitelistStatus whitelistStatus) {
        this.whitelistStatus = whitelistStatus;
    }
    public String getWorkCountryCode() {
        return workCountryCode;
    }
    public String getWorkNumber() {
        return workNumber;
    }

    // HELPER METHOD FOR THE CONNECTOR
    public boolean hasMobileFields() {
        return(
                !StringUtils.isNullOrEmpty(applicationId) ||
                ephemeralPublicKey != null ||
                maximumTimeout != null ||
                referenceNumber != null ||
                !StringUtils.isNullOrEmpty(sdkTransactionId) ||
                !StringUtils.isNullOrEmpty(encodedData) ||
                sdkInterface != null ||
                sdkUiTypes != null
        );
    }
    public boolean hasPriorAuthenticationData() {
        return (
                priorAuthenticationMethod != null ||
                !StringUtils.isNullOrEmpty(priorAuthenticationTransactionId) ||
                priorAuthenticationTimestamp != null ||
                !StringUtils.isNullOrEmpty(priorAuthenticationData)
        );
    }
    public boolean hasRecurringAuthData() {
        return (
                maxNumberOfInstallments != null ||
                recurringAuthorizationFrequency != null ||
                recurringAuthorizationExpiryDate != null
        );
    }
    public boolean hasPayerLoginData() {
        return (
                !StringUtils.isNullOrEmpty(customerAuthenticationData) ||
                customerAuthenticationTimestamp != null ||
                customerAuthenticationMethod != null
        );
    }

    public Secure3dBuilder withAddress(Address address) {
        return withAddress(address, AddressType.Billing);
    }
    public Secure3dBuilder withAddress(Address address, AddressType type) {
        if(type.equals(AddressType.Billing)) {
            billingAddress = address;
        }
        else {
            shippingAddress = address;
        }
        return this;
    }
    public Secure3dBuilder withAccountAgeIndicator(AgeIndicator ageIndicator) {
        this.accountAgeIndicator = ageIndicator;
        return this;
    }
    public Secure3dBuilder withAccountChangeDate(DateTime accountChangeDate) {
        this.accountChangeDate = accountChangeDate;
        return this;
    }
    public Secure3dBuilder withAccountCreateDate(DateTime accountCreateDate) {
        this.accountCreateDate = accountCreateDate;
        return this;
    }
    public Secure3dBuilder withAccountChangeIndicator(AgeIndicator accountChangeIndicator) {
        this.accountChangeIndicator = accountChangeIndicator;
        return this;
    }
    public Secure3dBuilder withAddressMatchIndicator(boolean value) {
        addressMatchIndicator = value;
        return this;
    }
    public Secure3dBuilder withAmount(BigDecimal value) {
         amount = value;
         return this;
    }
    public Secure3dBuilder withApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }
    public Secure3dBuilder withAuthenticationSource(AuthenticationSource value) {
        authenticationSource = value;
        return this;
    }
    public Secure3dBuilder withAuthenticationRequestType(AuthenticationRequestType value) {
        authenticationRequestType = value;
        return this;
    }
    public Secure3dBuilder withBrowserData(BrowserData value) {
        browserData = value;
        return this;
    }
    public Secure3dBuilder withChallengeRequestIndicator(ChallengeRequestIndicator value) {
        challengeRequestIndicator = value;
        return this;
    }
    public Secure3dBuilder withCurrency(String value) {
        currency = value;
        return this;
    }
    public Secure3dBuilder withCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
        return this;
    }
    public Secure3dBuilder withCustomerAuthenticationData(String customerAuthenticationData) {
        this.customerAuthenticationData = customerAuthenticationData;
        return this;
    }
    public Secure3dBuilder withCustomerAuthenticationMethod(CustomerAuthenticationMethod customerAuthenticationMethod) {
        this.customerAuthenticationMethod = customerAuthenticationMethod;
        return this;
    }
    public Secure3dBuilder withCustomerAuthenticationTimestamp(DateTime customerAuthenticationTimestamp) {
        this.customerAuthenticationTimestamp = customerAuthenticationTimestamp;
        return this;
    }
    public Secure3dBuilder withCustomerEmail(String value) {
        customerEmail = value;
        return this;
    }
    public Secure3dBuilder withDecoupledFlowRequest(DecoupledFlowRequest value) {
        this.decoupledFlowRequest = value;
        return this;
    }
    public Secure3dBuilder withDecoupledFlowTimeout(Integer value) {
        this.decoupledFlowTimeout = value;
        return this;
    }
    public Secure3dBuilder withDecoupledNotificationUrl(String value) {
        this.decoupledNotificationUrl = value;
        return this;
    }
    public Secure3dBuilder withDeliveryEmail(String deliveryEmail) {
        this.deliveryEmail = deliveryEmail;
        return this;
    }
    public Secure3dBuilder withDeliveryTimeFrame(DeliveryTimeFrame deliveryTimeframe) {
        this.deliveryTimeframe = deliveryTimeframe;
        return this;
    }
    public Secure3dBuilder withEncodedData(String encodedData) {
        this.encodedData = encodedData;
        return this;
    }
    public Secure3dBuilder withEphemeralPublicKey(String ephemeralPublicKey) {
        this.ephemeralPublicKey = JsonDoc.parse(ephemeralPublicKey);
        return this;
    }
    public Secure3dBuilder withGiftCardCount(Integer giftCardCount) {
        this.giftCardCount = giftCardCount;
        return this;
    }
    public Secure3dBuilder withGiftCardCurrency(String giftCardCurrency) {
        this.giftCardCurrency = giftCardCurrency;
        return this;
    }
    public Secure3dBuilder withGiftCardAmount(BigDecimal giftCardAmount) {
        this.giftCardAmount = giftCardAmount;
        return this;
    }
    public Secure3dBuilder withHomeNumber(String countryCode, String number) {
        this.homeCountryCode = countryCode;
        this.homeNumber = number;
        return this;
    }
    public Secure3dBuilder withMaxNumberOfInstallments(Integer maxNumberOfInstallments) {
        this.maxNumberOfInstallments = maxNumberOfInstallments;
        return this;
    }
    public Secure3dBuilder withMaximumTimeout(Integer maximumTimeout) {
        this.maximumTimeout = maximumTimeout;
        return this;
    }
    public Secure3dBuilder withMerchantData(MerchantDataCollection value) {
        merchantData = value;
        if(merchantData != null) {
            if(threeDSecure == null) {
                threeDSecure = new ThreeDSecure();
            }
            threeDSecure.setMerchantData(value);
        }

        return this;
    }
    public Secure3dBuilder withMessageCategory(MessageCategory value) {
        messageCategory = value;
        return this;
    }
    public Secure3dBuilder withMerchantInitiatedRequestType(MerchantInitiatedRequestType merchantInitiatedRequestType) {
        this.merchantInitiatedRequestType = merchantInitiatedRequestType;
        return this;
    }
    public Secure3dBuilder withMessageVersion(MessageVersion value) {
        messageVersion = value;
        return this;
    }
    public Secure3dBuilder withMethodUrlCompletion(MethodUrlCompletion value) {
        methodUrlCompletion = value;
        return this;
    }
    public Secure3dBuilder withMobileNumber(String countryCode, String number) {
        mobileCountryCode = countryCode;
        mobileNumber = number;
        return this;
    }
    public Secure3dBuilder withNumberOfAddCardAttemptsInLast24Hours(Integer numberOfAddCardAttemptsInLast24Hours) {
        this.numberOfAddCardAttemptsInLast24Hours = numberOfAddCardAttemptsInLast24Hours;
        return this;
    }
    public Secure3dBuilder withNumberOfPurchasesInLastSixMonths(Integer numberOfPurchasesInLastSixMonths) {
        this.numberOfPurchasesInLastSixMonths = numberOfPurchasesInLastSixMonths;
        return this;
    }
    public Secure3dBuilder withNumberOfTransactionsInLast24Hours(Integer numberOfTransactionsInLast24Hours) {
        this.numberOfTransactionsInLast24Hours = numberOfTransactionsInLast24Hours;
        return this;
    }
    public Secure3dBuilder withNumberOfTransactionsInLastYear(Integer numberOfTransactionsInLastYear) {
        this.numberOfTransactionsInLastYear = numberOfTransactionsInLastYear;
        return this;
    }
    public Secure3dBuilder withOrderCreateDate(DateTime value) {
        orderCreateDate = value;
        return this;
    }
    public Secure3dBuilder withOrderId(String value) {
        orderId = value;
        return this;
    }
    public Secure3dBuilder withOrderTransactionType(OrderTransactionType orderTransactionType) {
        this.orderTransactionType = orderTransactionType;
        return this;
    }
    public Secure3dBuilder withPasswordChangeDate(DateTime passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
        return this;
    }
    public Secure3dBuilder withPasswordChangeIndicator(AgeIndicator passwordChangeIndicator) {
        this.passwordChangeIndicator = passwordChangeIndicator;
        return this;
    }
    public Secure3dBuilder withPaymentAccountCreateDate(DateTime paymentAccountCreateDate) {
        this.paymentAccountCreateDate = paymentAccountCreateDate;
        return this;
    }
    public Secure3dBuilder withPaymentAccountAgeIndicator(AgeIndicator paymentAgeIndicator) {
        this.paymentAgeIndicator = paymentAgeIndicator;
        return this;
    }
    public Secure3dBuilder withPayerAuthenticationResponse(String value) {
        payerAuthenticationResponse = value;
        return this;
    }
    public Secure3dBuilder withPaymentMethod(IPaymentMethod value) {
        paymentMethod = value;
        if(paymentMethod instanceof ISecure3d) {
            ThreeDSecure secureEcom = ((ISecure3d) paymentMethod).getThreeDSecure();
            if(secureEcom != null){
                this.threeDSecure = secureEcom;
            }
        }
        return this;
    }
    public Secure3dBuilder withPreOrderAvailabilityDate(DateTime preOrderAvailabilityDate) {
        this.preOrderAvailabilityDate = preOrderAvailabilityDate;
        return this;
    }
    public Secure3dBuilder withPreOrderIndicator(PreOrderIndicator preOrderIndicator) {
        this.preOrderIndicator = preOrderIndicator;
        return this;
    }
    public Secure3dBuilder withPreviousSuspiciousActivity(Boolean previousSuspiciousActivity) {
        this.previousSuspiciousActivity = previousSuspiciousActivity;
        return this;
    }
    public Secure3dBuilder withPriorAuthenticationData(String priorAuthenticationData) {
        this.priorAuthenticationData = priorAuthenticationData;
        return this;
    }
    public Secure3dBuilder withPriorAuthenticationMethod(PriorAuthenticationMethod priorAuthenticationMethod) {
        this.priorAuthenticationMethod = priorAuthenticationMethod;
        return this;
    }
    public Secure3dBuilder withPriorAuthenticationTransactionId(String priorAuthencitationTransactionId) {
        this.priorAuthenticationTransactionId = priorAuthencitationTransactionId;
        return this;
    }
    public Secure3dBuilder withPriorAuthenticationTimestamp(DateTime priorAuthenticationTimestamp) {
        this.priorAuthenticationTimestamp = priorAuthenticationTimestamp;
        return this;
    }
    public Secure3dBuilder withRecurringAuthorizationExpiryDate(DateTime recurringAuthorizationExpiryDate) {
        this.recurringAuthorizationExpiryDate = recurringAuthorizationExpiryDate;
        return this;
    }
    public Secure3dBuilder withRecurringAuthorizationFrequency(Integer recurringAuthorizationFrequency) {
        this.recurringAuthorizationFrequency = recurringAuthorizationFrequency;
        return this;
    }
    public Secure3dBuilder withReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
        return this;
    }
    public Secure3dBuilder withReorderIndicator(ReorderIndicator reorderIndicator) {
        this.reorderIndicator = reorderIndicator;
        return this;
    }
    public Secure3dBuilder withSdkInterface(SdkInterface sdkInterface) {
        this.sdkInterface = sdkInterface;
        return this;
    }
    public Secure3dBuilder withSdkTransactionId(String sdkTransactionId) {
        this.sdkTransactionId = sdkTransactionId;
        return this;
    }
    public Secure3dBuilder withSdkUiTypes(SdkUiType... sdkUiTypes) {
        this.sdkUiTypes = sdkUiTypes;
        return this;
    }
    public Secure3dBuilder withServerTransactionId(String value) {
        if(threeDSecure == null) {
            threeDSecure = new ThreeDSecure();
        }
        threeDSecure.setServerTransactionId(value);
        return this;
    }
    public Secure3dBuilder withShippingAddressCreateDate(DateTime shippingAddressCreateDate) {
        this.shippingAddressCreateDate = shippingAddressCreateDate;
        return this;
    }
    public Secure3dBuilder withShippingAddressUsageIndicator(AgeIndicator shippingAddressUsageIndicator) {
        this.shippingAddressUsageIndicator = shippingAddressUsageIndicator;
        return this;
    }
    public Secure3dBuilder withShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
        return this;
    }
    public Secure3dBuilder withShippingNameMatchesCardHolderName(Boolean shippingNameMatchesCardHolderName) {
        this.shippingNameMatchesCardHolderName = shippingNameMatchesCardHolderName;
        return this;
    }
    public Secure3dBuilder withThreeDSecure(ThreeDSecure threeDSecure) {
        this.threeDSecure = threeDSecure;
        return this;
    }
    public Secure3dBuilder withTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }
    public Secure3dBuilder withWhitelistStatus(WhitelistStatus whitelistStatus) {
        this.whitelistStatus = whitelistStatus;
        return this;
    }
    public Secure3dBuilder withWorkNumber(String countryCode, String number) {
        this.workCountryCode = countryCode;
        this.workNumber = number;
        return this;
    }

    public Secure3dBuilder(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ThreeDSecure execute() throws ApiException {
        return execute(Secure3dVersion.ANY, "default");
    }
    public ThreeDSecure execute(String configName) throws ApiException {
        return execute(Secure3dVersion.ANY, configName);
    }
    public ThreeDSecure execute(Secure3dVersion version) throws ApiException {
        return execute(version, "default");
    }
    public ThreeDSecure execute(Secure3dVersion version, String configName) throws ApiException {
        validations.validate(this);

        // setup return object
        ThreeDSecure rvalue = this.threeDSecure;
        if(rvalue == null) {
            rvalue = new ThreeDSecure();
            rvalue.setVersion(version);
        }

        // working version
        if(rvalue.getVersion() != null) {
            version = rvalue.getVersion();
        }

        // get the provider
        ISecure3dProvider provider = ServicesContainer.getInstance().getSecure3d(configName, version);
        if(provider != null) {
            boolean canDowngrade = false;
            if(provider.getVersion().equals(Secure3dVersion.TWO) && version.equals(Secure3dVersion.ANY)){
                try{
                    ISecure3dProvider oneProvider = ServicesContainer.getInstance().getSecure3d(configName, Secure3dVersion.ONE);
                    canDowngrade = (oneProvider != null);
                }
                catch(ConfigurationException exc) { /* NOT CONFIGURED */ }
            }

            /* process the request, capture any exceptions which might have been thrown */
            Transaction response = null;
            try{
                response = provider.processSecure3d(this);
                if(response == null && canDowngrade) {
                    return execute(Secure3dVersion.ONE, configName);
                }
            }
            catch(GatewayException exc) {
                // check for not enrolled
                if(exc.getResponseCode() != null) {
                    if (exc.getResponseCode().equals("110") && provider.getVersion().equals(Secure3dVersion.ONE)) {
                        return rvalue;
                    }
                }
                // check if we can downgrade
                else if(canDowngrade && transactionType.equals(TransactionType.VerifyEnrolled)) {
                    return execute(Secure3dVersion.ONE, configName);
                }
                // throw exception
                else throw exc;
            }

            // check the response
            if(response != null) {
                switch (transactionType) {
                    case VerifyEnrolled: {
                        if(response.getThreeDsecure() != null) {
                            rvalue = response.getThreeDsecure();
                            if(rvalue.isEnrolled()) {
                                rvalue.setAmount(amount);
                                rvalue.setCurrency(currency);
                                rvalue.setOrderId(response.getOrderId());
                                rvalue.setVersion(provider.getVersion());
                            }
                            else if(canDowngrade) {
                                return execute(Secure3dVersion.ONE, configName);
                            }
                        }
                        else if(canDowngrade) {
                            return execute(Secure3dVersion.ONE, configName);
                        }
                    } break;
                    case InitiateAuthentication:
                    case VerifySignature: {
                        rvalue.merge(response.getThreeDsecure());
                    } break;
                }
            }
        }

        return rvalue;
    }

    public void setupValidations() {
        validations.of(TransactionType.VerifyEnrolled)
                .check("paymentMethod").isNotNull();

        validations.of(TransactionType.VerifyEnrolled)
                .when("paymentMethod").isNotNull()
                .check("paymentMethod").isInstanceOf(ISecure3d.class);

        validations.of(TransactionType.VerifySignature)
                .when("version").isEqualTo(Secure3dVersion.ONE)
                .check("threeDSecure").isNotNull()
                .when("version").isEqualTo(Secure3dVersion.ONE)
                .check("payerAuthenticationResponse").isNotNull();

        validations.of(TransactionType.VerifySignature)
                .when("version").isEqualTo(Secure3dVersion.TWO)
                .check("serverTransactionId").isNotNull();

        validations.of(TransactionType.InitiateAuthentication)
                .check("threeDSecure").isNotNull();

        validations.of(TransactionType.InitiateAuthentication)
                .when("paymentMethod").isNotNull()
                .check("paymentMethod").isInstanceOf(ISecure3d.class);

        validations.of(TransactionType.InitiateAuthentication)
                .when("merchantInitiatedRequestType").isNotNull()
                .check("merchantInitiatedRequestType").isNotEqual(AuthenticationRequestType.PaymentTransaction);

        validations.of(TransactionType.InitiateAuthentication)
                .when("accountAgeIndicator").isNotNull()
                .check("accountAgeIndicator").isNotEqual(AgeIndicator.NoChange);

        validations.of(TransactionType.InitiateAuthentication)
                .when("passwordChangeIndicator").isNotNull()
                .check("passwordChangeIndicator").isNotEqual(AgeIndicator.NoAccount);

        validations.of(TransactionType.InitiateAuthentication)
                .when("shippingAddressUsageIndicator").isNotNull()
                .check("shippingAddressUsageIndicator").isNotEqual(AgeIndicator.NoChange)
                .when("shippingAddressUsageIndicator").isNotNull()
                .check("shippingAddressUsageIndicator").isNotEqual(AgeIndicator.NoAccount);
    }
}
