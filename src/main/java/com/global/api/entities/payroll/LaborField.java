package com.global.api.entities.payroll;

import com.global.api.utils.JsonDoc;

import java.util.ArrayList;

public class LaborField extends PayrollCollectionItem {
    private ArrayList<LaborFieldLookup> lookup;

    public ArrayList<LaborFieldLookup> getLookup() {
        return lookup;
    }

    public LaborField() {
        super("LaborFieldId", "LaborFieldValue");
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) {
        super.fromJson(doc, encoder);

        if(description == null) {
            description = doc.getString("LaborFieldTitle");
        }

        if(doc.has("laborfieldLookups")) {
            lookup = new ArrayList<LaborFieldLookup>();
            for(JsonDoc lookupData: doc.getEnumerator("laborfieldLookups")) {
                LaborFieldLookup lookups = new LaborFieldLookup();
                lookups.setDescription(lookupData.getString("laborFieldDescription"));
                lookups.setValue(lookupData.getString("laborFieldValue"));

                lookup.add(lookups);
            }
        }
    }
}
