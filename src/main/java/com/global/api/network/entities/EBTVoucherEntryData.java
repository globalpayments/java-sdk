package com.global.api.network.entities;

import lombok.Getter;
import lombok.Setter;

public class EBTVoucherEntryData {

    @Getter
    @Setter
    private String originalTransactionDate;

    @Getter
    @Setter
    private String voucherNBR;

    @Getter
    @Setter
    private String telephoneAuthCode;
}
