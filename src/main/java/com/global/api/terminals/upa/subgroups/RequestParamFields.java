package com.global.api.terminals.upa.subgroups;

import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;

public class RequestParamFields implements IRequestSubGroup {
    StoredCredentialInitiator cardBrandStorage;
    String cardBrandTransactionId;
    String clerkId;
    Integer tokenRequest;
    String tokenValue;

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getClerkId() != null) {
            this.clerkId = builder.getClerkId().toString();
        }

        if (builder.isRequestMultiUseToken()) {
            this.tokenRequest = 1;
        }

        if (builder.getTokenValue() != null) {
            this.tokenValue = builder.getTokenValue();
        }

        if (builder.getCardBrandStorage() != null) {
            this.cardBrandStorage = builder.getCardBrandStorage();
        }

        if (builder.getCardBrandTransactionId() != null) {
            this.cardBrandTransactionId = builder.getCardBrandTransactionId();
        }
    }

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getClerkId() != null) {
            this.clerkId = builder.getClerkId().toString();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

        if (cardBrandStorage != null) {
            // only two values supported per v1.30 integrator's guide
            if (cardBrandStorage == StoredCredentialInitiator.Merchant) {
                params.set("cardOnFileIndicator", "M");
            }
            if (cardBrandStorage == StoredCredentialInitiator.CardHolder) {
                params.set("cardOnFileIndicator", "C");
            }
            hasContents = true;
        }

        if (cardBrandTransactionId != null) {
            params.set("cardBrandTransId", cardBrandTransactionId);
            hasContents = true;
        }

        if (clerkId != null) {
            params.set("clerkId", clerkId);
            hasContents = true;
        }

        if (tokenRequest != null) {
            params.set("tokenRequest", tokenRequest);
            hasContents = true;
        }

        if (tokenValue != null) {
            params.set("tokenValue", tokenValue);
            hasContents = true;
        }
        
        return hasContents ? params : null;
    }

    @Override
    public String getElementString() {
        return null;
    }
}
