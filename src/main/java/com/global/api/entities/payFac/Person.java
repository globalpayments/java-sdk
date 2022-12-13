package com.global.api.entities.payFac;

import com.global.api.entities.Address;
import com.global.api.entities.PhoneNumber;
import com.global.api.entities.enums.PersonFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class Person {
     // Describes the functions that a person can have in an organization
    private PersonFunctions functions;
     // Person's first name
    private String firstName;
     // Middle's first name
    private String middleName;
     // Person's last name
    private String lastName;
     // Person's email address
    private String email;
     // Person's date of birth
    private String dateOfBirth;
     // The national id number or reference for the person for their nationality. For example for Americans this would
     // be SSN, for Canadians it would be the SIN, for British it would be the NIN.
    private String nationalIdReference;
     // The job title the person has
    private String jobTitle;
     // The equity percentage the person owns of the business that is applying to Global Payments for payment processing services.
    private String equityPercentage;
     // Customer's address
    private Address address;
     // Person's home phone number
    private PhoneNumber homePhone;
     // Person's work phone number
    private PhoneNumber workPhone;
}