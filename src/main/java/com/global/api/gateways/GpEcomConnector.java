package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.utils.masking.ElementToMask;
import com.global.api.utils.masking.MaskValueUtil;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.utils.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.var;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.global.api.utils.CardUtils.getBaseCardType;
import static com.global.api.utils.StringUtils.extractDigits;

@Accessors(chain = true)
@Setter
public class GpEcomConnector extends XmlGateway implements IPaymentGateway, IRecurringGateway, IReportingService {
    private static HashMap<String, String> mapCardType = new HashMap<String, String>() {{
        put("DinersClub", "Diners");
    }};
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");

    private String merchantId;
    private String accountId;
    private String rebatePassword;
    private String refundPassword;
    private String sharedSecret;
    private String channel;
    private HostedPaymentConfig hostedPaymentConfig;
    @Getter
    @Setter
    private String paymentValues;
    @Getter
    @Setter
    private ShaHashType shaHashType;

    public Secure3dVersion getVersion() {
        return Secure3dVersion.ONE;
    }

    @Override
    public boolean supportsRetrieval() {
        return false;
    }

    @Override
    public boolean supportsUpdatePaymentDetails() {
        return true;
    }

    @Override
    public boolean supportsHostedPayments() {
        return true;
    }

    @Override
    public boolean supportsOpenBanking() {
        return true;
    }

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
        Element request =
                et.element("request")
                        .set("type", mapAuthRequestType(builder))
                        .set("timestamp", timestamp);
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        if (builder.getAmount() != null) {
            et.subElement(request, "amount").text(StringUtils.toNumeric(builder.getAmount()))
                    .set("currency", builder.getCurrency());
        }

