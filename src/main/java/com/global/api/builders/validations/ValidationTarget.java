package com.global.api.builders.validations;

import com.global.api.entities.enums.IFlag;

public class ValidationTarget {
    private Validations parent;
    private ValidationClause precondition;
    private ValidationClause clause;

    private Validations.ValidationKey type;
    private IFlag constraint;

    public Validations.ValidationKey getType() {
        return type;
    }
    public IFlag getConstraint() {
        return constraint;
    }
    public ValidationClause getClause() {
        return clause;
    }
    public ValidationClause getPrecondition() {
        return precondition;
    }

    public ValidationTarget(Validations parent, Validations.ValidationKey type) {
        this.parent = parent;
        this.type = type;
    }

    public ValidationTarget with(IFlag constraint) {
        this.constraint = constraint;
        return this;
    }

    public ValidationClause check(String propertyName) {
        clause = new ValidationClause(parent, this, propertyName);
        return clause;
    }

    public void check(String propertyName1, String propertyName2) {
        if (propertyName1 == null || propertyName2 == null) {
            throw new IllegalArgumentException("TransactionId or TerminalRefNumber must not be null");
        }
    }

    public ValidationClause when(String propertyName) {
        precondition = new ValidationClause(parent, this, propertyName, true);
        return precondition;
    }
}
