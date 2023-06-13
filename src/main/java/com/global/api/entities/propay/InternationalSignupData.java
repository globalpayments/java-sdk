package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternationalSignupData {

    /** Document Type
     * valid value can be
       1-Driver's license
       2-Passport
       3-Australia Medicare
     * */
    private String documentType;

    /** Document number provided by DocumentType.*/
    private String intlID;

    /** Expiry date of the document provided by DocumentType */
    private String documentExpDate;

    /** The driver’s license issuing state. If document type is 1(Driver license)*/
    private String documentIssuingState;

    /** Required if the DocumentType is 1 (Driver’s license) and Country is NZL.
      * This is driver’s license version number.
     */
    private String driversLicenseVersion;

    /** Required if the DocumentType is 3 (Australia Medicare) and Country is AUS */
    private String medicareReferenceNumber;

    /** Required if the DocumentType is 3 (Australia Medicare) and Country is AUS */
    private String medicareCardColor;

}
