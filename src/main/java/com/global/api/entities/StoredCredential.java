package com.global.api.entities;

import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.StoredCredentialReason;
import com.global.api.entities.enums.StoredCredentialSequence;
import com.global.api.entities.enums.StoredCredentialType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class StoredCredential {
    private StoredCredentialType type;
    private StoredCredentialInitiator initiator;
    private StoredCredentialSequence sequence;
    private StoredCredentialReason reason;
    private String schemeId;
}