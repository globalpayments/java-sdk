package com.global.api.utils;

import com.global.api.entities.enums.IStringConstant;
import com.google.gson.*;

import java.math.BigDecimal;
import java.util.*;

public class JsonDoc {
    private HashMap<String, Object> dict;
    private IRequestEncoder encoder;
    private static final Gson gson = new Gson();

    public JsonDoc() {
        this(new HashMap<String, Object>(), null);
    }
    public JsonDoc(IRequestEncoder encoder) {
        this(new HashMap<String, Object>(), encoder);
    }
    public JsonDoc(HashMap<String, Object> values) {
        this(values, null);
    }
    public JsonDoc(HashMap<String, Object> values, IRequestEncoder encoder) {
        this.dict = values;
        this.encoder = encoder;
    }

    // request stuff
    public JsonDoc remove(String key) {
        dict.remove(key);
        return this;
    }

    public JsonDoc set(String key, String value) {
        return set(key, value, false);
    }
    public JsonDoc set(String key, String value, boolean force) {
        if(!StringUtils.isNullOrEmpty(value) || force) {
            if(encoder != null)
                dict.put(key, encoder.encode(value));
            else dict.put(key, value);
        }
        return this;
    }
    public JsonDoc set(String key, IStringConstant value) {
        if(value != null)
            return set(key, value.getValue(), false);
        return this;
    }
    public JsonDoc set(String key, IStringConstant value, boolean force) {
        if(value != null)
            return set(key, value.getValue(), force);
        return this;
    }
    public JsonDoc set(String key, Integer value) {
        return set(key, value, false);
    }
    public JsonDoc set(String key, Integer value, boolean force) {
        if(value != null || force) {
            if(encoder != null)
                dict.put(key, encoder.encode(value));
            else dict.put(key, value);
        }
        return this;
    }
    public JsonDoc set(String key, boolean value) {
        if(encoder != null)
            dict.put(key, encoder.encode(value));
        else dict.put(key, value);
        return this;
    }

    public JsonDoc subElement(String name) {
        JsonDoc subRequest = new JsonDoc();
        dict.put(name, subRequest);
        return subRequest;
    }

    public String toString() {
        HashMap<String, Object> _final = finish();
        return gson.toJson(_final);
    }

    private HashMap<String, Object> finish() {
        HashMap<String, Object> _final = new HashMap<String, Object>();
        for(String key: dict.keySet()) {
            Object value = dict.get(key);
            if(value instanceof JsonDoc)
                _final.put(key, ((JsonDoc)value).finish());
            else _final.put(key, value);
        }
        return _final;
    }

    // response stuff
    public JsonDoc get(String name) {
        if(dict.containsKey(name)) {
            Object value = dict.get(name);
            if(value instanceof JsonDoc)
                return (JsonDoc)value;
            return null;
        }
        return null;
    }

    public <T> T getValue(String name, ValueConverter<T> converter) throws Exception {
        if(dict.containsKey(name)) {
            Object value = dict.get(name);
            return converter.call(value.toString());
        }
        return converter.call(null);
    }

    public String getString(String name) {
        if(dict.containsKey(name)) {
            Object value = dict.get(name);
            if(encoder != null)
                return encoder.decode(value);
            else return value.toString();
        }
        return null;
    }
    public BigDecimal getDecimal(String name) {
        String value = getString(name);
        if(value != null) {
            return new BigDecimal(value);
        }
        return null;
    }
    public boolean getBool(String name) {
        String value = getString(name);
        if(value.equals("true"))
            return true;
        return false;
    }
    public Integer getInt(String name) {
        String value = getString(name);
        if(value != null)
            return Integer.parseInt(value);
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<JsonDoc> getEnumerator(String name) {
        if(dict.containsKey(name)) {
            Object value = dict.get(name);
            if(value instanceof List)
                return (List<JsonDoc>)value;
            return null;
        }
        return null;
    }

    public boolean has(String name) {
        return dict.containsKey(name);
    }

    public static JsonDoc parse(String json) {
        return parse(json, null);
    }
    public static JsonDoc parse(String json, IRequestEncoder encoder) {
        JsonElement parsed = new JsonParser().parse(json);
        if(parsed.isJsonObject()) {
            return parseObject(parsed.getAsJsonObject(), encoder);
        }
        return null;
    }

    private static JsonDoc parseObject(JsonObject obj, IRequestEncoder encoder) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        for(Map.Entry<String, JsonElement> child: obj.entrySet()) {
            if(child.getValue().isJsonArray()) {
                List<JsonDoc> objs = parseArray(child.getValue().getAsJsonArray(), encoder);
                values.put(child.getKey(), objs);
            }
            else if(child.getValue().isJsonObject()){
                values.put(child.getKey(), parseObject(child.getValue().getAsJsonObject(), encoder));
            }
            else {
                JsonElement childValue = child.getValue();
                if(!childValue.isJsonNull())
                    values.put(child.getKey(), childValue.getAsString());
                //else values.put(child.getKey(), null);
            }
        }
        return new JsonDoc(values, encoder);
    }

    private static List<JsonDoc> parseArray(JsonArray objs, IRequestEncoder encoder) {
        List<JsonDoc> responses = new ArrayList<JsonDoc>();
        for (JsonElement obj: objs) {
            if(obj.isJsonObject())
                responses.add(parseObject(obj.getAsJsonObject(), encoder));
        }
        return responses;
    }
}
