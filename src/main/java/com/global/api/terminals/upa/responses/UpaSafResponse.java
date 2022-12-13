package com.global.api.terminals.upa.responses;

import com.global.api.entities.enums.SummaryType;
import com.global.api.terminals.SummaryResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.utils.JsonDoc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

public class UpaSafResponse implements ISAFResponse {
    private ArrayList<SummaryResponse> approved;
    private String command;
    private ArrayList<SummaryResponse> declined;
    private String deviceResponseCode;
    private String deviceResponseText;
    private ArrayList<SummaryResponse> pending;
    private String status;
    private BigDecimal totalAmount;
    private Integer totalCount;
    private String transactionType;
    private String version;
    public UpaSafResponse(JsonDoc responseObj) {
        JsonDoc responseData = responseObj.get("data");

        if (responseData != null) {
            JsonDoc cmdResult = responseData.get("cmdResult");

            if (cmdResult != null) {
                status = cmdResult.getString("result");
                deviceResponseCode = status.equalsIgnoreCase("success") ? "00" : cmdResult.getString("errorCode");
                deviceResponseText = cmdResult.getString("errorMessage");
            }

            transactionType = responseData.getString("response");

            JsonDoc innerData = responseData.get("data");
        }
    }

    @Override
    public Map<SummaryType, SummaryResponse> getApproved() {
        return (Map<SummaryType, SummaryResponse>) approved;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public Map<SummaryType, SummaryResponse> getDeclined() {
        return (Map<SummaryType, SummaryResponse>) declined;
    }

    @Override
    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    @Override
    public String getDeviceResponseText() {
        return deviceResponseText;
    }

    @Override
    public Map<SummaryType, SummaryResponse> getPending() {
        return (Map<SummaryType, SummaryResponse>) pending;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public Integer getTotalCount() {
        return totalCount;
    }

    @Override
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setCommand(String command) {}

    public void setDeviceResponseCode(String deviceResponseCode) {}

    public void setDeviceResponseText(String deviceResponseText) {}

    public void setStatus(String status) {}

    public void setVersion(String version) {}
}
