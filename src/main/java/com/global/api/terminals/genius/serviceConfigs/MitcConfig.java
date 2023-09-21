package com.global.api.terminals.genius.serviceConfigs;

import com.global.api.entities.enums.Environment;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class MitcConfig {
    private String xWebId;
    private String terminalId;
    private String authKey;
    private String apiSecret;
    private String apiKey;
    private String targetDevice;


    /**
     * The default environment
     */
    public Environment environment = Environment.PRODUCTION;

    /**
     * Required
     * <p>
     * Name is give to integration by the integrators.
     * Will default to 'JAVA-SDK' if none is provided.
     */
    public String appName = "JAVA SDK";

    /**
     * Optional
     * <p>
     * Version number given to the integration by the integrators.
     */
    public String appVersion = "";

    /**
     * Required
     * <p>
     * Currently supported regions:
     * US - United States
     * CA - Canada
     * AU - Australia
     * NZ - New Zealand
     */
    public String region = "US";

    /**
     * Optional
     * <p>
     * 'true' will allow card number entry on device
     */
    public boolean allowKeyEntry = true;


    /**
     * Optional
     *
     * To enable the logging request and responses.
     *
     */
    private boolean enableLogging = false;

    public MitcConfig(
            String xWebId,
            String terminalId,
            String authKey,
            String apiSecret,
            String apiKey,
            String targetDevice
    ) {
        this.xWebId = xWebId;
        this.terminalId = terminalId;
        this.authKey = authKey;
        this.apiSecret = apiSecret;
        this.apiKey = apiKey;
        this.targetDevice = targetDevice;
    }
}
