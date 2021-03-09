package com.global.api.gateways.bill_pay;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.Customer;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.billing.TokenResponse;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.bill_pay.requests.CreateCustomerAccountRequest;
import com.global.api.gateways.bill_pay.requests.CreateSingleSignOnAccountRequest;
import com.global.api.gateways.bill_pay.requests.DeleteCustomerAccountRequest;
import com.global.api.gateways.bill_pay.requests.DeleteSingleSignOnAccountRequest;
import com.global.api.gateways.bill_pay.requests.UpdateCustomerAccountRequest;
import com.global.api.gateways.bill_pay.requests.UpdateSingleSignOnAccountRequest;
import com.global.api.gateways.bill_pay.responses.CreateCustomerAccountResponse;
import com.global.api.gateways.bill_pay.responses.CustomerAccountResponse;
import com.global.api.gateways.bill_pay.responses.SingleSignOnAccountResponse;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class RecurringRequest<T> extends GatewayRequestBase {
    public RecurringRequest(Credentials credentials, String serviceUrl, int timeout) {
        this.credentials = credentials;
        this.serviceUrl = serviceUrl;
        this.timeout = timeout;
    }

    public T execute(RecurringBuilder<T> builder) throws ApiException {
        if (builder.getEntity() instanceof Customer) {
            return customerRequest((Customer) builder.getEntity(), builder.getTransactionType());
        }

        if (builder.getEntity() instanceof RecurringPaymentMethod) {
            return customerAccountRequest((RecurringPaymentMethod) builder.getEntity(), builder.getTransactionType());
        }

        throw new UnsupportedTransactionException();
    }

    private T customerRequest(Customer customer, TransactionType type) throws ApiException {
        switch (type) {
            case Create:
                return createSingleSignOnAccount(customer);
            case Edit:
                return updateSingleSignOnAccount(customer);
            case Delete:
                return deleteSingleSignOnAccount(customer);
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private T customerAccountRequest(RecurringPaymentMethod paymentMethod, TransactionType type) throws ApiException {
        switch (type) {
            case Create:
                return createCustomerAccount(paymentMethod);
            case Edit:
                return updateCustomerAccount(paymentMethod);
            case Delete:
                return deleteCustomerAccount(paymentMethod);
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private T createSingleSignOnAccount(Customer customer) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "CreateSingleSignOnAccount");
        String request = new CreateSingleSignOnAccountRequest(et)
            .build(envelope, credentials, customer);

        String response = doTransaction(request);

        BillingResponse result = new SingleSignOnAccountResponse()
            .withResponseTagName("CreateSingleSignOnAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            customer.setKey(customer.getId());
            return (T) customer;
        }

        throw new GatewayException("An error occurred while creating the customer", result.getResponseMessage(), result.getResponseMessage());
    }

    private T updateSingleSignOnAccount(Customer customer) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "UpdateSingleSignOnAccount");
        String request = new UpdateSingleSignOnAccountRequest(et)
            .build(envelope, credentials, customer);

        String response = doTransaction(request);

        BillingResponse result = new SingleSignOnAccountResponse()
            .withResponseTagName("UpdateSingleSignOnAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return (T) customer;
        }

        throw new GatewayException("An error occurred while updating the customer", result.getResponseMessage(), result.getResponseMessage());
    }

    private T deleteSingleSignOnAccount(Customer customer) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "DeleteSingleSignOnAccount");
        String request = new DeleteSingleSignOnAccountRequest(et)
            .build(envelope, credentials, customer);

        String response = doTransaction(request);

        BillingResponse result = new SingleSignOnAccountResponse()
            .withResponseTagName("DeleteSingleSignOnAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return (T) customer;
        }

        throw new GatewayException("An error occurred while deleting the customer", result.getResponseMessage(), result.getResponseMessage());
    }

    private T createCustomerAccount(RecurringPaymentMethod paymentMethod) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "SaveCustomerAccount");
        String request = new CreateCustomerAccountRequest(et)
            .build(envelope, credentials, paymentMethod);

        String response = doTransaction(request);

        TokenResponse result = new CreateCustomerAccountResponse()
            .withResponseTagName("SaveCustomerAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            paymentMethod.setKey(paymentMethod.getId());
            return (T) paymentMethod;
        }

        throw new GatewayException("An error occurred while creating the customer account", result.getResponseMessage(), result.getResponseMessage());
    }

    private T updateCustomerAccount(RecurringPaymentMethod paymentMethod) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "UpdateCustomerAccount");
        String request = new UpdateCustomerAccountRequest(et)
            .build(envelope, credentials, paymentMethod);

        String response = doTransaction(request);

        BillingResponse result = new CustomerAccountResponse()
            .withResponseTagName("UpdateCustomerAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return (T) paymentMethod;
        }

        throw new GatewayException("An error occurred while updating the customer account", result.getResponseMessage(), result.getResponseMessage());
    }

    private T deleteCustomerAccount(RecurringPaymentMethod paymentMethod) throws ApiException
    {
        ElementTree et = new ElementTree();
        Element envelope = createSOAPEnvelope(et, "DeleteCustomerAccount");
        String request = new DeleteCustomerAccountRequest(et)
            .build(envelope, credentials, paymentMethod);

        String response = doTransaction(request);

        BillingResponse result = new SingleSignOnAccountResponse()
            .withResponseTagName("DeleteCustomerAccountResponse")
            .withResponse(response)
            .map();

        if (result.isSuccessful()) {
            return (T) paymentMethod;
        }

        throw new GatewayException("An error occurred while deleting the customer account", result.getResponseMessage(), result.getResponseMessage());
    }
}
