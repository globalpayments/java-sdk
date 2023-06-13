package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficialOwnerDataResult {

    /** Beneficial Owner's first name */
    private String firstName;

    /** Beneficial Owner's last name */
    private String lastName;

    /** Beneficial Owner's status . validated or not */
    private String status;
}
