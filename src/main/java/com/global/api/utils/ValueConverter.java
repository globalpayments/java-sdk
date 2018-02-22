package com.global.api.utils;

import java.util.concurrent.Callable;

public class ValueConverter<TResult> implements Callable<TResult> {
    public TResult call() throws Exception {
        return call(null);
    }
    public TResult call(String value) throws Exception {
        return null;
    }
}
