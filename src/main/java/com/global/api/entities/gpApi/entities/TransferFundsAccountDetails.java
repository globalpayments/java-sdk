package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class TransferFundsAccountDetails {
    private String id;
    private String status;
    private String timeCreated;
    private String amount;
    private String reference;
    private String description;
}