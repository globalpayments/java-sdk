package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileProcessor {
    private String ResourceId;
    private String UploadUrl;
    private String ExpirationDate;
    private String Status;
    private String CreatedDate;
    private String TotalRecordCount;
    private String ResponseCode;
    private String ResponseMessage;
    private List<FileUploaded> filesUploaded;

}
