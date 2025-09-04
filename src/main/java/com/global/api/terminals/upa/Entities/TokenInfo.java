package com.global.api.terminals.upa.Entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenInfo {

    // Gets or sets the token value.
    public String token;

    // Gets or sets the expiry month of the token (MM format).
    public String expiryMonth;

    // Gets or sets the expiry year of the token (YYYY format).
    public String expiryYear;
}
