package com.global.api.entities;

import com.global.api.entities.enums.FraudFilterMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Accessors(chain = true)
@Getter
@Setter
public class FraudResponse {

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Rule {
        String name;
        String id;
        String action;
    }

    private FraudFilterMode mode;
    private String result;
    private ArrayList<Rule> rules;

    public FraudResponse()
    {
        rules = new ArrayList<>();
    }

}