package com.global.api.builders.validations;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.IFlag;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.terminals.enums.TerminalReportType;

import java.lang.reflect.Field;
import java.util.Set;

public class RequestBuilderValidations {

    private Validations _validations;

    public RequestBuilderValidations(Validations validations) {
        _validations = validations;
    }

    /// <summary>
    /// Validate method for Terminals
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="builder"></param>
    /// <param name="actionType"></param>
    public <T> void Validate(T builder, TerminalReportType actionType) throws BuilderException {
        for (Validations.ValidationKey key : _validations.getRules().keySet()) {
            IFlag value = getPropertyValue(builder, key.getType());
            if (value == null && builder instanceof TransactionBuilder) {
                value = getPropertyValue(((TransactionBuilder<T>) builder).getPaymentMethod(), key.getType());
                if (value == null)
                    continue;
            }

            Set<?> values = key.getSet();
            if (values.contains(value)) {
                for (ValidationTarget validation : _validations.getRules().get(key)) {
                    ValidationClause clause = validation.getClause();
                    if (clause == null) continue;

                    // modifier
                    IFlag constraint = validation.getConstraint();
                    if (constraint != null) {
                        IFlag modifier = getPropertyValue(builder, constraint.getClass());
                        if (!constraint.equals(modifier))
                            continue;
                    }

                    // check precondition
                    ValidationClause precondition = validation.getPrecondition();
                    if (precondition != null) {
                        try {
                            if (!precondition.getCallback().call(builder))
                                continue;
                        } catch (Exception exc) {
                            throw new BuilderException(exc.getMessage());
                        }
                    }

                    // run actual validation
                    try {
                        if (!clause.getCallback().call(builder))
                            throw new BuilderException(clause.getMessage());
                    } catch (Exception e) {
                        throw new BuilderException(e.getMessage());
                    }
                }
            }
        }
    }

    private IFlag getPropertyValue(Object obj, Object comp) {
        if (obj == null) return null;

        String name = ((Class) comp).getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        try {
            Field field = getField(obj.getClass(), name);
            return (IFlag) field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private Field getField(Class clazz, String name) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Class superClazz = clazz.getSuperclass();
            if (superClazz != null)
                return getField(superClazz, name);
            else throw e;
        }
    }

}
