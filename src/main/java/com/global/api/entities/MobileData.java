package com.global.api.entities;

import com.global.api.entities.enums.SdkInterface;
import com.global.api.entities.enums.SdkUiType;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class MobileData {
    private String encodedData;
    private String applicationReference;
    private SdkInterface sdkInterface;
    private SdkUiType[] sdkUiTypes;
    private JsonDoc ephemeralPublicKey;
    private Integer maximumTimeout;
    private String referenceNumber;
    private String sdkTransReference;

    public MobileData setSdkUiTypes(SdkUiType... sdkUiTypes) {
        this.sdkUiTypes = sdkUiTypes;
        return this;
    }
}
