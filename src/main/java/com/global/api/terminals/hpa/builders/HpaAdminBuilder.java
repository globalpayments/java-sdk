package com.global.api.terminals.hpa.builders;

import com.global.api.entities.exceptions.BuilderException;
import com.global.api.utils.StringUtils;

public class HpaAdminBuilder {
    private StringBuilder messageBuilder;
    private String[] messageIds;

    private boolean keepAlive;
    private boolean awaitResponse;

    public String[] getMessageIds() {
        return messageIds;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isAwaitResponse() {
        return awaitResponse;
    }

    public void setAwaitResponse(boolean awaitResponse) {
        this.awaitResponse = awaitResponse;
    }

    public HpaAdminBuilder(String... messageIds) throws BuilderException {
        if(messageIds.length <= 0) {
            throw new BuilderException("You must provide at least one message id.");
        }

        messageBuilder = new StringBuilder();
        messageBuilder.append(String.format("<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>%s</Request>", messageIds[0]));
        this.messageIds = messageIds;
        this.keepAlive = false;
        this.awaitResponse = true;
    }

    public HpaAdminBuilder set(String tagName, Integer value) {
        if(value != null) {
            messageBuilder.append(String.format("<%s>%s</%s>", tagName, value, tagName));
        }
        return this;
    }
    public HpaAdminBuilder set(String tagName, String value) {
        if(!StringUtils.isNullOrEmpty(value)) {
            messageBuilder.append(String.format("<%s>%s</%s>", tagName, value, tagName));
        }
        return this;
    }

    public String buildMessage() {
        messageBuilder.append("</SIP>");
        return messageBuilder.toString();
    }
}
