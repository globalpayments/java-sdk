package com.global.api.entities.propay;

import com.global.api.entities.Address;
import com.global.api.entities.enums.TermsVersion;
import lombok.*;

@Data
@AllArgsConstructor
public class UserPersonalData {

    /** Merchant/Individual first name */
    private String firstName;

    /** Merchant/Individual middle initial */
    private String middleInitial;

    /** Merchant/Individual lane name */
    private String lastName;

    /** Merchant/Individual date of birth. Must be in 'mm-dd-yyyy' format. Individual must be 18+ to obtain an account. The value 01-01-1981 will give a successul response. All others will return a Status 66 (Failed KYC). */
    private String dateOfBirth;

    /** Merchant/Individual social security number. Must be 9 characters without dashes. Required for USA when using personal validation. If business validated, do not pass! */
    private String ssn;
    
    /** Merchant/Individual email address. Must be unique in ProPay system. ProPay's system will send automated emails to the email address on file unless NotificationEmail is provided. This value is truncated beyond 55 characters. */
    private String sourceEmail;
    
    /** Merchant/Individual day phone number. For USA, CAN, NZL, and AUS value must be 10 characters */
    private String dayPhone;
    
    /** Merchant/Individual evening phone number. For USA, CAN, NZL, and AUS value must be 10 characters */
    private String eveningPhone;
    
    /** Communication email address. ProPay's system will send automated emails to the email address on file rather than the source email */
    private String notificationEmail;
    
    /** Required to specify the currency in which funds should be held, if other than USD. An affiliation must be granted permission to create accounts in currencies other than USD. ISO 4217 standard 3 character currency code. */
    private String currencyCode;
    
    /** One of the previously assigned merchant tiers. If not provided, will default to cheapest available tier. */
    private String tier;
    
    /** This is a partner's own unique identifier. Typically used as the distributor or consultant ID */
    private String externalID;
   
    /** Numeric value which will give a user access to ProPay's IVR system. Can also be used to reset password */
    private String phonePIN;
    
    /** ProPay account username. Must be unique in ProPay system. Username defaults to <sourceEmail> if userId is not provided */
    private String userID;

    /** Signup IP Address */
    private String ipSignup;

    /** When marked true, the sub-merchant is attesting that they are a US citizen.
      * Value passed should be either true, false, or null
     */
    private boolean isUsCitizen;

    private boolean isBOAttestation;

    /** IP address of the device that was used to agree to ProPay's Terms and Conditions */
    private String termsAcceptanceIP;

    /** Refers to the version of our terms and conditions that was provided to the submerchant for
     review and to which they are agreeing.
     Valid numeric values are 1 - 5
     1 - merchant US
     2 - payment US
     3 - merchant CA
     4 - merchant UK
     5 - merchant AU
     */
    private TermsVersion termsVersion;

    /** Represents the country in which the merchant was born
     * ISO 3166 3 Digit alpha code applies. For Example: GBR, USA, etc.
     */
    private String nationality;

    /** Merchant/Individual address */
    private Address personalAddress;

    /** Merchant/Individual mailing Address */
    private Address mailingAddress;

    public UserPersonalData() {
        personalAddress = new Address();
        mailingAddress=new Address();
    }
}