        // region AUTO/MULTI SETTLE
        //<editor-fold desc="AUTO/MULTI SETTLE">
        if (builder.getTransactionType() == TransactionType.Sale || builder.getTransactionType() == TransactionType.Auth) {
            String autoSettle = builder.getTransactionType() == TransactionType.Sale ? "1" : builder.isMultiCapture() ? "MULTI" : "0";
            et.subElement(request, "autosettle").set("flag", autoSettle);
        }
        //</editor-fold>

        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);

        // Hydrate the payment data fields
        //<editor-fold desc="CREDIT CARD DATA">
        if (builder.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData card = (CreditCardData) builder.getPaymentMethod();

            // for google-pay & apple-pay
            if (builder.getTransactionModifier() == TransactionModifier.EncryptedMobile) {
                et.subElement(request, "token", card.getToken());
                et.subElement(request, "mobile", card.getMobileType().getValue());
            } else {
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "number", card.getNumber());
                et.subElement(cardElement, "expdate", card.getShortExpiry());
                et.subElement(cardElement, "chname").text(card.getCardHolderName());
                et.subElement(cardElement, "type", mapCardType(getBaseCardType(card.getCardType())).toUpperCase());

                addMaskedData(MaskValueUtil.hideValues(
                        new ElementToMask("request.card.number", card.getNumber(), 4, 6),
                        new ElementToMask("request.card.expdate", card.getShortExpiry())
                ));

                if (card.getCvn() != null) {
                    Element cvnElement = et.subElement(cardElement, "cvn");
                    et.subElement(cvnElement, "number", card.getCvn());
                    et.subElement(cvnElement, "presind", card.getCvnPresenceIndicator().getValue());
                    addMaskedData(MaskValueUtil.hideValues(
                            new ElementToMask("request.card.cvn.number", card.getCvn())
                    ));
                }
            }

            String hash = null;
            if (builder.getTransactionType() == TransactionType.Verify)
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, card.getNumber());
            else {
                if (builder.getTransactionModifier() == TransactionModifier.EncryptedMobile) {
                    switch (card.getMobileType()) {
                        case GOOGLEPAY:
                        case APPLEPAY:
                            hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), card.getToken());
                            break;
                    }
                } else {
                    hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), card.getNumber());
                }
            }
            et.subElement(request, "sha1hash", hash);
        } else if (builder.getPaymentMethod() instanceof AlternativePaymentMethod) {
            this.buildAlternativePaymentMethod(builder, request, et);
        }
        //</editor-fold>

        //<editor-fold desc="DESCRIPTION">
        if (builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }
        //</editor-fold>

        if (builder.getPaymentMethod() instanceof AlternativePaymentMethod) {
            String hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), ((AlternativePaymentMethod) builder.getPaymentMethod()).getAlternativePaymentMethodType().getValue());
            et.subElement(request, "sha1hash").text(hash);
        }

        //<editor-fold desc="RECURRING PAYMENT METHOD">
        if (builder.getPaymentMethod() instanceof RecurringPaymentMethod) {
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
            if (builder.getTransactionType() == TransactionType.Verify)
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, recurring.getCustomerKey());
            else
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), recurring.getCustomerKey());
            et.subElement(request, "sha1hash", hash);
        }
        //</editor-fold>
        //<editor-fold desc="TOKEN">
        else {
            // TODO: Token Processing
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
        if (builder.getDccRateData() != null) {
            DccRateData dccRateData = builder.getDccRateData();

            Element dccInfo = et.subElement(request, "dccinfo");
            et.subElement(dccInfo, "ccp", dccRateData.getDccProcessor());
            et.subElement(dccInfo, "type", "1");
            et.subElement(dccInfo, "ratetype", dccRateData.getDccRateType());

            // authorization elements
            et.subElement(dccInfo, "rate", dccRateData.getCardHolderRate());
            if (dccRateData.getCardHolderAmount() != null) {
                et.subElement(dccInfo, "amount", dccRateData.getCardHolderAmount())
                        .set("currency", dccRateData.getCardHolderCurrency());
            }
        }
        //</editor-fold>

        //<editor-fold desc="FRAUD">
        // fraud filter mode
        if (builder.getFraudFilterMode() != null && builder.getFraudFilterMode() != FraudFilterMode.None) {
            Element fraudfilter = et.subElement(request, "fraudfilter").set("mode", builder.getFraudFilterMode());
            if (builder.getFraudRules() != null) {
                Element rules = et.subElement(fraudfilter, "rules");

                for (FraudRule fraudRule : builder.getFraudRules().getRules()) {
                    Element rule = et.subElement(rules, "rule");
                    rule.set("id", fraudRule.getKey());
                    rule.set("mode", fraudRule.getMode().getValue());
                }
            }
        }

        // recurring fraud filter
        if (builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
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
        if (builder.getPaymentMethod() instanceof ISecure3d) {
            ThreeDSecure secureEcom = ((ISecure3d) builder.getPaymentMethod()).getThreeDSecure();
            if (secureEcom != null) {
                Element mpi = et.subElement(request, "mpi");
                et.subElement(mpi, "eci", secureEcom.getEci());
                et.subElement(mpi, "cavv", secureEcom.getCavv());
                et.subElement(mpi, "xid", secureEcom.getXid());
                et.subElement(mpi, "ds_trans_id", secureEcom.getDirectoryServerTransactionId());
                et.subElement(mpi, "authentication_value", secureEcom.getAuthenticationValue());
                et.subElement(mpi, "message_version", secureEcom.getMessageVersion());
                if (secureEcom.getExemptStatus() != null) {
                    et.subElement(mpi, "exempt_status", secureEcom.getExemptStatus().getValue());
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="PRODUCT DATA">
        if (builder.getMiscProductData() != null) {
            ArrayList<Product> productValues = builder.getMiscProductData();
            Element products = et.subElement(request, "products");
            for (Product values : productValues) {
                Element product = et.subElement(products, "product");
                et.subElement(product, "productid", values.getProductId());
                et.subElement(product, "productname", values.getProductName());
                et.subElement(product, "quantity", values.getQuantity().toString());
                et.subElement(product, "unitprice", StringUtils.toNumeric(values.getUnitPrice()));
                et.subElement(product, "gift", values.isGift() ? "true" : "false");
                et.subElement(product, "type", values.getType());
                et.subElement(product, "risk", values.getRisk());
            }
        }
        //</editor-fold>

        //<editor-fold desc="REFUND HASH">
        if (builder.getTransactionType() == TransactionType.Refund) {
            String refundHash = GenerationUtils.generateHash(refundPassword);
            if (refundHash == null)
                refundHash = "";
            et.subElement(request, "refundhash", refundHash);
        }
        //</editor-fold>

        //<editor-fold desc="STORED CREDENTIAL">
        if (builder.getStoredCredential() != null) {
            Element storedCredentialElement = et.subElement(request, "storedcredential");
            et.subElement(storedCredentialElement, "type", EnumUtils.getMapping(Target.Realex, builder.getStoredCredential().getType()));
            if (builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.CardHolder) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(Target.Realex, StoredCredentialInitiator.CardHolder));
            }
            if (builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.Merchant) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(Target.Realex, StoredCredentialInitiator.Merchant));
            }
            if (builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.Scheduled) {
                et.subElement(storedCredentialElement, "initiator", EnumUtils.getMapping(Target.Realex, StoredCredentialInitiator.Scheduled));
            }
            et.subElement(storedCredentialElement, "sequence", EnumUtils.getMapping(Target.Realex, builder.getStoredCredential().getSequence()));
            et.subElement(storedCredentialElement, "srd", builder.getStoredCredential().getSchemeId());
        }
        //</editor-fold>

        //<editor-fold desc="SUPPLEMENTARY DATA">
        if (builder.getSupplementaryData() != null) {
            Element supplementaryData = et.subElement(request, "supplementarydata");
            HashMap<String, ArrayList<String[]>> suppData = builder.getSupplementaryData();

            for (String key : suppData.keySet()) {
                ArrayList<String[]> dataSets = suppData.get(key);

                for (String[] data : dataSets) {
                    Element item = et.subElement(supplementaryData, "item").set("type", key);
                    for (int i = 1; i <= data.length; i++) {
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

            if (builder.getBillingAddress() != null)
                tssInfo.append(buildAddress(et, builder.getBillingAddress()));
            if (builder.getShippingAddress() != null)
                tssInfo.append(buildAddress(et, builder.getShippingAddress()));
        }
        //</editor-fold>

        // DYNAMIC DESCRIPTOR
        if (builder.getTransactionType() == TransactionType.Auth || builder.getTransactionType() == TransactionType.Capture || builder.getTransactionType() == TransactionType.Refund) {
            if (!StringUtils.isNullOrEmpty(builder.getDynamicDescriptor())) {
                Element narrative = et.subElement(request, "narrative");
                et.subElement(narrative, "chargedescription", builder.getDynamicDescriptor());
            }
        }

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

        if (builder.getPaymentMethod() instanceof BankPayment && builder.getTransactionType() != TransactionType.Sale) {
            throw new UnsupportedTransactionException("Only Charge is supported for Bank Payment through HPP.");
        }

        request.set("MERCHANT_ID", merchantId);
        request.set("ACCOUNT", accountId);
        request.set("HPP_CHANNEL", channel);
        request.set("ORDER_ID", orderId);
        if (builder.getAmount() != null) {
            request.set("AMOUNT", StringUtils.toNumeric(builder.getAmount()));
        }
        request.set("CURRENCY", builder.getCurrency());
        request.set("TIMESTAMP", timestamp);
        request.set("AUTO_SETTLE_FLAG", (builder.getTransactionType() == TransactionType.Sale) ? "1" : builder.isMultiCapture() ? "MULTI" : "0");
        request.set("COMMENT1", builder.getDescription());
        // request.set("COMMENT2", );
        if (hostedPaymentConfig.isRequestTransactionStabilityScore() != null) {
            request.set("RETURN_TSS", hostedPaymentConfig.isRequestTransactionStabilityScore() ? "1" : "0");
        }
        if (hostedPaymentConfig.isDynamicCurrencyConversionEnabled() != null) {
            request.set("DCC_ENABLE", hostedPaymentConfig.isDynamicCurrencyConversionEnabled() ? "1" : "0");
        }
        if (builder.getHostedPaymentData() != null) {
            HostedPaymentData paymentData = builder.getHostedPaymentData();

            request.set("CUST_NUM", paymentData.getCustomerNumber());
            if (hostedPaymentConfig.isDisplaySavedCards() != null && paymentData.getCustomerKey() != null) {
                request.set("HPP_SELECT_STORED_CARD", paymentData.getCustomerKey());
            }
            if (paymentData.isOfferToSaveCard() != null) {
                request.set("OFFER_SAVE_CARD", paymentData.isOfferToSaveCard() ? "1" : "0");
            }
            if (paymentData.isCustomerExists() != null) {
                request.set("PAYER_EXIST", paymentData.isCustomerExists() ? "1" : "0");
            }
            if (hostedPaymentConfig.isDisplaySavedCards() == null) {
                request.set("PAYER_REF", paymentData.getCustomerKey());
            }
            request.set("PMT_REF", paymentData.getPaymentKey());
            request.set("PROD_ID", paymentData.getProductId());

            // APMs Fields
            request.set("HPP_CUSTOMER_COUNTRY", paymentData.getCustomerCountry());
            request.set("HPP_CUSTOMER_FIRSTNAME", paymentData.getCustomerFirstName());
            request.set("HPP_CUSTOMER_LASTNAME", paymentData.getCustomerLastName());
            if (!StringUtils.isNullOrEmpty(paymentData.getCustomerFirstName()) && !StringUtils.isNullOrEmpty(paymentData.getCustomerLastName())) {
                request.set("HPP_NAME", paymentData.getCustomerFirstName() + " " + paymentData.getCustomerLastName());
            }
            request.set("MERCHANT_RESPONSE_URL", paymentData.getMerchantResponseUrl());
            request.set("HPP_TX_STATUS_URL", paymentData.getTransactionStatusUrl());

            request.set("PM_METHODS", getPaymentValueList(paymentData));
            // end APMs Fields

            // 3DSv2
            request.set("HPP_CUSTOMER_EMAIL", paymentData.getCustomerEmail());
            request.set("HPP_CUSTOMER_PHONENUMBER_MOBILE", paymentData.getCustomerPhoneMobile());
            request.set("HPP_PHONE", paymentData.getCustomerPhoneMobile());
            request.set("HPP_CHALLENGE_REQUEST_INDICATOR", paymentData.getChallengeRequestIndicator() != null ? paymentData.getChallengeRequestIndicator().getValue() : null);
            request.set("HPP_ENABLE_EXEMPTION_OPTIMIZATION", builder.getHostedPaymentData().getEnableExemptionOptimization());
            if (paymentData.getAddressesMatch() != null) {
                request.set("HPP_ADDRESS_MATCH_INDICATOR", paymentData.getAddressesMatch() ? "TRUE" : "FALSE");
            }
        } else if (builder.getCustomerId() != null) {
            request.set("CUST_NUM", builder.getCustomerId());
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

        request.set("VAR_REF", builder.getClientTransactionId());
        request.set("HPP_LANG", hostedPaymentConfig.getLanguage());
        request.set("MERCHANT_RESPONSE_URL", hostedPaymentConfig.getResponseUrl());
        request.set("CARD_PAYMENT_BUTTON", hostedPaymentConfig.getPaymentButtonText());
        if (hostedPaymentConfig.isCardStorageEnabled() != null) {
            request.set("CARD_STORAGE_ENABLE", hostedPaymentConfig.isCardStorageEnabled() ? "1" : "0");
        }
        if (builder.getTransactionType() == TransactionType.Verify) {
            request.set("VALIDATE_CARD_ONLY", builder.getTransactionType() == TransactionType.Verify ? "1" : "0");
        }
        if (hostedPaymentConfig.getFraudFilterMode() != FraudFilterMode.None) {
            request.set("HPP_FRAUDFILTER_MODE", hostedPaymentConfig.getFraudFilterMode().getValue());
            if (hostedPaymentConfig.getFraudFilterRules() != null) {
                for (FraudRule fraudRule : hostedPaymentConfig.getFraudFilterRules().getRules()) {
                    request.set("HPP_FRAUDFILTER_RULE_" + fraudRule.getKey(), fraudRule.getMode().getValue());
                }
            }
        }
        if (builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
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
                    } else {
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

        if (builder.getHostedPaymentData() != null) {
            if (hostedPaymentConfig.isCardStorageEnabled() != null || builder.getHostedPaymentData().isOfferToSaveCard() != null || hostedPaymentConfig.isDisplaySavedCards() != null) {
                toHash.add(builder.getHostedPaymentData().getCustomerKey() != null ? builder.getHostedPaymentData().getCustomerKey() : null);
                toHash.add(builder.getHostedPaymentData().getPaymentKey() != null ? builder.getHostedPaymentData().getPaymentKey() : null);
            }

            if (builder.getHostedPaymentData().getCaptureAddress() != null) {
                request.set("HPP_CAPTURE_ADDRESS", builder.getHostedPaymentData().getCaptureAddress().booleanValue() ? "TRUE" : "FALSE");
            }

            if (builder.getHostedPaymentData().getReturnAddress() != null) {
                request.set("HPP_DO_NOT_RETURN_ADDRESS", builder.getHostedPaymentData().getReturnAddress().booleanValue() ? "TRUE" : "FALSE");
            }
        }

        if (hostedPaymentConfig.getFraudFilterMode() != null &&
                hostedPaymentConfig.getFraudFilterMode() != FraudFilterMode.None) {
            toHash.add(hostedPaymentConfig.getFraudFilterMode().getValue());
        }

        if (builder.getPaymentMethod() instanceof BankPayment) {
            BankPayment bankPaymentMethod = (BankPayment) builder.getPaymentMethod();

            request.set("HPP_OB_PAYMENT_SCHEME", bankPaymentMethod.getBankPaymentType() != null ? bankPaymentMethod.getBankPaymentType().toString() : OpenBankingProvider.getBankPaymentType(builder.getCurrency()).toString());
            request.set("HPP_OB_REMITTANCE_REF_TYPE", builder.getRemittanceReferenceType().toString());
            request.set("HPP_OB_REMITTANCE_REF_VALUE", builder.getRemittanceReferenceValue());
            request.set("HPP_OB_DST_ACCOUNT_IBAN", bankPaymentMethod.getIban());
            request.set("HPP_OB_DST_ACCOUNT_NAME", bankPaymentMethod.getAccountName());
            request.set("HPP_OB_DST_ACCOUNT_NUMBER", bankPaymentMethod.getAccountNumber());
            request.set("HPP_OB_DST_ACCOUNT_SORT_CODE", bankPaymentMethod.getSortCode());

            addMaskedData(
                    MaskValueUtil.hideValues(
                            new ElementToMask("request.HPP_OB_DST_ACCOUNT_IBAN", bankPaymentMethod.getIban()),
                            new ElementToMask("request.HPP_OB_DST_ACCOUNT_NUMBER", bankPaymentMethod.getAccountNumber(), 0, bankPaymentMethod.getAccountNumber().length() - 4)
                    )
            );

            if (builder.getHostedPaymentData() != null) {
                var hostedPaymentData = builder.getHostedPaymentData();

                request.set("HPP_OB_CUSTOMER_COUNTRIES", hostedPaymentData.getCustomerCountry());
            }


            if (!StringUtils.isNullOrEmpty(bankPaymentMethod.getSortCode())) {
                toHash.add(bankPaymentMethod.getSortCode());
            }
            if (!StringUtils.isNullOrEmpty(bankPaymentMethod.getAccountNumber())) {
                toHash.add(bankPaymentMethod.getAccountNumber());
            }
            if (!StringUtils.isNullOrEmpty(bankPaymentMethod.getIban())) {
                toHash.add(bankPaymentMethod.getIban());
            }
        }

        if (builder.getDynamicDescriptor() != null) {
            request.set("CHARGE_DESCRIPTION", builder.getDynamicDescriptor());
        }

        request.set("SHA1HASH", GenerationUtils.generateHash(sharedSecret, toHash.toArray(new String[toHash.size()])));

        return request.toString();
    }

    private String getPaymentValueList(HostedPaymentData hostedPaymentData) {

        String hosted = getHostedPaymentMethodsList(hostedPaymentData.getHostedPaymentMethods());
        String alternative = getAlternativePaymentTypeList(hostedPaymentData.getPresetPaymentMethods());

        if (StringUtils.isNullOrEmpty(alternative) && StringUtils.isNullOrEmpty(hosted)) {
            return "";
        } else if (StringUtils.isNullOrEmpty(alternative)) {
            return hosted;
        } else if (StringUtils.isNullOrEmpty(hosted)) {
            return alternative;
        } else {
            return hosted + "|" + alternative;
        }
    }

    private String getHostedPaymentMethodsList(HostedPaymentMethods[] hostedPaymentMethods) {
        if (hostedPaymentMethods == null || hostedPaymentMethods.length == 0) {
            return "";
        }

        StringBuffer hostedPaymentMethodsList = new StringBuffer();
        HostedPaymentMethods hostedPaymentMethod;
        for (int index = 0; index < hostedPaymentMethods.length; index++) {
            hostedPaymentMethod = hostedPaymentMethods[index];
            hostedPaymentMethodsList.append(hostedPaymentMethod.getValue());
            hostedPaymentMethodsList.append("|");
        }
        hostedPaymentMethodsList.deleteCharAt(hostedPaymentMethodsList.length() - 1);

        return hostedPaymentMethodsList.toString();
    }

    private String getAlternativePaymentTypeList(AlternativePaymentType paymentTypesKey[]) {
        if (paymentTypesKey == null || paymentTypesKey.length == 0) {
            return "";
        }

        StringBuffer alternativePaymentTypeList = new StringBuffer();
        AlternativePaymentType alternativePaymentType;
        for (int index = 0; index < paymentTypesKey.length; index++) {
            alternativePaymentType = paymentTypesKey[index];
            alternativePaymentTypeList.append(alternativePaymentType.getValue());
            alternativePaymentTypeList.append("|");
        }
        alternativePaymentTypeList.deleteCharAt(alternativePaymentTypeList.length() - 1);

        return alternativePaymentTypeList.toString();
    }

    private String generateCode(Address address) {
        String countryCode = CountryUtils.getCountryCodeByCountry(address.getCountry());
        switch (countryCode) {
            case "GB":
                return extractDigits(address.getPostalCode()) + (address.getStreetAddress1() != null ? "|" + extractDigits(address.getStreetAddress1()) : "");
            case "US":
            case "CA":
                return address.getPostalCode() + (address.getStreetAddress1() != null ? "|" + address.getStreetAddress1() : "");
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
        if (builder.getAmount() != null) {
            et.subElement(request, "amount", StringUtils.toNumeric(builder.getAmount())).set("currency", builder.getCurrency());
        }
        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);
        et.subElement(request, "pasref", builder.getTransactionId());

        //<editor-fold desc="DCC">
        if (builder.getDccRateData() != null) {
            DccRateData dccRateData = builder.getDccRateData();

            Element dccInfo = et.subElement(request, "dccinfo");
            et.subElement(dccInfo, "ccp", dccRateData.getDccProcessor());
            et.subElement(dccInfo, "type", "1");
            et.subElement(dccInfo, "ratetype", dccRateData.getDccRateType());

            // settlement elements
            et.subElement(dccInfo, "rate", dccRateData.getCardHolderRate());
            if (dccRateData.getCardHolderAmount() != null) {
                et.subElement(dccInfo, "amount", dccRateData.getCardHolderAmount())
                        .set("currency", dccRateData.getCardHolderCurrency());
            }
        }
        //</editor-fold>

        // payer authentication response
        if (builder.getTransactionType().equals(TransactionType.VerifySignature)) {
            et.subElement(request, "pares", builder.getPayerAuthenticationResponse());
        }

        // reason code
        if (builder.getReasonCode() != null) {
            et.subElement(request, "reasoncode").text(builder.getReasonCode().toString());
        }

        if (builder.getAlternativePaymentType() != null) {
            et.subElement(request, "paymentmethod", builder.getAlternativePaymentType());

            if (builder.getTransactionType() == TransactionType.Confirm) {
                Element paymentMethodDetails = et.subElement(request, "paymentmethoddetails");

                AlternativePaymentResponse apmResponse = ((TransactionReference) builder.getPaymentMethod()).getAlternativePaymentResponse();

                if (builder.getAlternativePaymentType() == AlternativePaymentType.PAYPAL) {
                    et.subElement(paymentMethodDetails, "Token", apmResponse.getSessionToken());
                    et.subElement(paymentMethodDetails, "PayerID", apmResponse.getProviderReference());
                }
            }
        }

        if (builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }

        //<editor-fold desc="SUPPLEMENTARY DATA">
        if (builder.getSupplementaryData() != null) {
            Element supplementaryData = et.subElement(request, "supplementarydata");
            HashMap<String, ArrayList<String[]>> suppData = builder.getSupplementaryData();

            for (String key : suppData.keySet()) {
                ArrayList<String[]> dataSets = suppData.get(key);

                for (String[] data : dataSets) {
                    Element item = et.subElement(supplementaryData, "item").set("type", key);
                    for (int i = 1; i <= data.length; i++) {
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

        // dynamic descriptor
        if (builder.getTransactionType() == TransactionType.Capture || builder.getTransactionType() == TransactionType.Refund) {
            if (!StringUtils.isNullOrEmpty(builder.getDynamicDescriptor())) {
                Element narrative = et.subElement(request, "narrative");
                et.subElement(narrative, "chargedescription", builder.getDynamicDescriptor());
            }
        }

        et.subElement(request, "sha1hash", GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), builder.getAlternativePaymentType() != null ? builder.getAlternativePaymentType().getValue() : null));

        if (builder.getTransactionType() == TransactionType.Refund) {
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

        if (builder instanceof TransactionReportBuilder) {
            TransactionReportBuilder<TResult> trb = (TransactionReportBuilder<TResult>) builder;
            et.subElement(request, "orderid", trb.getTransactionId());

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
                .set("timestamp", timestamp)
                .set("type", mapRecurringRequestType(builder));
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "channel", channel);
        et.subElement(request, "account", accountId);

        if (builder.getTransactionType() == TransactionType.Create || builder.getTransactionType() == TransactionType.Edit) {
            if (builder.getEntity() instanceof Customer) {
                et.subElement(request, "orderid", orderId);

                Customer customer = (Customer) builder.getEntity();
                request.append(buildCustomer(et, customer));
                et.subElement(request, "sha1hash").text(GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, null, null, customer.getKey()));
            } else if (builder.getEntity() instanceof RecurringPaymentMethod) {
                et.subElement(request, "orderid", orderId);

                RecurringPaymentMethod payment = (RecurringPaymentMethod) builder.getEntity();
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "ref").text(payment.getKey());
                et.subElement(cardElement, "payerref").text(payment.getCustomerKey());

                CreditCardData card = (CreditCardData) payment.getPaymentMethod();
                String expiry = card.getShortExpiry();

                if (payment.getPaymentMethod() != null) {
                    et.subElement(cardElement, "number").text(card.getNumber());
                    et.subElement(cardElement, "expdate").text(expiry);
                    et.subElement(cardElement, "chname").text(card.getCardHolderName());
                    et.subElement(cardElement, "type").text(mapCardType(getBaseCardType(card.getCardType())));

                    addMaskedData(MaskValueUtil.hideValues(
                            new ElementToMask("request.card.number", card.getNumber(), 4, 6),
                            new ElementToMask("request.card.expdate", card.getShortExpiry())
                    ));
                }

                if (payment.getStoredCredential() != null) {
                    Element storedCredentialElement = et.subElement(request, "storedcredential");
                    et.subElement(storedCredentialElement, "srd", payment.getStoredCredential().getSchemeId());
                }

                String sha1hash;
                if (builder.getTransactionType() == TransactionType.Create)
                    sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, null, null, payment.getCustomerKey(), card.getCardHolderName(), card.getNumber());
                else
                    sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, payment.getCustomerKey(), payment.getKey(), expiry, card.getNumber());
                et.subElement(request, "sha1hash").text(sha1hash);
            }
            //Schedule
            else if (builder.getEntity() instanceof Schedule) {
                var schedule = (Schedule) builder.getEntity();
                var amount = StringUtils.toNumeric(schedule.getAmount());
                var frequency = MapScheduleFrequency(schedule.getFrequency());
                var hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, schedule.getId(), amount, schedule.getCurrency(), schedule.getCustomerKey(), frequency);

                et.subElement(request, "scheduleref", schedule.getId());
                et.subElement(request, "alias", schedule.getName());
                et.subElement(request, "orderidstub", schedule.getOrderPrefix());
                et.subElement(request, "transtype", "auth");
                et.subElement(request, "schedule", frequency);

                if (schedule.getStartDate() != null) {
                    et.subElement(request, "startdate", schedule.getStartDate() != null ? SDF.format(schedule.getStartDate()) : null);
                }
                et.subElement(request, "numtimes", schedule.getNumberOfPayments());
                if (schedule.getEndDate() != null) {
                    et.subElement(request, "enddate", schedule.getEndDate() != null ? SDF.format(schedule.getEndDate()) : null);
                }
                et.subElement(request, "payerref", schedule.getCustomerKey());
                et.subElement(request, "paymentmethod", schedule.getPaymentKey());
                if (!StringUtils.isNullOrEmpty(amount)) {
                    et.subElement(request, "amount", amount)
                            .set("currency", schedule.getCurrency());
                }

                et.subElement(request, "prodid", schedule.getProductId() != null ? schedule.getProductId() : "");
                et.subElement(request, "varref", schedule.getPoNumber() != null ? schedule.getPoNumber() : "");
                et.subElement(request, "custno", schedule.getCustomerNumber());
                et.subElement(request, "comment", schedule.getDescription());
                et.subElement(request, "sha1hash").text(hash);
            }
        } else if (builder.getTransactionType() == TransactionType.Delete) {
            if (builder.getEntity() instanceof RecurringPaymentMethod) {
                RecurringPaymentMethod payment = (RecurringPaymentMethod) builder.getEntity();
                Element cardElement = et.subElement(request, "card");
                et.subElement(cardElement, "ref").text(payment.getKey());
                et.subElement(cardElement, "payerref").text(payment.getCustomerKey());

                String sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, payment.getCustomerKey(), payment.getKey() == null ? payment.getId() : payment.getKey());
                et.subElement(request, "sha1hash").text(sha1hash);
            }
            //Schedule
            else if (builder.getEntity() instanceof Schedule) {
                var schedule = (Schedule) builder.getEntity();
                et.subElement(request, "scheduleref", schedule.getKey());
                var hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, schedule.getKey());
                et.subElement(request, "sha1hash").text(hash);
            }
        } else if (builder.getTransactionType() == TransactionType.Fetch) {
            if (builder.getEntity() instanceof Schedule) {
                var scheduleRef = builder.getEntity().getKey();

                et.subElement(request, "scheduleref", scheduleRef);

                String sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, scheduleRef);
                et.subElement(request, "sha1hash").text(sha1hash);
            }
        } else if (builder.getTransactionType() == TransactionType.Search) {
            if (builder.getEntity() instanceof Schedule) {
                String customerKey = "", paymentKey = "";

                if (builder.getSearchCriteria().containsKey(SearchCriteria.CustomerId.toString())) {
                    customerKey = builder.getSearchCriteria().get(SearchCriteria.CustomerId.toString());
                    et.subElement(request, "payerref", customerKey);
                }

                if (builder.getSearchCriteria().containsKey(SearchCriteria.PaymentMethodKey.toString())) {
                    paymentKey = builder.getSearchCriteria().get(SearchCriteria.PaymentMethodKey.toString());
                    et.subElement(request, "paymentmethod", paymentKey);
                }

                var hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, customerKey, paymentKey);

                et.subElement(request, "sha1hash").text(hash);
            }
        }

        String response = doTransaction(et.toString(request));
        return mapRecurringResponse(response, builder);
    }

    private String MapScheduleFrequency(ScheduleFrequency frequency) {
        if (frequency == null) {
            return null;
        }

        switch (frequency) {
            case BiMonthly:
                return "bimonthly";
            case SemiAnnually:
                return "halfyearly";
            case Annually:
                return "yearly";
            default:
                return frequency.getValue();
        }
    }

    private Transaction mapResponse(String rawResponse, TransactionBuilder<Transaction> builder) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get("response");

        List<String> acceptedCodes = new ArrayList<>();
        if (builder instanceof AuthorizationBuilder) {
            acceptedCodes = mapAcceptedCodes(mapAuthRequestType((AuthorizationBuilder) builder));
        } else if (builder instanceof ManagementBuilder) {
            acceptedCodes = mapAcceptedCodes(mapManageRequestType((ManagementBuilder) builder));
        }

        checkResponse(root, acceptedCodes);
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

        // alternativePaymentResponse
        Element paymentMethodDetails = root.get("paymentmethoddetails");

        if (paymentMethodDetails != null) {

            AlternativePaymentResponse alternativePaymentResponse = new AlternativePaymentResponse();

            alternativePaymentResponse.setPaymentMethod(paymentMethodDetails.getString("paymentmethod"));
            alternativePaymentResponse.setBankAccount(paymentMethodDetails.getString("bankaccount"));
            alternativePaymentResponse.setAccountHolderName(paymentMethodDetails.getString("accountholdername"));
            alternativePaymentResponse.setCountry(paymentMethodDetails.getString("country"));
            alternativePaymentResponse.setRedirectUrl(paymentMethodDetails.getString("redirecturl"));
            alternativePaymentResponse.setPaymentPurpose(paymentMethodDetails.getString("paymentpurpose"));
            alternativePaymentResponse.setPaymentMethod(paymentMethodDetails.getString("paymentmethod"));
            alternativePaymentResponse.setProviderName(root.getString("paymentmethod"));

            Element apmResponseDetails = null;
            if (paymentMethodDetails.getElement() != null && paymentMethodDetails.get("SetExpressCheckoutResponse").getElement() != null) {
                apmResponseDetails = paymentMethodDetails.get("SetExpressCheckoutResponse");
            } else if (paymentMethodDetails.getElement() != null && paymentMethodDetails.get("DoExpressCheckoutPaymentResponse").getElement() != null) {
                apmResponseDetails = paymentMethodDetails.get("DoExpressCheckoutPaymentResponse");
            }

            if (apmResponseDetails != null) {
                alternativePaymentResponse.setSessionToken(apmResponseDetails.getString("Token"));
                alternativePaymentResponse.setAck(apmResponseDetails.getString("Ack"));
                alternativePaymentResponse.setTimeCreatedReference(apmResponseDetails.getDateTime("Timestamp"));
                alternativePaymentResponse.setCorrelationReference(apmResponseDetails.getString("CorrelationID"));
                alternativePaymentResponse.setVersionReference(apmResponseDetails.getString("Version"));
                alternativePaymentResponse.setBuildReference(apmResponseDetails.getString("Build"));

                Element paymentInfo = apmResponseDetails.get("PaymentInfo");
                if (paymentInfo != null) {
                    alternativePaymentResponse.setTransactionReference(paymentInfo.getString("TransactionID"));
                    alternativePaymentResponse.setPaymentType(paymentInfo.getString("PaymentType"));
                    alternativePaymentResponse.setPaymentTimeReference(paymentInfo.getDateTime("PaymentDate"));
                    alternativePaymentResponse.setGrossAmount(paymentInfo.getDecimal("GrossAmount"));
                    alternativePaymentResponse.setFeeAmount(paymentInfo.getDecimal("TaxAmount"));
                    alternativePaymentResponse.setPaymentStatus(paymentInfo.getString("PaymentStatus"));
                    alternativePaymentResponse.setPendingReason(paymentInfo.getString("PendingReason"));
                    alternativePaymentResponse.setReasonCode(paymentInfo.getString("ReasonCode"));
                    alternativePaymentResponse.setAuthProtectionEligibility(paymentInfo.getString("ProtectionEligibility"));
                    alternativePaymentResponse.setAuthProtectionEligibilityType(paymentInfo.getString("ProtectionEligibilityType"));
                }
            }

            result.setAlternativePaymentResponse(alternativePaymentResponse);
        }

        // fraud response
        if (root.has("fraudresponse")) {
            Element fraudResponseElement = root.get("fraudresponse");

            FraudResponse fraudResponse =
                    new FraudResponse()
                            .setMode(FraudFilterMode.fromString(fraudResponseElement.getAttributeString("mode")))
                            .setResult(fraudResponseElement.getString("result"));

            if (fraudResponseElement.has("rules")) {
                for (Element rule : fraudResponseElement.get("rules").getAll("rule")) {
                    fraudResponse.getRules().add((
                            new FraudResponse.Rule()
                                    .setName(rule.getAttributeString("name"))
                                    .setId(rule.getAttributeString("id"))
                                    .setAction(rule.getString("action"))));
                }
            }

            result.setFraudResponse(fraudResponse);
        }

        if (builder instanceof ManagementBuilder) {
            ManagementBuilder mb = (ManagementBuilder) builder;
            if (mb.isMultiCapture()) {
                result.setMultiCapturePaymentCount(mb.getMultiCapturePaymentCount());
                result.setMultiCaptureSequence(mb.getMultiCaptureSequence());
            }
        }

        // dccinfo
        if (root.has("dccinfo")) {
            DccRateData dccRateData = new DccRateData();
            if (builder instanceof AuthorizationBuilder && ((AuthorizationBuilder) builder).getDccRateData() != null) {
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
        if (root.has("enrolled")) {
            ThreeDSecure secureEcom = new ThreeDSecure();
            secureEcom.setEnrolled(root.getString("enrolled").equals("Y"));
            secureEcom.setPayerAuthenticationRequest(root.getString("pareq"));
            secureEcom.setXid(root.getString("xid"));
            secureEcom.setIssuerAcsUrl(root.getString("url"));
            result.setThreeDsecure(secureEcom);
        }

        // three d secure
        if (root.has("threedsecure")) {
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

        return result;
    }

    @SuppressWarnings("unchecked")
    private <TResult> TResult mapReportResponse(String rawResponse, ReportType reportType, Class<TResult> clazz) throws ApiException {
        Element response = ElementTree.parse(rawResponse).get("response");
        checkResponse(response);

        try {
            TResult rvalue = clazz.newInstance();
            if (reportType.equals(ReportType.TransactionDetail)) {
                TransactionSummary summary = new TransactionSummary();
                summary.setTransactionId(response.getString("pasref"));
                summary.setClientTransactionId(response.getString("orderid"));
                summary.setOrderId(response.getString("orderid"));
                summary.setAuthCode(response.getString("authcode"));
                summary.setMaskedCardNumber(response.getString("cardnumber"));
                summary.setAvsResponseCode(response.getString("avspostcoderesponse"));
                summary.setCvnResponseCode(response.getString("cvnresult"));
                summary.setGatewayResponseCode(response.getString("result"));
                summary.setGatewayResponseMessage(response.getString("message"));
                summary.setBatchId(response.getString("batchid"));
                summary.setSchemeReferenceData(response.getString("srd"));

                if (response.has("fraudresponse")) {
                    Element fraud = response.get("fraudresponse");
                    summary.setFraudRuleInfo(fraud.getString("result"));
                }

                if (response.has("threedsecure")) {
                    summary.setCavvResponseCode(response.getString("cavv"));
                    summary.setEciIndicator(response.getString("eci"));
                    summary.setXid(response.getString("xid"));
                }

                rvalue = (TResult) summary;
            }
            return rvalue;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <TResult> TResult mapRecurringResponse(String rawResponse, RecurringBuilder<TResult> builder) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get("response");

        // check response
        checkResponse(root);

        switch (builder.getTransactionType()) {
            case Create:
            case Edit:
            case Delete:
            case Fetch:
                if (builder.getEntity() instanceof Schedule) {
                    try {
                        builder.setEntity(mapScheduleResponse(root, (Schedule) builder.getEntity()));
                    } catch (ParseException pe) {
                        throw new ApiException(pe.getMessage(), pe);
                    }
                }
                break;
            case Search:
                if (builder instanceof RecurringBuilder && builder.getEntity() instanceof Schedule) {
                    try {
                        return ((TResult) mapSchedulesSearchResponse(root));
                    } catch (ParseException pe) {
                        throw new ApiException(pe.getMessage(), pe);
                    }
                }
                break;

            default:
                break;
        }

        return (TResult) builder.getEntity();
    }

    private RecurringCollection<Schedule> mapSchedulesSearchResponse(Element root) throws ParseException {
        RecurringCollection<Schedule> schedulesResponse = new RecurringCollection<>();

        if (root.has("schedules")) {
            for (Element schedule : root.get("schedules").getAll("schedule")) {
                if (schedule.has("scheduleref")) {
                    schedulesResponse.add(mapScheduleResponse(schedule, new Schedule()));
                }
            }
        }

        return schedulesResponse;
    }

    private Schedule mapScheduleResponse(Element root, Schedule entity) throws ParseException {
        var schedule = entity;

        schedule.setAmount(root.getString("amount") != null ? StringUtils.toAmount(root.getString("amount")) : null);
        schedule.setCurrency(root.getString("currency") != null ? root.getString("currency") : null);
        schedule.setId(root.getString("scheduleref") != null ? root.getString("scheduleref") : null);
        schedule.setKey(root.getString("scheduleref") != null ? root.getString("scheduleref") : null);
        schedule.setName(root.getString("alias") != null ? root.getString("alias") : null);
        schedule.setCustomerKey(root.getString("payerref") != null ? root.getString("payerref") : null);
        schedule.setNumberOfPayments(root.getString("numtimes") != null ? Integer.valueOf(root.getString("numtimes")) : null);
        schedule.setCustomerNumber(root.getString("custno") != null ? root.getString("custno") : null);
        schedule.setStartDate(!StringUtils.isNullOrEmpty(root.getString("startdate")) ? SDF.parse(root.getString("startdate")) : null);
        schedule.setEndDate(!StringUtils.isNullOrEmpty(root.getString("enddate")) ? SDF.parse(root.getString("enddate")) : null);
        schedule.setDescription(root.getString("comment") != null ? root.getString("comment") : null);
        schedule.setResponseCode(root.getString("result") != null ? root.getString("result") : null);
        schedule.setResponseMessage(root.getString("message") != null ? root.getString("message") : null);

        return schedule;
    }

    private void checkResponse(Element root) throws GatewayException {
        checkResponse(root, null);
    }

    private void checkResponse(Element root, List<String> acceptCodes) throws GatewayException {
        if (acceptCodes == null) {
            acceptCodes = new ArrayList<String>();
            acceptCodes.add("00");
        }

        String responseCode = root.getString("result");
        String responseMessage = root.getString("message");
        if (!acceptCodes.contains(responseCode)) {
            throw new GatewayException(String.format("Unexpected Gateway Response: %s - %s", responseCode, responseMessage), responseCode, responseMessage);
        }
    }

    private String mapAuthRequestType(AuthorizationBuilder builder) throws ApiException {
        TransactionType trans = builder.getTransactionType();
        IPaymentMethod payment = builder.getPaymentMethod();

        switch (trans) {
            case Sale:
            case Auth: {
                if (payment instanceof Credit) {
                    if (builder.getTransactionModifier().equals(TransactionModifier.Offline)) {
                        if (builder.getPaymentMethod() != null) {
                            return "manual";
                        }
                        return "offline";
                    } else if (builder.getTransactionModifier().equals(TransactionModifier.EncryptedMobile)) {
                        return "auth-mobile";
                    }
                    return "auth";
                } else if (payment instanceof AlternativePaymentMethod) {
                    return "payment-set";
                }
                return "receipt-in";
            }
            case Capture: {
                return "settle";
            }
            case Verify: {
                if (payment instanceof RecurringPaymentMethod) {
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
                if (payment instanceof RecurringPaymentMethod) {
                    return "realvault-dccrate";
                }
                return "dccrate";
            }
            case VerifyEnrolled: {
                if (payment instanceof RecurringPaymentMethod) {
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

        switch (trans) {
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
            case Confirm:
                return "payment-do";
            default:
                return "unknown";
        }
    }

    private String mapReportType(ReportType reportType) throws ApiException {
        switch (reportType) {
            case TransactionDetail:
                return "query";
            default:
                throw new UnsupportedTransactionException("This reporting call is not supported by your currently configured gateway.");
        }
    }

    @SuppressWarnings("unchecked")
    private <TResult> String mapRecurringRequestType(RecurringBuilder<TResult> builder) throws UnsupportedTransactionException {
        TResult entity = (TResult) builder.getEntity();
        switch (builder.getTransactionType()) {
            case Create:
                if (entity instanceof Customer) {
                    return "payer-new";
                } else if (entity instanceof IPaymentMethod) {
                    return "card-new";
                } else if (entity instanceof Schedule) {
                    return "schedule-new";
                }
                throw new UnsupportedTransactionException();
            case Edit:
                if (entity instanceof Customer) {
                    return "payer-edit";
                } else if (entity instanceof IPaymentMethod) {
                    return "card-update-card";
                }
                throw new UnsupportedTransactionException();
            case Delete:
                if (entity instanceof RecurringPaymentMethod) {
                    return "card-cancel-card";
                } else if (entity instanceof Schedule) {
                    return "schedule-delete";
                }
                throw new UnsupportedTransactionException();
            case Fetch:
                if (entity instanceof Schedule) {
                    return "schedule-get";
                }
                throw new UnsupportedTransactionException();
            case Search:
                if (builder instanceof RecurringBuilder) {
                    return "schedule-search";
                }
                throw new UnsupportedTransactionException();
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private String mapCardType(String cardType) {
        for (var map : mapCardType.keySet()) {
            if (cardType.equals(map)) {
                return mapCardType.get(map);
            }
        }
        return cardType;
    }

    private List<String> mapAcceptedCodes(String transactionType) {
        switch (transactionType) {
            case "3ds-verifysig":
            case "3ds-verifyenrolled":
                return Arrays.asList("00", "110");
            case "payment-set":
                return Arrays.asList("01", "00");
            default:
                return Arrays.asList("00");
        }
    }

    public void buildAlternativePaymentMethod(AuthorizationBuilder builder, Element request, ElementTree et) {
        AlternativePaymentMethod apm = (AlternativePaymentMethod) builder.getPaymentMethod();

        et.subElement(request, "paymentmethod", apm.getAlternativePaymentMethodType().getValue());

        Element paymentmethoddetails = et.subElement(request, "paymentmethoddetails");

        List<String> apmUrls = this.mapAPMUrls(apm.getAlternativePaymentMethodType());
        String returnUrl = apmUrls.get(0);
        String statusUpdateUrl = apmUrls.get(1);
        String cancelUrl = apmUrls.get(2);

        et.subElement(paymentmethoddetails, returnUrl, apm.getReturnUrl());
        et.subElement(paymentmethoddetails, statusUpdateUrl, apm.getStatusUpdateUrl());
        et.subElement(paymentmethoddetails, cancelUrl, apm.getCancelUrl());

        et.subElement(paymentmethoddetails, "descriptor", apm.getDescriptor());
        et.subElement(paymentmethoddetails, "country", apm.getCountry());
        et.subElement(paymentmethoddetails, "accountholdername", apm.getAccountHolderName());
    }

    private List<String> mapAPMUrls(AlternativePaymentType paymentMethodType) {
        switch (paymentMethodType) {
            case PAYPAL:
                return Arrays.asList("ReturnURL", "StatusUpdateURL", "CancelURL");
            default:
                return Arrays.asList("returnurl", "statusupdateurl", "cancelurl");
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
        if (address == null)
            return null;

        String code = address.getPostalCode();
        if (!StringUtils.isNullOrEmpty(code) && !code.contains("|")) {
            if (address.getStreetAddress1() != null) {
                code = String.format("%s|%s", address.getPostalCode(), address.getStreetAddress1());
            } else {
                code = String.format("%s|", address.getPostalCode());
            }
            if (address.isCountry("GB"))
                if (address.getStreetAddress1() != null) {
                    code = String.format("%s|%s", address.getPostalCode().replaceAll("[^0-9]", ""), address.getStreetAddress1().replaceAll("[^0-9]", ""));
                } else {
                    code = String.format("%s|", address.getPostalCode().replaceAll("[^0-9]", ""));
                }
        }

        Element addressNode = et.element("address").set("type", address.getType().equals(AddressType.Billing) ? "billing" : "shipping");
        et.subElement(addressNode, "code").text(code);
        et.subElement(addressNode, "country").text(address.getCountryCode());

        return addressNode;
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new ApiException("Realex does not support KeepAlive.");
    }
}
