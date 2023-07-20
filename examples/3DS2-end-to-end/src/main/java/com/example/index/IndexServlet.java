/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.index;

import com.example.token.AccessTokenCreator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {

            String accessToken = AccessTokenCreator.getInstance().getAccessToken();
            req.setAttribute("accessToken", accessToken);

            RequestDispatcher nextJsp = req.getRequestDispatcher("/index.jsp");
            nextJsp.forward(req, resp);
        } catch (Exception e) {
            // TODO: set your proper error handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }
    }

}