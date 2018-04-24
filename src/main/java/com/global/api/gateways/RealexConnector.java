package com.global.api.gateways;

import com.global.api.serviceConfigs.HostedPaymentConfig;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.RecurringBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
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

			// For DCC rate lookup
			if (builder.getTransactionType() == TransactionType.DccRateLookup) {
				Element dccinfo = et.subElement(request, "dccinfo");
				et.subElement(dccinfo, "ccp", builder.getDccProcessor().getValue());
				et.subElement(dccinfo, "type", builder.getDccType());
				et.subElement(dccinfo, "ratetype", builder.getDccRateType().getValue());
			}

			// For DCC charge/auth
			DccRateData dccValues = builder.getDccRateData();
			if (dccValues != null) {
				Element dccinfo = et.subElement(request, "dccinfo");
				et.subElement(dccinfo, "amount", dccValues.getAmount()).set("currency", dccValues.getCurrency());
				et.subElement(dccinfo, "ccp", dccValues.getDccProcessor());
				et.subElement(dccinfo, "type", dccValues.getDccType());
				et.subElement(dccinfo, "rate", dccValues.getDccRate());
				et.subElement(dccinfo, "ratetype", dccValues.getDccRateType());
			}

            // mpi
            ThreeDSecure secureEcom = card.getThreeDSecure();
            if(secureEcom != null) {
                Element mpi = et.subElement(request, "mpi");
                et.subElement(mpi, "cavv", secureEcom.getCavv());
                et.subElement(mpi, "xid", secureEcom.getXid());
                et.subElement(mpi, "eci", secureEcom.getEci());
             }
			}

            // issueno
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
        else if(builder.getPaymentMethod() instanceof RecurringPaymentMethod) {
            RecurringPaymentMethod recurring = (RecurringPaymentMethod) builder.getPaymentMethod();
            et.subElement(request, "payerref").text(recurring.getCustomerKey());
            et.subElement(request, "paymentmethod").text(recurring.getKey());

			// For DCC rate lookup
			if (builder.getTransactionType() == TransactionType.DccRateLookup) {
				Element dccinfo = et.subElement(request, "dccinfo");
				et.subElement(dccinfo, "ccp", builder.getDccProcessor().getValue());
				et.subElement(dccinfo, "type", builder.getDccType());
				et.subElement(dccinfo, "ratetype", builder.getDccRateType().getValue());
			}

			// For DCC charge/auth
			DccRateData dccValues = builder.getDccRateData();
			if (dccValues != null) {
				Element dccinfo = et.subElement(request, "dccinfo");
				et.subElement(dccinfo, "amount", dccValues.getAmount()).set("currency", dccValues.getCurrency());
				et.subElement(dccinfo, "ccp", dccValues.getDccProcessor());
				et.subElement(dccinfo, "type", dccValues.getDccType());
				et.subElement(dccinfo, "rate", dccValues.getDccRate());
				et.subElement(dccinfo, "ratetype", dccValues.getDccRateType());
			}

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
            String autoSettle = builder.getTransactionType() == TransactionType.Sale ? "1" : builder.isMultiCapture() ? "MULTI" : "0";
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
        request.set("AUTO_SETTLE_FLAG", (builder.getTransactionType() == TransactionType.Sale) ? "1" : builder.isMultiCapture() ? "MULTI" : "0");
        request.set("COMMENT1", builder.getDescription());
        // request.set("COMMENT2", );
        if(hostedPaymentConfig.isRequestTransactionStabilityScore() != null)
            request.set("RETURN_TSS", hostedPaymentConfig.isRequestTransactionStabilityScore() ? "1" : "0");
        if(hostedPaymentConfig.isDynamicCurrencyConversionEnabled() != null)
            request.set("DCC_ENABLE", hostedPaymentConfig.isDynamicCurrencyConversionEnabled() ? "1" : "0");
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
            request.set("HPP_CUSTOMER_COUNTRY", paymentData.getCustomerCountry());
            request.set("HPP_CUSTOMER_FIRSTNAME", paymentData.getCustomerFirstName());
            request.set("HPP_CUSTOMER_LASTNAME", paymentData.getCustomerLastName());
            request.set("MERCHANT_RESPONSE_URL", paymentData.getMerchantResponseUrl());
            request.set("HPP_TX_STATUS_URL", paymentData.getTransactionStatusUrl());
            request.set("PM_METHODS", paymentValues.toString());
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
                .set("type", mapManageRequestType(builder));
        et.subElement(request, "merchantid").text(merchantId);
        et.subElement(request, "account", accountId);
        if(builder.getAmount() != null)
            et.subElement(request, "amount", StringUtils.toNumeric(builder.getAmount())).set("currency", builder.getCurrency());
        et.subElement(request, "channel", channel);
        et.subElement(request, "orderid", orderId);
        et.subElement(request, "pasref", builder.getTransactionId());

        // payment method for APM
        if(builder.getAlternativePaymentType() != null)
            et.subElement(request, "paymentmethod", builder.getAlternativePaymentType().getValue());

        // payer authentication response
        if(builder.getTransactionType().equals(TransactionType.VerifySignature))
            et.subElement(request, "pares", builder.getPayerAuthenticationResponse());

        // reason code
        if(builder.getReasonCode() != null)
            et.subElement(request, "reasoncode").text(builder.getReasonCode());

        // TODO: comments should support multiples
        if(builder.getDescription() != null) {
            Element comments = et.subElement(request, "comments");
            et.subElement(comments, "comment", builder.getDescription()).set("id", "1");
        }

		et.subElement(request, "sha1hash", GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, orderId, StringUtils.toNumeric(builder.getAmount()), builder.getCurrency(), builder.getAlternativePaymentType() != null ? builder.getAlternativePaymentType().getValue() : null));

        if(builder.getTransactionType() == TransactionType.Refund) {
			if (builder.getAuthorizationCode() != null) {
				et.subElement(request, "authcode").text(builder.getAuthorizationCode());
			}
			et.subElement(request, "refundhash", GenerationUtils.generateHash(builder.getAlternativePaymentType() != null ? refundPassword : rebatePassword));
        }

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

                String sha1hash = GenerationUtils.generateHash(sharedSecret, timestamp, merchantId, payment.getCustomerKey(), payment.getKey() == null ? payment.getId() : payment.getKey());
                et.subElement(request, "sha1hash").text(sha1hash);
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
        transReference.setAlternativePaymentType(root.getString("paymentmethod"));
        result.setTransactionReference(transReference);

		// dccinfo
		if (root.has("dccinfo")) {
			DccResponseResult dccResponseResult = new DccResponseResult();
			dccResponseResult.setCardHolderCurrency(root.getString("cardholdercurrency"));
			dccResponseResult.setCardHolderAmount(root.getDecimal("cardholderamount"));
			dccResponseResult.setCardHolderRate(root.getDecimal("cardholderrate"));
			dccResponseResult.setMerchantCurrency(root.getString("merchantcurrency"));
			dccResponseResult.setMerchantAmount(root.getDecimal("merchantamount"));
			result.setDccResponseResult(dccResponseResult);
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
                    else if (builder.getTransactionModifier().equals(TransactionModifier.EncryptedMobile)) {
                        return "auth-mobile";
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
            case DccRateLookup:
                if(payment instanceof Credit)
                    return "dccrate";
                 return "realvault-dccrate";
            case VerifyEnrolled:
                return "3ds-verifyenrolled";
            case Reversal:
                throw new UnsupportedTransactionException();
            default:
                return "unknown";
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

}
