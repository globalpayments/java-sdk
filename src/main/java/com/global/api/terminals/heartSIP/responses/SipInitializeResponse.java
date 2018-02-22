package com.global.api.terminals.heartSIP.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.utils.Element;
import com.global.api.utils.StringUtils;

import java.util.Dictionary;
import java.util.Hashtable;

public class SipInitializeResponse extends SipBaseResponse implements IInitializeResponse {
    private Dictionary<String, Dictionary<String, String>> _params;
    private String serialNumber;
    private String lastCategory;

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public SipInitializeResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);
        if (_params == null)
            _params = new Hashtable<String, Dictionary<String, String>>();

        // set category
        String category = response.getString("TableCategory");
        if(StringUtils.isNullOrEmpty(category))
            category = lastCategory;
        else lastCategory = category;

        if(category != null) {
            if (_params.get(category) == null)
                _params.put(category, new Hashtable<String, String>());

            for (Element field : response.getAll("Field")) {
                String key = field.getString("Key");
                String value = field.getString("Value");
                _params.get(category).put(key, value);
            }
        }
    }

    protected void finalizeResponse() {
        super.finalizeResponse();

        Dictionary<String, String> terminalInformation = this._params.get("TERMINAL INFORMATION");
        if(terminalInformation != null)
            serialNumber = terminalInformation.get("SERIAL NUMBER");
    }
}
