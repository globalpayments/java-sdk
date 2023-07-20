/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.initiateauthentication;

import com.example.jsonreader.JsonReaderHelper;
import com.example.jsonresponsebuilder.JsonResponseBuilder;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class InitiateAuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final JsonReaderHelper jsonReaderHelper = new JsonReaderHelper();

    private final AuthenticationHandler authenticationHandler = new AuthenticationHandler();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        JsonObject requestData = jsonReaderHelper.getRequestBodyAsJson(req);

        Map<String, Object> response = authenticationHandler.doAuthentication(requestData);

        JsonResponseBuilder.respond(response, resp);
    }

}