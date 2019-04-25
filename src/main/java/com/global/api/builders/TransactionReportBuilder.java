package com.global.api.builders;

import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.TimeZoneConversion;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.SearchCriteriaBuilder;

import java.util.Date;

public class TransactionReportBuilder<TResult> extends ReportBuilder<TResult> {
    private String deviceId;
    private Date endDate;
    private Date startDate;
    private String transactionId;
    private SearchCriteriaBuilder<TResult> _searchBuilder;

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

    public TransactionReportBuilder<TResult> withDeviceId(String value) {
        this.deviceId = value;
        return this;
    }
    public TransactionReportBuilder<TResult> withEndDate(Date value) {
        this.endDate = value;
        return this;
    }
    public TransactionReportBuilder<TResult> withStartDate(Date value) {
        this.startDate = value;
        return this;
    }
    public TransactionReportBuilder<TResult> withTransactionId(String value) {
        this.transactionId = value;
        return this;
    }
    public TransactionReportBuilder<TResult> withTimeZoneConversion(TimeZoneConversion value) {
        setTimeZoneConversion(value);
        return this;
    }

    public TransactionReportBuilder(ReportType type, Class<TResult> clazz) {
        super(type, clazz);
    }

    public SearchCriteriaBuilder<TResult> getSearchBuilder() {
        if (_searchBuilder == null) {
            _searchBuilder = new SearchCriteriaBuilder<TResult>(this);
        }
        return _searchBuilder;
    }

    public <T> SearchCriteriaBuilder<TResult> where(SearchCriteria criteria, T value) {
        return getSearchBuilder().and(criteria, value);
    }

    public void setupValidations() {
        this.validations.of(ReportType.TransactionDetail)
                .check("transactionId").isNotNull();

        this.validations.of(ReportType.Activity).check("transactionId").isNull();
    }
}