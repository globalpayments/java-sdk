/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.check3dsversion;

import com.example.jsonresponsebuilder.JsonResponseBuilder;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Check3dsVersionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ThreeDSVersionChecker threeDSVersionChecker = new ThreeDSVersionChecker();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        JsonObject response = threeDSVersionChecker.check(req.getReader());

        JsonResponseBuilder.respond(response, resp);
    }

}