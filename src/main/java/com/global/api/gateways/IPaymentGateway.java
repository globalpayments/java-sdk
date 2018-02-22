package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;

public interface IPaymentGateway {
    Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException;
    Transaction manageTransaction(ManagementBuilder builder) throws ApiException;
    <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException;
    String serializeRequest(AuthorizationBuilder builder) throws ApiException;
    boolean supportsHostedPayments();
}
