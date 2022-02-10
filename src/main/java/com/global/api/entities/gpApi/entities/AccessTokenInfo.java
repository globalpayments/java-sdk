package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class AccessTokenInfo {
    private String token;
    private String dataAccountName;
    private String disputeManagementAccountName;
    private String tokenizationAccountName;
    private String transactionProcessingAccountName;
}
