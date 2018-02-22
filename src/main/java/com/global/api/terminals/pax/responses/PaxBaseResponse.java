package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.pax.subgroups.*;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.MessageReader;

import java.util.Arrays;

public class PaxBaseResponse extends TerminalResponse {
    protected PaxMsgId[] messageIds;
    protected byte[] buffer;

    protected HostResponse hostResponse;
    protected AmountResponse amountResponse;
    protected AccountResponse accountResponse;
    protected TraceResponse traceResponse;
    protected AvsResponse avsResponse;
    protected CommercialResponse commercialResponse;
    protected EcomSubGroup ecomResponse;
    protected ExtDataSubGroup extDataResponse;
    protected CheckSubGroup checkSubResponse;

    public PaxBaseResponse(byte[] buffer, PaxMsgId... messageIds) throws MessageException {
        this.messageIds = messageIds;
        this.buffer = buffer;

        this.parseResponse(new MessageReader(buffer));
    }

    protected void parseResponse(MessageReader mr) throws MessageException {
        ControlCodes code = mr.readCode();  //STX
        setStatus(mr.readToCode(ControlCodes.FS));
        setCommand(mr.readToCode(ControlCodes.FS));
        setVersion(mr.readToCode(ControlCodes.FS));
        setDeviceResponseCode(mr.readToCode(ControlCodes.FS));
        setDeviceResponseText(mr.readToCode(ControlCodes.FS));

        PaxMsgId msgId = EnumUtils.parse(PaxMsgId.class, command);
        if(!Arrays.asList(messageIds).contains(msgId))
            throw new MessageException(String.format("Unexpected message type received: %s", command));
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(byte b : buffer){
            if(EnumUtils.isDefined(ControlCodes.class, b)){
                ControlCodes code = EnumUtils.parse(ControlCodes.class, b);
                sb.append(code.toString());
            }
            else sb.append((char)b);
        }

        return sb.toString();
    }
}
