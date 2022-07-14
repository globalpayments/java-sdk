package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class LodgingItems {
    private String types;
    private String reference;
    private String totalAmount;
    private String[] paymentMethodProgramCodes;
}