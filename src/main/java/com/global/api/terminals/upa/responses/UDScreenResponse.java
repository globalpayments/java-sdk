package com.global.api.terminals.upa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IDeviceScreen;
import com.global.api.utils.JsonDoc;

public class UDScreenResponse extends UpaResponseHandler implements IDeviceScreen {

    public UDScreenResponse(JsonDoc root) throws ApiException {
        super.parseResponse(root);

    }
}
