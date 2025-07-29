package com.global.api;

import com.global.api.entities.BankList;
import lombok.Setter;

@Setter
public class BankResponse {
    private String name;
    private String identifierCode;
    private String iban;
    private String code;
    private String accountNumber;
}
