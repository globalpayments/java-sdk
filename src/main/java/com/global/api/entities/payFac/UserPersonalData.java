package com.global.api.entities.payFac;

import com.global.api.entities.Address;
import com.global.api.entities.enums.UserType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class UserPersonalData {
    // Merchant/Individual first name
    private String firstName;
    // Merchant/Individual middle initial
    private String middleInitial;
    // Merchant/Individual last name
    private String lastName;
    // Merchant/Individual first name
    private String userName;
    // Merchant/Individual date of birth. Must be in 'mm-dd-yyyy' format. Individual must be 18+ to obtain an account.
    // The value 01-01-1981 will give a successful response. All others will return a Status 66 (Failed KYC).
    private String dateOfBirth;
    // Merchant/Individual social security number.
    // Must be 9 characters without dashes. Required for USA when using personal validation.
    // If business validated, do not pass!
    private String SSN;
    // Merchant/Individual email address. Must be unique in ProPay system.
    // ProPay's system will send automated emails to the email address on file unless NotificationEmail is provided.
    // This value is truncated beyond 55 characters.
    private String sourceEmail;
    // Merchant/Individual day phone number. For USA, CAN, NZL, and AUS value must be 10 characters
    private String dayPhone;
    // Merchant/Individual evening phone number. For USA, CAN, NZL, and AUS value must be 10 characters
    private String eveningPhone;
    // Communication email address.
    // ProPay's system will send automated emails to the email address on file rather than the source email
    private String notificationEmail;
    // Required to specify the currency in which funds should be held, if other than USD.
    // An affiliation must be granted permission to create accounts in currencies other than USD.
    // ISO 4217 standard 3 character currency code.
    private String currencyCode;
    // One of the previously assigned merchant tiers. If not provided, will default to cheapest available tier.
    private String tier;
    // This is a partner's own unique identifier. Typically used as the distributor or consultant ID
    private String externalID;
    //  Numeric value which will give a user access to ProPay's IVR system. Can also be used to reset password
    private String phonePIN;
    // ProPay account username. Must be unique in ProPay system.
    // Username defaults to <sourceEmail> if userId is not provided
    private String userID;
    // Merchant/Individual address
    private Address userAddress;
    // Business physical address
    private Address mailingAddress;
    // The legal business name of the merchant being boarded.
    private String legalName;
    // The merchant's DBA (Doing Business As) name or the alternate name the merchant may be known as
    private String DBA;
    // A four-digit number used to classify the merchant into an industry or market segment.
    private int merchantCategoryCode;
    // The merchant's business website URL
    private String website;
    private UserType type;
    private String notificationStatusUrl;
    // The merchants tax identification number.
    // For example, in the US the (EIN) Employer Identification Number would be used
    private String taxIdReference;

    public UserPersonalData() {
        this.userAddress = new Address();
        this.mailingAddress = new Address();
    }

}