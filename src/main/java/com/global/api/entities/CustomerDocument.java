package com.global.api.entities;

import com.global.api.entities.enums.CustomerDocumentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class CustomerDocument {
    private String reference;
    private String issuer;
    private CustomerDocumentType type;
}