package com.global.api.paymentMethods;

import com.global.api.entities.enums.PaymentMethodType;

public class TransactionReference implements IPaymentMethod {
	private String alternativePaymentType;
    private String authCode;
    private String orderId;
    private PaymentMethodType paymentMethodType;
    private String transactionId;
    private String clientTransactionId;

    public String getAlternativePaymentType() {
		return alternativePaymentType;
	}
	public void setAlternativePaymentType(String alternativePaymentType) {
		this.alternativePaymentType = alternativePaymentType;
	}

    public String getAuthCode() {
        return authCode;
    }
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    public void setPaymentMethodType(PaymentMethodType paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }
    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }
}
