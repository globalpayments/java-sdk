package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;

public interface IOpenBankingProvider {
    boolean supportsHostedPayments();

    Transaction processOpenBanking(AuthorizationBuilder builder) throws ApiException;

    Transaction manageOpenBanking(ManagementBuilder builder) throws GatewayException;
}