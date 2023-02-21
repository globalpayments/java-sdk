package com.global.api.entities;

import com.global.api.entities.enums.RiskAssessmentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class RiskAssessment {

    // A unique identifier for the risk assessment
    private String Id;

    // Time indicating when the object was created
    private DateTime TimeCreated;

    // Indicates where the risk assessment is in its lifecycle
    private RiskAssessmentStatus Status;

    // The amount associated with the risk assessment
    private BigDecimal Amount;

    // The currency of the amount in ISO-4217(alpha-3)
    private String Currency;

    // A unique identifier for the merchant set by Global Payments
    private String MerchantId;

    // A meaningful label for the merchant set by Global Payments
    private String MerchantName;

    // A unique identifier for the merchant account set by Global Payments
    private String AccountId;

    // A meaningful label for the merchant account set by Global Payments
    private String AccountName;

    // Merchant defined field to reference the risk assessment resource
    private String Reference;

    // The result from the risk assessment service
    private String ResponseCode;

    // The result message from the risk assessment service that describes the result given
    private String ResponseMessage;

    private Card CardDetails;

    private ThirdPartyResponse ThirdPartyResponse;

    // A unique identifier for the object created by Global Payments
    private String ActionId;

}