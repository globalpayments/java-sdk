/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.methodnotificationurl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MethodNotificationUrlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MethodNotificationUrlHandler methodNotificationUrlHandler = new MethodNotificationUrlHandler();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String threeDSMethodData = req.getParameter("threeDSMethodData");

        String threeDSServerTransID = methodNotificationUrlHandler.handleMethodNotification(threeDSMethodData);

        req.setAttribute("threeDSServerTransID", threeDSServerTransID);

        RequestDispatcher nextJsp = req.getRequestDispatcher("/methodNotificationUrl.jsp");
        nextJsp.forward(req, resp);
    }

}