package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.BillingBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.RecurringBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.billing.Credentials;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.bill_pay.AuthorizationRequest;
import com.global.api.gateways.bill_pay.BillingRequest;
import com.global.api.gateways.bill_pay.ManagementRequest;
import com.global.api.gateways.bill_pay.RecurringRequest;
import com.global.api.network.NetworkMessageHeader;

public class BillPayProvider implements IBillingProvider, IPaymentGateway, IRecurringGateway {
    private Credentials credentials;
    private boolean isBillDataHosted;
    private int timeout;
    private String serviceUrl;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isBillDataHosted() {
        return isBillDataHosted;
    }

    public void setIsBillDataHosted(boolean isBillDataHosted) {
        this.isBillDataHosted = isBillDataHosted;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /// <summary>
    /// Invokes a request against the BillPay gateway using the AuthorizationBuilder
    /// </summary>
    /// <param name="builder">The <see
    /// cref="AuthorizationBuilder">AuthroizationBuilder</see> containing the
    /// required information to build the request</param>
    /// <returns>A Transaction response</returns>
    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        return new AuthorizationRequest(credentials, serviceUrl, timeout)
            .execute(builder, isBillDataHosted);
    }

    /// <summary>
    /// Invokes a request against the BillPay gateway using the ManagementBuilder
    /// </summary>
    /// <param name="builder">The <see
    /// cref="ManagementBuilder">ManagementBuilder</see> containing the required
    /// information to build the request</param>
    /// <returns>A Transaction response</returns>
    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        return new ManagementRequest(credentials, serviceUrl, timeout)
            .execute(builder, isBillDataHosted);
    }

    public BillingResponse processBillingRequest(BillingBuilder builder) throws ApiException {
        return new BillingRequest(credentials, serviceUrl, timeout)
            .execute(builder);
    }

    public <T> T processRecurring(RecurringBuilder<T> builder, Class<T> clazz) throws ApiException {
        return new RecurringRequest<T>(credentials, serviceUrl, timeout)
            .execute(builder);
    }

    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public String serializeRequest(AuthorizationBuilder builder) throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public boolean supportsHostedPayments() {
        return true;
    }

    public boolean supportsRetrieval() {
        return false;
    }

    public boolean supportsUpdatePaymentDetails() {
        return false;
    }
}
