package com.global.api.entities;

import com.global.api.entities.enums.FraudFilterMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FraudRule {
    private String key;
    private FraudFilterMode mode;
    private String description;
    private String result;

    FraudRule(String key, FraudFilterMode mode) {
        this.key = key;
        this.mode = mode;
    }
}
