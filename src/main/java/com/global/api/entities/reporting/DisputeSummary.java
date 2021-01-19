package com.global.api.entities.reporting;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Getter @Setter
public class DisputeSummary {
    private String merchantHierarchy;
    private String merchantName;
    private String merchantDbaName;
    private String merchantNumber;
    private String merchantCategory;
    private Date depositDate;
    private String depositReference;
    private String depositType;
    private String type;
    private BigDecimal caseAmount;
    private String caseCurrency;
    private String caseStage;
    private String caseStatus;
    private String caseDescription;
    private String transactionOrderId;
    private Date transactionLocalTime;
    private Date transactionTime;
    private String transactionType;
    private BigDecimal transactionAmount;
    private String transactionCurrency;
    private String caseNumber;
    private Date caseTime;
    private String caseId;
    private Date caseIdTime;
    private String caseMerchantId;
    private String caseTerminalId;
    private String transactionARN;
    private String transactionReferenceNumber;
    private String transactionSRD;
    private String transactionAuthCode;
    private String transactionCardType;
    private String transactionMaskedCardNumber;
    private String reason;
    private String reasonCode;
    private String result;
    private String issuerComment;
    private String issuerCaseNumber;
    private BigDecimal disputeAmount;
    private String disputeCurrency;
    private BigDecimal disputeCustomerAmount;
    private String disputeCustomerCurrency;
    private Date respondByDate;
    private String caseOriginalReference;
    private BigDecimal lastAdjustmentAmount;
    private String lastAdjustmentCurrency;
    private String lastAdjustmentFunding;

    public ManagementBuilder Accept() {
        return new ManagementBuilder(TransactionType.DisputeAcceptance)
                .withDisputeId(caseId);
    }

    public ManagementBuilder Challenge(ArrayList<DisputeDocument> documents) {
        return new ManagementBuilder(TransactionType.DisputeChallenge)
                .withDisputeId(caseId)
                .withDisputeDocuments(documents);
    }

}