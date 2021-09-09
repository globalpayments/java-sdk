package com.global.api.entities;

import com.global.api.entities.enums.FraudFilterMode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class FraudRuleCollection {
    @Getter @Setter public List<FraudRule> rules;

    public FraudRuleCollection()
    {
        this.rules = new ArrayList<>();
    }

    public void addRule(String key, FraudFilterMode mode) {
        if (hasRule(key)) {
            return;
        }
        rules.add(new FraudRule(key, mode));
    }

    private boolean hasRule(String key) {
        for(FraudRule rule : rules) {
            if (rule.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
