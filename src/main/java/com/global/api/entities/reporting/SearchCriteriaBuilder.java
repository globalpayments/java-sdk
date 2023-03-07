package com.global.api.entities.reporting;

import com.global.api.builders.ReportBuilder;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.IPaymentMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
public class SearchCriteriaBuilder<TResult> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ReportBuilder<TResult> _reportBuilder;

    private String accountName;
    private String accountNumberLastFour;
    private String actionId;
    private String actionType;
    private String altPaymentStatus;
    private BigDecimal amount;
    private String appName;
    private String aquirerReferenceNumber;
    private String authCode;
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String batchId;
    private String batchSequenceNumber;
    private String brandReference;
    private String buyerEmailAddress;
    private String cardBrand;
    private String cardHolderFirstName;
    private String cardHolderLastName;
    private String cardHolderPoNumber;
    private String cardNumberFirstSix;
    private String cardNumberLastFour;
    private String caseId;
    private String caseNumber;
    private ArrayList<CardType> cardTypes;
    private Channel channel;
    private String checkFirstName;
    private String checkLastName;
    private String checkName;
    private String checkNumber;
    private String clerkId;
    private String clientTransactionId;
    private String country;
    private String currency;
    private String customerId;
    private String depositReference;
    private DepositStatus depositStatus;
    private String description;
    private String displayName;
    private String disputeId;
    private DisputeStage disputeStage;
    private DisputeStatus disputeStatus;
    private String disputeDocumentId;
    private Date endBatchDate;
    private Date endDate;
    private Date endDepositDate;
    private Date endLastUpdatedDate;
    private Date endStageDate;
    private DateTime expirationDate;
    private boolean fullyCaptured;
    private String giftCurrency;
    private String giftMaskedAlias;
    private String hierarchy;
    private String httpResponseCode;
    private String invoiceNumber;
    private String issuerResult;
    private String issuerTransactionId;
    private Date localTransactionEndTime;
    private Date localTransactionStartTime;
    private String maskedCardNumber;
    private String merchantId;
    private String merchantName;
    private String name;
    private boolean oneTime;
    private String orderId;
    private String payLinkStatus;
    private PaymentEntryMode paymentEntryMode;
    private String paymentMethodKey;
    private PaymentMethodType paymentMethodType;
    private PaymentProvider paymentProvider;
    private PaymentType paymentType;
    private PaymentMethodName paymentMethodName;
    private String paymentMethodUsageMode;
    private IPaymentMethod paymentMethod;
    private ArrayList<PaymentMethodType> paymentTypes;
    private String referenceNumber;
    private String bankPaymentId;
    private String payLinkId;
    private Boolean returnPII;
    private FraudFilterMode riskAssessmentMode;
    private FraudFilterResult riskAssessmentResult;
    private ReasonCode riskAssessmentReasonCode;
    private String resource;
    private String resourceStatus;
    private String resourceId;
    private String responseCode;
    private ArrayList<TransactionType> transactionType;
    private BigDecimal settlementAmount;
    private String settlementDisputeId;
    private String scheduleId;
    private String siteTrace;
    private Date startBatchDate;
    private Date startDate;
    private Date startDepositDate;
    private Date startLastUpdatedDate;
    private Date startStageDate;
    private String storedPaymentMethodId;
    private StoredPaymentMethodStatus storedPaymentMethodStatus;
    private String systemHierarchy;
    private String tokenFirstSix;
    private String tokenLastFour;
    private TransactionStatus transactionStatus;
    private BankPaymentStatus bankPaymentStatus;
    private String uniqueDeviceId;
    private String username;
    String timezone;
    String version;

    public SearchCriteriaBuilder(ReportBuilder<TResult> reportBuilder) {
        _reportBuilder = reportBuilder;
    }

    public TResult execute() throws ApiException {
        return execute("default");
    }

    public TResult execute(String configName) throws ApiException {
        return _reportBuilder.execute(configName);
    }

    public <T> SearchCriteriaBuilder<TResult> and(SearchCriteria criteria, T value) {
        String criteriaValue = criteria.toString();
        if (criteriaValue != null) {
            set(this, criteriaValue, value);
        }
        return this;
    }

    public <T> SearchCriteriaBuilder<TResult> and(DataServiceCriteria criteria, T value) {
        String criteriaValue = criteria.toString();
        if (criteriaValue != null) {
            set(this, criteriaValue, value);
        }
        return this;
    }

    // https://stackoverflow.com/questions/14374878/using-reflection-to-set-an-object-property/14374995
    private static <T> boolean set(Object object, String fieldName, T fieldValue) {
        Class<?> clazz = object.getClass();

        // https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
        char c[] = fieldName.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        fieldName = new String(c);

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }
}