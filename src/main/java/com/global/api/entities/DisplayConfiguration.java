package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Display configuration for iframe settings.
 * Contains domain configuration for iframe dimensions and response handling.
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
}
