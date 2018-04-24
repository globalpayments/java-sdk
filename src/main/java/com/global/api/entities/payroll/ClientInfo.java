package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

import java.lang.reflect.Type;
import java.util.HashMap;

public class ClientInfo extends PayrollEntity {
    private String clientCode;
    private String clientName;
    private Integer federalEin;

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Integer getFederalEin() {
        return federalEin;
    }

    public void setFederalEin(Integer federalEin) {
        this.federalEin = federalEin;
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException {
        try {
            clientCode = doc.getValue("ClientCode", encoder.getDecoder());
            clientName = doc.getString("ClientName");
            federalEin = Integer.parseInt(doc.getValue("FederalEin", encoder.getDecoder()));
        }
        catch(Exception exc) {
            throw new ApiException(exc.getMessage(), exc);
        }
    }

    public IPayrollRequestBuilder getClientInfoRequest() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                String request = new JsonDoc()
                        .set("FederalEin", encoder.encode(federalEin))
                        .toString();

                return new PayrollRequest("/api/pos/client/getclients", request);
            }
        };
    }

    public IPayrollRequestBuilder getCollectionRequestByType() {
        return new IPayrollRequestBuilder() {
            public PayrollRequest buildRequest(PayrollEncoder encoder, Class<?> clazz) {
                HashMap<Type, String> endpoints = new HashMap<Type, String>();
                endpoints.put(TerminationReason.class, "/api/pos/termination/GetTerminationReasons");
                endpoints.put(WorkLocation.class, "/api/pos/worklocation/GetWorkLocations");
                endpoints.put(LaborField.class, "/api/pos/laborField/GetLaborFields");
                endpoints.put(PayGroup.class, "/api/pos/payGroup/GetPayGroups");
                endpoints.put(PayItem.class, "/api/pos/payItem/GetPayItems");

                String requestBody = new JsonDoc()
                        .set("ClientCode", encoder.encode(clientCode))
                        .toString();

                return new PayrollRequest(endpoints.get(clazz), requestBody);
            }
        };
    }
}
