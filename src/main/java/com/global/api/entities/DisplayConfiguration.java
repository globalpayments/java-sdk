package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Display configuration for iframe settings.
 * Contains domain configuration for iframe dimensions and response handling,
 * as well as display options for cardholder information and CVV.
 */
@Getter
@Setter
@Accessors(chain = true)
public class DisplayConfiguration {
    /**
     * The domain used for iframe dimensions configuration.
     */
    private String iframeDimensionsDomain;
    
    /**
     * The domain used for iframe response handling.
     */
    private String iframeResponseDomain;
    
    /**
     * Whether to display the cardholder name field (YES/NO).
     */
    private String cardholderName;
    
    /**
     * Whether to display the CVV field (YES/NO).
     */
    private String cvv;
}
