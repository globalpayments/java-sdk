package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Represents the Terms and Conditions entity.
 */
public class TermsAndConditions {
    /**
     * The URL of the terms and conditions.
     */
    private String url;

    /**
     * The version of the terms and conditions.
     */
    private String version;

    /**
     * The description of the terms and conditions.
     */
    private String description;

    /**
     * The language of the terms and conditions.
     */
    private String language;
}
