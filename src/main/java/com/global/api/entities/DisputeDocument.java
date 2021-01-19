package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputeDocument {
    private String type;
    private String base64Content;
}