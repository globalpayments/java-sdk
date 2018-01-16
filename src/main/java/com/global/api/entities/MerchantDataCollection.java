package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MerchantDataCollection {
    private ArrayList<MerchantKvp> collection;

    public String get(String key) {
        for(MerchantKvp kvp: collection) {
            if(kvp.getKey().equals(key) && kvp.isVisible())
                return kvp.getValue();
        }
        return null;
    }
    public List<String> getKeys() {
        List<String> keys = new ArrayList<String>();
        for(MerchantKvp kvp: collection) {
            if(kvp.isVisible())
                keys.add(kvp.getKey());
        }
        return keys;
    }

    public int count() {
        int count = 0;
        for(MerchantKvp kvp: collection) {
            if(kvp.isVisible())
                count++;
        }
        return count;
    }

    private int indexOf(String key) {
        for(int i = 0; i < collection.size(); i++) {
            if(collection.get(i).getKey().equals(key))
                return i;
        }
        return -1;
    }

    private ArrayList<MerchantKvp> getHiddenValues() {
        ArrayList<MerchantKvp> list = new ArrayList<MerchantKvp>();
        for(MerchantKvp kvp: collection) {
            if(!kvp.isVisible())
                list.add(kvp);
        }
        return list;
    }

    public MerchantDataCollection() {
        collection = new ArrayList<MerchantKvp>();
    }

    public void put(String key, String value, boolean visible) throws ApiException {
        if(hasKey(key)) {
            if(visible)
                throw new ApiException(String.format("Key %s already exists in the collection.", key));
            else collection.remove(indexOf(key));
        }

        MerchantKvp kvp = new MerchantKvp();
        kvp.setKey(key);
        kvp.setValue(value);
        kvp.setVisible(visible);

        collection.add(kvp);
    }
    public void put(String key, String value) throws ApiException {
        put(key, value, true);
    }

    BigDecimal getDecimal(String key) {
        for(MerchantKvp kvp: collection) {
            if(kvp.getKey().equals(key)) {
                return new BigDecimal(kvp.getValue());
            }
        }
        return null;
    }

    String getString(String key) {
        for(MerchantKvp kvp: collection) {
            if(kvp.getKey().equals(key)) {
                return kvp.getValue();
            }
        }
        return null;
    }

    void mergeHidden(MerchantDataCollection oldCollection) {
        for(MerchantKvp kvp: oldCollection.getHiddenValues()) {
            if(!this.hasKey(kvp.getKey()))
                this.collection.add(kvp);
        }
    }

    boolean hasKey(String key) {
        return getString(key) != null;
    }

    public static MerchantDataCollection parse(String kvpString) throws ApiException {
        return parse(kvpString, null);
    }
    public static MerchantDataCollection parse(String kvpString, MerchantDataEncoder encoder) throws ApiException {
        MerchantDataCollection collection = new MerchantDataCollection();

        // decrypt the string
        String decodedKvp = new String(Base64.decodeBase64(kvpString));
        if(encoder != null) {
            decodedKvp = encoder.decode(decodedKvp);
        }

        // build the object
        String[] merchantData = decodedKvp.split("\\|");
        for(String kvp: merchantData) {
            String[] data = kvp.split(":");
            collection.put(data[0], data[1], Boolean.parseBoolean(data[2]));
        }

        return collection;
    }

    public String toString() {
        return toString(null);
    }
    public String toString(MerchantDataEncoder encoder) {
        StringBuilder sb = new StringBuilder();

        for(MerchantKvp kvp: collection) {
            sb.append(String.format("%s:%s:%s|", kvp.getKey(), kvp.getValue(), kvp.isVisible()));
        }
        sb.deleteCharAt(sb.lastIndexOf("|"));

        try {
            String formatted = sb.toString();
            if (encoder != null) {
                formatted = encoder.encode(formatted);
            }

            return Base64.encodeBase64String(formatted.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
