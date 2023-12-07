package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FileUploaded {
        public    String fileId;
        public    String fileName;
        public    String timeCreated;
        public    String url;
        public    String expirationDate;

}
