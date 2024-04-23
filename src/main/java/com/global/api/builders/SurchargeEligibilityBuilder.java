package com.global.api.builders;


import com.global.api.ServicesContainer;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.SurchargeLookup;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.IReportingService;
import com.global.api.paymentMethods.IPaymentMethod;

import java.util.EnumSet;

public class SurchargeEligibilityBuilder<TResult> extends BaseBuilder<TResult> {

    private IPaymentMethod paymentMethod;
    private TransactionType transType;

    public TransactionType getTransType(){
        return TransactionType.SurchargeEligibilityLookup;
    }

    public IPaymentMethod getPaymentMethod(){
        return paymentMethod;
    }
    public SurchargeEligibilityBuilder(TransactionType type){
        transType = type;
        return;
    }
    public SurchargeEligibilityBuilder<TResult> withPaymentMethod(IPaymentMethod value){
        this.paymentMethod = value;
        return this;
    }

    @Override
    public TResult execute(String configName) throws ApiException {
       IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);
       return ((IReportingService)client).surchargeEligibilityLookup(this, SurchargeLookup.class);
    }

    @Override
    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.SurchargeEligibilityLookup))
                .when("setNumber").isNotNull()
                .check("setExpMonth").isNotNull()
                .check("setExpYear").isNotNull();
        this.validations.of(EnumSet.of(TransactionType.SurchargeEligibilityLookup))
                .when("setVersion").isNotNull()
                .check("setTrackNumber").isNotNull()
                .check("setValue").isNotNull()
                .check("setKsn").isNotNull();
    }
}
