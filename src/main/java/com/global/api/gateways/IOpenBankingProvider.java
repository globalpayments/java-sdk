package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;

public interface IOpenBankingProvider {
    boolean supportsHostedPayments();
    Transaction processOpenBanking(AuthorizationBuilder builder) throws ApiException;
}