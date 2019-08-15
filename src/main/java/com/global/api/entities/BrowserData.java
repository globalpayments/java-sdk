package com.global.api.entities;

import com.global.api.entities.enums.ChallengeWindowSize;
import com.global.api.entities.enums.ColorDepth;

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

    public String getAcceptHeader() {
        return acceptHeader;
    }
    public void setAcceptHeader(String acceptHeader) {
        this.acceptHeader = acceptHeader;
    }
    public ColorDepth getColorDepth() {
        return colorDepth;
    }
    public void setColorDepth(ColorDepth colorDepth) {
        this.colorDepth = colorDepth;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public boolean isJavaEnabled() {
        return javaEnabled;
    }
    public void setJavaEnabled(boolean javaEnabled) {
        this.javaEnabled = javaEnabled;
    }
    public boolean isJavaScriptEnabled() {
        return javaScriptEnabled;
    }
    public void setJavaScriptEnabled(boolean javaScriptEnabled) {
        this.javaScriptEnabled = javaScriptEnabled;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public int getScreenHeight() {
        return screenHeight;
    }
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
    public int getScreenWidth() {
        return screenWidth;
    }
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
    public ChallengeWindowSize getChallengeWindowSize() {
        return challengeWindowSize;
    }
    public void setChallengeWindowSize(ChallengeWindowSize challengeWindowSize) {
        this.challengeWindowSize = challengeWindowSize;
    }
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
