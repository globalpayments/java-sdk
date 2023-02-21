package com.global.api.gateways;

import com.global.api.builders.FraudBuilder;
import com.global.api.entities.RiskAssessment;
import com.global.api.entities.exceptions.ApiException;

public interface IFraudCheckService {
    <T> RiskAssessment processFraud(FraudBuilder<T> builder) throws ApiException;
}