package com.global.api.entities.reporting;

import com.global.api.entities.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class MerchantSummary {
    // A unique identifier for the object created by Global Payments.
    // The first 3 characters identifies the resource an id relates to.
    private String id;
    // The label to identify the merchant
    private String name;
    private UserStatus status;
    private List<UserLinks> links;
}