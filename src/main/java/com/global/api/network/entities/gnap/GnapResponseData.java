package com.global.api.network.entities.gnap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class GnapResponseData extends GnapData {

    /** FID L -Balance Info */
    private BalanceInfo balanceInfo;
    /** FID M - PIN Encryption Key */
    private String pinEncryptionKey;
    /** FID g - Response Message */
    private String responseMessage;
}
