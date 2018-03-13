package com.global.api.builders.validations;

import java.util.concurrent.Callable;

class MyCallable implements Callable<Boolean> {
    public Boolean call() throws Exception {
        return call(null);
    }
    public Boolean call(Object builder) throws Exception {
        return true;
    }
}
