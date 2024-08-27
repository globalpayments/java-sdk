package com.global.api.terminals;

import com.global.api.utils.Element;
import com.global.api.utils.IRawRequestBuilder;
import com.global.api.utils.JsonDoc;

public class DeviceMessageWithDocument<T extends IRawRequestBuilder> extends DeviceMessage {

    private T document;

    public DeviceMessageWithDocument(T doc, byte[] buffer) {
        super(buffer);
        document = doc;
    }

    public IRawRequestBuilder getRequestBuilder() {
        return document;
    }

}
