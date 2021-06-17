package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class MessageExtension {

    private String criticalityIndicator;
    private String messageExtensionData;
    private String messageExtensionId;
    private String messageExtensionName;
}
