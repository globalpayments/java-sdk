package com.global.api.paymentMethods;

import com.global.api.entities.AlternativePaymentResponse;
import com.global.api.entities.BNPLResponse;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.FundsAccountDetails;
import com.global.api.network.entities.NtsData;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.gnap.ISOResponseCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private BNPLResponse BNPLResponse;
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
    private Integer sequenceNumber;
    private String systemTraceAuditNumber;
    private String transactionId;
    private List<FundsAccountDetails> transferFundsAccountDetailsList;
    private String originalTransactionDate;
    private String responseCode;
    private boolean useAuthorizedAmount;
    private String transactionIdentifier;
    private String originalInvoiceNumber;
    private String originalTransactionInfo;
    private String originalPosEntryMode;
    private TransactionType originalTransactionType;
    private ISOResponseCode isoResponseCode;

    private String approvalCode;
    private String originalMessageCode;
    private Map<UserDataTag, String> bankcardData;
    private TransactionCode originalTransactionCode;
    private TransactionTypeIndicator originalTransactionTypeIndicator;
    private String mastercardBanknetRefNo;
    private String mastercardBanknetSettlementDate;
    private AuthorizerCode authorizer;
    private String debitAuthorizer;
    private String visaTransactionId;
    private String discoverNetworkRefId;
    private String feeAmount;

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
