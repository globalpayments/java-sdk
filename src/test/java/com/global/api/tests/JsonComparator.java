package com.global.api.tests;

import com.global.api.utils.JsonDoc;

public class JsonComparator {
    public static boolean areEqual(String expectedString, String compareString) {
        JsonDoc expected = JsonDoc.parse(expectedString);
        JsonDoc compare = JsonDoc.parse(compareString);

        return areEqual(expected, compare);
    }

    public static boolean areEqual(JsonDoc expected, JsonDoc compare) {
        for(String key: expected.getKeys()) {
            if(!compare.has(key))
                return false;

            Object expObj = expected.getValue(key);
            Object compObj = compare.getValue(key);

            if(compObj == null || compObj.getClass() != expObj.getClass())
                return false;

            if(expObj instanceof JsonDoc) {
                if(!areEqual((JsonDoc)expObj, (JsonDoc)compObj))
                    return false;
            }
            else {
                if(!expObj.equals(compObj))
                    return false;
            }
        }

        // extra property check
        for(String key: compare.getKeys()) {
            if(!expected.has(key))
                return false;
        }

        return true;
    }
}
