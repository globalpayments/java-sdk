package com.global.api.gateways;

import com.global.api.HostedPaymentConfig;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.RecurringBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.*;
import com.global.api.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RealexConnector extends XmlGateway implements IPaymentGateway, IRecurringGateway {
    private String merchantId;
    private String accountId;
    private String rebatePassword;
    private String refundPassword;
    private String sharedSecret;
    private String channel;
    private HostedPaymentConfig hostedPaymentConfig;

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
        if (builder.getPaymentMethod() instanceof CreditCardData) {
            CreditCardData card = (CreditCardData)builder.getPaymentMethod();

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
            // issueno
            String hash;
            if(builder.getTransactionType() == TransactionType.Verify)
                hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, card.getNumber());
            else hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), card.getNumber());
            et.subElement(request, "sha1hash", hash);
        }
        else if(builder.getPaymentMethod() instanceof RecurringPaymentMethod) {
            RecurringPaymentMethod recurring = (RecurringPaymentMethod) builder.getPaymentMethod();
            et.subElement(request, "payerref").text(recurring.getCustomerKey());
            et.subElement(request, "paymentmethod").text(recurring.getKey());

            if(!StringUtils.isNullOrEmpty(builder.getCvn())) {
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
        else {
            // TODO: Token Processing
            //et.SubElement(request, "sha1hash", GenerateHash(order, token));
        }

        // refund hash
        if (builder.getTransactionType() == TransactionType.Refund) {
            String refundHash = GenerationUtils.generateHash(refundPassword);
            if(refundHash == null)
                refundHash = "";
            et.subElement(request, "refundhash", refundHash);
        }

        // This needs to be figured out based on txn type and set to 0, 1 or MULTI
        if (builder.getTransactionType() == TransactionType.Sale || builder.getTransactionType() == TransactionType.Auth) {
            String autoSettle = builder.getTransactionType() == TransactionType.Sale ? "1" : "0";
            et.subElement(request, "autosettle").set("flag", autoSettle);
        }

        // TODO: comments should support multiples
        if(builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }

        // fraudfilter
        if(builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
            et.subElement(request, "recurring")
                .set("type", builder.getRecurringType().getValue().toLowerCase())
                .set("sequence", builder.getRecurringSequence().getValue().toLowerCase());
        }

        // tssinfo
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

        // TODO: mpi
        if(builder.getEcommerceInfo() != null) {
            EcommerceInfo ecom = builder.getEcommerceInfo();
            Element mpi = et.subElement(request, "mpi");
            et.subElement(mpi, "cavv", ecom.getCavv());
            et.subElement(mpi, "xid", ecom.getXid());
            et.subElement(mpi, "eci", ecom.getEci());
        }

        //et.SubElement(request, "mobile");
        //et.SubElement(request, "token", token);

        String response = doTransaction(et.toString(request));
        return mapResponse(response);
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
        request.set("CHANNEL", channel);
        request.set("ORDER_ID", orderId);
        if(builder.getAmount() != null)
            request.set("AMOUNT", StringUtils.toNumeric(builder.getAmount()));
        request.set("CURRENCY", builder.getCurrency());
        request.set("TIMESTAMP", timestamp);
        //request.set("SHA1HASH", GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, (builder.getAmount() != null) ? StringUtils.toNumeric(builder.getAmount()) : null, builder.getCurrency()));
        request.set("AUTO_SETTLE_FLAG", (builder.getTransactionType() == TransactionType.Sale) ? "1" : "0");
        request.set("COMMENT1", builder.getDescription());
        // request.set("COMMENT2", );
        if(hostedPaymentConfig.isRequestTransactionStabilityScore() != null)
            request.set("RETURN_TSS", hostedPaymentConfig.isRequestTransactionStabilityScore() ? "1" : "0");
        if(hostedPaymentConfig.isDynamicCurrencyConversionEnabled() != null)
            request.set("DCC_ENABLE", hostedPaymentConfig.isDynamicCurrencyConversionEnabled() ? "1" : "0");
        if (builder.getHostedPaymentData() != null) {
            HostedPaymentData paymentData = builder.getHostedPaymentData();
            request.set("CUST_NUM", paymentData.getCustomerNumber());
            if(hostedPaymentConfig.isDisplaySavedCards() != null && paymentData.getCustomerKey() != null)
                request.set("HPP_SELECT_STORED_CARD", paymentData.getCustomerKey());
            if(paymentData.isOfferToSaveCard() != null)
                request.set("OFFER_SAVE_CARD", paymentData.isOfferToSaveCard() ? "1" : "0");
            if(paymentData.isCustomerExists() != null)
                request.set("PAYER_EXIST", paymentData.isCustomerExists() ? "1" : "0");
            if(hostedPaymentConfig.isDisplaySavedCards() == null)
                request.set("PAYER_REF", paymentData.getCustomerKey());
            request.set("PMT_REF", paymentData.getPaymentKey());
            request.set("PROD_ID", paymentData.getProductId());
        }
        if (builder.getShippingAddress() != null) {
            request.set("SHIPPING_CODE", builder.getShippingAddress().getPostalCode());
            request.set("SHIPPING_CO", builder.getShippingAddress().getCountry());
        }
        if (builder.getBillingAddress() != null) {
            request.set("BILLING_CODE", builder.getBillingAddress().getPostalCode());
            request.set("BILLING_CO", builder.getBillingAddress().getCountry());
        }
        request.set("CUST_NUM", builder.getCustomerId());
        request.set("VAR_REF", builder.getClientTransactionId());
        request.set("HPP_LANG", hostedPaymentConfig.getLanguage());
        request.set("MERCHANT_RESPONSE_URL", hostedPaymentConfig.getResponseUrl());
        request.set("CARD_PAYMENT_BUTTON", hostedPaymentConfig.getPaymentButtonText());
        if(hostedPaymentConfig.isCardStorageEnabled() != null)
            request.set("CARD_STORAGE_ENABLE", hostedPaymentConfig.isCardStorageEnabled() ? "1" : "0");
        if (builder.getTransactionType() == TransactionType.Verify)
            request.set("VALIDATE_CARD_ONLY", builder.getTransactionType() == TransactionType.Verify ? "1" : "0");
        if(!hostedPaymentConfig.getFraudFilterMode().equals(FraudFilterMode.None))
            request.set("HPP_FRAUDFILTER_MODE", hostedPaymentConfig.getFraudFilterMode());
        if(builder.getRecurringType() != null || builder.getRecurringSequence() != null) {
            request.set("RECURRING_TYPE", builder.getRecurringType().getValue().toLowerCase());
            request.set("RECURRING_SEQUENCE", builder.getRecurringSequence().getValue().toLowerCase());
        }
        request.set("HPP_VERSION", hostedPaymentConfig.getVersion());
        request.set("HPP_POST_DIMENSIONS", hostedPaymentConfig.getPostDimensions());
        request.set("HPP_POST_RESPONSE", hostedPaymentConfig.getPostResponse());

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

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        String timestamp = GenerationUtils.generateTimestamp();
        String orderId = builder.getOrderId() != null ? builder.getOrderId() : GenerationUtils.generateOrderId();

        Element request = et.element("request")
                .set("timestamp", timestamp)
                .set("type", mapManageRequestType(builder.getTransactionType()));
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);
        et.subElement(request, "pasref", builder.getTransactionId());
        if(builder.getAmount() != null)
            et.subElement(request, "amount", StringUtils.toNumeric(builder.getAmount())).set("currency", builder.getCurrency());

        if(builder.getTransactionType() == TransactionType.Refund) {
            et.subElement(request, "authcode").text(builder.getAuthorizationCode());
            et.subElement(request, "refundhash", GenerationUtils.generateHash(rebatePassword));
        }

        // reason code
        if(builder.getReasonCode() != null)
            et.subElement(request, "reasoncode").text(builder.getReasonCode());

        // TODO: comments should support multiples
        if(builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }

        et.subElement(request, "sha1hash", GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), ""));

        String response = doTransaction(et.toString(request));
        return mapResponse(response);
    }

    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        throw new UnsupportedTransactionException("Reporting functionality is not supported through this gateway.");
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
            }
        }

        String response = doTransaction(et.toString(request));
        return mapRecurringResponse(response, builder);
    }

    private Transaction mapResponse(String rawResponse) throws ApiException {
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
        result.setTransactionReference(transReference);

        return result;
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
            case Auth:
                if(payment instanceof Credit) {
                    if (builder.getTransactionModifier().equals(TransactionModifier.Offline)) {
                        if(builder.getPaymentMethod() != null)
                            return "manual";
                        return "offline";
                    }
                    return "auth";
                }
                else return "receipt-in";
            case Capture:
                return "settle";
            case Verify:
                if(payment instanceof Credit)
                    return "otb";
                else {
                    if(builder.getTransactionModifier().equals(TransactionModifier.Secure3D))
                        return "realvault-3ds-verifyenrolled";
                    else return "receipt-in-otb";
                }
            case Refund:
                if(payment instanceof Credit)
                    return "credit";
                else return "payment-out";
            case Reversal:
                throw new UnsupportedTransactionException();
            default:
                return "unknown";
        }
    }

    private String mapManageRequestType(TransactionType trans) {
        switch(trans) {
            case Capture:
                return "settle";
            case Hold:
                return "hold";
            case Refund:
                return "rebate";
            case Release:
                return "release";
            case Void:
            case Reversal:
                return "void";
            default:
                return "unknown";
        }
    }

    @SuppressWarnings("unchecked")
    private <TResult> String mapRecurringRequestType(RecurringBuilder<TResult> builder) throws UnsupportedTransactionException {
        TResult entity = (TResult) builder.getEntity();
        switch(builder.getTransactionType()) {
            case Create:
                if(entity instanceof Customer)
                    return "payer-new";
                else if(entity instanceof IPaymentMethod)
                    return "card-new";
                throw new UnsupportedTransactionException();
            case Edit:
                if(entity instanceof Customer)
                    return "payer-edit";
                else if(entity instanceof IPaymentMethod)
                    return "card-update-card";
                throw new UnsupportedTransactionException();
            case Delete:
                if(entity instanceof RecurringPaymentMethod)
                    return "card-cancel-card";
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
                country.set("code", "GB"); // TODO: I need a mapping for this somehow
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
        String code = String.format("%s|%s", address.getPostalCode(), address.getStreetAddress1());
        if(address.getCountry().equals("GB"))
            code = String.format("%s|%s", address.getPostalCode().replaceAll("[^0-9]", ""), address.getStreetAddress1().replaceAll("[^0-9]", ""));

        Element addressNode = et.element("address").set("type", address.getType().equals(AddressType.Billing) ? "billing" : "shipping");
        et.subElement(addressNode, "code").text(code);
        et.subElement(addressNode, "country").text(address.getCountry());

        return addressNode;
    }
}
