package com.global.api.gateways;

import com.global.api.builders.BankPaymentBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;

public interface IOpenBankingProvider {
    boolean supportsHostedPayments();
    Transaction processOpenBanking(BankPaymentBuilder builder) throws ApiException;
    String serializeRequest(BankPaymentBuilder builder) throws ApiException;
}