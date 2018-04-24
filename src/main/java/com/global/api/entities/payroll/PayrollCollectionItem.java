package com.global.api.entities.payroll;

import com.global.api.utils.JsonDoc;

public abstract class PayrollCollectionItem extends PayrollEntity {
    private String idField;
    private String descriptionField;

    protected String id;
    protected String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PayrollCollectionItem(String idFieldName, String descriptionFieldName) {
        idField = idFieldName;
        descriptionField = descriptionFieldName;
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) {
        id = doc.getString(idField);
        description = doc.getString(descriptionField);
    }
}
