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
            if(!compare.has(key)) {
                System.out.println(key + " not included into the compared Json: " + compare);
                return false;
            }

            Object expObj = expected.getValue(key);
            Object compObj = compare.getValue(key);

            if(compObj == null || compObj.getClass() != expObj.getClass())
                return false;

            if(expObj instanceof JsonDoc) {
                if(!areEqual((JsonDoc)expObj, (JsonDoc)compObj)) {
                    System.out.println("Not identical values for: " + key + ". Expected: " + expObj + ", compared: " + compObj);
                    return false;
                }
            }
            else {
                if(!expObj.equals(compObj)) {
                    System.out.println("Not identical values for: " + key + ". Expected: " + expObj + ", compared: " + compObj);
                    return false;
                }
            }
        }

        // extra property check
        for(String key: compare.getKeys()) {
            if(!expected.has(key)) {
                System.out.println(key + " not included into the expected json: " + expected);
                return false;
            }
        }

        return true;
    }
}