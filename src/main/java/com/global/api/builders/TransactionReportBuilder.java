package com.global.api.builders;

import com.global.api.entities.enums.ReportType;

import java.util.Date;

public class TransactionReportBuilder<TResult> extends ReportBuilder<TResult> {
    private String deviceId;
    private Date endDate;
    private Date startDate;
    private String transactionId;

    public String getDeviceId() {
        return deviceId;
    }
    public Date getEndDate() {
        return endDate;
    }
    public Date getStartDate() {
        return startDate;
    }
    public String getTransactionId() {
        return transactionId;
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

    public TransactionReportBuilder(ReportType type, Class<TResult> clazz) {
        super(type, clazz);
    }

    public void setupValidations() {
        this.validations.of(ReportType.TransactionDetail)
                .check("transactionId").isNotNull()
                .check("deviceId").isNull()
                .check("startDate").isNull()
                .check("endDate").isNull();

        this.validations.of(ReportType.Activity).check("transactionId").isNull();
    }
}
