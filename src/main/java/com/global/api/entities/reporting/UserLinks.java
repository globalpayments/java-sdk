package com.global.api.entities.reporting;

import com.global.api.entities.enums.UserLevelRelationship;
import com.global.api.entities.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class UserLinks {
    // Describes the relationship the associated link href value has to the current resource
    private UserLevelRelationship rel;
    // A href link to the resources or resource actions as indicated in the corresponding rel value
    private String href;
}