package com.global.api.terminals.hpa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.Element;

public class SipSendFileResponse extends SipBaseResponse {
    private Integer maxDataSize;

    public Integer getMaxDataSize() {
        return maxDataSize;
    }

    public SipSendFileResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }
    
    protected void mapResponse(Element response) {
        super.mapResponse(response);
        
        maxDataSize = response.getInt("MaxDataSize");
    }

}
