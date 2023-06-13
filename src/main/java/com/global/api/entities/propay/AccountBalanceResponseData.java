package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountBalanceResponseData {

    /** ability to transfer funds in specified manner is currently allowed or not */
    private String enabled ;

    /** remaining limit for funds transfer */
    private String limitRemaining ;

    /** Cost to transfer money using the specified method */
    private String transferFee ;

    /** Describes whether trasnferFee is a flat amount or a percentage.
     * valid value $ or %.
     */
    private String feeType ;

    /** Obfuscated account details for recipient */
    private String accountLastFour ;
}
