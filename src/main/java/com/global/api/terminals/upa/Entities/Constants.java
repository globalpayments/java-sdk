package com.global.api.terminals.upa.Entities;

public class Constants {
    // Validation error messages
    public static final String VALIDATION_NOT_NUMERIC = "cannot be alphabet nor contains alphanumeric for this transaction type";
    public static final String VALIDATION_NULL_MSG = "cannot be null for this transaction type";
    public static final String VALIDATION_NOT_NULL_MSG = "should be null for this transaction type";
    public static final String VALIDATION_EQUAL_MSG = "was not the expected value";
    public static final String VALIDATION_NOT_EQUAL_MSG = "cannot be the value";
    public static final String VALIDATION_NOT_GREATER_THAN_MSG = "cannot be greater than";
    public static final String VALIDATION_NOT_LESS_THAN_OR_EQUAL_MSG = "cannot be less than or equal";
    public static final String VALIDATION_NOT_LESS_THAN = "cannot be less than";

    // Error throw messages
    public static final String NO_SUCH_FIELD = "No such field named: ";

    // JSON API response message fields
    public static final String ACK_MESSAGE = "ACK";
    public static final String NAK_MESSAGE = "NAK";
    public static final String BUSY_MESSAGE = "BUSY";
    public static final String TIMEOUT_MESSAGE = "TO";
    public static final String READY_MESSAGE = "READY";
    public static final String DATA_MESSAGE = "MSG";

    // JSON API data response fields
    public static final String COMMAND_USED = "response";
    public static final String COMMAND_MESSAGE = "message";
    public static final String COMMAND_DATA = "data";
    public static final String COMMAND_RESULTS = "cmdResult";
    public static final String COMMAND_STATUS = "result";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "errorMessage";

    public static final String GET_PARAM = "GetParam";
    public static final String REBOOT = "Reboot";
    public static final String EMV = "emv";

    // JSON Request parameter fields
    // Note: First index of array of string should be
    // the parameter field name of each object
    public static final String[] PARAMS = {
            "params",
            "_clerkId", "_tokenRequest", "_tokenValue", "_batch",
            "_displayOption", "_configuration", "_timeZone", "_downloadType",
            "_fileType", "_slotNum", "_file", "_configType", "_lineItemLeft", "_lineItemRight",
            "_content", "_fileName", "_header", "_prompt1", "_prompt2", "_reportOutput", "_reportType",
            "_line1", "_line2", "_timeOut"
    };

    public static final String[] TRANSACTION = {
            "transaction",
            "_amount",
            "_referenceNumber", "_authorizedAmount",
            "_baseAmount", "_taxAmount", "_tipAmount", "_taxIndicator",
            "_cashBackAmount", "_invoiceNbr", "_allowPartialAuth", "_tranNo", "_totalAmount"
    };
}
