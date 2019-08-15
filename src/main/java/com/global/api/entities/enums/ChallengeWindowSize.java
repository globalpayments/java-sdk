package com.global.api.entities.enums;

public enum ChallengeWindowSize implements IStringConstant {
    Windowed_250x400("WINDOWED_250X400"),
    Windowed_390x400("WINDOWED_390X400"),
    Windowed_500x600("WINDOWED_500X600"),
    Windowed_600x400("WINDOWED_600X400"),
    FullScreen("FULL_SCREEN");

    String value;
    ChallengeWindowSize(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
