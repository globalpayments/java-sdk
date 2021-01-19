package com.global.api.entities;

import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.StoredCredentialReason;
import com.global.api.entities.enums.StoredCredentialSequence;
import com.global.api.entities.enums.StoredCredentialType;

public class StoredCredential {
    private StoredCredentialType type;
    private StoredCredentialInitiator initiator;
    private StoredCredentialSequence sequence;
    private StoredCredentialReason reason;
    private String schemeId;

    public StoredCredentialType getType() {
        return type;
    }
    public void setType(StoredCredentialType type) {
        this.type = type;
    }
    public StoredCredentialInitiator getInitiator() {
        return initiator;
    }
    public void setInitiator(StoredCredentialInitiator initiator) {
        this.initiator = initiator;
    }
    public StoredCredentialSequence getSequence() {
        return sequence;
    }
    public void setSequence(StoredCredentialSequence sequence) {
        this.sequence = sequence;
    }
    public StoredCredentialReason getReason() {
        return reason;
    }
    public void setReason(StoredCredentialReason reason) {
        this.reason = reason;
    }
    public String getSchemeId() {
        return schemeId;
    }
    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }
}
