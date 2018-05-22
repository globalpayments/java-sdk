package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.pax.subgroups.*;
import com.global.api.utils.MessageReader;

public class CreditResponse extends PaxDeviceResponse {
    public CreditResponse(byte[] buffer) throws MessageException {
        super(buffer, PaxMsgId.T01_RSP_DO_CREDIT);
    }

    @Override
    protected void parseResponse(MessageReader br) throws MessageException {
        super.parseResponse(br);

        if (deviceResponseCode.equals("000000")) {
            hostResponse = new HostResponse(br);
            transactionType = br.readToCode(ControlCodes.FS);
            amountResponse = new AmountResponse(br);
            accountResponse = new AccountResponse(br);
            traceResponse = new TraceResponse(br);
            avsResponse = new AvsResponse(br);
            commercialResponse = new CommercialResponse(br);
            ecomResponse = new EcomSubGroup(br);
            extDataResponse = new ExtDataSubGroup(br);

            mapResponse();
        }
    }
}