package com.global.api.terminals.hpa.responses;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.Element;
import com.global.api.utils.VariableDictionary;

public abstract class SipKvpResponse extends SipBaseResponse  {
    protected String category;
    protected String lastCategory;
    protected VariableDictionary fieldValues;

    public SipKvpResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);

        // set category
        category = response.getString("TableCategory");
        if(category == null) {
            category = lastCategory;
        }

        fieldValues = new VariableDictionary();
        if(category != null) {
            for (Element field : response.getAll("Field")) {
                String key = field.getString("Key");
                String value = field.getString("Value");
                fieldValues.put(key, value);
            }
        }
    }

    private String formatCategory(String category) {
        String[] elements = category.split("\\s");

        StringBuilder sb = new StringBuilder(elements[0].toLowerCase());
        for(int i = 1; i < elements.length; i++) {
            String element = elements[i];
            sb.append(element.substring(0, 1).toUpperCase());
            sb.append(element.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
