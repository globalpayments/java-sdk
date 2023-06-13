package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreatRiskData {

    /** SourceIp of Merchant */
    private String merchantSourceIP;

    /** Threat Metrix Policy */
    private String threatMetrixPolicy;

    /** SessionId for Threat Metrix */
    private String threatMetrixSessionID;
}
