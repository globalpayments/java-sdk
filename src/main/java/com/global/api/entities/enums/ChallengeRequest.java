package com.global.api.entities.enums;

public enum ChallengeRequest implements IStringConstant {
    NoPreference("NO_PREFERENCE"),
    NoChallengeRequested("NO_CHALLENGE_REQUESTED"),
    ChallengePreferred("CHALLENGE_PREFERRED"),
    ChallengeMandated("CHALLENGE_MANDATED");

    String value;
    ChallengeRequest(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
