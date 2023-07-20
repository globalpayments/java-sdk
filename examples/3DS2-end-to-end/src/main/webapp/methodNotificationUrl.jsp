< <!--
this sample code is not specific to the Global Payments SDK and is intended as a simple example and
should not be treated as Production-ready code. You'll need to add your own message parsing and 
security in line with your application or website
-->
<%@ page isELIgnored ="false" %>ed ="false" %>
<script src="globalpayments-3ds.js?v=01"></script>
<script>
      GlobalPayments.ThreeDSecure.handleMethodNotification("${threeDSServerTransID}");
</script>