/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.authorization;

import com.global.api.entities.ThreeDSecure;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthenticationDataRetriever authenticationDataRetriever = new AuthenticationDataRetriever();

    private final AuthorizationHandler authorizationHandler = new AuthorizationHandler();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String serverTransactionId = req.getParameter("serverTransactionId");
        String paymentToken = req.getParameter("tokenResponse");

        ThreeDSecure threeDSecure = authenticationDataRetriever
                .getAuthenticationData(serverTransactionId, paymentToken);

        AuthorizationResult authorizationResult = authorizationHandler.authorize(threeDSecure, paymentToken);

        req.setAttribute("authenticationValue", threeDSecure.getAcsEndVersion());
        req.setAttribute("dsTransId", threeDSecure.getDirectoryServerTransactionId());
        req.setAttribute("messageVersion", threeDSecure.getMessageVersion());
        req.setAttribute("eci", threeDSecure.getEci());

        if (authorizationResult.isFailedAuthentication()) {
            req.setAttribute("message", "Oh Dear! Your transaction was not authenticated successfully!");
        } else {
            req.setAttribute("message", "Hurray! Your transaction was authenticated successfully!");
        }

        if (null != authorizationResult.getTransaction()) {
            req.setAttribute("transactionId", authorizationResult.getTransaction().getTransactionId());
            req.setAttribute("transactionStatus", authorizationResult.getTransaction().getResponseMessage());
        }

        RequestDispatcher nextJsp = req.getRequestDispatcher("/authorization.jsp");
        nextJsp.forward(req, resp);
    }

}