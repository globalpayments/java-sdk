package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
public class ActionSummary {
    private String id;
    private String type;
    private DateTime timeCreated;
    private String resource;
    private String version;
    private String resourceId;
    private String resourceStatus;
    private String httpResponseCode;
    private String responseCode;
    private String appId;
    private String appName;
    private String accountId;
    private String accountName;
    private String merchantName;
}
