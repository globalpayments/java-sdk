package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;

/**
 * PorticoTokenConfig holds configuration for Portico token authentication.
 */
@Getter @Setter
public class PorticoTokenConfig {
    /**
     * Site ID.
     */
    private int siteId;

    /**
     * Account's license ID.
     */
    private int licenseId;

    /**
     * Account's device ID.
     */
    private int deviceId;

    /**
     * Account's username.
     */
    private String username;

    /**
     * Account's password.
     */
    private String password;

    /**
     * Gets or sets the secret API key used for authenticating requests.
     * <p>
     * This property should be handled with care to avoid exposing sensitive information.
     * Ensure the API key is stored securely and only accessed by authorized components.
     */
    private String secretApiKey;
}