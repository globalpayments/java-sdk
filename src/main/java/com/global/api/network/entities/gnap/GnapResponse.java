package com.global.api.network.entities.gnap;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GnapResponse {
    private GnapMessageHeader gnapMessageHeader;
    private GnapResponseData gnapResponseData;
}
