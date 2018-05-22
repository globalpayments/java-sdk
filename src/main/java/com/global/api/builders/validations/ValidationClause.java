package com.global.api.builders.validations;

import java.lang.reflect.Field;

public class ValidationClause {
    private Validations parent;
    private ValidationTarget target;
    private String propertyName;
    private MyCallable callback;
    private String message;
    private boolean precondition;

    public MyCallable getCallback() {
        return callback;
    }
    public String getMessage() {
        return message;
    }

    public ValidationClause(Validations parent, ValidationTarget target, String propertyName) {
        this(parent, target, propertyName, false);
    }
    public ValidationClause(Validations parent, ValidationTarget target, String propertyName, boolean precondition) {
        this.parent = parent;
        this.target = target;
        this.propertyName = propertyName;
        this.precondition = precondition;
    }

    public ValidationTarget isNull() {
        return isNull(null);
    }
    public ValidationTarget isNull(String message){
        callback = new MyCallable() {
            public Boolean call(Object builder) throws Exception {
                try {
                    Field f = getField(builder.getClass(), propertyName);
                    Object value = f.get(builder);
                    return value == null;
                }
                catch(NoSuchFieldException exc) {
                    return false;
                }
            }
        };
        this.message = (message != null) ? message : String.format("%s cannot be null for this transaction type.", propertyName);
        if(precondition)
            return target;
        return parent.of(target.getType()).with(target.getConstraint());
    }

    public ValidationTarget isNotNull() {
        return isNotNull(null);
    }
    public ValidationTarget isNotNull(String message){
        callback = new MyCallable() {
            public Boolean call(Object builder) throws Exception {
                try {
                    Field f = getField(builder.getClass(), propertyName);
                    Object value = f.get(builder);
                    return value != null;
                }
                catch(NoSuchFieldException exc) {
                    return false;
                }
            }
        };
        this.message = (message != null) ? message : String.format("%s cannot be null for this transaction type.", propertyName);
        if(precondition)
            return target;
        return parent.of(target.getType()).with(target.getConstraint());
    }

    public ValidationTarget isClass(Class clazz) {
        return isClass(clazz, null);
    }
    public ValidationTarget isClass(Class clazz, String message) {
        final Class checkClass = clazz;
        callback = new MyCallable() {
            public Boolean call(Object builder) throws Exception {
                try {
                    Field f = getField(builder.getClass(), propertyName);
                    Object value = f.get(builder);
                    return value.getClass() == checkClass;
                }
                catch(NoSuchFieldException exc) {
                    return false;
                }
            }
        };
        this.message = (message != null) ? message : String.format("%s must be an instance of the %s class.", propertyName, clazz.getName());
        if(precondition)
            return target;
        return parent.of(target.getType()).with(target.getConstraint());
    }

    public ValidationTarget isEqualTo(final Object expected) {
        return isEqualTo(expected, null);
    }
    public ValidationTarget isEqualTo(final Object expected, String message) {
        callback = new MyCallable() {
            public Boolean call(Object builder) throws Exception {
                try {
                    Field f = getField(builder.getClass(), propertyName);
                    Object value = f.get(builder);
                    return value.equals(expected);
                }
                catch(NoSuchFieldException exc) {
                    return false;
                }
            }
        };
        this.message = (message != null) ? message : String.format("%s was not the expected value %s", propertyName, expected.toString());
        if(precondition)
            return target;
        return parent.of(target.getType()).with(target.getConstraint());
    }

    public ValidationTarget isNotEqual(final Object expected) {
        return isNotEqual(expected, null);
    }
    public ValidationTarget isNotEqual(final Object expected, String message) {
        callback = new MyCallable() {
            public Boolean call(Object builder) throws Exception {
                try {
                    Field f = getField(builder.getClass(), propertyName);
                    Object value = f.get(builder);
                    return !value.equals(expected);
                }
                catch(NoSuchFieldException exc) {
                    return false;
                }
            }
        };
        this.message = (message != null) ? message : String.format("%s cannot be the value %s.", propertyName, expected.toString());
        if(precondition)
            return target;
        return parent.of(target.getType()).with(target.getConstraint());
    }

    private Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        try{
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        }
        catch(NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if(superClass == null)
                throw e;
            else {
                return getField(superClass, fieldName);
            }
        }
    }
}
