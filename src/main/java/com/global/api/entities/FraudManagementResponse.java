package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class FraudManagementResponse {
    // This element indicates the mode the Fraud Filter executed in
    private String fraudResponseMode ;
    // This field is used to determine what the overall result the Fraud Filter returned
    private String fraudResponseResult;
    // Filter rules
    private List<FraudRule> fraudResponseRules;
    private String fraudResponseMessage;
}