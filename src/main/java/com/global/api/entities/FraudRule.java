package com.global.api.entities;

import com.global.api.entities.enums.FraudFilterMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class FraudRule {
    @Getter @Setter private String key;
    @Getter @Setter private FraudFilterMode mode;
}
