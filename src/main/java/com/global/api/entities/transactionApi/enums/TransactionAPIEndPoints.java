package com.global.api.entities.transactionApi.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.transactionApi.TransactionApiRequest;

public enum TransactionAPIEndPoints implements IStringConstant {
    CreditSale("/creditsales", TransactionApiRequest.HttpMethod.POST),
    CreditAuth("/creditauths",TransactionApiRequest.HttpMethod.POST),
    CreditReturn("/creditreturns",TransactionApiRequest.HttpMethod.POST),
    CheckSales("/checksales",TransactionApiRequest.HttpMethod.POST),
    CheckRefund("/checkrefunds",TransactionApiRequest.HttpMethod.POST),
    RefundCreditSaleWithTransactionId("/creditsales/%s/creditreturns", TransactionApiRequest.HttpMethod.POST),
    RefundCreditSaleWithReferenceId("/creditsales/reference_id/%s/creditreturns",TransactionApiRequest.HttpMethod.POST),
    RefundChecksaleWithTransactionId("/checksales/%s/checkrefunds",TransactionApiRequest.HttpMethod.POST),
    RefundCheckSaleWithReferenceId("/checksales/reference_id/%s/checkrefunds",TransactionApiRequest.HttpMethod.POST),
    CreditSaleWithTransactionId("/creditsales/%s", TransactionApiRequest.HttpMethod.PATCH),
    CreditSaleWithReferenceId("/creditsales/reference_id/%s",TransactionApiRequest.HttpMethod.PATCH),
    CreditVoidWithTransactionId("/creditsales/%s/voids",TransactionApiRequest.HttpMethod.PUT),
    CreditVoidWithReferenceID("/creditsales/reference_id/%s/voids",TransactionApiRequest.HttpMethod.PUT),
    VoidCreditReturnWithTransactionID("/creditreturns/%s/voids",TransactionApiRequest.HttpMethod.PUT),
    VoidCreditReturnWithReferenceID("/creditreturns/reference_id/%s/voids",TransactionApiRequest.HttpMethod.PUT),
    CreditSaleWithTransactionIdGet("/creditsales/%s", TransactionApiRequest.HttpMethod.GET),
    CreditSaleWithReferenceIdGet("/creditsales/reference_id/%s",TransactionApiRequest.HttpMethod.GET),
    CreditReturnWithTransactionIdGet("/creditreturns/%s", TransactionApiRequest.HttpMethod.GET),
    CreditReturnWithReferenceIdGet("/creditreturns/reference_id/%s",TransactionApiRequest.HttpMethod.GET),
    ACHSaleWithTransactionId("/checksales/%s",TransactionApiRequest.HttpMethod.GET),
    ACHSaleWithReferenceId("/checksales/reference_id/%s",TransactionApiRequest.HttpMethod.GET),
    ACHRefundWithTransactionId("/checkrefunds/%s",TransactionApiRequest.HttpMethod.GET),
    ACHRefundWithReferenceId("/checkrefunds/reference_id/%s",TransactionApiRequest.HttpMethod.GET);
    final String endpoint;
    final TransactionApiRequest.HttpMethod method;

    TransactionAPIEndPoints(String endpoint, TransactionApiRequest.HttpMethod method) {
        this.endpoint = endpoint;
        this.method = method;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public String getValue() {
        return this.endpoint;
    }

    public TransactionApiRequest.HttpMethod getMethod(){
        return this.method;
    }
}
