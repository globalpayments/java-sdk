package com.global.api.gateways;

import com.global.api.builders.ReportBuilder;
import com.global.api.builders.SurchargeEligibilityBuilder;
import com.global.api.entities.exceptions.ApiException;

public interface IReportingService {
    <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException;
    <T> T surchargeEligibilityLookup(SurchargeEligibilityBuilder builder, Class clazz) throws ApiException;
}