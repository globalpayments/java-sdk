package com.global.api.entities.gpApi;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class PagedResult<T>  {
    @Getter @Setter public int totalRecordCount;
    @Getter @Setter public int pageSize;
    @Getter @Setter public int page;
    @Getter @Setter public String Order;
    @Getter @Setter public String OrderBy;
    @Getter @Setter public List<T> results = new ArrayList<T>();

    public void add(T item) {
        results.add(item);
    }

}