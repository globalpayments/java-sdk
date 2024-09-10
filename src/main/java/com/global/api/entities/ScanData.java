package com.global.api.entities;

import com.global.api.entities.enums.DisplayOption;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScanData {
    private String header;
    private String prompt1;
    private String prompt2;
    private DisplayOption displayOption;
    private Integer timeout;
}
