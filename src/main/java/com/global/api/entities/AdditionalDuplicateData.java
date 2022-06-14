package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdditionalDuplicateData {

    private String originalGatewayTxnId;
    private String originalRspDT;
    private String originalClientTxnId;
    private String originalAuthCode;
    private String originalRefNbr;
    private BigDecimal originalAuthAmt;
    private String originalCardType;
    private String OriginalCardNbrLast4;
}
