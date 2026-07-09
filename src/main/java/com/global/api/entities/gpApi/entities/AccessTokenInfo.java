package com.global.api.entities.gpApi.entities;

import com.global.api.entities.enums.IntervalToExpire;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Getter
@Setter
public class AccessTokenInfo {
    private String accessToken;
    private String type;
    private String merchantId;
    private String merchantName;
    private String appId;
    private String appName;
    private Date timeCreated;
    private int secondsToExpire;
    private IntervalToExpire intervalToExpire;
    private String email;
    private GpApiAccount[] accounts;
    private String dataAccountName;
    private String disputeManagementAccountName;
    private String tokenizationAccountName;
    private String transactionProcessingAccountName;
    private String riskAssessmentAccountName;
    private String merchantManagementAccountName;
    private String fileProcessingAccountName;
    private String dataAccountID;
    private String disputeManagementAccountID;
    private String tokenizationAccountID;
    private String transactionProcessingAccountID;
    private String riskAssessmentAccountID;
    private String merchantManagementAccountID;
    private String fileProcessingAccountID;
}
