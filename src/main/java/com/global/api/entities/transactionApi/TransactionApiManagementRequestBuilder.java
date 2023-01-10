package com.global.api.entities.transactionApi;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.transactionApi.entities.TransactionApiRegion;
import com.global.api.entities.transactionApi.entities.TransactionApiUtils;
import com.global.api.entities.transactionApi.enums.TransactionAPIEndPoints;
import com.global.api.gateways.TransactionApiConnector;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.paymentMethods.eCheck;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.math.RoundingMode;

public class TransactionApiManagementRequestBuilder {

    private TransactionApiManagementRequestBuilder() {
        throw new IllegalStateException("TransactionApiManagementRequestBuilder class");
    }

    public static TransactionApiRequest buildRequest(ManagementBuilder builder, TransactionApiConnector gateway) {
        TransactionAPIEndPoints transactionUrl = TransactionApiUtils.getEndpointURL(builder);

        TransactionReference reference = null;
        if (builder.getPaymentMethod() instanceof TransactionReference) {
            reference = (TransactionReference) builder.getPaymentMethod();
        }

        // Main request.
        JsonDoc request = new JsonDoc();
        if(builder.getTransactionType()!=TransactionType.Fetch) {
            // Added check Details.
            if (builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.ACH
                    && builder.getBankTransferDetails() != null) {
                eCheck echeck = builder.getBankTransferDetails();
                String account = echeck.getAccountType().getValue();
                JsonDoc check = new JsonDoc();
                check
                        .set("account_type", account.substring(0, 1).toUpperCase() + account.substring(1).toLowerCase())
                        .set("check_number", echeck.getCheckNumber());

                request.set("check", check);

            }

            // Preparing the payment information.
            JsonDoc paymentInfo = new JsonDoc();
            if (builder.getTransactionType() == TransactionType.Void) {
                paymentInfo
                        .set("amount", String.valueOf(builder.getAmount().setScale(2, RoundingMode.HALF_UP)));

            } else {
                paymentInfo
                        .set("amount", String.valueOf(builder.getAmount().setScale(2, RoundingMode.HALF_UP)))
                        .set("invoice_number", builder.getInvoiceNumber());

            }
            request.set("payment", paymentInfo);
            // Preparing the transaction information.
            if (builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.Credit) {
                JsonDoc transactionInfo = new JsonDoc();
                if (builder.getTransactionType() == TransactionType.Void) {
                    JsonDoc processingIndicators = new JsonDoc();
                    processingIndicators
                            .set("generate_receipt", builder.isGenerateReceipt());

                    transactionInfo.set("processing_indicators", processingIndicators);

                } else {
                    transactionInfo
                            .set("soft_descriptor", builder.getDescription() != null ? StringUtils.subString(builder.getDescription(), 17, ' ') : null);

                    JsonDoc processingIndicators = new JsonDoc();
                    processingIndicators
                            .set("generate_receipt", builder.isGenerateReceipt())
                            .set("allow_duplicate", builder.isAllowDuplicates());

                    transactionInfo.set("processing_indicators", processingIndicators);

                }
                request.set("transaction", transactionInfo);

            } else if (builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.ACH) {
                JsonDoc transactionInfo = new JsonDoc();
                eCheck check = builder.getBankTransferDetails();
                if (gateway.getApiConfig().getRegion() == TransactionApiRegion.US) {
                    transactionInfo
                            .set("entry_class", check.getSecCode());

                } else if (gateway.getApiConfig().getRegion() == TransactionApiRegion.CA) {
                    transactionInfo
                            .set("payment_purpose_code", builder.getPaymentPurposeCode());
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
                request.set("customer", customerInfo);
            }

            // Clerk ID
            if (builder.getClerkId() != null) {
                JsonDoc clerkId = new JsonDoc();
                clerkId
                        .set("clerk_id", builder.getClerkId());
                request.set("receipt", clerkId);
            }
        }

        String url = transactionUrl.getValue();
        if (reference != null) {
            if (reference.getTransactionId() != null)
                url = String.format(url, reference.getTransactionId());
            else
                url = String.format(url, reference.getClientTransactionId());
        }

        return
                new TransactionApiRequest()
                        .setEndpoint(url)
                        .setRequestBody(request.toString())
                        .setVerb(transactionUrl.getMethod());
    }

}
