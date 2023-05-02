package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.TimeZoneConversion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.SearchCriteriaBuilder;
import com.global.api.gateways.IReportingService;

public abstract class ReportBuilder<TResult> extends BaseBuilder<TResult> {
    private ReportType reportType;
    private TimeZoneConversion timeZoneConversion;
    private Class<TResult> clazz;

    private SearchCriteriaBuilder<TResult> _searchBuilder;

    public SearchCriteriaBuilder<TResult> getSearchBuilder() {
        if (_searchBuilder == null) {
            _searchBuilder = new SearchCriteriaBuilder<TResult>(this);
        }
        return _searchBuilder;
    }

    public ReportType getReportType() {
        return reportType;
    }
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    public TimeZoneConversion getTimeZoneConversion() {
        return timeZoneConversion;
    }
    public void setTimeZoneConversion(TimeZoneConversion timeZoneConversion) {
        this.timeZoneConversion = timeZoneConversion;
    }

    public ReportBuilder(ReportType type, Class<TResult> clazz) {
        super();
        this.reportType = type;
        this.clazz = clazz;
    }

    public TResult execute(String configName) throws ApiException {
        super.execute(configName);
        Object client;

        switch (reportType) {
            case FindBankPayment:
                client = ServicesContainer.getInstance().getOpenBankingClient(configName);
                break;

            default:
                client = ServicesContainer.getInstance().getGateway(configName);
        }

        return ((IReportingService) client).processReport(this, clazz);
    }

}