package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.utils.*;
import org.joda.time.format.DateTimeFormat;

import java.util.*;

import static com.global.api.utils.StringUtils.extractDigits;

public class RealexConnector extends XmlGateway implements IPaymentGateway, IRecurringGateway, ISecure3dProvider {
    private String merchantId;
    private String accountId;
    private String rebatePassword;
    private String refundPassword;
    private String sharedSecret;
    private String channel;
    private HostedPaymentConfig hostedPaymentConfig;

    public Secure3dVersion getVersion() { return Secure3dVersion.ONE; }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public void setRebatePassword(String rebatePassword) {
        this.rebatePassword = rebatePassword;
    }
    public void setRefundPassword(String refundPassword) {
        this.refundPassword = refundPassword;
    }
    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public void setHostedPaymentConfig(HostedPaymentConfig config) {
        this.hostedPaymentConfig = config;
    }

    public boolean supportsRetrieval() { return false; }
    public boolean supportsUpdatePaymentDetails() { return true; }
    public boolean supportsHostedPayments() { return true; }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String timestamp = GenerationUtils.generateTimestamp(builder.getTimestamp());
        String orderId = GenerationUtils.generateOrderId(builder.getOrderId());

        // amount and currency are required for googlePay
        if (builder.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData card = (CreditCardData) builder.getPaymentMethod();
            if (builder.getTransactionModifier() == TransactionModifier.EncryptedMobile) {
                if (card.getToken() == null) {
                    throw new BuilderException("Token can not be null");
                }
                if (card.getMobileType() == MobilePaymentMethodType.GOOGLEPAY && (builder.getAmount() == null || builder.getCurrency() == null)) {
                    throw new BuilderException("Amount and Currency cannot be null for capture");
                }
            }
        }

