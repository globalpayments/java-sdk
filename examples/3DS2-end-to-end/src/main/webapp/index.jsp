 <!--
this sample code is not specific to the Global Payments SDK and is intended as a simple example and
should not be treated as Production-ready code. You'll need to add your own message parsing and 
security in line with your application or website
-->
<%@ page isELIgnored ="false" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Global Payments end-to-end with GP-API example</title>
    <link rel="stylesheet" href="styles.css" />
    <script src="https://js.globalpay.com/v1/globalpayments.js"></script>
    <script src="globalpayments-3ds.js?v=01"></script>
    <script>
        let accessToken = "${accessToken}";
    </script>
    <script defer src="main.js?v=05"></script>
</head>
<body>
    <div class="container">
        <p>3DS test card with CHALLENGE_REQUIRED: 4012 0010 3848 8884</p>
        <p>Amount: 100 EUR</p>
        <form id="payment-form" method="post">
            <!-- Target for the credit card form -->
            <div id="credit-card"></div>
        </form>

        <div id="responseDiv" class="messagebox"></div>

</div>
</body>
</html>
