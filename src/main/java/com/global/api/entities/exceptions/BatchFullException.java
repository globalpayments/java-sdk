package com.global.api.entities.exceptions;

public class BatchFullException extends ApiException {
    public BatchFullException() {
        super("The IBatchProvider is reporting your batch as full. Please close the batch current batch to continue processing.");
    }
}
