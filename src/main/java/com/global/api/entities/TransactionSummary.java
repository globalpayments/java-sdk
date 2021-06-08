package com.global.api.entities;

import java.math.BigDecimal;
import java.util.Date;

import com.global.api.entities.reporting.AltPaymentData;
import com.global.api.entities.reporting.CheckData;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
public class TransactionSummary {
    private String accountDataSource;
    private BigDecimal adjustmentAmount;
    private String adjustmentCurrency;
    private String adjustmentReason;
    private AltPaymentData altPaymentData;
    private BigDecimal amount;
    private String attachmentInfo;
    private String acquirerReferenceNumber;
    private BigDecimal authorizedAmount;
    private String authCode;
    private String avsResponseCode;
    private DateTime batchCloseDate;
    private String batchId;
    private String batchSequenceNumber;
    private Address billingAddress;
    private String brandReference;
    private BigDecimal captureAmount;
    private String cardHolderFirstName;
    private String cardHolderLastName;
    private String cardHolderName;
    private String cardSwiped;
    private String cardType;
    private String cavvResponseCode;
    private String channel;
    private CheckData checkData;
    private String clerkId;
    private String clientTransactionId;
    private String companyName;
    private BigDecimal convenienceAmount;
    private String currency;
    private String customerFirstName;
    private String customerId;
    private String customerLastName;
    private String cvnResponseCode;
    private boolean debtRepaymentIndicator;
    private BigDecimal depositAmount;
    private String depositCurrency;
    private Date depositDate;
    private String depositReference;
    private String depositStatus;
    private String description;
    private int deviceId;
    private String eciIndicator;
    private String emvChipCondition;
    private String emvIssuerResponse;
	private String entryMode;
    private String fraudRuleInfo;
    private boolean fullyCaptured;
    private BigDecimal cashBackAmount;
    private BigDecimal gratuityAmount;
    private boolean hasEcomPaymentData;
    private boolean hasEmvTags;
    private Boolean hostTimeOut;
    private String invoiceNumber;
    private String issuerResponseCode;
    private String issuerResponseMessage;
    private String issuerTransactionId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String giftCurrency;
    private LodgingData lodgingData;
    private String maskedAlias;
    private String maskedCardNumber;
    private String merchantCategory;
    private String merchantDbaName;
    private String merchantHierarchy;
    private String merchantId;
    private String merchantName;
    private String merchantNumber;
    private boolean oneTimePayment;
    private String orderId;
    private String originalTransactionId;
    private String paymentMethodKey;
    private String paymentType;
    private String poNumber;
    private String recurringDataCode;
    private String referenceNumber;
    private int repeatCount;
    private DateTime responseDate;
    private String scheduleId;
    private String schemeReferenceData;
    private String serviceName;
    private BigDecimal settlementAmount;
    private BigDecimal shippingAmount;
    private String siteTrace;
    private String status;
    private BigDecimal surchargeAmount;
    private BigDecimal taxAmount;
    private String taxType;
    private String terminalId;
    private String tokenPanLastFour;
    private DateTime transactionDate;
    private DateTime transactionLocalDate;
    private String transactionDescriptor;
    private String transactionStatus;
    private String transactionId;
    private String uniqueDeviceId;
    private String username;
    private String xid;
    private String transactionType;
    private String CardEntryMethod;
    private BigDecimal amountDue;
    public String country;

	public String getOrderId() {
	    return this.clientTransactionId;
    }
    public void setOrderId(String value) {
		this.clientTransactionId = value;
	}
}
