package com.global.api.entities.enums;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UDData {
    private UDFileType fileType;
    private int slot;
    private String fileName;
    private DisplayOption displayOption;
    private String filePath;
}
