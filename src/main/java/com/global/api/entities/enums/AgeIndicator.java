package com.global.api.entities.enums;

public enum AgeIndicator implements IStringConstant {
    NoAccount("NO_ACCOUNT"),
    NoChange("NO_CHANGE"),
    ThisTransaction("THIS_TRANSACTION"),
    LessThanThirtyDays("LESS_THAN_THIRTY_DAYS"),
    ThirtyToSixtyDays("THIRTY_TO_SIXTY_DAYS"),
    MoreThanSixtyDays("MORE_THEN_SIXTY_DAYS");

    String value;
    AgeIndicator(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
