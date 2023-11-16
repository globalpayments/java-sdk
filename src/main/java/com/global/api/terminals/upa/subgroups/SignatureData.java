package com.global.api.terminals.upa.subgroups;

import lombok.Getter;
import lombok.Setter;

public class SignatureData {
    @Getter @Setter
    private String prompt1;
    @Getter @Setter
    private String prompt2;
    @Getter @Setter
    private Integer displayOption;
}
