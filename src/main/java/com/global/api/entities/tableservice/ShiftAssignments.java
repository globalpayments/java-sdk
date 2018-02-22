package com.global.api.entities.tableservice;

import com.global.api.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftAssignments {
    private Map<String, Integer[]> dict;

    public ShiftAssignments() {
        dict = new HashMap<String, Integer[]>();
    }

    public boolean containsKey(String key) {
        return dict.containsKey(key);
    }

    public Integer[] get(String key) {
        return dict.get(key);
    }

    public void put(String key, Integer[] value) {
        dict.put(key, value);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(String key: dict.keySet()) {
            sb.append(key).append("-");
            sb.append(StringUtils.join(",", dict.get(key)));
            sb.append("|");
        }

        return StringUtils.trimEnd(sb.toString(), "|");
    }
}
