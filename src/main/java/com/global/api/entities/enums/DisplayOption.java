package com.global.api.entities.enums;

import lombok.Getter;

@Getter
public enum DisplayOption {
    NO_SCREEN_CHANGE(0),
    RETURN_TO_IDLE_SCREEN(1);

    private final int value;

    DisplayOption(int value) {
        this.value = value;
    }

}