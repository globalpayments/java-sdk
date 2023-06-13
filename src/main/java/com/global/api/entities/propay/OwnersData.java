package com.global.api.entities.propay;

import com.global.api.entities.Address;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnersData {
    
    /** Owner title */
    private String title;
    
    /** Owner first name */
    private String firstName;
    
    /** Owner last name */
    private String lastName;
    
    /** Owner email ID */
    private String email;
    
    /** Date of birth of the owner. Must be in 'mm-dd-yyyy' format. */
    private String dateOfBirth;
    
    /** Social Security Number of the owner. Should be 9 digits. */
    private String ssn;
    
    /** Percentage stake in company by owner. Must be whole number between 0 and 100. */
    private String percentage;

    /** Address of the owner */
    private Address ownerAddress;

    public OwnersData() {
        ownerAddress = new Address();
    }
}
