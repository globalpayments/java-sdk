package com.global.api.terminals.upa.subgroups;

import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;

public class RequestParamFields implements IRequestSubGroup {
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
    }

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getClerkId() != null) {
            this.clerkId = builder.getClerkId().toString();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

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
