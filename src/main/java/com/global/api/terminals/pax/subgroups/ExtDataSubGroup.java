package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxExtData;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class ExtDataSubGroup implements IRequestSubGroup, IResponseSubGroup {
    private Dictionary<String, String> collection = new Hashtable<String, String>();

    public String get(PaxExtData key){
        String rvalue = collection.get(key.getValue());
        if(rvalue == null)
            return "";
        return rvalue;
    }

    public void set(PaxExtData key, String value) {
        collection.put(key.getValue(), value);
    }

    public ExtDataSubGroup() { }
    public ExtDataSubGroup(MessageReader br) {
        String values = br.readToCode(ControlCodes.ETX);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] elements = values.split("\\[US\\]");
        for(String element: elements) {
            String[] kv = element.split("=");

            try {
                collection.put(kv[0].toUpperCase(), kv[1]);
            }
            catch (IndexOutOfBoundsException e) {
                // nom nom
            }
        }
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();

        Enumeration<String> keys = collection.keys();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            sb.append(String.format("%s=%s%s", key, collection.get(key), (char) ControlCodes.US.getByte()));
        }

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}