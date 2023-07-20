package com.example.authorization;

import com.global.api.entities.Transaction;

class AuthorizationResult {

    private boolean failedAuthentication;

    private Transaction transaction;

    boolean isFailedAuthentication() {
        return failedAuthentication;
    }

    void setFailedAuthentication(boolean failedAuthentication) {
        this.failedAuthentication = failedAuthentication;
    }

    Transaction getTransaction() {
        return transaction;
    }

    void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

}
