package com.global.api.entities.payFac;

import com.global.api.entities.enums.UserStatus;
import com.global.api.entities.enums.UserType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class UserReference {
    private String userId;
    private UserType userType;
    private UserStatus userStatus;
}