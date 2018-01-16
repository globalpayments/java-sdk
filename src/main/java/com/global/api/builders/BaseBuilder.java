package com.global.api.builders;

import com.global.api.builders.validations.Validations;
import com.global.api.entities.exceptions.ApiException;

public abstract class BaseBuilder<TResult> {
    protected Validations validations;

    public Validations getValidations() {
        return validations;
    }
    public void setValidations(Validations validations) {
        this.validations = validations;
    }

    public BaseBuilder() {
        validations = new Validations();
        setupValidations();
    }

    public TResult execute() throws ApiException {
        return execute("default");
    }
    public TResult execute(String configName) throws ApiException {
        validations.validate(this);
        return null;
    }

    public abstract void setupValidations();
}
