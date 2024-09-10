package com.global.api.terminals.upa.subgroups;

import lombok.Getter;
import lombok.Setter;

public class RegisterPOS {
    @Getter
    @Setter
    private String appName;
    @Getter
    @Setter
    private Integer launchOrder;
    @Getter
    @Setter
    private Boolean remove = false;
    @Getter
    @Setter
    public Integer silent;
}