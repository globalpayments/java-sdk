/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.jsonresponsebuilder;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class JsonResponseBuilder {

    private static final Gson GSON = new Gson();

    public static void respond(Object src, HttpServletResponse resp) {

        try {
            String result = GSON.toJson(src);

            PrintWriter respPrintWriter = resp.getWriter();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            respPrintWriter.print(result);
            respPrintWriter.flush();
            respPrintWriter.close();

        } catch (Exception e) {
            // TODO: set your proper error handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }
    }

}