package com.global.api.entities.propay;

import com.global.api.entities.Address;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BankAccountOwnershipData {

    /** Bank account owner’s first name  */
    private String firstName;

    /** Bank account owner’s last name */
    private String lastName;

    /** Bank account owner’s  address */
    private Address ownerAddress;

    /** Bank account owner’s phone number */
    private String phoneNumber;

    public BankAccountOwnershipData() {
        ownerAddress = new Address();
    }
}
