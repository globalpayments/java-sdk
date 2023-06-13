package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignificantOwnerData {

    /** Seller's authorized Signer First Name. By default Merchant's First name is saved. */
    private String authorizedSignerFirstName;

    /** Seller's Authorized Signer Last Name. By default Merchant's Last name is saved. */
    private String authorizedSignerLastName;

    /** This field contains the Seller's Authorized Signer Title */
    private String authorizedSignerTitle;

    /** Seller's Authorized Signer owner information */
    private OwnersData significantOwner;

    public SignificantOwnerData() {
        significantOwner = new OwnersData();
    }
}
