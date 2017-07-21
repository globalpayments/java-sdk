package com.global.api.builders.validations;

import com.global.api.builders.BaseBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.IFlag;
import com.global.api.entities.exceptions.BuilderException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.*;

public class Validations {
    class ValidationKey {
        private Class<?> type;
        private long value;

        public Type getType() {
            return type;
        }
        public long getValue() {
            return value;
        }

        ValidationKey(Class type, long value) {
            this.type = type;
            this.value = value;
        }

        Set<?> getSet() {
            try {
                Method method = type.getMethod("getSet", long.class);
                return (Set<?>)method.invoke(this.type, this.value);
            }
            catch(Exception exc) { return null; }
        }
    }

    private HashMap<ValidationKey, List<ValidationTarget>> rules;

    public Validations() {
        rules = new HashMap<ValidationKey, List<ValidationTarget>>();
    }

    public <T extends IFlag> ValidationTarget of(Set<T> types) {
        Class clazz = types.iterator().next().getClass();
        return of(new ValidationKey(clazz, getSetValue(types)));
    }
    public ValidationTarget of(IFlag type) {
        return of(new ValidationKey(type.getClass(), type.getLongValue()));
    }
    ValidationTarget of(ValidationKey key) {
        if(!rules.containsKey(key))
            rules.put(key, new ArrayList<ValidationTarget>());

        ValidationTarget target = new ValidationTarget(this, key);
        rules.get(key).add(target);
        return target;
    }

    public <T> void validate(BaseBuilder<T> builder) throws BuilderException {
        for(ValidationKey key: rules.keySet()) {
            IFlag value = getPropertyValue(builder, key.getType());
            if(value == null && builder instanceof TransactionBuilder) {
                value = getPropertyValue(((TransactionBuilder<T>)builder).getPaymentMethod(), key.getType());
                if(value == null)
                    continue;
            }

            Set<?> values = key.getSet();
            if(values.contains(value)) {
                for(ValidationTarget validation: rules.get(key)) {
                    ValidationClause clause = validation.getClause();
                    if(clause == null) continue;

                    // modifier
                    IFlag constraint = validation.getConstraint();
                    if(constraint != null) {
                        IFlag modifier = getPropertyValue(builder, constraint.getClass());
                        if(!constraint.equals(modifier))
                            continue;
                    }

                    // check precondition
                    ValidationClause precondition = validation.getPrecondition();
                    if(precondition != null) {
                        try {
                            if (!precondition.getCallback().call(builder))
                                continue;
                        }
                        catch(Exception exc) {
                            throw new BuilderException(exc.getMessage());
                        }
                    }

                    // run actual validation
                    try{
                        if(!clause.getCallback().call(builder))
                            throw new BuilderException(clause.getMessage());
                    }
                    catch(Exception e) {
                        throw new BuilderException(e.getMessage());
                    }
                }
            }
        }
    }

    private Field getField(Class clazz, String name) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
        catch(NoSuchFieldException e) {
            Class superClazz = clazz.getSuperclass();
            if(superClazz != null)
                return getField(superClazz, name);
            else throw e;
        }
    }
    private IFlag getPropertyValue(Object obj, Object comp) {
        if(obj == null) return null;

        String name = ((Class)comp).getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        try{
            Field field = getField(obj.getClass(), name);
            return (IFlag)field.get(obj);
        }
        catch(Exception e) {
            return null;
        }
    }
    private <T extends IFlag> long getSetValue(Set<T> flags) {
        long value = 0;
        for(IFlag type : flags) {
            value |= type.getLongValue();
        }
        return value;
    }
}
