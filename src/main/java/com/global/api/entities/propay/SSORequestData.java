package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SSORequestData {

    /** The ProPay system requires that your single-sign-on originate from the URL originally provided here */
    private String referrerURL;

    /** The ProPay system requires that your single-sign-on originate from the URL originally provided here. Can supply a range of class c or more restrictive */
    private String ipAddress;

    /** The ProPay system requires that your single-sign-on originate from the URL originally provided here. Can supply a range of class c or more restrictive */
    private String ipSubnetMask;
}
