package com.global.api.entities.payroll;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.JsonDoc;

public class SessionInfo extends PayrollEntity {
    private String sessionToken;
    private String errorMessage;

    public String getSessionToken() {
        return sessionToken;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    void fromJson(JsonDoc doc, PayrollEncoder encoder) throws ApiException {
        sessionToken = doc.getString("SessionToken");
        errorMessage = doc.getString("ErrorMessage");
    }

    static public PayrollRequest signIn(String username, String password, PayrollEncoder encoder) {
        String request = new JsonDoc()
                .set("Username", username)
                .set("Password", encoder.encode(password))
                .toString();

        return new PayrollRequest("/api/pos/session/signin", request);
    }

    static public PayrollRequest signOut() {
        return new PayrollRequest("/api/pos/session/signout", null);
    }
}
