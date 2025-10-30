package com.global.api.entities;

import com.global.api.entities.enums.ChallengeRequestIndicator;
import com.global.api.entities.enums.StorageMode;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class PaymentMethodConfiguration {

    private Boolean isAddressOverrideAllowed;

    private Boolean isShippableAddressEnabled;

    private String exemptStatus;

    private StorageMode storageMode;

    private Boolean isBillingAddressRequired;

    private ChallengeRequestIndicator challengeRequestIndicator;
}
