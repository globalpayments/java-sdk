package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenInfo {
    private String token;
    private String dataAccountName;
    private String disputeManagementAccountName;
    private String tokenizationAccountName;
    private String transactionProcessingAccountName;
}
