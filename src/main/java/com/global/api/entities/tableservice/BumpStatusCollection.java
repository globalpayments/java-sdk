package com.global.api.entities.tableservice;

import java.util.*;

public class BumpStatusCollection {
    private HashMap<String, Integer> bumpStatus;

    public int get(String status) {
        Integer value = bumpStatus.get(status);
        return value == null ? 0 : value;
    }

    public String[] getKeys() {
        return bumpStatus.keySet().toArray(new String[bumpStatus.keySet().size()]);
    }

    public BumpStatusCollection(String statusString) {
        bumpStatus = new HashMap<String, Integer>();

        int index = 1;
        for(String status: statusString.split(","))
            bumpStatus.put(status.trim(), index++);
    }
}
