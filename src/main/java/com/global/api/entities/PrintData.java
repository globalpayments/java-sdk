package com.global.api.entities;

import com.global.api.entities.enums.DisplayOption;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrintData {
    private String filePath;
    private String line1;
    private String line2;
    private DisplayOption displayOption;
    /**
     * The bitmap must be converted in a base64 encoded data
     */
    private String content;
}
