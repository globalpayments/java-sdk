package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class AccessTokenInfo {
    private String accessToken;
    private String dataAccountName;
    private String disputeManagementAccountName;
    private String tokenizationAccountName;
    private String transactionProcessingAccountName;
    private String riskAssessmentAccountName;
}
