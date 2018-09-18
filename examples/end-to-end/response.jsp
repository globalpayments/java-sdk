<%@ page language="java" import="java.math.BigDecimal" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<title>Payment Status</title>
<body>
<%
    BigDecimal amount = (BigDecimal)session.getAttribute("amount");
    String name = (String)session.getAttribute("name");
    String message = (String)session.getAttribute("message");
    String transactionId = (String)session.getAttribute("transactionId");
    String errorMessage = (String)session.getAttribute("errorMessage");
    if(message.equals("success")){
%>
<h2>Success !!</h2>
<p>Thank You, <%= name %> for your order of <%= amount %>.</p> Transaction Id: <%= transactionId %>
<%
}
else{
%>
<h2>Failed !!</h2>
<p><%= errorMessage %></p>
<A HREF="index.html"><button>Please Retry</button></A>
<%
    }
%>
</body>
</html>