        // Build Request
        Element request = et.element("request")
                .set("timestamp", timestamp)
                .set("type", mapAuthRequestType(builder));
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);
        if(builder.getAmount() != null) {
            et.subElement(request, "amount").text(StringUtils.toNumeric(builder.getAmount()))
                    .set("currency", builder.getCurrency());
        }

        // Hydrate the payment data fields
        //<editor-fold desc="CREDIT CARD DATA">
        if (builder.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData card = (CreditCardData)builder.getPaymentMethod();

            // for google-pay & apple-pay
            if (builder.getTransactionModifier() == TransactionModifier.EncryptedMobile) {
                et.subElement(request, "mobile", card.getMobileType().getValue());
                et.subElement(request, "token", card.getToken());
            }
            else {
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "number", card.getNumber());
                et.subElement(cardElement, "expdate", card.getShortExpiry());
                et.subElement(cardElement, "chname").text(card.getCardHolderName());
                et.subElement(cardElement, "type", card.getCardType().toUpperCase());

                if (card.getCvn() != null) {
                    Element cvnElement = et.subElement(cardElement, "cvn");
                    et.subElement(cvnElement, "number", card.getCvn());
                    et.subElement(cvnElement, "presind", card.getCvnPresenceIndicator().getValue());
                }
            }

            String hash;
            if(builder.getTransactionType() == TransactionType.Verify)
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, card.getNumber());
            else {
                if (builder.getTransactionModifier() == TransactionModifier.EncryptedMobile)
                    hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency() != null ? builder.getCurrency() : "", card.getToken());
                else
                    hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), card.getNumber());
            }
            et.subElement(request, "sha1hash", hash);
        }
        //</editor-fold>
        //<editor-fold desc="RECURRING PAYMENT METHOD">
        else if(builder.getPaymentMethod() instanceof RecurringPaymentMethod) {
            RecurringPaymentMethod recurring = (RecurringPaymentMethod) builder.getPaymentMethod();
            et.subElement(request, "payerref").text(recurring.getCustomerKey());
            et.subElement(request, "paymentmethod").text(recurring.getKey());

            // CVN
            if (!StringUtils.isNullOrEmpty(builder.getCvn())) {
                Element paymentData = et.subElement(request, "paymentdata");
                Element cvn = et.subElement(paymentData, "cvn");
                et.subElement(cvn, "number").text(builder.getCvn());
            }

            String hash;
            if(builder.getTransactionType() == TransactionType.Verify)
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, recurring.getCustomerKey());
            else hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), recurring.getCustomerKey());
            et.subElement(request, "sha1hash", hash);
        }
        //</editor-fold>
        //<editor-fold desc="TOKEN">
        else {
            // TODO: Token Processing
        }
        //</editor-fold>

        //<editor-fold desc="AUTO/MULTI SETTLE">
        if (builder.getTransactionType() == TransactionType.Sale || builder.getTransactionType() == TransactionType.Auth) {
            String autoSettle = builder.getTransactionType() == TransactionType.Sale ? "1" : builder.isMultiCapture() ? "MULTI" : "0";
            et.subElement(request, "autosettle").set("flag", autoSettle);
        }
        //</editor-fold>

        //<editor-fold desc="CUSTOM DATA">
        if (builder.getCustomData() != null) {
            Element custom = et.subElement(request, "custom");
            ArrayList<String[]> customValues = builder.getCustomData();
            for (String[] values : customValues) {
                for (int i = 1; i <= values.length; i++) {
                    et.subElement(custom, "field" + StringUtils.padLeft("" + i, 2, '0'), values[i - 1]);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="CUSTOMER DATA">
        if (builder.getCustomerData() != null) {
            Customer customerValue = builder.getCustomerData();
            Element customer = et.subElement(request, "customer");
            et.subElement(customer, "customerid", customerValue.getId());
            et.subElement(customer, "firstname", customerValue.getFirstName());
            et.subElement(customer, "lastname", customerValue.getLastName());
            et.subElement(customer, "dateofbirth", customerValue.getDateOfBirth());
            et.subElement(customer, "customerpassword", customerValue.getCustomerPassword());
            et.subElement(customer, "email", customerValue.getEmail());
            et.subElement(customer, "domainname", customerValue.getDomainName());
            et.subElement(customer, "devicefingerprint", customerValue.getDeviceFingerPrint());
            et.subElement(customer, "phonenumber", customerValue.getHomePhone());
        }
        //</editor-fold>

        //<editor-fold desc="DCC">
        if(builder.getDccRateData() != null) {
            DccRateData dccRateData = builder.getDccRateData();

            Element dccInfo = et.subElement(request, "dccinfo");
            et.subElement(dccInfo, "ccp", dccRateData.getDccProcessor());
            et.subElement(dccInfo, "type", "1");
            et.subElement(dccInfo, "ratetype", dccRateData.getDccRateType());

            // authorization elements
            et.subElement(dccInfo, "rate", dccRateData.getCardHolderRate());
            if(dccRateData.getCardHolderAmount() != null) {
                et.subElement(dccInfo, "amount", dccRateData.getCardHolderAmount())
                        .set("currency", dccRateData.getCardHolderCurrency());
            }
        }
        //</editor-fold>

        //<editor-fold desc="DESCRIPTION">
        if(builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }
        //</editor-fold>

        //<editor-fold desc="FRAUD">
        // fraud filter mode
        if (builder.getFraudFilterMode() == FraudFilterMode.Passive){
            et.subElement(request, "fraudfilter").set("mode", builder.getFraudFilterMode().getValue());
        }

        // recurring fraud filter
        if(builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
            et.subElement(request, "recurring")
                    .set("type", builder.getRecurringType().getValue().toLowerCase())
                    .set("sequence", builder.getRecurringSequence().getValue().toLowerCase());
        }

        // fraud Decision Manager
        if (builder.getDecisionManager() != null) {
            DecisionManager dmValues = builder.getDecisionManager();
            Element fraud = et.subElement(request, "fraud");
            Element dm = et.subElement(fraud, "dm");
            et.subElement(dm, "billtohostname", dmValues.getBillToHostName());
            et.subElement(dm, "billtohttpbrowsercookiesaccepted", !dmValues.isBillToHttpBrowserCookiesAccepted() ? "false" : "true");
            et.subElement(dm, "billtohttpbrowseremail", dmValues.getBillToHttpBrowserEmail());
            et.subElement(dm, "billtohttpbrowsertype", dmValues.getBillToHttpBrowserType());
            et.subElement(dm, "billtoipnetworkaddress", dmValues.getBillToIpNetworkAddress());
            et.subElement(dm, "businessrulesscorethreshold", dmValues.getBusinessRulesCoreThreshold());
            et.subElement(dm, "billtopersonalid", dmValues.getBillToPersonalId());
            et.subElement(dm, "invoiceheadertendertype", dmValues.getInvoiceHeaderTenderType());
            et.subElement(dm, "invoiceheaderisgift", !dmValues.isInvoiceHeaderIsGift() ? "false" : "true");
            et.subElement(dm, "decisionmanagerprofile", dmValues.getDecisionManagerProfile());
            et.subElement(dm, "invoiceheaderreturnsaccepted", !dmValues.isInvoiceHeaderReturnsAccepted() ? "false" : "true");
            et.subElement(dm, "itemhosthedge", dmValues.getItemHostHedge().getValue());
            et.subElement(dm, "itemnonsensicalhedge", dmValues.getItemNonsensicalHedge().getValue());
            et.subElement(dm, "itemobscenitieshedge", dmValues.getItemObscenitiesHedge().getValue());
            et.subElement(dm, "itemphonehedge", dmValues.getItemPhoneHedge().getValue());
            et.subElement(dm, "itemtimehedge", dmValues.getItemTimeHedge().getValue());
            et.subElement(dm, "itemvelocityhedge", dmValues.getItemVelocityHedge().getValue());
        }
        //</editor-fold>

        //<editor-fold desc="3DS">
        if(builder.getPaymentMethod() instanceof ISecure3d) {
            ThreeDSecure secureEcom = ((ISecure3d)builder.getPaymentMethod()).getThreeDSecure();
            if(secureEcom != null) {
                Element mpi = et.subElement(request, "mpi");
                et.subElement(mpi, "eci", secureEcom.getEci());
                et.subElement(mpi, "cavv", secureEcom.getCavv());
                et.subElement(mpi, "xid", secureEcom.getXid());
                et.subElement(mpi, "ds_trans_id", secureEcom.getDirectoryServerTransactionId());
                et.subElement(mpi, "authentication_value", secureEcom.getAuthenticationValue());
                et.subElement(mpi, "message_version", secureEcom.getMessageVersion());
                if(secureEcom.getExemptStatus() != null) {
                    et.subElement(mpi, "exempt_status", secureEcom.getExemptStatus().getValue());
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="PRODUCT DATA">
        if (builder.getMiscProductData() != null) {
            ArrayList<String[]> productValues = builder.getMiscProductData();
            Element products = et.subElement(request, "products");
            String str[] = { "productid", "productname", "quantity", "unitprice", "gift", "type", "risk" };
            for (String[] values : productValues) {
                Element product = et.subElement(products, "product");
                for (int x = 0; x < values.length; x++) {
                    et.subElement(product, str[x], values[x]);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="REFUND HASH">
        if (builder.getTransactionType() == TransactionType.Refund) {
            String refundHash = GenerationUtils.generateHash(refundPassword);
            if(refundHash == null)
                refundHash = "";
            et.subElement(request, "refundhash", refundHash);
        }
        //</editor-fold>

        //<editor-fold desc="STORED CREDENTIAL">
        if(builder.getStoredCredential() != null) {
            Element storedCredentialElement = et.subElement(request, "storedcredential");
            et.subElement(storedCredentialElement, "type", EnumUtils.getMapping(builder.getStoredCredential().getType(), Target.Realex));
            if(builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.CardHolder) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(StoredCredentialInitiator.CardHolder, Target.Realex));
            }
            if(builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.Merchant) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(StoredCredentialInitiator.Merchant, Target.Realex));
            }
            if(builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.Scheduled) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(StoredCredentialInitiator.Scheduled, Target.Realex));
            }
            et.subElement(storedCredentialElement, "sequence", EnumUtils.getMapping(builder.getStoredCredential().getSequence(), Target.Realex));
            et.subElement(storedCredentialElement, "srd", builder.getStoredCredential().getSchemeId());
        }
        //</editor-fold>

        //<editor-fold desc="SUPPLEMENTARY DATA">
        if (builder.getSupplementaryData() != null) {
            Element supplementaryData = et.subElement(request, "supplementarydata");
            HashMap<String, ArrayList<String[]>> suppData = builder.getSupplementaryData();

            for (String key : suppData.keySet()) {
                ArrayList<String[]> dataSets = suppData.get(key);

                for(String[] data: dataSets) {
                    Element item = et.subElement(supplementaryData, "item").set("type", key);
                    for(int i = 1; i <= data.length; i++) {
                        et.subElement(item, "field" + StringUtils.padLeft(i, 2, '0'), data[i - 1]);
                    }
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="TSS INFO">
        if (builder.getCustomerId() != null || builder.getProductId() != null || builder.getCustomerIpAddress() != null || builder.getClientTransactionId() != null || builder.getBillingAddress() != null || builder.getShippingAddress() != null) {
            Element tssInfo = et.subElement(request, "tssinfo");
            et.subElement(tssInfo, "custnum", builder.getCustomerId());
            et.subElement(tssInfo, "prodid", builder.getProductId());
            et.subElement(tssInfo, "varref", builder.getClientTransactionId());
            et.subElement(tssInfo, "custipaddress", builder.getCustomerIpAddress());

            if(builder.getBillingAddress() != null)
                tssInfo.append(buildAddress(et, builder.getBillingAddress()));
            if(builder.getShippingAddress() != null)
                tssInfo.append(buildAddress(et, builder.getShippingAddress()));
        }
        //</editor-fold>

        String response = doTransaction(et.toString(request));
        return mapResponse(response, builder);
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        // check for hpp config
        if (hostedPaymentConfig == null)
            throw new ApiException("Hosted configuration missing, Please check you configuration.");

        IRequestEncoder encoder = (hostedPaymentConfig.getVersion() == HppVersion.Version2) ? null : JsonEncoders.base64Encoder();
        JsonDoc request = new JsonDoc(encoder);

        String orderId = GenerationUtils.generateOrderId(builder.getOrderId());
        final String timestamp = GenerationUtils.generateTimestamp(builder.getTimestamp());

        // check for right transaction types
        if (builder.getTransactionType() != TransactionType.Sale && builder.getTransactionType() != TransactionType.Auth && builder.getTransactionType() != TransactionType.Verify)
            throw new UnsupportedTransactionException("Only Charge and Authorize are supported through hpp.");

        request.set("MERCHANT_ID", merchantId);
        request.set("ACCOUNT", accountId);
        request.set("HPP_CHANNEL", channel);
        request.set("ORDER_ID", orderId);
        if(builder.getAmount() != null) {
            request.set("AMOUNT", StringUtils.toNumeric(builder.getAmount()));
        }
        request.set("CURRENCY", builder.getCurrency());
        request.set("TIMESTAMP", timestamp);
        request.set("AUTO_SETTLE_FLAG", (builder.getTransactionType() == TransactionType.Sale) ? "1" : builder.isMultiCapture() ? "MULTI" : "0");
        request.set("COMMENT1", builder.getDescription());
        // request.set("COMMENT2", );
        if(hostedPaymentConfig.isRequestTransactionStabilityScore() != null) {
            request.set("RETURN_TSS", hostedPaymentConfig.isRequestTransactionStabilityScore() ? "1" : "0");
        }
        if(hostedPaymentConfig.isDynamicCurrencyConversionEnabled() != null) {
            request.set("DCC_ENABLE", hostedPaymentConfig.isDynamicCurrencyConversionEnabled() ? "1" : "0");
        }
        if (builder.getHostedPaymentData() != null) {
            HostedPaymentData paymentData = builder.getHostedPaymentData();
            AlternativePaymentType paymentTypesKey[] = paymentData.getPresetPaymentMethods();
            AlternativePaymentType paymentTypesValues;
            StringBuffer paymentValues = new StringBuffer();
            if (paymentTypesKey != null)
                for (int arr = 0; arr < paymentTypesKey.length; arr++) {
                    paymentTypesValues = paymentTypesKey[arr];
                    paymentValues.append(paymentTypesValues.getValue());
                    if (arr != paymentTypesKey.length - 1) {
                        paymentValues.append("|");
                    }
                }
            request.set("CUST_NUM", paymentData.getCustomerNumber());
            if(hostedPaymentConfig.isDisplaySavedCards() != null && paymentData.getCustomerKey() != null) {
                request.set("HPP_SELECT_STORED_CARD", paymentData.getCustomerKey());
            }
            if(paymentData.isOfferToSaveCard() != null) {
                request.set("OFFER_SAVE_CARD", paymentData.isOfferToSaveCard() ? "1" : "0");
            }
            if(paymentData.isCustomerExists()) {
                request.set("PAYER_EXIST", paymentData.isCustomerExists() ? "1" : "0");
            }
            if(hostedPaymentConfig.isDisplaySavedCards() == null) {
                request.set("PAYER_REF", paymentData.getCustomerKey());
            }
            request.set("PMT_REF", paymentData.getPaymentKey());
            request.set("PROD_ID", paymentData.getProductId());
            request.set("HPP_CUSTOMER_COUNTRY", paymentData.getCustomerCountry());
            request.set("HPP_CUSTOMER_FIRSTNAME", paymentData.getCustomerFirstName());
            request.set("HPP_CUSTOMER_LASTNAME", paymentData.getCustomerLastName());
            request.set("MERCHANT_RESPONSE_URL", paymentData.getMerchantResponseUrl());
            request.set("HPP_TX_STATUS_URL", paymentData.getTransactionStatusUrl());
            request.set("PM_METHODS", paymentValues.toString());

            // 3DSv2
            request.set("HPP_CUSTOMER_EMAIL", paymentData.getCustomerEmail());
            request.set("HPP_CUSTOMER_PHONENUMBER_MOBILE", paymentData.getCustomerPhoneMobile());
            request.set("HPP_CHALLENGE_REQUEST_INDICATOR", paymentData.getChallengeRequest());
            if(paymentData.getAddressesMatch() != null) {
                request.set("HPP_ADDRESS_MATCH_INDICATOR", paymentData.getAddressesMatch() ? "TRUE" : "FALSE");
            }
        }

        if (builder.getShippingAddress() != null) {
            // FRAUD VALUES
            request.set("SHIPPING_CODE", generateCode(builder.getShippingAddress()));
            request.set("SHIPPING_CO", CountryUtils.getCountryCodeByCountry(builder.getShippingAddress().getCountry()));

            // 3DS2 VALUES
            request.set("HPP_SHIPPING_STREET1", builder.getShippingAddress().getStreetAddress1());
            request.set("HPP_SHIPPING_STREET2", builder.getShippingAddress().getStreetAddress2());
            request.set("HPP_SHIPPING_STREET3", builder.getShippingAddress().getStreetAddress3());
            request.set("HPP_SHIPPING_CITY", builder.getShippingAddress().getCity());
            request.set("HPP_SHIPPING_STATE", builder.getShippingAddress().getState());
            request.set("HPP_SHIPPING_POSTALCODE", builder.getShippingAddress().getPostalCode());
            request.set("HPP_SHIPPING_COUNTRY", CountryUtils.getNumericCodeByCountry(builder.getShippingAddress().getCountry()));
        }
        // HPP_ADDRESS_MATCH_INDICATOR (is shipping same as billing)

        if (builder.getBillingAddress() != null) {
            // FRAUD VALUES
            request.set("BILLING_CODE", generateCode(builder.getBillingAddress()));
            request.set("BILLING_CO", CountryUtils.getCountryCodeByCountry(builder.getBillingAddress().getCountry()));

            // 3DS2 VALUES
            request.set("HPP_BILLING_STREET1", builder.getBillingAddress().getStreetAddress1());
            request.set("HPP_BILLING_STREET2", builder.getBillingAddress().getStreetAddress2());
            request.set("HPP_BILLING_STREET3", builder.getBillingAddress().getStreetAddress3());
            request.set("HPP_BILLING_CITY", builder.getBillingAddress().getCity());
            request.set("HPP_BILLING_STATE", builder.getBillingAddress().getState());
            request.set("HPP_BILLING_POSTALCODE", builder.getBillingAddress().getPostalCode());
            request.set("HPP_BILLING_COUNTRY", CountryUtils.getNumericCodeByCountry(builder.getBillingAddress().getCountry()));
        }

        request.set("CUST_NUM", builder.getCustomerId());
        request.set("VAR_REF", builder.getClientTransactionId());
        request.set("HPP_LANG", hostedPaymentConfig.getLanguage());
        request.set("MERCHANT_RESPONSE_URL", hostedPaymentConfig.getResponseUrl());
        request.set("CARD_PAYMENT_BUTTON", hostedPaymentConfig.getPaymentButtonText());
        if(hostedPaymentConfig.isCardStorageEnabled() != null) {
            request.set("CARD_STORAGE_ENABLE", hostedPaymentConfig.isCardStorageEnabled() ? "1" : "0");
        }
        if (builder.getTransactionType() == TransactionType.Verify) {
            request.set("VALIDATE_CARD_ONLY", builder.getTransactionType() == TransactionType.Verify ? "1" : "0");
        }
        if(!hostedPaymentConfig.getFraudFilterMode().equals(FraudFilterMode.None)) {
            request.set("HPP_FRAUDFILTER_MODE", hostedPaymentConfig.getFraudFilterMode());
        }
        if(builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
            request.set("RECURRING_TYPE", builder.getRecurringType().getValue().toLowerCase());
            request.set("RECURRING_SEQUENCE", builder.getRecurringSequence().getValue().toLowerCase());
        }
        request.set("HPP_VERSION", hostedPaymentConfig.getVersion());
        request.set("HPP_POST_DIMENSIONS", hostedPaymentConfig.getPostDimensions());
        request.set("HPP_POST_RESPONSE", hostedPaymentConfig.getPostResponse());

        // SUPPLEMENTARY DATA
        if (builder.getSupplementaryData() != null) {
            for (Map.Entry<String, ArrayList<String[]>> entry : builder.getSupplementaryData().entrySet()) {
                for (String[] arrayValues : entry.getValue()) {
                    if (arrayValues.length == 1) {
                        request.set(entry.getKey(), arrayValues[0]);
                    }
                    else {
                        StringBuilder serializedValues = new StringBuilder("[");
                        for (int i = 0; i < arrayValues.length; i++) {
                            serializedValues.append(arrayValues[i]);
                            if ((i + 1) < arrayValues.length) {
                                serializedValues.append(" ,");
                            }
                        }
                        request.set(entry.getKey(), serializedValues.append("]").toString());
                    }
                }
            }
        }

        List<String> toHash = new ArrayList<String>(Arrays.asList(
                timestamp, merchantId, orderId,
                (builder.getAmount() != null) ? StringUtils.toNumeric(builder.getAmount()) : null,
                builder.getCurrency()));

        if(builder.getHostedPaymentData() != null) {
            if (hostedPaymentConfig.isCardStorageEnabled() != null || builder.getHostedPaymentData().isOfferToSaveCard() != null || hostedPaymentConfig.isDisplaySavedCards() != null) {
                toHash.add(builder.getHostedPaymentData().getCustomerKey() != null ? builder.getHostedPaymentData().getCustomerKey() : null);
                toHash.add(builder.getHostedPaymentData().getPaymentKey() != null ? builder.getHostedPaymentData().getPaymentKey() : null);
            }
        }

        if(hostedPaymentConfig.getFraudFilterMode() != null && hostedPaymentConfig.getFraudFilterMode() != FraudFilterMode.None)
            toHash.add(hostedPaymentConfig.getFraudFilterMode().getValue());

        request.set("SHA1HASH", GenerationUtils.generateHash(sharedSecret, toHash.toArray(new String[toHash.size()])));

        return request.toString();
    }

    private String generateCode(Address address) {
        String countryCode = CountryUtils.getCountryCodeByCountry(address.getCountry());
        switch (countryCode) {
            case "GB":
                return extractDigits(address.getPostalCode()) + "|" + extractDigits(address.getStreetAddress1());
            case "US":
            case "CA":
                return address.getPostalCode() + "|" + address.getStreetAddress1();
            default:
                return null;
        }
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String timestamp = GenerationUtils.generateTimestamp();
        String orderId = builder.getOrderId() != null ? builder.getOrderId() : GenerationUtils.generateOrderId();

        Element request = et.element("request")
                .set("timestamp", timestamp)
                .set("type", mapManageRequestType(builder));
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        if(builder.getAmount() != null) {
            et.subElement(request, "amount", StringUtils.toNumeric(builder.getAmount())).set("currency", builder.getCurrency());
        }
        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);
        et.subElement(request, "pasref", builder.getTransactionId());

        //<editor-fold desc="DCC">
        if(builder.getDccRateData() != null) {
            DccRateData dccRateData = builder.getDccRateData();

            Element dccInfo = et.subElement(request, "dccinfo");
            et.subElement(dccInfo, "ccp", dccRateData.getDccProcessor());
            et.subElement(dccInfo, "type", "1");
            et.subElement(dccInfo, "ratetype", dccRateData.getDccRateType());

            // settlement elements
            et.subElement(dccInfo, "rate", dccRateData.getCardHolderRate());
            if(dccRateData.getCardHolderAmount() != null) {
                et.subElement(dccInfo, "amount", dccRateData.getCardHolderAmount())
                        .set("currency", dccRateData.getCardHolderCurrency());
            }
        }
        //</editor-fold>

        // payment method for APM
        if(builder.getAlternativePaymentType() != null) {
            et.subElement(request, "paymentmethod", builder.getAlternativePaymentType().getValue());
        }

        // payer authentication response
        if(builder.getTransactionType().equals(TransactionType.VerifySignature)) {
            et.subElement(request, "pares", builder.getPayerAuthenticationResponse());
        }

        // reason code
        if(builder.getReasonCode() != null) {
            et.subElement(request, "reasoncode").text(builder.getReasonCode());
        }

        if(builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }

        //<editor-fold desc="SUPPLEMENTARY DATA">
        if (builder.getSupplementaryData() != null) {
            Element supplementaryData = et.subElement(request, "supplementarydata");
            HashMap<String, ArrayList<String[]>> suppData = builder.getSupplementaryData();

            for (String key : suppData.keySet()) {
                ArrayList<String[]> dataSets = suppData.get(key);

                for(String[] data: dataSets) {
                    Element item = et.subElement(supplementaryData, "item").set("type", key);
                    for(int i = 1; i <= data.length; i++) {
                        et.subElement(item, "field" + StringUtils.padLeft(i, 2, '0'), data[i - 1]);
                    }
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="TSS INFO">
        if (builder.getCustomerId() != null || builder.getClientTransactionId() != null || builder.getProductId() != null) {
            Element tssInfo = et.subElement(request, "tssinfo");
            et.subElement(tssInfo, "custnum", builder.getCustomerId());
            et.subElement(tssInfo, "prodid", builder.getProductId());
            et.subElement(tssInfo, "varref", builder.getClientTransactionId());
        }
        //</editor-fold>

        et.subElement(request, "sha1hash", GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), builder.getAlternativePaymentType() != null ? builder.getAlternativePaymentType().getValue() : null));

        if(builder.getTransactionType() == TransactionType.Refund) {
            if (builder.getAuthorizationCode() != null) {
                et.subElement(request, "authcode").text(builder.getAuthorizationCode());
            }
            et.subElement(request, "refundhash", GenerationUtils.generateHash(builder.getAlternativePaymentType() != null ? refundPassword : rebatePassword));
        }

        String response = doTransaction(et.toString(request));
        return mapResponse(response, builder);
    }

    public <TResult> TResult processReport(ReportBuilder<TResult> builder, Class<TResult> clazz) throws ApiException {
        ElementTree et = new ElementTree();
        String timestamp = GenerationUtils.generateTimestamp();

        // build request
        Element request = et.element("request")
                .set("type", mapReportType(builder.getReportType()))
                .set("timestamp", timestamp);
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);

        if(builder instanceof TransactionReportBuilder) {
            TransactionReportBuilder<TResult> trb = (TransactionReportBuilder<TResult>)builder;
            et.subElement(request,"orderid", trb.getTransactionId());

            String sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, trb.getTransactionId(), "", "", "");
            et.subElement(request, "sha1hash").text(sha1hash);
        }

        String response = doTransaction(et.toString(request));
        return mapReportResponse(response, builder.getReportType(), clazz);
    }

    public <TResult> TResult processRecurring(RecurringBuilder<TResult> builder, Class<TResult> clazz) throws ApiException {
        ElementTree et = new ElementTree();
        String timestamp = GenerationUtils.generateTimestamp();
        String orderId = builder.getOrderId() != null ? builder.getOrderId() : GenerationUtils.generateOrderId();

        // Build Request
        Element request = et.element("request")
                .set("type", mapRecurringRequestType(builder))
                .set("timestamp", timestamp);
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        et.subElement(request, "orderid", orderId);

        if (builder.getTransactionType() == TransactionType.Create || builder.getTransactionType() == TransactionType.Edit) {
            if (builder.getEntity() instanceof Customer) {
                Customer customer = (Customer) builder.getEntity();
                request.append(buildCustomer(et, customer));
                et.subElement(request, "sha1hash").text(GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, null, null, customer.getKey()));
            }
            else if (builder.getEntity() instanceof RecurringPaymentMethod) {
                RecurringPaymentMethod payment = (RecurringPaymentMethod)builder.getEntity();
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "ref").text(payment.getKey());
                et.subElement(cardElement, "payerref").text(payment.getCustomerKey());

                if (payment.getPaymentMethod() != null) {
                    CreditCardData card = (CreditCardData)payment.getPaymentMethod();
                    String expiry = card.getShortExpiry();
                    et.subElement(cardElement, "number").text(card.getNumber());
                    et.subElement(cardElement, "expdate").text(expiry);
                    et.subElement(cardElement, "chname").text(card.getCardHolderName());
                    et.subElement(cardElement, "type").text(card.getCardType());

                    String sha1hash;
                    if (builder.getTransactionType() == TransactionType.Create)
                        sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, null, null, payment.getCustomerKey(), card.getCardHolderName(), card.getNumber());
                    else sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, payment.getCustomerKey(), payment.getKey(), expiry, card.getNumber());
                    et.subElement(request, "sha1hash").text(sha1hash);
                }
            }
        }
        else if (builder.getTransactionType() == TransactionType.Delete) {
            if (builder.getEntity() instanceof RecurringPaymentMethod) {
                RecurringPaymentMethod payment = (RecurringPaymentMethod)builder.getEntity();
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "ref").text(payment.getKey());
                et.subElement(cardElement, "payerref").text(payment.getCustomerKey());

                String sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, payment.getCustomerKey(), payment.getKey() == null ? payment.getId() : payment.getKey());
                et.subElement(request, "sha1hash").text(sha1hash);
            }
        }

        String response = doTransaction(et.toString(request));
        return mapRecurringResponse(response, builder);
    }

    public  Transaction processSecure3d(Secure3dBuilder builder) throws ApiException {
        TransactionType transType = builder.getTransactionType();
        if(transType.equals(TransactionType.VerifyEnrolled)) {
            AuthorizationBuilder authBuilder = new AuthorizationBuilder(transType, builder.getPaymentMethod())
                    .withAmount(builder.getAmount())
                    .withCurrency(builder.getCurrency())
                    .withOrderId(builder.getOrderId());

            return processAuthorization(authBuilder);
        }
        else if(transType.equals(TransactionType.VerifySignature)) {
            // get our three d secure object
            ThreeDSecure secureEcom = builder.getThreeDSecure();

            // create our transaction reference
            TransactionReference reference = new TransactionReference();
            reference.setOrderId(secureEcom.getOrderId());

            ManagementBuilder managementBuilder = new ManagementBuilder(transType)
                    .withAmount(secureEcom.getAmount())
                    .withCurrency(secureEcom.getCurrency())
                    .withPayerAuthenticationResponse(builder.getPayerAuthenticationResponse())
                    .withPaymentMethod(reference);
            return manageTransaction(managementBuilder);
        }
        throw new UnsupportedTransactionException(String.format("Unknown transaction type %s", transType));
    }

    private Transaction mapResponse(String rawResponse, TransactionBuilder<Transaction> builder) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get("response");

        checkResponse(root);
        Transaction result = new Transaction();
        result.setResponseCode(root.getString("result"));
        result.setResponseMessage(root.getString("message"));
        result.setCvnResponseCode(root.getString("cvnresult"));
        result.setAvsResponseCode(root.getString("avspostcoderesponse"));
        result.setTimestamp(root.getAttributeString("timestamp"));

        TransactionReference transReference = new TransactionReference();
        transReference.setAuthCode(root.getString("authcode"));
        transReference.setOrderId(root.getString("orderid"));
        transReference.setPaymentMethodType(PaymentMethodType.Credit);
        transReference.setTransactionId(root.getString("pasref"));
        transReference.setAlternativePaymentType(root.getString("paymentmethod"));
        transReference.setBatchNumber(root.getInt("batchid"));
        result.setTransactionReference(transReference);

        // dccinfo
        if (root.has("dccinfo")) {
            DccRateData dccRateData = new DccRateData();
            if(builder instanceof AuthorizationBuilder && ((AuthorizationBuilder) builder).getDccRateData() != null) {
                dccRateData = ((AuthorizationBuilder) builder).getDccRateData();
            }

            dccRateData.setCardHolderCurrency(root.getString("cardholdercurrency"));
            dccRateData.setCardHolderAmount(root.getDecimal("cardholderamount"));
            dccRateData.setCardHolderRate(root.getString("cardholderrate"));
            dccRateData.setMerchantCurrency(root.getString("merchantcurrency"));
            dccRateData.setMerchantAmount(root.getDecimal("merchantamount"));
            dccRateData.setMarginRatePercentage(root.getString("marginratepercentage"));
            dccRateData.setExchangeRateSourceName(root.getString("exchangeratesourcename"));
            dccRateData.setCommissionPercentage(root.getString("commissionpercentage"));
            dccRateData.setExchangeRateSourceTimestamp(root.getDateTime(DateTimeFormat.forPattern("yyyyMMdd hh:mm"), "exchangeratesourcetimestamp"));
            result.setDccRateData(dccRateData);
        }

        // 3d secure enrolled
        if(root.has("enrolled")) {
            ThreeDSecure secureEcom = new ThreeDSecure();
            secureEcom.setEnrolled(root.getString("enrolled").equals("Y"));
            secureEcom.setPayerAuthenticationRequest(root.getString("pareq"));
            secureEcom.setXid(root.getString("xid"));
            secureEcom.setIssuerAcsUrl(root.getString("url"));
            result.setThreeDsecure(secureEcom);
        }

        // three d secure
        if(root.has("threedsecure")) {
            ThreeDSecure secureEcom = new ThreeDSecure();
            secureEcom.setStatus(root.getString("status"));
            secureEcom.setEci(root.getString("eci"));
            secureEcom.setXid(root.getString("xid"));
            secureEcom.setCavv(root.getString("cavv"));
            secureEcom.setAlgorithm(root.getInt("algorithm"));
            result.setThreeDsecure(secureEcom);
        }

        // stored credential
        result.setSchemeId(root.getString("srd"));

        // fraud response
        if(root.has("fraudresponse")) {
            Element fraudResponseElement = root.get("fraudresponse");

            FraudResponse fraudResponse =
                    new FraudResponse()
                            .setMode(FraudFilterMode.fromString(fraudResponseElement.getAttributeString("mode")))
                            .setResult(fraudResponseElement.getString("result"));

            if (fraudResponseElement.has("rules")) {
                for (Element rule : fraudResponseElement.get("rules").getAll("rule")) {
                    fraudResponse.addRule(
                            new FraudResponse.Rule()
                                    .setName(rule.getAttributeString("name"))
                                    .setId(rule.getAttributeString("id"))
                                    .setAction(rule.getString("action")));
                }
            }

            result.setFraudResponse(fraudResponse);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <TResult> TResult mapReportResponse(String rawResponse, ReportType reportType, Class<TResult> clazz) throws ApiException {
        Element response = ElementTree.parse(rawResponse).get("response");
        checkResponse(response);

        try {
            TResult rvalue = clazz.newInstance();
            if(reportType.equals(ReportType.TransactionDetail)) {
                TransactionSummary summary = new TransactionSummary();
                summary.setTransactionId(response.getString("pasref"));
                summary.setClientTransactionId(response.getString("orderid"));
                summary.setAuthCode(response.getString("authcode"));
                summary.setMaskedCardNumber(response.getString("cardnumber"));
                summary.setAvsResponseCode(response.getString("avspostcoderesponse"));
                summary.setCvnResponseCode(response.getString("cvnresult"));
                summary.setGatewayResponseCode(response.getString("result"));
                summary.setGatewayResponseMessage(response.getString("message"));
                summary.setBatchId(response.getString("batchid"));

                if(response.has("fraudresponse")) {
                    Element fraud = response.get("fraudresponse");
                    summary.setFraudRuleInfo(fraud.getString("result"));
                }

                if(response.has("threedsecure")) {
                    summary.setCavvResponseCode(response.getString("cavv"));
                    summary.setEciIndicator(response.getString("eci"));
                    summary.setXid(response.getString("xid"));
                }

                rvalue = (TResult)summary;
            }
            return rvalue;
        }
        catch(Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <TResult> TResult mapRecurringResponse(String rawResponse, RecurringBuilder<TResult> builder) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get("response");

        // check response
        checkResponse(root);
        return (TResult) builder.getEntity();
    }

    private void checkResponse(Element root) throws GatewayException {
        checkResponse(root, null);
    }
    private void checkResponse(Element root, List<String> acceptCodes) throws GatewayException {
        if(acceptCodes == null) {
            acceptCodes = new ArrayList<String>();
            acceptCodes.add("00");
        }

        String responseCode = root.getString("result");
        String responseMessage = root.getString("message");
        if(!acceptCodes.contains(responseCode)) {
            throw new GatewayException(String.format("Unexpected Gateway Response: %s - %s", responseCode, responseMessage), responseCode, responseMessage);
        }
    }

    private String mapAuthRequestType(AuthorizationBuilder builder) throws ApiException {
        TransactionType trans = builder.getTransactionType();
        IPaymentMethod payment = builder.getPaymentMethod();

        switch(trans) {
            case Sale:
            case Auth: {
                if (payment instanceof Credit) {
                    if (builder.getTransactionModifier().equals(TransactionModifier.Offline)) {
                        if (builder.getPaymentMethod() != null) {
                            return "manual";
                        }
                        return "offline";
                    }
                    else if (builder.getTransactionModifier().equals(TransactionModifier.EncryptedMobile)) {
                        return "auth-mobile";
                    }
                    return "auth";
                }
                return "receipt-in";
            }
            case Capture: {
                return "settle";
            }
            case Verify: {
                if(payment instanceof RecurringPaymentMethod) {
                    return "receipt-in-otb";
                }
                return "otb";
            }
            case Refund: {
                if (payment instanceof RecurringPaymentMethod) {
                    return "payment-out";
                }
                return "credit";
            }
            case DccRateLookup: {
                if(payment instanceof RecurringPaymentMethod) {
                    return "realvault-dccrate";
                }
                return "dccrate";
            }
            case VerifyEnrolled: {
                if(payment instanceof RecurringPaymentMethod) {
                    return "realvault-3ds-verifyenrolled";
                }
                return "3ds-verifyenrolled";
            }
            case Reversal: {
                throw new UnsupportedTransactionException();
            }
            default: {
                return "unknown";
            }
        }
    }

    private String mapManageRequestType(ManagementBuilder builder) {
        TransactionType trans = builder.getTransactionType();

        switch(trans) {
            case Capture:
                return "settle";
            case Hold:
                return "hold";
            case Refund:
                if (builder.getAlternativePaymentType() != null)
                    return "payment-credit";
                return "rebate";
            case Release:
                return "release";
            case Void:
            case Reversal:
                return "void";
            case VerifySignature:
                return "3ds-verifysig";
            default:
                return "unknown";
        }
    }

    private String mapReportType(ReportType reportType) throws ApiException {
        switch (reportType) {
//            case TransactionDetail:
//                return "query";
            default:
                throw new UnsupportedTransactionException("This reporting call is not supported by your currently configured gateway.");
        }
    }

    @SuppressWarnings("unchecked")
    private <TResult> String mapRecurringRequestType(RecurringBuilder<TResult> builder) throws UnsupportedTransactionException {
        TResult entity = (TResult) builder.getEntity();
        switch(builder.getTransactionType()) {
            case Create:
                if(entity instanceof Customer) {
                    return "payer-new";
                }
                else if(entity instanceof IPaymentMethod) {
                    return "card-new";
                }
                throw new UnsupportedTransactionException();
            case Edit:
                if(entity instanceof Customer) {
                    return "payer-edit";
                }
                else if(entity instanceof IPaymentMethod) {
                    return "card-update-card";
                }
                throw new UnsupportedTransactionException();
            case Delete:
                if(entity instanceof RecurringPaymentMethod) {
                    return "card-cancel-card";
                }
                throw new UnsupportedTransactionException();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private Element buildCustomer(ElementTree et, Customer customer) {
        Element payer = et.element("payer")
                .set("ref", GenerationUtils.generateRecurringKey(customer.getKey()))
                .set("type", "Retail");
        et.subElement(payer, "title", customer.getTitle());
        et.subElement(payer, "firstname", customer.getFirstName());
        et.subElement(payer, "surname", customer.getLastName());
        et.subElement(payer, "company", customer.getCompany());

        if (customer.getAddress() != null) {
            Address addy = customer.getAddress();
            Element address = et.subElement(payer, "address");
            et.subElement(address, "line1", addy.getStreetAddress1());
            et.subElement(address, "line2", addy.getStreetAddress2());
            et.subElement(address, "line3", addy.getStreetAddress3());
            et.subElement(address, "city", addy.getCity());
            et.subElement(address, "county", addy.getProvince());
            et.subElement(address, "postcode", addy.getPostalCode());
            Element country = et.subElement(address, "country", customer.getAddress().getCountry());
            if (country != null)
                country.set("code", customer.getAddress().getCountryCode());
        }

        Element phone = et.subElement(payer, "phonenumbers");
        et.subElement(phone, "home", customer.getHomePhone());
        et.subElement(phone, "work", customer.getWorkPhone());
        et.subElement(phone, "fax", customer.getFax());
        et.subElement(phone, "mobile", customer.getMobilePhone());

        et.subElement(payer, "email", customer.getEmail());

        // comments
        return payer;
    }

    private Element buildAddress(ElementTree et, Address address) {
        if(address == null)
            return null;

        String code = address.getPostalCode();
        if(!StringUtils.isNullOrEmpty(code) && !code.contains("|")) {
            if (address.getStreetAddress1() != null) {
                code = String.format("%s|%s", address.getPostalCode(), address.getStreetAddress1());
            }
            else{
                code = String.format("%s|", address.getPostalCode());
            }
            if (address.isCountry("GB"))
                if (address.getStreetAddress1() != null) {
                    code = String.format("%s|%s", address.getPostalCode().replaceAll("[^0-9]", ""), address.getStreetAddress1().replaceAll("[^0-9]", ""));
                }
                else{
                    code = String.format("%s|", address.getPostalCode().replaceAll("[^0-9]", ""));
                }
        }

        Element addressNode = et.element("address").set("type", address.getType().equals(AddressType.Billing) ? "billing" : "shipping");
        et.subElement(addressNode, "code").text(code);
        et.subElement(addressNode, "country").text(address.getCountry());

        return addressNode;
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
    	throw new ApiException("Realex does not support KeepAlive.");
    }
}
