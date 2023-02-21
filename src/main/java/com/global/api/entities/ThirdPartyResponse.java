package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class ThirdPartyResponse {

    // Another platform's name
    private String Platform;

    // Data json string that represents the raw data received from another platform
    private String Data;

}