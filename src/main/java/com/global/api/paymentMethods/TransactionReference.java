package com.global.api.paymentMethods;

import com.global.api.entities.AlternativePaymentResponse;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.entities.NtsData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class TransactionReference implements IPaymentMethod {
	private String alternativePaymentType;
	private String acquiringInstitutionId;
    private String authCode;
    private Integer batchNumber;
    private String clientTransactionId;
    private String messageTypeIndicator;
    private NtsData ntsData;
    private AlternativePaymentResponse alternativePaymentResponse;
    private String orderId;
    private BigDecimal originalAmount;
    private BigDecimal originalApprovedAmount;
    private EmvChipCondition originalEmvChipCondition;
    private IPaymentMethod originalPaymentMethod;
    private String originalProcessingCode;
    private String originalTransactionTime;
    private boolean partialApproval;
    private PaymentMethodType paymentMethodType;
    private String posDataCode;
    private int sequenceNumber;
    private String systemTraceAuditNumber;
    private String transactionId;
    private boolean useAuthorizedAmount;

    public void setNtsData(NtsData ntsData) {
        this.ntsData = ntsData;
    }

    public void setNtsData(String ntsData) throws GatewayException {
        this.ntsData = NtsData.fromString(ntsData);
    }

    public BigDecimal getOriginalApprovedAmount() {
        if(originalApprovedAmount != null) {
            return originalApprovedAmount;
        }
        return originalAmount;
    }

    public PaymentMethodType getPaymentMethodType() {
        if(originalPaymentMethod != null) {
            return originalPaymentMethod.getPaymentMethodType();
        }
        return paymentMethodType;
    }

}
