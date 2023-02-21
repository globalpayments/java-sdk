package com.global.api.entities;

import com.global.api.entities.enums.ChallengeWindowSize;
import com.global.api.entities.enums.ColorDepth;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class BrowserData {
    private String acceptHeader;
    private ColorDepth colorDepth;
    private String ipAddress;
    private boolean javaEnabled;
    private boolean javaScriptEnabled;
    private String language;
    private int screenHeight;
    private int screenWidth;
    private ChallengeWindowSize challengeWindowSize;
    private String timezone;
    private String userAgent;
}
