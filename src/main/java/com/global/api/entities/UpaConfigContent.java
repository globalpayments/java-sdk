package com.global.api.entities;

import com.global.api.entities.enums.Reinitialize;
import com.global.api.entities.enums.TerminalConfigType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpaConfigContent {
    private TerminalConfigType configType;
    private String fileContent;
    private int length;
    private Reinitialize reinitialize;
}
