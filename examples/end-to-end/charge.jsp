<%@ page language="java" import="java.math.BigDecimal" %>
<%@ page language="java" import="com.global.api.entities.Address" %>
<%@ page language="java" import="com.global.api.serviceConfigs.GatewayConfig" %>
<%@ page language="java" import="com.global.api.services.CreditService" %>
<%@ page language="java" import="com.global.api.entities.Customer" %>
<%@ page language="java" import="com.global.api.builders.AuthorizationBuilder" %>
<%@ page language="java" import="com.global.api.entities.Transaction" %>
<%@ page language="java" import="com.global.api.entities.exceptions.ApiException" %>
<%@ page language="java" import="com.global.api.entities.exceptions.GatewayException" %>
<%@ page language="java" import="com.global.api.paymentMethods.CreditCardData"%>
<%@ page language="java" import="com.global.api.ServicesContainer"%>

<%
    GatewayConfig serviceConfig = new GatewayConfig();
    serviceConfig.setSecretApiKey("skapi_cert_MYl2AQAowiQAbLp5JesGKh7QFkcizOP2jcX9BrEMqQ");
    serviceConfig.setServiceUrl("https://cert.api2.heartlandportico.com");

// The following variables will be provided to you during certification
    serviceConfig.setVersionNumber("0000");
    serviceConfig.setDeveloperId("000000");

    ServicesContainer.configureService(serviceConfig);

    Address address = new Address();
    address.setStreetAddress1(request.getParameter("address"));
    address.setCity(request.getParameter("City"));
    address.setState(request.getParameter("State"));
    address.setPostalCode(request.getParameter("Zip"));
    address.setCountry("United States");

    String firstName = request.getParameter("FirstName");
    String lastName = request.getParameter("LastName");
    String fullName = String.format("%1$s %2$s", (firstName != null) ? firstName : "", (lastName != null) ? lastName : "").trim();

    CreditCardData card = new CreditCardData();
    card.setCardHolderName(fullName);
    card.setToken(request.getParameter("Token_value"));

    BigDecimal amount = new BigDecimal("15.15");
    String errorMessage = "";
    Transaction creditResponse = null;
    try {
        creditResponse = card.charge(amount)
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
    } catch (ApiException exc) {
        errorMessage = exc.getMessage();
    }
%>
<%  session.setAttribute("name", fullName);
    session.setAttribute("message", "error");
    session.setAttribute("errorMessage", errorMessage);
    response.sendRedirect("response.jsp");
%>

<% if(errorMessage.equals("") && creditResponse.getResponseCode().equals("00")) { %>
<%  session.setAttribute("amount", amount);
    session.setAttribute("message", "success");
    session.setAttribute("transactionId", creditResponse.getTransactionId());
%>
<%  } %>