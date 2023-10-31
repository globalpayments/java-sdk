package com.global.api.entities;

import com.global.api.entities.enums.DocumentCategory;
import com.global.api.entities.enums.FileType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter

public class Document {

    private String Id;

    private String Name;

    private String Status;

    private String TimeCreated;

    private FileType Format;

    private DocumentCategory Category;
    
}
