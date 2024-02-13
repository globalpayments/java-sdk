package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.pax.subgroups.*;
import com.global.api.utils.MessageReader;

public class LocalDetailReportResponse  extends PaxDeviceResponse {

    private String totalRecords;
    private String recordNumber;
    private String edcType;
    private CashierSubGroup cashierSubGroup;


    public LocalDetailReportResponse(byte[] buffer) throws MessageException{
        super(buffer,PaxMsgId.R03_RSP_LOCAL_DETAIL_REPORT);
    }

    @Override
    protected void parseResponse(MessageReader br) throws MessageException {
        super.parseResponse(br);

        if (deviceResponseCode.equals("000000")) {
            hostResponse = new HostResponse(br);
            totalRecords = br.readToCode(ControlCodes.FS);
            recordNumber = br.readToCode(ControlCodes.FS);
            edcType = br.readToCode(ControlCodes.FS);
            transactionType = br.readToCode(ControlCodes.FS);
            originalTransactionType = br.readToCode(ControlCodes.FS);
            amountResponse = new AmountResponse(br);
            accountResponse = new AccountResponse(br);
            traceResponse = new TraceResponse(br);
            cashierSubGroup = new CashierSubGroup(br);
            commercialResponse = new CommercialResponse(br);
            checkSubResponse = new CheckSubGroup(br);

            mapResponse();
        }
    }


}
