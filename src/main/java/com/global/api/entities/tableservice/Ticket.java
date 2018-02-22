package com.global.api.entities.tableservice;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MultipartForm;
import com.global.api.utils.StringUtils;

import java.util.Date;

public class Ticket extends TableServiceResponse {
    private int bumpStatusId;
    private int checkId;
    private Date checkInTime;
    private String partyName;
    private int partyNumber;
    private String section;
    private Integer tableNumber;
    private Integer waitTime;

    public int getBumpStatusId() {
        return bumpStatusId;
    }
    public void setBumpStatusId(int bumpStatusId) {
        this.bumpStatusId = bumpStatusId;
    }
    public int getCheckId() {
        return checkId;
    }
    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }
    public Date getCheckInTime() {
        return checkInTime;
    }
    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }
    public String getPartyName() {
        return partyName;
    }
    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }
    public int getPartyNumber() {
        return partyNumber;
    }
    public void setPartyNumber(int partyNumber) {
        this.partyNumber = partyNumber;
    }
    public String getSection() {
        return section;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public Integer getTableNumber() {
        return tableNumber;
    }
    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }
    public Integer getWaitTime() {
        return waitTime;
    }
    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public Ticket(String json) throws ApiException {
        this(json, "default");
    }
    public Ticket(String json, String configName) throws ApiException {
        super(json, configName);
        expectedAction = "assignCheck";
    }

    protected void mapResponse(JsonDoc response) throws ApiException {
        super.mapResponse(response);

        checkId = response.getInt("checkID");
        checkInTime = response.getDate("checkInTime", "dd-MM-yyyy hh:mm");
        tableNumber = response.getInt("tableNumber");
        waitTime = response.getInt("waitTime");
    }

    public TableServiceResponse bumpStatus(String bumpStatus) throws ApiException {
        return bumpStatus(bumpStatus, null);
    }
    public TableServiceResponse bumpStatus(String bumpStatus, Date bumpTime) throws ApiException {
        int bumpStatusId = ServicesContainer.getInstance().getTableService(configName).getBumpStatusCollection().get(bumpStatus);
        if(bumpStatusId == 0)
            throw new MessageException(String.format("Unknown status value: %s", bumpStatus));
        return bumpStatus(bumpStatusId, bumpTime);
    }
    public TableServiceResponse bumpStatus(int bumpStatusId) throws ApiException {
        return bumpStatus(bumpStatusId, null);
    }
    public TableServiceResponse bumpStatus(int bumpStatusId, Date bumpTime) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("bumpStatusID", bumpStatusId)
                .set("bumpTime", bumpTime != null ? bumpTime : new Date());

        TableServiceResponse response = sendRequest(TableServiceResponse.class, "pos/bumpStatus", content);
        this.bumpStatusId = bumpStatusId;
        return response;
    }

    public TableServiceResponse clearTable() throws ApiException {
        return clearTable(null);
    }
    public TableServiceResponse clearTable(Date clearTime) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("clearTime", clearTime == null ? new Date() : clearTime);

        return sendRequest(TableServiceResponse.class, "pos/clearTable", content);
    }

    public TableServiceResponse openOrder() throws ApiException {
        return openOrder(null);
    }
    public TableServiceResponse openOrder(Date openTime) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("openTime", openTime == null ? new Date() : openTime);

        return sendRequest(TableServiceResponse.class, "pos/openOrder", content);
    }

    public TableServiceResponse settleCheck() throws ApiException {
        return settleCheck("", null);
    }
    public TableServiceResponse settleCheck(String bumpStatus) throws ApiException {
        return settleCheck(bumpStatus, null);
    }
    public TableServiceResponse settleCheck(String bumpStatus, Date settleTime) throws ApiException {
        Integer bumpStatusId = null;
        if (!StringUtils.isNullOrEmpty(bumpStatus)) {
            bumpStatusId = ServicesContainer.getInstance().getTableService(configName).getBumpStatusCollection().get(bumpStatus);
            if (bumpStatusId == 0)
                throw new MessageException(String.format("Unknown status value: %s", bumpStatus));
        }
        return settleCheck(bumpStatusId, settleTime);
    }
    public TableServiceResponse settleCheck(Integer bumpStatusId) throws ApiException {
        return settleCheck(bumpStatusId, null);
    }
    public TableServiceResponse settleCheck(Integer bumpStatusId, Date settleTime) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("bumpStatusID", bumpStatusId)
                .set("settleTime", settleTime == null ? new Date() : settleTime);

        TableServiceResponse response = sendRequest(TableServiceResponse.class, "pos/settleCheck", content);
        if(bumpStatusId != null)
            this.bumpStatusId = bumpStatusId;
        return response;
    }

    public TableServiceResponse transfer(int newTableNumber) throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("newTableNumber", newTableNumber);

        TableServiceResponse response = sendRequest(TableServiceResponse.class, "pos/settleCheck", content);
        if(response.getResponseCode().equals("00"))
            this.tableNumber = newTableNumber;
        return response;
    }

    public TableServiceResponse update() throws ApiException {
        MultipartForm content = new MultipartForm()
                .set("checkID", checkId)
                .set("partyName", partyName)
                .set("partNum", partyNumber)
                .set("section", section)
                .set("bumpStatusID", bumpStatusId);

        return sendRequest(TableServiceResponse.class, "pos/editTable", content);
    }

    public static Ticket fromId(int checkId) throws ApiException {
        return Ticket.fromId(checkId, null, "default");
    }
    public static Ticket fromId(int checkId, String configName) throws ApiException {
        return Ticket.fromId(checkId, null, configName);
    }
    public static Ticket fromId(int checkId, Integer tableNumber) throws ApiException {
        return fromId(checkId, tableNumber, "default");
    }
    public static Ticket fromId(int checkId, Integer tableNumber, String configName) throws ApiException {
        Ticket ticket = new Ticket("", configName);
        ticket.setTableNumber(tableNumber);
        ticket.setCheckId(checkId);

        return ticket;
    }
}
