package com.global.api.entities.enums;

public enum ChallengeRequestIndicator implements IStringConstant {
    NoPreference("NO_PREFERENCE"),
    NoChallengeRequested("NO_CHALLENGE_REQUESTED"),
    ChallengePreferred("CHALLENGE_PREFERRED"),
    ChallengeMandated("CHALLENGE_MANDATED"),
    NoChallengeRequestedTransactionRiskAnalysisPerformed("NO_CHALLENGE_REQUESTED_TRANSACTION_RISK_ANALYSIS_PERFORMED"),
    NoChallengeRequestedDataShareOnly("NO_CHALLENGE_REQUESTED_DATA_SHARE_ONLY"),
    NoChallengeRequestedScaAlreadyPerformed("NO_CHALLENGE_REQUESTED_SCA_ALREADY_PERFORMED"),
    NoChallengeRequestedWhiteList("NO_CHALLENGE_REQUESTED_WHITELIST"),
    ChallengeRequestedPromptForWhiteList("CHALLENGE_REQUESTED_PROMPT_FOR_WHITELIST");

    String value;
    ChallengeRequestIndicator(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
