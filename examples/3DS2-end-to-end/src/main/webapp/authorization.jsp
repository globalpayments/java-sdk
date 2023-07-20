 <!--
this sample code is not specific to the Global Payments SDK and is intended as a simple example and
should not be treated as Production-ready code. You'll need to add your own message parsing and 
security in line with your application or website
-->
<%@ page isELIgnored ="false" %>
<!DOCTYPE html>
<html>

<head>
    <title>3D Secure 2 Authentication</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body>
    <h2>3D Secure 2 Authentication</h2>

    <p><strong>"${message}"</strong></p>

    <p>Server Trans ID: ${serverTransactionId}</p>
    <p>Authentication Value: ${authenticationValue}</p>
    <p>DS Trans ID: ${dsTransId}</p>
    <p>Message Version: ${messageVersion}</p>
    <p>ECI: ${eci}</p>

    <h2>Transaction details:</h2>
    <p>Trans ID: ${transactionId}</p>
    <p>Trans status: ${transactionStatus}</p>
    
</body>
    
</html>
