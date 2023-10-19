package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccount {
    private String id;
    private String name;
    private String type;

    public UserAccount(String id) {
        this.id = id;
    }

    public UserAccount(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
