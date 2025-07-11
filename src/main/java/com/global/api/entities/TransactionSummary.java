package com.global.api.entities;

import com.global.api.entities.billing.AuthorizationRecord;
import com.global.api.entities.billing.Bill;
import com.global.api.entities.reporting.AltPaymentData;
import com.global.api.entities.reporting.CheckData;
import com.global.api.paymentMethods.InstallmentData;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TransactionSummary {
    private String appName;
    private BigDecimal availableBalance;
    private String approvalCode;
    private String accountDataSource;
    private BigDecimal adjustmentAmount;
    private String adjustmentCurrency;
    private String adjustmentReason;
    private AltPaymentData altPaymentData;
    private BigDecimal amount;
    private String attachmentInfo;
    private String acquirerReferenceNumber;
    private List<AuthorizationRecord> authorizationRecord;
    private BigDecimal authorizedAmount;
    private String authCode;
    private String avsResponseCode;
    private BigDecimal baseAmount;
    private DateTime batchCloseDate;
    private String batchId;
    private String batchSequenceNumber;
    private Address billingAddress;
    private List<Bill> billTransactions;
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
    private String email;
    private String emvChipCondition;
    private String emvIssuerResponse;
	private String entryMode;
    private String expiryDate;
    private BigDecimal feeAmount;
    private String fraudRuleInfo;
    private boolean fullyCaptured;
    private BigDecimal cashBackAmount;
    private BigDecimal gratuityAmount;
    private boolean hasEcomPaymentData;
    private boolean hasEmvTags;
    private String hasLevelIII;
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
    private String merchantDeviceIdentifier;
    private String merchantName;
    private String merchantNumber;
    private BigDecimal netAmount;
    private BigDecimal netFeeAmount;
    private boolean oneTimePayment;
    private String orderId;
    private String originalTransactionId;
    private String paymentMethodKey;
    private String paymentType;
    private Customer payOrData;
    private String pinVerified;
    private String poNumber;
    private String recurringDataCode;
    private String referenceNumber;
    private int repeatCount;
    private BigDecimal requestAmount;
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
    private String tranNo;
    private String terminalId;
    private ThreeDSecure threeDSecure;
    private String tokenPanLastFour;
    private DateTime transactionDate;
    private DateTime transactionLocalDate;
    private String transactionDescriptor;
    private String transactionStatus;
    private String transactionId;
    private String uniqueDeviceId;
    private String username;
    private String xid;
    private String accountNumberLast4;
    private String accountType;
    private AlternativePaymentResponse alternativePaymentResponse;
    private BankPaymentResponse bankPaymentResponse;
    private BNPLResponse BNPLResponse;
    private String fingerprint;
    private String fingerprintIndicator;
    private FraudManagementResponse fraudManagementResponse;
    private String transactionType;
    private String cardEntryMethod;
    private BigDecimal amountDue;
    public String country;
    private String language;
    private String paymentPurposeCode;
    private String verificationCode;
    private BigDecimal batchAmount;
    private String safTotal;
    private String totalAmount;
    private String safReferenceNumber;
    private String cardAcquisition;
    private String transactionTime;
    private Card cardDetails;
    private InstallmentData installmentData;
}
