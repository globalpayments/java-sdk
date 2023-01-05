package com.global.api.paymentMethods;

public interface INotificationData {
    // A ReturnUrl is representing after the payment where the transaction return to
    String getReturnUrl();
    INotificationData setReturnUrl(String value);
    // A StatusUpdateUrl is representing after the transaction where the status response will come like SUCCESS/PENDING
    String getStatusUpdateUrl();
    INotificationData setStatusUpdateUrl(String value);
    // A CancelUrl is representing during the payment where the transaction cancels to
    String getCancelUrl();
    INotificationData setCancelUrl(String value);
}