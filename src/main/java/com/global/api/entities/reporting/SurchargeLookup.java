package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurchargeLookup {
    private String gatewayTxnId;
    private String gatewayRspCode;
    private String gatewayRspMsg;
    private String isSurchargeable;
}
