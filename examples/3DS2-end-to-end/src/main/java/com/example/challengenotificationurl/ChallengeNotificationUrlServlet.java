/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.challengenotificationurl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;

public class ChallengeNotificationUrlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ChallengeNotificationHandler challengeNotificationHandler = new ChallengeNotificationHandler();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String cres = req.getParameter("cres");

        Hashtable<String, String> challengeUrlResponse = challengeNotificationHandler.handleChallengeNotification(cres);

        req.setAttribute("threeDSServerTransID", challengeUrlResponse.get("threeDSServerTransID"));
        req.setAttribute("transStatus", challengeUrlResponse.get("transStatus"));

        RequestDispatcher nextJsp = req.getRequestDispatcher("/challengeNotificationUrl.jsp");
        nextJsp.forward(req, resp);
    }

}