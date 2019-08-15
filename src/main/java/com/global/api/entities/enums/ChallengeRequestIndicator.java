package com.global.api.entities.enums;

public enum ChallengeRequestIndicator implements IStringConstant {
    NoPreference("NO_PREFERENCE"),
    NoChallengeRequested("NO_CHALLENGE_REQUESTED"),
    ChallengePreferred("CHALLENGE_PREFERRED"),
    ChallengeMandated("CHALLENGE_MANDATED");

    String value;
    ChallengeRequestIndicator(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
