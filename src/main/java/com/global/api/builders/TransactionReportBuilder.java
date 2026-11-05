package com.global.api.builders;

import com.global.api.entities.enums.*;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.SearchCriteriaBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class TransactionReportBuilder<TResult> extends ReportBuilder<TResult> {
    private String deviceId;
    private Date endDate;
    private Date startDate;
    @Getter @Setter private String startDateUTC;
    @Getter @Setter private String endDateUTC;
    private String transactionId;
    @Getter @Setter private int page = 1;       // 1: DEFAULT PARAM VALUE
    @Getter @Setter private int pageSize = 5;   // 5: DEFAULT PARAM VALUE
    @Getter @Setter private SortDirection order;
    @Getter @Setter private TransactionSortProperty transactionOrderBy;
    @Getter @Setter private DepositSortProperty depositOrderBy;
    @Getter @Setter private DisputeSortProperty disputeOrderBy;
    @Getter @Setter private StoredPaymentMethodSortProperty storedPaymentMethodOrderBy;
    @Getter @Setter private SortDirection storedPaymentMethodOrder;
    @Getter @Setter private ActionSortProperty actionOrderBy;
    @Getter @Setter private PayByLinkSortProperty payByLinkOrderBy;
    @Getter @Setter private String payByLinkId;
    @Getter @Setter private int batchId;


    private SearchCriteriaBuilder<TResult> _searchBuilder;

    public TransactionReportBuilder(ReportType type, Class<TResult> clazz) {
        super(type, clazz);
    }

    public SearchCriteriaBuilder<TResult> getSearchBuilder() {
        if (_searchBuilder == null) {
            _searchBuilder = new SearchCriteriaBuilder<TResult>(this);
        }
        return _searchBuilder;
    }

    public String getDeviceId() {
        return getSearchBuilder().getUniqueDeviceId();
    }
    public Date getEndDate() {
        return getSearchBuilder().getEndDate();
    }
    public Date getStartDate() {
        return getSearchBuilder().getStartDate();
    }
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionReportBuilder<TResult> withActionId(String value) {
        getSearchBuilder().setActionId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withDepositReference(String value) {
        getSearchBuilder().setDepositReference(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withDeviceId(String value) {
        getSearchBuilder().setUniqueDeviceId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withDisputeId(String value) {
        getSearchBuilder().setDisputeId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withEndDate(Date value) {
        getSearchBuilder().setEndDate(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withStartDate(Date value) {
        getSearchBuilder().setStartDate(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withStartDateUTC(String value) {
        setStartDateUTC(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withEndDateUTC(String value) {
        setEndDateUTC(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withTimeZoneConversion(TimeZoneConversion value) {
        setTimeZoneConversion(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withBatchId(int value){
        setBatchId(value);
        return this;
    }
    public TransactionReportBuilder<TResult> withTransactionId(String value) {
        this.transactionId = value;
        return this;
    }
    public <T> SearchCriteriaBuilder<TResult> where(SearchCriteria criteria, T value) {
        return getSearchBuilder().and(criteria, value);
    }

    public <T> SearchCriteriaBuilder<TResult> where(DataServiceCriteria criteria, T value) {
        return getSearchBuilder().and(criteria, value);
    }

    public TransactionReportBuilder<TResult> withPaging(int page, int pageSize)
    {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public TransactionReportBuilder<TResult> withSettlementDisputeId(String value) {
        getSearchBuilder().setSettlementDisputeId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withStoredPaymentMethodId(String value) {
        getSearchBuilder().setStoredPaymentMethodId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> withBankPaymentId(String bankPaymentId) {
        getSearchBuilder().setBankPaymentId(bankPaymentId);
        return this;
    }

    public TransactionReportBuilder<TResult> withPayByLinkId(String payByLinkId) {
        getSearchBuilder().setPayByLinkId(payByLinkId);
        this.payByLinkId = payByLinkId;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(TransactionSortProperty orderBy, SortDirection direction)
    {
        transactionOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(DepositSortProperty orderBy, SortDirection direction) {
        depositOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(DisputeSortProperty orderBy, SortDirection direction) {
        disputeOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(StoredPaymentMethodSortProperty orderBy, SortDirection direction) {
        storedPaymentMethodOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(ActionSortProperty orderBy, SortDirection direction) {
        actionOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(PayByLinkSortProperty orderBy, SortDirection direction) {
        payByLinkOrderBy = orderBy;
        order = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public void setupValidations() {
        this.validations.of(ReportType.TransactionDetail)
                .check("transactionId").isNotNull();

        this.validations.of(ReportType.Activity).check("transactionId").isNull();
        this.validations.of(ReportType.DocumentDisputeDetail)
                .check("_searchBuilder").propertyOf(String.class, "disputeDocumentId").isNotNull();
        this.validations.of(ReportType.PayByLinkDetail).check("payByLinkId").isNotNull();
        this.validations.of(ReportType.BatchDetail).check("batchId").isNotNull();
    }
}