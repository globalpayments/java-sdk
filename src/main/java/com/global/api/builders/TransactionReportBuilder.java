package com.global.api.builders;

import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.enums.TimeZoneConversion;
import com.global.api.entities.enums.TransactionSortProperty;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.SearchCriteriaBuilder;
import com.global.api.entities.enums.DepositSortProperty;
import com.global.api.entities.enums.DisputeSortProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class TransactionReportBuilder<TResult> extends ReportBuilder<TResult> {
    private String deviceId;
    private Date endDate;
    private Date startDate;
    private String transactionId;
    @Getter @Setter private int page = 1;       // 1: DEFAULT PARAM VALUE
    @Getter @Setter private int pageSize = 5;   // 5: DEFAULT PARAM VALUE
    @Getter @Setter private TransactionSortProperty transactionOrderBy;
    @Getter @Setter private SortDirection transactionOrder;
    @Getter @Setter private DepositSortProperty depositOrderBy;
    @Getter @Setter private SortDirection depositOrder;
    @Getter @Setter private DisputeSortProperty disputeOrderBy;
    @Getter @Setter private SortDirection disputeOrder;

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

    public TransactionReportBuilder<TResult> withDepositId(String value) {
        getSearchBuilder().setDepositId(value);
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
    public TransactionReportBuilder<TResult> withTimeZoneConversion(TimeZoneConversion value) {
        setTimeZoneConversion(value);
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

    public TransactionReportBuilder<TResult> WithSettlementDisputeId(String value) {
        getSearchBuilder().setSettlementDisputeId(value);
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(TransactionSortProperty orderBy, SortDirection direction)
    {
        this.transactionOrderBy = orderBy;
        this.transactionOrder = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(DepositSortProperty orderBy, SortDirection direction) {
        depositOrderBy = orderBy;
        depositOrder = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public TransactionReportBuilder<TResult> orderBy(DisputeSortProperty orderBy, SortDirection direction) {
        disputeOrderBy = orderBy;
        disputeOrder = (direction != null) ? direction : SortDirection.Ascending;
        return this;
    }

    public void setupValidations() {
        this.validations.of(ReportType.TransactionDetail)
                .check("transactionId").isNotNull();

        this.validations.of(ReportType.Activity).check("transactionId").isNull();
    }
}