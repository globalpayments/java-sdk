package com.global.api.mapping;

import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.PagedResult;
import com.global.api.entities.gpApi.entities.FundsAccountDetails;
import com.global.api.entities.gpApi.entities.UserAccount;
import com.global.api.entities.payFac.Person;
import com.global.api.entities.payFac.UserReference;
import com.global.api.entities.reporting.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.Installment;
import com.global.api.paymentMethods.InstallmentData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.google.gson.JsonElement;
import lombok.var;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.global.api.gateways.GpApiConnector.parseGpApiDate;
import static com.global.api.gateways.GpApiConnector.parseGpApiDateTime;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiMapping {

    private static final String BATCH_CLOSE = "CLOSE";
    private static final String PAYMENT_METHOD_CREATE = "PAYMENT_METHOD_CREATE";
    private static final String PAYMENT_METHOD_DETOKENIZE = "PAYMENT_METHOD_DETOKENIZE";
    private static final String PAYMENT_METHOD_EDIT = "PAYMENT_METHOD_EDIT";
    private static final String PAYMENT_METHOD_DELETE = "PAYMENT_METHOD_DELETE";
    private static final String LINK_CREATE = "LINK_CREATE";
    private static final String LINK_EDIT = "LINK_EDIT";

    private static final String TRANSFER = "TRANSFER";

    private static final String MERCHANT_CREATE = "MERCHANT_CREATE";
    private static final String MERCHANT_LIST = "MERCHANT_LIST";
    private static final String MERCHANT_SINGLE = "MERCHANT_SINGLE";
    private static final String MERCHANT_EDIT = "MERCHANT_EDIT";
    private static final String MERCHANT_EDIT_INITIATED = "MERCHANT_EDIT_INITIATED";
    private static final String FUNDS = "FUNDS";
    private static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
    private static final String FILE_CREATE = "FILE_CREATE";
    private static final String FILE_SINGLE = "FILE_SINGLE";
    private static final String DISPUTE_CHALLENGE = "CHALLENGE";

    public static Transaction mapResponse(String rawResponse) throws GatewayException {
        Transaction transaction = new Transaction();

        if (!isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            transaction.setResponseCode(json.get("action").getString("result_code"));
            transaction.setResponseMessage(json.getString("status"));
            transaction.setTransactionId(json.getString("id"));
            transaction.setBalanceAmount(json.getAmount("amount"));
            transaction.setAuthorizedAmount(
                    json.getString("status").toUpperCase(Locale.ENGLISH).equals(TransactionStatus.Preauthorized.getValue().toUpperCase(Locale.ENGLISH)) &&
                            !isNullOrEmpty(json.getString("amount")) ? json.getAmount("amount") : null
            );
            transaction.setTimestamp(json.getString("time_created"));
            transaction.setReferenceNumber(json.getString("reference"));
            transaction.setClientTransactionId(json.getString("reference"));
            transaction.setMultiCapture("MULTIPLE".equals(json.getString("capture_mode")));

            String actionType = json.get("action").getString("type");

            switch (actionType) {
                case BATCH_CLOSE:
                    BatchSummary batchSummary = new BatchSummary();

                    batchSummary.setBatchReference(json.getString("id"));
                    batchSummary.setStatus(json.getString("status"));
                    batchSummary.setTotalAmount(json.getAmount("amount"));
                    batchSummary.setTransactionCount(json.getInt("transaction_count"));

                    transaction.setBatchSummary(batchSummary);

                    return transaction;

                case PAYMENT_METHOD_CREATE:
                case PAYMENT_METHOD_DETOKENIZE:
                case PAYMENT_METHOD_EDIT:
                case PAYMENT_METHOD_DELETE:
                    transaction.setToken(json.getString("id"));
                    if (!isNullOrEmpty(json.getString("usage_mode"))) {
                        transaction.setTokenUsageMode(getPaymentMethodUsageMode(json));
                    }
                    transaction.setTimestamp(json.getString("time_created"));
                    transaction.setReferenceNumber(json.getString("reference"));

                    if (json.has("card")) {
                        JsonDoc card = json.get("card");

                        transaction.setCardType(card.getString("brand"));
                        transaction.setCardNumber(card.getString("number"));
                        transaction.setCardLast4(card.getString("masked_number_last4"));
                        if (!isNullOrEmpty(card.getString("expiry_month"))) {
                            transaction.setCardExpMonth(card.getInt("expiry_month"));
                        }
                        if (!isNullOrEmpty(card.getString("expiry_year"))) {
                            transaction.setCardExpYear(card.getInt("expiry_year"));
                        }
                    }
                    break;
                case LINK_CREATE:
                case LINK_EDIT:

                    PayByLinkResponse payByLinkResponse = mapPayByLinkResponse(json);

                    if (json.has("transactions")) {
                        JsonDoc trn = json.get("transactions");
                        transaction.setBalanceAmount(trn.getString("amount") != null ? trn.getAmount("amount") : null);
                        payByLinkResponse.setAllowedPaymentMethods(trn.getStringArrayList("allowed_payment_methods").toArray(new String[0]));
                    }

                    transaction.setPayByLinkResponse(payByLinkResponse);
                    break;
                case TRANSFER:
                    transaction.setPaymentMethodType(PaymentMethodType.AccountFunds);
                    break;
                case DISPUTE_CHALLENGE:
                    if (json.has("documents")) {
                        List<JsonDoc> documents = json.getEnumerator("documents");

                        List<DisputeDocument> disputeDocuments = new ArrayList<>();
                        for (JsonDoc document : documents) {
                            if (document.getString("id") != null) {
                                DisputeDocument disputeDocument = new DisputeDocument();
                                disputeDocument.setId(document.getString("id"));
                                disputeDocument.setType(document.getString("type") != null ? document.getString("type") : null);

                                disputeDocuments.add(disputeDocument);
                            }
                        }

                        transaction.setDisputeDocuments(disputeDocuments);
                    }
                    break;
                default:
                    break;
            }

            BatchSummary batchSummary = new BatchSummary();
            batchSummary.setBatchReference(json.getString("batch_id"));
            transaction.setBatchSummary(batchSummary);

            if (json.has("payment_method")) {
                JsonDoc paymentMethod = json.get("payment_method");

                transaction.setMultiCapture(getIsMultiCapture(json));
                transaction.setFingerPrint(paymentMethod.getString("fingerprint"));
                transaction.setFingerPrintIndicator(paymentMethod.getString("fingerprint_presence_indicator"));

                if (paymentMethod.has("bnpl")) {
                    mapBNPLResponse(json, transaction);
                    return transaction;
                }

                transaction.setToken(paymentMethod.getString("id"));

                JsonDoc paymentMethodObj = paymentMethod.has("card") ? paymentMethod.get("card") : (
                        paymentMethod.has("digital_wallet") ? paymentMethod.get("digital_wallet") : null);
                if (paymentMethodObj != null) {

                    transaction.setAuthorizationCode(paymentMethodObj.getString("authcode"));

                    var cardDetails = new Card();
                    cardDetails.setMaskedNumberLast4(paymentMethodObj.getString("masked_number_last4"));
                    cardDetails.setBrand(paymentMethodObj.getString("brand"));
                    cardDetails.setIssuer(paymentMethodObj.getString("issuer"));
                    cardDetails.setFunding(paymentMethodObj.getString("funding"));
                    cardDetails.setBinCountry(paymentMethodObj.getString("country"));
                    transaction.setCardDetails(cardDetails);

                    transaction.setCardType(paymentMethodObj.getString("brand"));
                    transaction.setCardLast4(paymentMethodObj.getString("masked_number_last4"));
                    transaction.setCvnResponseMessage(paymentMethodObj.getString("cvv_result"));
                    transaction.setCardBrandTransactionId(paymentMethodObj.getString("brand_reference"));
                    transaction.setAvsResponseCode(paymentMethodObj.getString("avs_postal_code_result"));
                    transaction.setAvsAddressResponse(paymentMethodObj.getString("avs_address_result"));
                    transaction.setAvsResponseMessage(paymentMethodObj.getString("avs_action"));
                    transaction.setPaymentMethodType(!paymentMethod.has("bank_transfer") ? PaymentMethodType.ACH : transaction.getPaymentMethodType());
                    if (paymentMethod.has("card") && paymentMethod.get("card").has("provider")) {
                        transaction.setCardIssuerResponse(mapCardIssuerResponse(paymentMethodObj.get("provider")));
                    }else if(paymentMethod.has("result")) {
                        transaction.setCardIssuerResponse(new CardIssuerResponse(paymentMethod.getString("result")));
                    }
                }

                if (paymentMethod.has("apm") &&
                        paymentMethod.get("apm").getString("provider").toUpperCase().equals(PaymentProvider.OPEN_BANKING.toString())) {
                    transaction.setPaymentMethodType(PaymentMethodType.BankPayment);

                    var obResponse = new BankPaymentResponse();

                    obResponse.setRedirectUrl(paymentMethod.getString("redirect_url") != null ? paymentMethod.getString("redirect_url") : null);
                    obResponse.setPaymentStatus(paymentMethod.getString("message") != null ? paymentMethod.getString("message") : null);

                    if (paymentMethod.has("bank_transfer")) {
                        JsonDoc bankTransfer = paymentMethod.get("bank_transfer");

                        obResponse.setAccountNumber(bankTransfer.getString("account_number") != null ? bankTransfer.getString("account_number") : null);
                        obResponse.setIban(bankTransfer.getString("iban") != null ? bankTransfer.getString("iban") : null);

                        if (bankTransfer.has("bank")) {
                            JsonDoc bank = bankTransfer.get("bank");

                            obResponse.setSortCode(bank.getString("code") != null ? bank.getString("code") : null);
                            obResponse.setAccountName(bank.getString("name") != null ? bank.getString("name") : null);
                        }
                    }

                    transaction.setBankPaymentResponse(obResponse);
                } else if (paymentMethod.has("bank_transfer")) {
                    transaction.setPaymentMethodType(PaymentMethodType.ACH);
                } else if (paymentMethod.has("apm")) {
                    transaction.setPaymentMethodType(PaymentMethodType.APM);
                }
            }

            if (json.has("payment_method")) {
                JsonDoc paymentMethod = json.get("payment_method");

                if (paymentMethod.has("shipping_address") || paymentMethod.has("payer")) {
                    var payerDetails = new PayerDetails();
                    payerDetails.setEmail(paymentMethod.get("payer").getString("email"));

                    if (paymentMethod.has("payer")) {
                        JsonDoc payer = paymentMethod.get("payer");

                        if (payer.has("billing_address")) {
                            var billingAddress = payer.get("billing_address");
                            payerDetails.setFirstName(billingAddress.getString("first_name"));
                            payerDetails.setLastName(billingAddress.getString("last_name"));

                            var billing = mapAddressObject(billingAddress);
                            billing.setType(AddressType.Billing);
                            payerDetails.setBillingAddress(billing);
                        }
                    }

                    var shipping = mapAddressObject(paymentMethod.get("shipping_address"));
                    shipping.setType(AddressType.Shipping);

                    payerDetails.setShippingAddress(shipping);
                    transaction.setPayerDetails(payerDetails);
                }
            }

            transaction.setDccRateData(mapDccInfo(json));
            transaction.setFraudFilterResponse(json.has("risk_assessment") ? mapFraudManagement(json) : null);

            if (json.has("transfers")) {
                transaction.setTransferFundsAccountDetailsList(mapTransferFundsAccountDetails(json));
            }

            if (json.has("card")) {
                JsonDoc card = json.get("card");

                var cardDetail = new Card();
                cardDetail.setCardNumber(card.getString("number"));
                cardDetail.setBrand(card.getString("brand"));
                cardDetail.setCardExpMonth(card.getString("expiry_month"));
                cardDetail.setCardExpYear(card.getString("expiry_year"));

                transaction.setCardDetails(cardDetail);
            }

            if (json.has("installment")) {
                JsonDoc installment = json.get("installment");
                transaction.setInstallmentData(setInstallmentData(installment));
            }
        }

        return transaction;
    }

    private static List<FundsAccountDetails> mapTransferFundsAccountDetails(JsonDoc json) {

        var transferResponse = new ArrayList<FundsAccountDetails>();

        for (JsonDoc item : json.getEnumerator("transfers")) {

            var transfer = new FundsAccountDetails();

            transfer.setId(item.getString("id"));
            transfer.setStatus(item.getString("status"));
            String timeCreated = item.getString("time_created");
            if (null == timeCreated) {
                timeCreated = "";
            }
            transfer.setTimeCreated(timeCreated);
            transfer.setAmount(StringUtils.toAmount(item.getString("amount")).toString());
            transfer.setReference(item.getString("reference"));
            transfer.setDescription(item.getString("description"));

            transferResponse.add(transfer);
        }

        return transferResponse;
    }

    private static Boolean getIsMultiCapture(JsonDoc json) {
        if (!isNullOrEmpty(json.getString("capture_mode"))) {
            switch (json.getString("capture_mode")) {
                case "MULTIPLE":
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private static PaymentMethodType getPaymentMethodType(JsonDoc json) {
        if (json.get("payment_method").has("bank_transfer")) {
            return PaymentMethodType.ACH;
        } else if (json.get("payment_method").has("apm")) {
            return PaymentMethodType.APM;
        }
        return null;
    }

    private static PaymentMethodUsageMode getPaymentMethodUsageMode(JsonDoc json) {
        if (json.has("usage_mode")) {
            return PaymentMethodUsageMode.valueOf(json.getString("usage_mode"));
        }
        return null;
    }

    public static Transaction mapResponseAPM(String rawResponse) throws GatewayException {
        AlternativePaymentResponse apm = new AlternativePaymentResponse();
        Transaction transaction = mapResponse(rawResponse);

        JsonDoc json = JsonDoc.parse(rawResponse);

        JsonDoc paymentMethodApm = json.get("payment_method").get("apm");
        String redirectUrl = json.get("payment_method").has("redirect_url")
                ? json.get("payment_method").getString("redirect_url")
                : (paymentMethodApm.has("redirect_url")
                ? paymentMethodApm.getString("redirect_url")
                : null
        );
        apm.setRedirectUrl(redirectUrl);

        if (paymentMethodApm != null) {
            if (paymentMethodApm.has("provider")) {
                if (paymentMethodApm.get("provider") != null) {
                    JsonDoc provider = paymentMethodApm.get("provider");
                    apm.setProviderName(provider.getString("name"));
                    apm.setProviderReference(provider.getString("merchant_identifier"));
                    apm.setTimeCreatedReference(provider.getDateTime("time_created_reference"));
                } else if (paymentMethodApm.getString("provider") != null) {
                    apm.setProviderName(paymentMethodApm.getString("provider"));
                }
            }
            apm.setAck(paymentMethodApm.getString("ack"));
            apm.setSessionToken(paymentMethodApm.getString("session_token"));
            apm.setCorrelationReference(paymentMethodApm.getString("correlation_reference"));
            apm.setVersionReference(paymentMethodApm.getString("version_reference"));
            apm.setBuildReference(paymentMethodApm.getString("build_reference"));
            apm.setTimeCreatedReference(paymentMethodApm.getDateTime("time_created_reference"));
            apm.setTransactionReference(paymentMethodApm.getString("transaction_reference"));
            apm.setSecureAccountReference(paymentMethodApm.getString("secure_account_reference"));
            apm.setReasonCode(paymentMethodApm.getString("reason_code"));
            apm.setPendingReason(paymentMethodApm.getString("pending_reason"));
            apm.setGrossAmount(paymentMethodApm.getAmount("gross_amount"));
            apm.setPaymentTimeReference(paymentMethodApm.getDateTime("payment_time_reference"));
            apm.setPaymentType(paymentMethodApm.getString("payment_type"));
            apm.setPaymentStatus(paymentMethodApm.getString("payment_status"));
            apm.setType(paymentMethodApm.getString("type"));
            apm.setProtectionEligibility(paymentMethodApm.getString("protection_eligibilty"));
            apm.setFeeAmount(paymentMethodApm.getAmount("fee_amount"));
        }

        JsonDoc authorization = json.get("payment_method").get("authorization");
        if (authorization != null) {
            apm.setAuthStatus(authorization.getString("status"));
            apm.setAuthAmount(StringUtils.toAmount(authorization.getAmount("amount").toString()));
            apm.setAuthAck(authorization.getString("ack"));
            apm.setAuthCorrelationReference(authorization.getString("correlation_reference"));
            apm.setAuthVersionReference(authorization.getString("version_reference"));
            apm.setAuthBuildReference(authorization.getString("build_reference"));
            apm.setAuthPendingReason(authorization.getString("pending_reason"));
            apm.setAuthProtectionEligibility(authorization.getString("protection_eligibilty"));
            apm.setAuthProtectionEligibilityType(authorization.getString("protection_eligibilty_type"));
            apm.setAuthReference(authorization.getString("reference"));
        }

        transaction.setAlternativePaymentResponse(apm);

        return transaction;
    }

    private static boolean getIsShippable(JsonDoc doc) {
        if (doc.has("shippable")) {
            return doc.getString("shippable").equalsIgnoreCase("YES") ? true : false;
        }
        return false;
    }

    public static TransactionSummary mapTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary summary = createTransactionSummary(doc);

        summary.setClientTransactionId(doc.getString("reference"));
        summary.setTransactionLocalDate(parseGpApiDateTime(doc.getString("time_created_reference")));
        summary.setBatchSequenceNumber(doc.getString("batch_id"));
        summary.setCountry(doc.getString("country"));
        summary.setOriginalTransactionId(doc.getString("parent_resource_id"));
        summary.setDepositReference(doc.getString("deposit_id"));
        summary.setDepositStatus(doc.getString("deposit_status"));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));
        summary.setOrderId(doc.getString("order_reference"));
        summary.setBatchCloseDate(parseGpApiDateTime(doc.getString("batch_time_created")));
        summary.setGratuityAmount(doc.getAmount("gratuity_amount"));
        summary.setCashBackAmount(doc.getAmount("cashback_amount"));
        if (doc.has("payment_method")) {
            final JsonDoc paymentMethod = doc.get("payment_method");

            summary.setGatewayResponseMessage(paymentMethod.getString("message"));
            summary.setEntryMode(paymentMethod.getString("entry_mode"));
            summary.setCardHolderName(paymentMethod.getString("name"));

            if (paymentMethod.has("card")) {
                final JsonDoc card = paymentMethod.get("card");

                summary.setCardType(card.getString("brand"));
                summary.setAuthCode(card.getString("authcode"));
                summary.setBrandReference(card.getString("brand_reference"));
                summary.setAcquirerReferenceNumber(card.getString("arn"));
                summary.setMaskedCardNumber(card.getString("masked_number_first6last4"));

                summary.setCardDetails(mapCardDetails(card));
            } else if (paymentMethod.has("digital_wallet")) {
                JsonDoc digitalWallet = paymentMethod.get("digital_wallet");

                summary.setCardType(digitalWallet.getString("brand"));
                summary.setAuthCode(digitalWallet.getString("authcode"));
                summary.setBrandReference(digitalWallet.getString("brand_reference"));
                summary.setMaskedCardNumber(digitalWallet.getString("masked_token_first6last4"));
            }

            if (paymentMethod.has("authentication")) {
                summary.setThreeDSecure(map3DSInfo(paymentMethod.get("authentication")));
            }
        }

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");

            summary.setMerchantId(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
            summary.setMerchantDeviceIdentifier(system.getString("tid"));
        }

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            summary.setGatewayResponseMessage(paymentMethod.getString("message"));
            summary.setEntryMode(paymentMethod.getString("entry_mode"));
            summary.setCardHolderName(paymentMethod.getString("name"));

            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                summary.setCardType(card.getString("brand"));
                summary.setAuthCode(card.getString("authcode"));
                summary.setBrandReference(card.getString("brand_reference"));
                summary.setAcquirerReferenceNumber(card.getString("arn"));
                summary.setMaskedCardNumber(card.getString("masked_number_first6last4"));
                summary.setPaymentType(PaymentMethodName.Card.getValue(Target.GP_API));
            } else if (paymentMethod.has("digital_wallet")) {
                JsonDoc digitalWallet = paymentMethod.get("digital_wallet");
                summary.setMaskedCardNumber(digitalWallet.getString("masked_token_first6last4"));
                summary.setPaymentType(PaymentMethodName.DigitalWallet.getValue(Target.GP_API));
            } else if (paymentMethod.has("bank_transfer") &&
                    paymentMethod.has("apm") &&
                    !paymentMethod.get("apm").getString("provider").toUpperCase().equals(PaymentProvider.OPEN_BANKING.toString())) {
                JsonDoc bankTransfer = paymentMethod.get("bank_transfer");
                summary.setAccountNumberLast4(bankTransfer.getString("masked_account_number_last4"));
                summary.setAccountType(bankTransfer.getString("account_type"));
                summary.setPaymentType(PaymentMethodName.BankTransfer.getValue(Target.GP_API));
                summary.setAccountType(bankTransfer.getString("account_type"));
            } else if (paymentMethod.has("apm")) {
                if (paymentMethod.get("apm").getString("provider").toUpperCase().equals(PaymentProvider.OPEN_BANKING.toString())) {

                    summary.setPaymentType(EnumUtils.getMapping(Target.GP_API, PaymentMethodName.BankPayment));
                    var bankPaymentResponse = new BankPaymentResponse();

                    if (paymentMethod.has("bank_transfer")) {
                        JsonDoc bankTransfer = paymentMethod.get("bank_transfer");

                        bankPaymentResponse.setIban(bankTransfer.getString("iban"));
                        bankPaymentResponse.setMaskedIbanLast4(bankTransfer.getString("masked_iban_last4"));
                        bankPaymentResponse.setAccountNumber(bankTransfer.getString("account_number"));

                        if (bankTransfer.has("bank")) {
                            bankPaymentResponse.setAccountName(bankTransfer.get("bank").getString("name"));
                            bankPaymentResponse.setSortCode(bankTransfer.get("bank").getString("code"));
                        }

                        if (bankTransfer.has("remittance_reference")) {
                            bankPaymentResponse.setRemittanceReferenceValue(bankTransfer.get("remittance_reference").getString("value"));
                            bankPaymentResponse.setRemittanceReferenceType(bankTransfer.get("remittance_reference").getString("type"));
                        }
                        summary.setAccountNumberLast4(bankTransfer.getString("masked_account_number_last4"));
                    }

                    summary.setBankPaymentResponse(bankPaymentResponse);
                } else {
                    JsonDoc apm = paymentMethod.get("apm");
                    var alternativePaymentResponse = new AlternativePaymentResponse();

                    alternativePaymentResponse.setRedirectUrl(apm.getString("redirect_url"));
                    alternativePaymentResponse.setProviderName(apm.getString("provider"));
                    alternativePaymentResponse.setProviderReference(apm.getString("provider_reference"));
                    summary.setAlternativePaymentResponse(alternativePaymentResponse);
                    summary.setPaymentType(EnumUtils.getMapping(Target.GP_API, PaymentMethodName.APM));
                }
            } else if (paymentMethod.has("bnpl")) {
                JsonDoc bnpl = paymentMethod.get("bnpl");

                var bnplResponse =
                        new BNPLResponse()
                                .setProviderName(bnpl.getString("provider"));

                summary.setBNPLResponse(bnplResponse);
                summary.setPaymentType(EnumUtils.getMapping(Target.GP_API, PaymentMethodName.BNPL));
            }
        }
        if(doc.has("installment")){
            JsonDoc installment = doc.get("installment");
            summary.setInstallmentData(setInstallmentData(installment));
        }

        summary.setFraudManagementResponse(doc.has("risk_assessment") ? mapFraudManagementReport(doc.get("risk_assessment")) : null);

        return summary;
    }

    public static PayByLinkSummary mapPayByLinkSummary(JsonDoc doc) throws GatewayException {
        PayByLinkSummary summary = new PayByLinkSummary();

        summary.setId(doc.getString("id"));
        summary.setMerchantId(doc.getString("merchant_id"));
        summary.setMerchantName(doc.getString("merchant_name"));
        summary.setAccountId(doc.getString("account_id"));
        summary.setAccountName(doc.getString("account_name"));
        summary.setUrl(doc.getString("url"));
        summary.setStatus(PayByLinkStatus.valueOf(doc.getString("status")));
        summary.setType(PayByLinkType.valueOf(doc.getString("type").toUpperCase()));
        summary.setAllowedPaymentMethods(getAllowedPaymentMethods(doc));
        summary.setUsageMode(getPaymentMethodUsageMode(doc));
        summary.setUsageCount(doc.getString("usage_count"));
        summary.setReference(doc.getString("reference"));
        summary.setName(doc.getString("name"));
        summary.setDescription(doc.getString("description"));
        summary.setShippable(doc.getString("shippable"));
        summary.setViewedCount(doc.getString("viewed_count"));
        summary.setExpirationDate(doc.getDateTime("expiration_date"));
        summary.setImages(doc.getStringArrayList("images"));
        summary.setShippable(doc.getString("shippable"));
        summary.setUsageCount(doc.getString("usage_count"));
        summary.setImages(doc.getStringArrayList("images"));
        summary.setShippingAmount(doc.getString("shipping_amount"));

        if (doc.has("transactions")) {
            JsonDoc transactions = doc.get("transactions");
            if (transactions.has("transaction_list")) {
                List<TransactionSummary> transactionSummaryList = new ArrayList<>();
                for (JsonDoc transaction : transactions.getEnumerator("transaction_list")) {
                    transactionSummaryList.add(createTransactionSummary(transaction));
                }
                summary.setTransactions(transactionSummaryList);
                summary.setAmount(transactions.getAmount("amount"));
            }
        }

        return summary;
    }

    private static PayByLinkResponse mapPayByLinkResponse(JsonDoc doc) {
        return
                new PayByLinkResponse()
                        .setId(doc.getString("id"))
                        .setAccountName(doc.getString("account_name"))
                        .setUrl(doc.getString("url"))
                        .setStatus(PayByLinkStatus.valueOf(doc.getString("status")))
                        .setType(PayByLinkType.valueOf(doc.getString("type")))
                        .setUsageMode(PaymentMethodUsageMode.valueOf(doc.getString("usage_mode")))
                        .setUsageLimit(doc.getInt("usage_limit"))
                        .setReference(doc.getString("reference"))
                        .setName(doc.getString("name"))
                        .setDescription(doc.getString("description"))
                        .setIsShippable(getIsShippable(doc))
                        .setViewedCount(doc.getString("viewed_count"))
                        .setExpirationDate(doc.getString("expiration_date") != null ? new DateTime(doc.getDate("expiration_date")) : null);
    }

    private static void mapBNPLResponse(JsonDoc response, Transaction transaction) {
        transaction.setPaymentMethodType(PaymentMethodType.BNPL);

        var bnplResponse = new BNPLResponse();
        bnplResponse.setRedirectUrl(response.get("payment_method").getString("redirect_url"));
        bnplResponse.setProviderName(response.get("payment_method").get("bnpl").getString("provider"));
        transaction.setBNPLResponse(bnplResponse);
    }

    private static TransactionSummary createTransactionSummary(JsonDoc doc) throws GatewayException {
        TransactionSummary transaction = new TransactionSummary();

        transaction.setTransactionId(doc.getString("id"));
        transaction.setTransactionDate(doc.getString("time_created") != null ? parseGpApiDateTime(doc.getString("time_created")) : null);
        transaction.setTransactionStatus(doc.getString("status"));
        transaction.setTransactionType(doc.getString("type"));
        transaction.setChannel(doc.getString("channel"));
        transaction.setAmount(doc.getAmount("amount"));
        transaction.setCurrency(doc.getString("currency"));
        transaction.setReferenceNumber(doc.getString("reference"));
        transaction.setClientTransactionId(doc.getString("reference"));
        transaction.setDescription(doc.getString("description"));
        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            transaction.setFingerprint(paymentMethod.getString("fingerprint"));
            transaction.setFingerprintIndicator(paymentMethod.getString("fingerprint_presence_indicator"));
        }

        return transaction;
    }

    private static List<PaymentMethodName> getAllowedPaymentMethods(JsonDoc doc) {
        List<PaymentMethodName> list = new ArrayList<>();
        for (String item : doc.get("transactions").getStringArrayList("allowed_payment_methods")) {
            for (PaymentMethodName paymentMethodName : PaymentMethodName.values()) {
                if (paymentMethodName.getValue(Target.GP_API).equals(item)) {
                    list.add(paymentMethodName);
                }
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> T mapReportResponse(String rawResponse, ReportType reportType) throws ApiException {
        JsonDoc json = JsonDoc.parse(rawResponse);

        switch (reportType) {
            case TransactionDetail:
                return (T) mapTransactionSummary(json);

            case FindTransactionsPaged:
            case FindSettlementTransactionsPaged:
                return (T) mapTransactions(json);

            case DepositDetail:
                return (T) mapDepositSummary(json);

            case FindDepositsPaged:
                return (T) mapDeposits(json);

            case DisputeDetail:
                return (T) mapDisputeSummary(json);

            case DocumentDisputeDetail:
                return (T) mapDisputeDocument(json);

            case SettlementDisputeDetail:
                return (T) mapSettlementDisputeSummary(json);

            case FindDisputesPaged:
                return (T) mapDisputes(json);

            case FindSettlementDisputesPaged:
                return (T) mapSettlementDisputes(json);

            case StoredPaymentMethodDetail:
                return (T) mapStoredPaymentMethodSummary(json);

            case FindStoredPaymentMethodsPaged:
                return (T) mapStoredPaymentMethods(json);

            case ActionDetail:
                return (T) mapActionSummary(json);

            case FindActionsPaged:
                return (T) mapActions(json);

            case PayByLinkDetail:
                return (T) mapPayByLinkSummary(json);

            case FindPayByLinkPaged:
                return (T) mapPayByLinks(json);

            case FindMerchantsPaged:
                return (T) mapMerchants(json);

            case FindAccountsPaged:
                return (T) mapAccounts(json);

            case FindAccountDetail:
                return (T) mapMerchantAccountSummary(json);

            default:
                throw new UnsupportedTransactionException();
        }
    }

    private static <T> void setPagingInfo(PagedResult<T> result, JsonDoc json) {
        if (json.getInt("total_record_count") != null) {
            result.setTotalRecordCount(json.getInt("total_record_count"));
        } else if (json.getInt("total_count") != null) {
            result.setTotalRecordCount(json.getInt("total_count"));
        } else {
            result.setTotalRecordCount(json.getInt("current_page_size"));
        }

        JsonDoc paging = json.get("paging");
        if (paging != null) {
            result.setPageSize(paging.getInt("page_size") != null ? paging.getInt("page_size") : 0);
            result.setPage(paging.getInt("page") != null ? paging.getInt("page") : 0);
            result.setOrder(paging.getString("order"));
            result.setOrderBy(paging.getString("order_by"));
        }
    }

    public static DepositSummary mapDepositSummary(JsonDoc doc) throws GatewayException {
        DepositSummary summary = new DepositSummary();

        summary.setDepositId(doc.getString("id"));
        summary.setDepositDate(parseGpApiDate(doc.getString("time_created")));
        summary.setStatus(doc.getString("status"));
        summary.setType(doc.getString("funding_type"));
        summary.setAmount(doc.getAmount("amount"));
        summary.setCurrency(doc.getString("currency"));

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");
            summary.setMerchantNumber(system.getString("mid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        if (doc.has("sales")) {
            JsonDoc sales = doc.get("sales");
            summary.setSalesTotalCount(sales.getInt("count"));
            summary.setSalesTotalAmount(sales.getAmount("amount"));
        }

        if (doc.has("refunds")) {
            JsonDoc refunds = doc.get("refunds");
            summary.setRefundsTotalCount(refunds.getInt("count"));
            summary.setRefundsTotalAmount(refunds.getAmount("amount"));
        }

        if(doc.has("discounts")) {
            JsonDoc discounts = doc.get("discounts");
            summary.setDiscountsTotalCount(discounts.getInt("count"));
            summary.setDiscountsTotalAmount(discounts.getAmount("amount"));
        }

        if (doc.has("disputes")) {
            JsonDoc disputes = doc.get("disputes");

            if (disputes.has("chargebacks")) {
                JsonDoc chargebacks = disputes.get("chargebacks");

                summary.setChargebackTotalCount(chargebacks.getInt("count"));
                summary.setChargebackTotalAmount(chargebacks.getAmount("amount"));
            }

            if (disputes.has("reversals")) {
                JsonDoc reversals = disputes.get("reversals");

                summary.setAdjustmentTotalCount(reversals.getInt("count"));
                summary.setAdjustmentTotalAmount(reversals.getAmount("amount"));
            }
        }

        if (doc.has("fees")) {
            JsonDoc fees = doc.get("fees");

            summary.setFeesTotalAmount(fees.getAmount("amount"));
        }

        if (doc.has("bank_transfer")) {
            JsonDoc bankTransfer = doc.get("bank_transfer");

            summary.setAccountNumber(bankTransfer.getString("masked_account_number_last4"));
        }

        return summary;
    }

    public static DisputeDocument mapDisputeDocument(JsonDoc doc) {
        DisputeDocument document = new DisputeDocument();
        document.setId(doc.getString("id"));
        document.setType(doc.get("action") != null ? doc.get("action").getString("type") : "");
        document.setBase64Content(doc.getString("b64_content"));

        return document;
    }

    public static DisputeSummary mapDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = new DisputeSummary();

        summary.setCaseId(doc.getString("id"));
        summary.setCaseIdTime(parseGpApiDateTime(doc.getString("time_created")));
        summary.setCaseStatus(doc.getString("status"));
        summary.setCaseStage(doc.getString("stage"));
        summary.setCaseStageTime(parseGpApiDateTime(doc.getString("stage_time_created")));
        summary.setCaseAmount(doc.getAmount("amount"));
        summary.setCaseCurrency(doc.getString("currency"));

        summary.setReasonCode(doc.getString("reason_code"));
        summary.setReason(doc.getString("reason_description"));
        summary.setResult(doc.getString("result"));

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");

            summary.setCaseMerchantId(system.getString("mid"));
            summary.setCaseTerminalId(system.getString("tid"));
            summary.setMerchantHierarchy(system.getString("hierarchy"));
            summary.setMerchantName(system.getString("name"));
            summary.setMerchantDbaName(system.getString("dba"));
        }

        summary.setLastAdjustmentAmount(doc.getAmount("last_adjustment_amount"));
        summary.setLastAdjustmentCurrency(doc.getString("last_adjustment_currency"));
        summary.setLastAdjustmentFunding(doc.getString("last_adjustment_funding"));

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");

            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");

                summary.setTransactionMaskedCardNumber(card.getString("number"));
                summary.setTransactionARN(card.getString("arn"));
                summary.setTransactionCardType(card.getString("brand"));
            }

        }
        if (doc.has("transaction")) {
            JsonDoc transaction = doc.get("transaction");
            if (transaction.has("payment_method")) {
                JsonDoc paymentMethod = transaction.get("payment_method");

                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");

                    summary.setTransactionMaskedCardNumber(card.getString("number"));
                    summary.setTransactionARN(card.getString("arn"));
                    summary.setTransactionCardType(card.getString("brand"));
                    summary.setTransactionBrandReference(card.getString("brand_reference"));
                }
            }
        }
        String timeToRespondBy = doc.getString("time_to_respond_by");
        if (!isNullOrEmpty(timeToRespondBy)) {
            summary.setRespondByDate(parseGpApiDateTime(timeToRespondBy));
        }

        if (doc.has("documents")) {
            ArrayList<JsonDoc> documents = (ArrayList<JsonDoc>) doc.getEnumerator("documents");

            ArrayList<DisputeDocument> disputeDocuments = new ArrayList<>();
            for (JsonDoc document : documents) {
                if (document.getString("id") != null) {
                    DisputeDocument disputeDocument = new DisputeDocument();
                    disputeDocument.setId(document.getString("id"));
                    disputeDocument.setType(document.getString("type") != null ? document.getString("type") : null);

                    disputeDocuments.add(disputeDocument);
                }
            }

            summary.setDocuments(disputeDocuments);
        }

        return summary;
    }

    public static DisputeSummary mapSettlementDisputeSummary(JsonDoc doc) throws GatewayException {
        DisputeSummary summary = mapDisputeSummary(doc);

        summary.setCaseIdTime(parseGpApiDateTime(doc.getString("stage_time_created")));
        summary.setDepositDate(parseGpApiDate(doc.getString("deposit_time_created")));
        summary.setDepositReference(doc.getString("deposit_id"));

        if (doc.has("transaction")) {
            JsonDoc transaction = doc.get("transaction");

            summary.setTransactionTime(parseGpApiDateTime(transaction.getString("time_created")));
            summary.setTransactionType(transaction.getString("type"));
            summary.setTransactionAmount(transaction.getAmount("amount"));
            summary.setTransactionCurrency(transaction.getString("currency"));
            summary.setTransactionReferenceNumber(transaction.getString("reference"));

            if (transaction.has("payment_method")) {
                JsonDoc paymentMethod = transaction.get("payment_method");

                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");

                    summary.setTransactionMaskedCardNumber(card.getString("masked_number_first6last4"));
                    summary.setTransactionARN(card.getString("arn"));
                    summary.setTransactionCardType(card.getString("brand"));
                    summary.setTransactionAuthCode(card.getString("authcode"));
                }
            }
        }

        return summary;
    }

    public static RiskAssessment mapRiskAssessmentResponse(String rawResponse) throws GatewayException {

        var riskAssessment = new RiskAssessment();

        if (!isNullOrEmpty(rawResponse)) {

            JsonDoc response = JsonDoc.parse(rawResponse);

            riskAssessment.setId(response.getString("id"));
            riskAssessment.setTimeCreated(response.getDateTime("time_created"));
            riskAssessment.setStatus(RiskAssessmentStatus.valueOf(response.getString("status")));
            riskAssessment.setAmount(StringUtils.toAmount(response.getString("amount")));
            riskAssessment.setCurrency(response.getString("currency"));
            riskAssessment.setMerchantId(response.getString("merchant_id"));
            riskAssessment.setMerchantName(response.getString("merchant_name"));
            riskAssessment.setAccountId(response.getString("account_id"));
            riskAssessment.setAccountName(response.getString("account_name"));
            riskAssessment.setReference(response.getString("reference"));
            riskAssessment.setResponseCode(response.get("action").getString("result_code"));
            riskAssessment.setResponseMessage(response.getString("result"));

            if (response.get("payment_method").has("card")) {
                var paymentMethod = response.get("payment_method").get("card");
                var card = new Card();

                card.setMaskedNumberLast4(paymentMethod.getString("masked_number_last4"));
                card.setBrand(paymentMethod.getString("brand"));
                card.setBrandReference(paymentMethod.getString("brand_reference"));
                card.setBin(paymentMethod.getString("bin"));
                card.setBinCountry(paymentMethod.getString("bin_country"));
                card.setAccountType(paymentMethod.getString("account_type"));
                card.setIssuer(paymentMethod.getString("issuer"));

                riskAssessment.setCardDetails(card);
            }

            if (response.has("raw_response")) {
                var rawResponseField = response.get("raw_response");
                var thirdPartyResponse = new ThirdPartyResponse();

                thirdPartyResponse.setPlatform(rawResponseField.getString("platform"));
                thirdPartyResponse.setData(rawResponseField.get("data").toString());
                riskAssessment.setThirdPartyResponse(thirdPartyResponse);
            }

            riskAssessment.setActionId(response.get("action").getString("id"));
        }

        return riskAssessment;
    }

    private static FraudManagementResponse mapFraudManagement(JsonDoc response) {
        var fraudFilterResponse = new FraudManagementResponse();

        if (response.has("risk_assessment")) {
            var fraudResponses = response.getEnumerator("risk_assessment");

            for (var fraudResponse : fraudResponses) {
                fraudFilterResponse = mapFraudManagementReport(fraudResponse);
            }

            return fraudFilterResponse;
        }

        return null;
    }

    private static FraudManagementResponse mapFraudManagementReport(JsonDoc response) {
        var fraudFilterResponse = new FraudManagementResponse();
        var fraudResponse = response;

        fraudFilterResponse.setFraudResponseMode(fraudResponse.getString("mode"));
        fraudFilterResponse.setFraudResponseResult(fraudResponse.has("result") ? fraudResponse.getString("result") : "");
        fraudFilterResponse.setFraudResponseMessage(fraudResponse.getString("message"));
        if (fraudResponse.has("rules")) {
            fraudFilterResponse.setFraudResponseRules(new ArrayList<>());

            for (var rule : fraudResponse.getEnumerator("rules")) {
                var fraudRule = new FraudRule();

                fraudRule.setKey(rule.getString("reference"));
                fraudRule.setMode(FraudFilterMode.fromString(rule.getString("mode")));
                fraudRule.setDescription(rule.getString("description"));
                fraudRule.setResult(rule.has("result") ? rule.getString("result") : null);

                fraudFilterResponse.getFraudResponseRules().add(fraudRule);
            }
        }

        return fraudFilterResponse;
    }

    public static DccRateData mapDccInfo(JsonDoc response) throws GatewayException {
        JsonDoc currencyConversion = response;

        if (!response.get("action").getString("type").equals("RATE_LOOKUP") &&
                response.get("currency_conversion") == null) {
            return null;
        }

        if (response.get("currency_conversion") != null) {
            currencyConversion = response.get("currency_conversion");
        }

        return
                new DccRateData()
                        .setCardHolderCurrency(currencyConversion.getString("payer_currency"))
                        .setCardHolderAmount(currencyConversion.getAmount("payer_amount"))
                        .setCardHolderRate(currencyConversion.getString("exchange_rate"))
                        .setMerchantCurrency(currencyConversion.getString("currency"))
                        .setMerchantAmount(currencyConversion.getAmount("amount"))
                        .setMarginRatePercentage(currencyConversion.getString("margin_rate_percentage"))
                        .setExchangeRateSourceName(currencyConversion.getString("exchange_rate_source"))
                        .setCommissionPercentage(currencyConversion.getString("commission_percentage"))
                        .setExchangeRateSourceTimestamp(currencyConversion.getDateTime("exchange_rate_time_created"))
                        .setDccId(currencyConversion.getString("id"));
    }

    private static Secure3dVersion parse3DSVersion(String messageVersion) {
        if (messageVersion.startsWith("1."))
            return Secure3dVersion.ONE;
        if (messageVersion.startsWith("2."))
            return Secure3dVersion.TWO;
        return Secure3dVersion.ANY;
    }

    public static Transaction map3DSecureData(String rawResponse) throws ApiException {
        if (!isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            ThreeDSecure threeDSecure = new ThreeDSecure();
            threeDSecure.setServerTransactionId(json.getString("id"));
            threeDSecure.setProviderServerTransRef(
                    !isNullOrEmpty(json.get("three_ds").getString("server_trans_ref")) ?
                            json.get("three_ds").getString("server_trans_ref") :
                            null);
            threeDSecure.setStatus(json.getString("status"));
            threeDSecure.setCurrency(json.getString("currency"));
            threeDSecure.setAmount(json.getAmount("amount"));

            if (json.has("three_ds")) {
                JsonDoc three_ds = json.get("three_ds");

                threeDSecure.setMessageVersion(three_ds.getString("message_version"));
                threeDSecure.setVersion(parse3DSVersion(three_ds.getString("message_version")));
                threeDSecure.setDirectoryServerStartVersion(three_ds.getString("ds_protocol_version_start"));
                threeDSecure.setDirectoryServerEndVersion(three_ds.getString("ds_protocol_version_end"));
                threeDSecure.setAcsStartVersion(three_ds.getString("acs_protocol_version_start"));
                threeDSecure.setAcsEndVersion(three_ds.getString("acs_protocol_version_end"));
                threeDSecure.setAcsReferenceNumber(three_ds.getString("acs_reference_number"));
                // In other SDKs, enrolled is simply a String.
                // In JAVA, enrolled was used in another connectors as boolean. So enrolledStatus was created as String for that purpose.
                threeDSecure.setEnrolledStatus(three_ds.getString("enrolled_status"));
                threeDSecure.setEnrolled(!isNullOrEmpty(three_ds.getString("enrolled_status")) && three_ds.getString("enrolled_status").equals("ENROLLED"));
                threeDSecure.setEci(!isNullOrEmpty(three_ds.getString("eci")) ? three_ds.getString("eci") : null);
                threeDSecure.setAcsInfoIndicator(three_ds.getStringArrayList("acs_info_indicator"));
                threeDSecure.setChallengeMandated(three_ds.getString("challenge_status").equals("MANDATED"));
                threeDSecure.setPayerAuthenticationRequest(
                        !isNullOrEmpty(three_ds.getString("acs_challenge_request_url")) && json.getString("status").equals("CHALLENGE_REQUIRED") ?
                                three_ds.getString("challenge_value") :
                                three_ds.get("method_data") != null ?
                                        (!isNullOrEmpty(three_ds.get("method_data").getString("encoded_method_data")) ? three_ds.get("method_data").getString("encoded_method_data") : null) :
                                        null
                );

                // Mobile data
                if (!isNullOrEmpty(json.getString("source")) && json.getString("source").equals("MOBILE_SDK")) {
                    if (three_ds.has("mobile_data")) {
                        JsonDoc mobile_data = three_ds.get("mobile_data");

                        threeDSecure.setPayerAuthenticationRequest(mobile_data.getString("acs_signed_content"));

                        if (mobile_data.has("acs_rendering_type")) {
                            JsonDoc acs_rendering_type = mobile_data.get("acs_rendering_type");
                            threeDSecure.setAcsInterface(acs_rendering_type.getString("acs_interface"));
                            threeDSecure.setAcsUiTemplate(acs_rendering_type.getString("acs_ui_template"));
                        }
                    }
                }

                threeDSecure.setIssuerAcsUrl(
                        !isNullOrEmpty(three_ds.getString("acs_challenge_request_url")) && json.getString("status").equals("CHALLENGE_REQUIRED") ?
                                three_ds.getString("acs_challenge_request_url") :
                                three_ds.getString("method_url")
                );

                threeDSecure.setCurrency(json.getString("currency"));
                threeDSecure.setAmount(json.getAmount("amount"));
                threeDSecure.setAuthenticationValue(three_ds.getString("authentication_value"));
                threeDSecure.setDirectoryServerTransactionId(three_ds.getString("ds_trans_ref"));
                threeDSecure.setAcsTransactionId(three_ds.getString("acs_trans_ref"));
                threeDSecure.setStatusReason(three_ds.getString("status_reason"));
                threeDSecure.setMessageCategory(three_ds.getString("message_category"));
                threeDSecure.setMessageType(three_ds.getString("message_type"));
                threeDSecure.setSessionDataFieldName(three_ds.getString("session_data_field_name"));
                if (json.has("notifications")) {
                    threeDSecure.setChallengeReturnUrl(json.get("notifications").getString("challenge_return_url"));
                }
                threeDSecure.setLiabilityShift(three_ds.getString("liability_shift"));
                threeDSecure.setAuthenticationSource(three_ds.getString("authentication_source"));
                threeDSecure.setAuthenticationType(three_ds.getString("authentication_request_type"));
                threeDSecure.setDecoupledResponseIndicator(three_ds.getString("acs_decoupled_response_indicator"));
                threeDSecure.setWhitelistStatus(three_ds.getString("whitelist_status"));
                threeDSecure.setMessageExtensions(new ArrayList<>());

                List<JsonDoc> messageExtensions = three_ds.getEnumerator("message_extension");
                List<MessageExtension> msgExtensions = new ArrayList<>();

                if (messageExtensions != null) {
                    for (JsonDoc messageExtension : messageExtensions) {
                        MessageExtension msgExtension =
                                new MessageExtension()
                                        .setCriticalityIndicator(messageExtension.getString("criticality_indicator"))
                                        .setMessageExtensionData(messageExtension.get("data").toString())
                                        .setMessageExtensionId(messageExtension.getString("id"))
                                        .setMessageExtensionName(messageExtension.getString("name"));

                        msgExtensions.add(msgExtension);
                    }
                }
                threeDSecure.setMessageExtensions(msgExtensions);
            }

            Transaction transaction = new Transaction();
            transaction.setThreeDsecure(threeDSecure);

            return transaction;
        }

        return new Transaction();
    }

    public static StoredPaymentMethodSummary mapStoredPaymentMethodSummary(JsonDoc doc) throws GatewayException {
        StoredPaymentMethodSummary storedPaymentMethodSummary = new StoredPaymentMethodSummary();

        storedPaymentMethodSummary.setId(doc.getString("id"));
        storedPaymentMethodSummary.setTimeCreated(parseGpApiDateTime(doc.getString("time_created")));
        storedPaymentMethodSummary.setStatus(doc.getString("status"));
        storedPaymentMethodSummary.setReference(doc.getString("reference"));
        storedPaymentMethodSummary.setName(doc.getString("name"));

        if (doc.has("card")) {
            JsonDoc card = doc.get("card");

            storedPaymentMethodSummary.setCardLast4(card.getString("number_last4"));
            storedPaymentMethodSummary.setCardType(card.getString("brand"));
            storedPaymentMethodSummary.setCardExpMonth(card.getString("expiry_month"));
            storedPaymentMethodSummary.setCardExpYear(card.getString("expiry_year"));
        }

        return storedPaymentMethodSummary;
    }

    public static ActionSummary mapActionSummary(JsonDoc doc) throws GatewayException {
        ActionSummary actionSummary = new ActionSummary();

        actionSummary.setId(doc.getString("id"));
        actionSummary.setType(doc.getString("type"));
        actionSummary.setTimeCreated(parseGpApiDateTime(doc.getString("time_created")));
        actionSummary.setResource(doc.getString("resource"));
        actionSummary.setVersion(doc.getString("version"));
        actionSummary.setResourceId(doc.getString("resource_id"));
        actionSummary.setResourceStatus(doc.getString("resource_status"));
        actionSummary.setHttpResponseCode(doc.getString("http_response_code"));
        actionSummary.setResponseCode(doc.getString("response_code"));
        actionSummary.setAppId(doc.getString("app_id"));
        actionSummary.setAppName(doc.getString("app_name"));
        actionSummary.setAccountId(doc.getString("account_id"));
        actionSummary.setAccountName(doc.getString("account_name"));
        actionSummary.setMerchantName(doc.getString("merchant_name"));
        return actionSummary;
    }
    public static <T> T mapRecurringEntity(String rawResponse, T recurringEntity) {
        T result = null;
        if (rawResponse != null && !rawResponse.isEmpty()) {
            JsonDoc json = JsonDoc.parse(rawResponse);
            if (recurringEntity instanceof Customer) {
                Customer payer = (Customer) recurringEntity;
                payer.setId(json.getString("id"));
                if (json.has("payment_methods")) {
                    payer.setPaymentMethods(new ArrayList<>());
                    for (JsonDoc payment : json.getEnumerator("payment_methods")) {
                        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod();
                        paymentMethod.setId(payment.getString("id"));
                        paymentMethod.setKey(payment.getString("default"));
                        payer.getPaymentMethods().add(paymentMethod);
                    }
                }
                return (T) payer;
            }
        }
        return result;
    }


    public static TransactionSummaryPaged mapTransactions(JsonDoc doc) throws GatewayException {
        TransactionSummaryPaged pagedResult = new TransactionSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("transactions")) {
            pagedResult.add(mapTransactionSummary(transaction));
        }

        return pagedResult;
    }

    public static DepositSummaryPaged mapDeposits(JsonDoc doc) throws GatewayException {
        DepositSummaryPaged pagedResult = new DepositSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc deposit : doc.getEnumerator("deposits")) {
            pagedResult.add(mapDepositSummary(deposit));
        }

        return pagedResult;
    }

    public static DisputeSummaryPaged mapDisputes(JsonDoc doc) throws GatewayException {
        DisputeSummaryPaged pagedResult = new DisputeSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("disputes")) {
            pagedResult.add(mapDisputeSummary(transaction));
        }

        return pagedResult;
    }

    public static DisputeSummaryPaged mapSettlementDisputes(JsonDoc doc) throws GatewayException {
        DisputeSummaryPaged pagedResult = new DisputeSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("disputes")) {
            pagedResult.add(mapSettlementDisputeSummary(transaction));
        }

        return pagedResult;
    }

    public static StoredPaymentMethodSummaryPaged mapStoredPaymentMethods(JsonDoc doc) throws GatewayException {
        StoredPaymentMethodSummaryPaged pagedResult = new StoredPaymentMethodSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc paymentMethod : doc.getEnumerator("payment_methods")) {
            pagedResult.add(mapStoredPaymentMethodSummary(paymentMethod));
        }

        return pagedResult;
    }

    public static ActionSummaryPaged mapActions(JsonDoc doc) throws GatewayException {
        ActionSummaryPaged pagedResult = new ActionSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc element : doc.getEnumerator("actions")) {
            pagedResult.add(mapActionSummary(element));
        }

        return pagedResult;
    }

    public static PayByLinkSummaryPaged mapPayByLinks(JsonDoc doc) throws GatewayException {
        PayByLinkSummaryPaged pagedResult = new PayByLinkSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("links")) {
            pagedResult.add(mapPayByLinkSummary(transaction));
        }

        return pagedResult;
    }

    public static MerchantSummaryPaged mapMerchants(JsonDoc doc) {
        MerchantSummaryPaged pagedResult = new MerchantSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc transaction : doc.getEnumerator("merchants")) {
            pagedResult.add(mapMerchantSummary(transaction));
        }

        return pagedResult;
    }

    public static MerchantAccountSummaryPaged mapAccounts(JsonDoc doc) {
        MerchantAccountSummaryPaged pagedResult = new MerchantAccountSummaryPaged();
        setPagingInfo(pagedResult, doc);

        for (JsonDoc element : doc.getEnumerator("accounts")) {
            pagedResult.add(mapMerchantAccountSummary(element));
        }

        return pagedResult;
    }

    private static MerchantSummary mapMerchantSummary(JsonDoc merchant) {
        var merchantInfo = new MerchantSummary();
        merchantInfo.setId(merchant.getString("id"));
        merchantInfo.setName(merchant.getString("name"));

        if (merchant.has("status")) {
            merchantInfo.setStatus((UserStatus.valueOf(merchant.getString("status"))));
        }

        if (merchant.has("links")) {
            merchantInfo.setLinks(new ArrayList<>());

            for (var link : merchant.getEnumerator("links")) {
                var userLink = new UserLinks();
                if (link.has("rel")) {
                    userLink.setRel(UserLevelRelationship.valueOf(link.getString("rel").toUpperCase()));
                }
                userLink.setHref(link.getString("href"));
                merchantInfo.getLinks().add(userLink);
            }
        }

        return merchantInfo;
    }

    private static MerchantAccountSummary mapMerchantAccountSummary(JsonDoc account) {
        var merchantAccountSummary = new MerchantAccountSummary();

        merchantAccountSummary.setId(account.getString("id"));
        if (account.has("type")) {
            merchantAccountSummary.setType(MerchantAccountType.valueOf(account.getString("type")));
        }
        merchantAccountSummary.setName(account.getString("name"));
        if (account.has("status")) {
            merchantAccountSummary.setStatus(MerchantAccountStatus.valueOf(account.getString("status")));
        }
        merchantAccountSummary.setPermissions(account.getStringArrayList("permissions"));
        merchantAccountSummary.setCountries(account.getStringArrayList("countries"));

        if (account != null && account.has("channels")) {
            List<Channel> channelsList = new ArrayList<>();
            for (String channel : account.getStringArrayList("channels")) {
                channelsList.add(Channel.fromString(channel));
            }
            merchantAccountSummary.setChannels(channelsList);
        }

        merchantAccountSummary.setCurrencies(account.getStringArrayList("currencies"));

        merchantAccountSummary.setPaymentMethods(getPaymentMethodsName(account));

        merchantAccountSummary.setConfigurations(account.getStringArrayList("configurations"));

        if (account.has("addresses")) {
            var addresses = new ArrayList<Address>();
            for (JsonDoc address : account.getEnumerator("addresses")) {
                addresses.add(mapAddressObject(address));
            }
            merchantAccountSummary.setAddresses(addresses);
        }

        return merchantAccountSummary;
    }

    @SuppressWarnings("unchecked")
    public static <T> T mapMerchantEndpointResponse(String rawResponse) throws GatewayException, UnsupportedTransactionException {
        if (!isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);
            String actionType = json.get("action").getString("type");

            switch (actionType) {

                case DOCUMENT_UPLOAD:
                    var userDoc = new User();
                    userDoc.setUserReference(new UserReference());
                    userDoc.getUserReference().setUserId(json.getString("merchant_id"));
                    userDoc.getUserReference().setUserType(UserType.MERCHANT);
                    userDoc.setName(json.getString("merchant_name"));
                    var doc = new Document();
                    doc.setName(json.getString("name"));
                    doc.setId(json.getString("id"));
                    doc.setStatus(json.getString("status"));
                    doc.setTimeCreated(json.getString("time_created"));
                    doc.setFormat(FileType.valueOf(json.getString("format")));
                    doc.setCategory(DocumentCategory.valueOf(json.getString("function")));
                    userDoc.setDocument(doc);
                    if (json.has("action")) {
                        userDoc.setResponseCode(json.get("action").getString("result_code"));
                    }
                    return (T) userDoc;

                case MERCHANT_CREATE:
                case MERCHANT_EDIT:
                case MERCHANT_EDIT_INITIATED:
                case MERCHANT_SINGLE:
                    var user = new User();
                    user.setUserReference(new UserReference());
                    user.getUserReference().setUserId(json.getString("id"));

                    user.setName(json.getString("name"));
                    user.getUserReference().setUserStatus(UserStatus.valueOf(json.getString("status")));
                    user.getUserReference().setUserType(UserType.valueOf(json.getString("type")));
                    user.setTimeCreated(json.getDateTime("time_created"));
                    user.setTimeLastUpdated(json.getDateTime("time_last_updated"));
                    user.setResponseCode(json.get("action").getString("result_code"));
                    user.setStatusDescription(json.getString("status_description"));
                    user.setEmail(json.getString("email"));

                    if (json.has("addresses")) {
                        user.setAddresses(new ArrayList<>());
                        for (var address : json.getEnumerator("addresses")) {
                            var userAddress = mapAddressObject(address);
                            if (address.has("functions")) {
                                userAddress.setType(AddressType.valueOf(toCamelCase(address.getStringArrayList("functions").get(0))));
                            }
                            user.getAddresses().add(userAddress);
                        }
                    }

                    if (json.has("payment_methods")) {
                        user.setPaymentMethods(mapMerchantPaymentMethod(json));
                    }

                    if (json.has("contact_phone")) {
                        if (!isNullOrEmpty(json.get("contact_phone").getString("country_code")) &&
                                !isNullOrEmpty(json.get("contact_phone").getString("subscriber_number"))) {
                            PhoneNumber phoneNumber = new PhoneNumber();
                            phoneNumber.setCountryCode(json.get("contact_phone").getString("country_code"));
                            phoneNumber.setNumber(json.get("contact_phone").getString("subscriber_number"));
                            // TODO: Set PhoneNumberType
                            user.setContactPhone(phoneNumber);
                        }
                    }

                    if (json.has("persons")) {
                        user.setPersonList(mapMerchantPersonList(json));
                    }

                    return (T) user;

                case FUNDS:
                    var userFunds = new User()
                            .setUserReference(
                                    new UserReference()
                                            .setUserId(json.getString("merchant_id"))
                            )
                            .setName(json.getString("merchant_name"))
                            .setFundsAccountDetails(
                                    new FundsAccountDetails()
                                            .setId(json.getString("id"))
                                            .setTimeCreated(json.getString("time_created"))
                                            .setTimeLastUpdated(json.getString("time_last_updated"))
                                            .setPaymentMethodType(json.getString("type"))
                                            .setPaymentMethodName(json.getString("payment_method"))
                                            .setStatus(json.getString("status"))
                                            .setAmount(json.getString("amount"))
                                            .setCurrency(json.getString("currency"))
                                            .setAccount(
                                                    new UserAccount(json.getString("account_id"), json.getString("account_name"))
                                            )
                            )
                            .setResponseCode(json.get("action").getString("result_code"));

                    return (T) userFunds;

                default:
                    throw new UnsupportedTransactionException("Unknown transaction type " + actionType);
            }
        }

        return null;
    }

    public static FileProcessor MapFileProcessingResponse(String rawResponse) throws UnsupportedTransactionException {
        FileProcessor fp = new FileProcessor();
        if (!isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);
            JsonDoc actionJsonDoc = json.get("action");
            String actionType = null;
            if (actionJsonDoc != null) {
                actionType = actionJsonDoc.getString("type");
            }
            switch (actionType) {
                case FILE_CREATE:
                    MapGeneralFileProcessingResponse(json, fp);
                    fp.setCreatedDate(json.getString("time_created"));
                    fp.setUploadUrl(json.getString("url"));
                    fp.setExpirationDate(json.getString("expiration_date"));
                    break;
                case FILE_SINGLE:
                    MapGeneralFileProcessingResponse(json, fp);
                    if (json.has("response_files")) {
                        fp.setFilesUploaded(new ArrayList<>());
                        List<JsonDoc> responseFiles = json.getEnumerator("response_files");
                        if (responseFiles == null) {
                            responseFiles = new ArrayList<>();
                        }

                        for (JsonDoc responseFile : responseFiles) {
                            FileUploaded file = new FileUploaded();
                            file.setUrl(responseFile.getString("url"));
                            file.setTimeCreated(responseFile.getString("time_created"));
                            file.setExpirationDate(responseFile.getString("expiration_date"));
                            file.setFileName(responseFile.getString("name"));
                            file.setFileId(responseFile.getString("response_file_id"));
                            fp.getFilesUploaded().add(file);
                        }
                    }
                    break;
                default:
                    throw new UnsupportedTransactionException("Unknown action type " + actionType);
            }
        }

        return fp;
    }

    private static void MapGeneralFileProcessingResponse(JsonDoc json, FileProcessor fp) {
        fp.setResourceId(json.getString("id"));
        fp.setStatus(json.getString("status"));
        fp.setResponseMessage(json.getString("status"));
        fp.setTotalRecordCount(json.getStringOrNull("total_record_count"));

        JsonDoc actionJsonDoc = json.get("action");
        if (actionJsonDoc != null) {
            fp.setResponseCode(actionJsonDoc.getString("result_code"));
        }
    }

    private static List<PaymentMethodList> mapMerchantPaymentMethod(JsonDoc json) {
        List<PaymentMethodList> merchantPaymentList = new ArrayList<>();

        for (var payment : json.getEnumerator("payment_methods")) {
            var merchantPayment = new PaymentMethodList();
            merchantPayment.setFunction(PaymentMethodFunction.valueOf(payment.getStringArrayList("functions").get(0)));

            if (payment.has("bank_transfer")) {
                var bankTransfer = payment.get("bank_transfer");
                var pm = new eCheck();

                if (bankTransfer.has("account_holder_type") && !isNullOrEmpty(bankTransfer.getString("account_holder_type"))) {
                    pm.setCheckType(CheckType.valueOf(bankTransfer.getString("account_holder_type")));
                }

                if (bankTransfer.has("account_type") && !isNullOrEmpty(bankTransfer.getString("account_type"))) {
                    pm.setAccountType(AccountType.valueOf(bankTransfer.getString("account_type")));
                }

                if (bankTransfer.has("bank")) {
                    var jsonBank = bankTransfer.get("bank");
                    pm.setRoutingNumber(jsonBank.getString("code"));
                    pm.setBankName(jsonBank.getString("name"));
                }

                pm.setCheckHolderName(payment.getString("name"));

                merchantPayment.setPaymentMethod(pm);
            }

            if (payment.has("card")) {
                var card = payment.get("card");
                var pm = new CreditCardData();

                pm.setCardHolderName(card.getString("name"));
                pm.setNumber(card.getString("number"));
                pm.setExpYear(card.getInt("expiry_year"));

                merchantPayment.setPaymentMethod(pm);
            }

            merchantPaymentList.add(merchantPayment);
        }

        return merchantPaymentList;
    }

    private static Address mapAddressObject(JsonDoc address) {
        Address addressReturn = new Address();

        addressReturn.setStreetAddress1(address.getString("line_1"));

        if (address.has("line_2")) {
            addressReturn.setStreetAddress2(address.getString("line_2"));
        }

        if (address.has("line_3")) {
            addressReturn.setStreetAddress3(address.getString("line_3"));
        }

        addressReturn.setCity(address.getString("city"));
        addressReturn.setState(address.getString("state"));
        addressReturn.setPostalCode(address.getString("postal_code"));
        addressReturn.setCountryCode(address.getString("country"));

        return addressReturn;
    }

    private static List<Person> mapMerchantPersonList(JsonDoc json) {
        List<Person> personList = new ArrayList<>();

        for (var person : json.getEnumerator("persons")) {
            var newPerson = new Person();
            var functions = person.getStringArrayList("functions");

            newPerson.setFunctions(PersonFunctions.valueOf(functions.get(0)));
            newPerson.setFirstName(person.getString("first_name"));
            newPerson.setMiddleName(person.getString("middle_name"));
            newPerson.setLastName(person.getString("last_name"));
            newPerson.setEmail(person.getString("email"));

            if (person.has("address")) {
                var address = person.get("address");

                newPerson.setAddress(mapAddressObject(address));
            }

            if (person.has("work_phone")) {
                newPerson.setWorkPhone(
                        new PhoneNumber()
                                .setNumber(person.get("work_phone").getString("subscriber_number")));
            }

            if (person.has("contact_phone")) {
                newPerson.setHomePhone(
                        new PhoneNumber()
                                .setNumber(person.get("contact_phone").getString("subscriber_number")));
            }

            personList.add(newPerson);
        }

        return personList;
    }

    private static List<PaymentMethodName> getPaymentMethodsName(JsonDoc doc) {
        var result = new ArrayList<PaymentMethodName>();

        if (doc.has("payment_methods")) {
            for (var payment : doc.getStringArrayList("payment_methods")) {
                switch (payment) {
                    case "BANK_TRANSFER":
                        result.add(PaymentMethodName.BankTransfer);
                        break;

                    case "BANK_PAYMENT":
                        result.add(PaymentMethodName.BankPayment);
                        break;

                    case "DIGITAL_WALLET":
                        result.add(PaymentMethodName.DigitalWallet);
                        break;

                    default:
                        result.add(PaymentMethodName.fromString(payment, Target.GP_API));
                        break;
                }
            }
        }

        return result;
    }

    private static String toCamelCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static CardIssuerResponse mapCardIssuerResponse(JsonDoc response) {
        return
                new CardIssuerResponse()
                        .setResult(response.getString("result"))
                        .setAvsResult(response.getString("avs_result"))
                        .setCvvResult(response.getString("cvv_result"))
                        .setAvsAddressResult(response.getString("avs_address_result"))
                        .setAvsPostalCodeResult(response.getString("avs_postal_code_result"));
    }

    private static ThreeDSecure map3DSInfo(JsonDoc response) {
        ThreeDSecure threeDSecure = new ThreeDSecure();
        threeDSecure.setServerTransactionId(response.getString("id"));
        if (response.has("three_ds")) {
            JsonDoc threeDSNode = response.get("three_ds");
            threeDSecure.setAuthenticationValue(threeDSNode.getString("value"));
            threeDSecure.setProviderServerTransRef(threeDSNode.getString("server_trans_ref"));
            threeDSecure.setDirectoryServerTransactionId(threeDSNode.getString("ds_trans_ref"));
            if (threeDSNode.has("exempt_status") && !threeDSNode.getString("exempt_status").isEmpty()) {
                threeDSecure.setExemptStatus(ExemptStatus.valueOf(threeDSNode.getString("exempt_status").toUpperCase()));
            }
            threeDSecure.setCavv(threeDSNode.getString("cavv_result"));
            threeDSecure.setMessageVersion(threeDSNode.getString("message_version"));
            threeDSecure.setEci(threeDSNode.getString("eci"));
            threeDSecure.setStatus(threeDSNode.getString("status"));
        }

        return threeDSecure;
    }

    private static Card mapCardDetails(JsonDoc cardInfo) {
        Card cardDetails = new Card();
        cardDetails.setMaskedCardNumber(cardInfo.getString("masked_number_first6last4"));
        cardDetails.setFunding(cardInfo.getString("funding"));
        cardDetails.setBrand(cardInfo.getString("brand"));
        cardDetails.setIssuer(cardInfo.getString("issuer"));
        cardDetails.setBinCountry(cardInfo.getString("country"));

        return cardDetails;
    }

    private static InstallmentData setInstallmentData(JsonDoc installment)
    {
        var installmentData = new InstallmentData();
        installmentData.setProgram(installment.getString("program"));
        installmentData.setMode(installment.getString("mode"));
        installmentData.setCount(installment.getString("count"));
        installmentData.setGracePeriodCount(installment.getString("grace_period_count"));
        return installmentData;
    }

    /**
     * It maps the GP API create installment response to Installment object
     * @param  rawResponse
     * @return {@link Installment}
     * @throws GatewayException
     */

    public static Installment mapInstallmentResponse(String rawResponse) throws GatewayException {

        Installment installment = new Installment();
        if (!isNullOrEmpty(rawResponse)) {
            JsonDoc json = JsonDoc.parse(rawResponse);

            installment.setId(json.getString("id"));
            installment.setTimeCreated(json.getDateTime("time_created"));
            installment.setType(json.getString("type"));
            installment.setStatus(json.getString("status"));
            installment.setChannel(json.getString("channel"));
            installment.setAmount(json.getAmount("amount"));
            installment.setCurrency(json.getString("currency"));
            installment.setCountry(json.getString("country"));
            installment.setMerchantId(json.getString("merchant_id"));
            installment.setMerchantName(json.getString("merchant_name"));
            installment.setAccountId(json.getString("account_id"));
            installment.setAccountName(json.getString("account_name"));
            installment.setReference(json.getString("reference"));
            installment.setProgram(json.getString("program"));

            if (json.has("payment_method")) {
                JsonDoc paymentMethodJson = json.get("payment_method");
                installment.setResult(paymentMethodJson.getString("result"));
                installment.setEntryMode(paymentMethodJson.getString("entry_mode"));
                installment.setMessage(paymentMethodJson.getString("message"));
                if (paymentMethodJson.has("card")) {
                    JsonDoc cardJson = paymentMethodJson.get("card");
                    Card card = new Card();
                    if (cardJson != null) {
                        card.setBrand(cardJson.getString("brand"));
                        card.setMaskedNumberLast4(cardJson.getString("masked_number_last4"));
                        card.setBrandReference(cardJson.getString("brand_reference"));
                        installment.setAuthCode(cardJson.getString("authcode"));

                    }
                    installment.setCard(card);
                }

            }
            if (json.has("action")) {
                JsonDoc actionJson = json.get("action");
                Action action = new Action();
                action.setType(actionJson.getString("type"));
                action.setId(actionJson.getString("id"));
                action.setTimeCreated(actionJson.getString("time_created"));
                action.setAppId(actionJson.getString("app_id"));
                action.setAppName(actionJson.getString("app_name"));
                action.setResultCode(actionJson.getString("result_code"));
                installment.setAction(action);
            }

            List<Terms> termsList = new ArrayList();
            if (json.has("terms")) {
                List<JsonDoc> terms = json.getEnumerator("terms");
                for (JsonDoc termObject : terms) {
                    Terms term = new Terms();
                    term.setId(termObject.getString("id"));
                    term.setTimeUnit(termObject.getString("time_unit"));
                    term.setTimeUnitNumbers((List<Integer>) termObject.get("time_unit_numbers"));
                    termsList.add(term);
                }
                installment.setTerms(termsList);
            }
        }
        return  installment;
    }
}