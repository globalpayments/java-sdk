package com.global.api.entities.transactionApi;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.transactionApi.entities.TransactionApiRegion;
import com.global.api.entities.transactionApi.entities.TransactionApiUtils;
import com.global.api.entities.transactionApi.enums.TransactionAPIEndPoints;
import com.global.api.gateways.TransactionApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.CurrencyUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.math.RoundingMode;

public class TransactionApiAuthRequestBuilder {
    private TransactionApiAuthRequestBuilder() {
        throw new IllegalStateException("TransactionApiAuthRequestBuilder.class");
    }

    public static TransactionApiRequest buildRequest(AuthorizationBuilder builder, TransactionApiConnector gateway) {
        TransactionApiRegion region = gateway.getApiConfig().getRegion();
        TransactionAPIEndPoints transactionUrl = TransactionApiUtils.getEndpointURL(builder);
        boolean isCreditCardData = builder.getPaymentMethod() instanceof CreditCardData;
        boolean iseCheck = builder.getPaymentMethod() instanceof eCheck;
        PaymentMethodUsageMode mode = builder.getPaymentMethodUsageMode();

        // Main request.
        JsonDoc request = new JsonDoc();

        if (isCreditCardData) {
            CreditCardData card = (CreditCardData) builder.getPaymentMethod();

            // Added Card Details.
            JsonDoc cardDetails = new JsonDoc();
            if(!StringUtils.isNullOrEmpty(card.getToken())){
                if(mode == null || mode == PaymentMethodUsageMode.MULTIPLE){
                    cardDetails
                            .set("token", card.getToken());
                } else {
                    cardDetails
                            .set("temporary_token", card.getToken());
                }
            } else {
                cardDetails
                        .set("card_number", card.getNumber())
                        .set("card_security_code", card.getCvn())
                        .set("cardholder_name", card.getCardHolderName())
                        .set("expiry_month", card.getExpMonth().toString())
                        .set("expiry_year", card.getExpYear().toString());
            }

            request.set("card", cardDetails);
        } else if (iseCheck) {
            eCheck echeck = (eCheck) builder.getPaymentMethod();
            String account = echeck.getAccountType().getValue();
            JsonDoc check = new JsonDoc();
            check
                    .set("account_type", account.substring(0, 1).toUpperCase() + account.substring(1).toLowerCase())
                    .set("check_number", echeck.getCheckNumber());

            if(!StringUtils.isNullOrEmpty(echeck.getToken())){
                check
                        .set("token", echeck.getToken())
                        .set("check_number", echeck.getCheckNumber());
            } else {
                check
                        .set("account_number", echeck.getAccountNumber());
                if (region == TransactionApiRegion.US) {
                    check.set("routing_number", echeck.getRoutingNumber());
                } else if (region == TransactionApiRegion.CA) {
                    check
                            .set("branch_transit_number", echeck.getTransitNumber())
                            .set("financial_institution_number", echeck.getFinancialInstituteNumber());
                }
            }
            request.set("check", check);
        }

        // Preparing the payment information.
        JsonDoc paymentInfo = new JsonDoc();
        paymentInfo
                .set("amount", String.valueOf(builder.getAmount().setScale(2, RoundingMode.HALF_UP)))
                .set("invoice_number", builder.getInvoiceNumber())
                .set("currency_code", CurrencyUtils.getCurrencyByCode(builder.getCurrency()));

        request.set("payment", paymentInfo);

        // Preparing the transaction information.
        if (isCreditCardData) {
            JsonDoc transactionInfo = new JsonDoc();
            transactionInfo
                    .set("country_code", TransactionApiUtils.getCountryCode(builder))
                    .set("language", builder.getCardHolderLanguage())
                    .set("soft_descriptor", getDescription(builder.getDescription()));

            JsonDoc processingIndicators = new JsonDoc();
            processingIndicators
                    .set("generate_receipt", builder.isGenerateReceipt());
            if (builder.getTransactionType() == TransactionType.Auth) {
                transactionInfo.set("ecommerce_indicator", builder.getEcommerceAuthIndicator());
                processingIndicators
                        .set("address_verification_service", builder.isAvs())
                        .set("partial_approval", builder.isAllowPartialAuth());
            } else if (builder.getTransactionType() == TransactionType.Sale) {
                transactionInfo.set("ecommerce_indicator", builder.getEcommerceAuthIndicator());
                processingIndicators
                        .set("address_verification_service", builder.isAvs())
                        .set("partial_approval", builder.isAllowPartialAuth())
                        .set("allow_duplicate", builder.isAllowDuplicates());
            } else if (builder.getTransactionType() == TransactionType.Refund) {
                processingIndicators
                        .set("allow_duplicate", builder.isAllowDuplicates());
            }

            // Multi-use token creation flag.
            if(builder.isRequestMultiUseToken()) {
                processingIndicators
                        .set("create_token", builder.isRequestMultiUseToken());
            }

            transactionInfo.set("processing_indicators", processingIndicators);

            request.set("transaction", transactionInfo);

        } else if (iseCheck) {
            eCheck check = (eCheck) builder.getPaymentMethod();
            JsonDoc transactionInfo = new JsonDoc();
            transactionInfo
                    .set("country_code", TransactionApiUtils.getCountryCode(builder))
                    .set("language", builder.getCardHolderLanguage())
                    .set("soft_descriptor", getDescription(builder.getDescription()));
            if (region == TransactionApiRegion.US) {
                transactionInfo
                        .set("entry_class", check.getSecCode());
            } else if (region == TransactionApiRegion.CA) {
                transactionInfo
                        .set("payment_purpose_code", builder.getPaymentPurposeCode());
            }
            if (builder.getTransactionType() == (TransactionType.Sale)) {
                JsonDoc processingIndicators = new JsonDoc();
                processingIndicators
                        .set("check_verify", false);

                // Multi-use token creation flag.
                if(builder.isRequestMultiUseToken()) {
                    processingIndicators
                            .set("create_token", builder.isRequestMultiUseToken());
                }
                transactionInfo.set("processing_indicators", processingIndicators);
            }
            request.set("transaction", transactionInfo);

        }

        // Preparing Customer Info.
        if (builder.getCustomer() != null) {
            JsonDoc customerInfo = new JsonDoc();
            Customer customer = builder.getCustomer();
            customerInfo
                    .set("id", customer.getId())
                    .set("phone", customer.getMobilePhone())
                    .set("email", customer.getEmail())
                    .set("note", customer.getNote());

            if (StringUtils.isNullOrEmpty(customer.getCompany())) {
                customerInfo
                        .set("title", customer.getTitle())
                        .set("first_name", customer.getFirstName())
                        .set("middle_name", customer.getMiddleName())
                        .set("last_name", customer.getLastName());
            } else {
                customerInfo
                        .set("business_name", customer.getCompany());
            }


            if (isCreditCardData) {
                JsonDoc billingAddress = new JsonDoc();
                if (customer.getAddress() != null) {
                    Address address = customer.getAddress();
                    billingAddress
                            .set("line1", address.getStreetAddress1())
                            .set("line2", address.getStreetAddress2())
                            .set("city", address.getCity())
                            .set("state", address.getState())
                            .set("country", address.getCountry())
                            .set("postal_code", address.getPostalCode());
                    customerInfo.set("billing_address", billingAddress);
                }
            }
            request.set("customer", customerInfo);
        }

        // Generating the reference ID.
        request.set("reference_id", builder.getClientTransactionId());

        // Clerk ID
        if (builder.getClerkId() != null) {
            JsonDoc clerkId = new JsonDoc();
            clerkId.set("clerk_id", builder.getClerkId());
            request.set("receipt", clerkId);
        }

        return
                new TransactionApiRequest()
                        .setEndpoint(transactionUrl.getValue())
                        .setRequestBody(request.toString())
                        .setVerb(transactionUrl.getMethod());
    }

    private static String getDescription(String description){
        return
                description != null
                        ? StringUtils.subString(description, 17, ' ')
                        : null;
    }
}
