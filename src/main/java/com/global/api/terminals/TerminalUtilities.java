package com.global.api.terminals;

import com.global.api.entities.enums.*;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.Element;
import com.global.api.utils.IRawRequestBuilder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MessageWriter;

public class TerminalUtilities {
    private static final String version = "1.35";

    public static String getElementString(Object... elements) {
        StringBuilder sb = new StringBuilder();
        for(Object element: elements){
            if(element instanceof ControlCodes) {
                sb.append((char) ((ControlCodes) element).getByte());
            }
            else if(element instanceof IRequestSubGroup) {
                sb.append(((IRequestSubGroup) element).getElementString());
            }
            else if(element instanceof String[]) {
                for(String sub_element: (String[])element){
                    sb.append(ControlCodes.FS.getByte());
                    sb.append(sub_element);
                }
            }
            else if(element instanceof IStringConstant) {
                sb.append(((IStringConstant) element).getValue());
            }
            else if(element instanceof IByteConstant) {
                sb.append(((IByteConstant) element).getByte());
            }
            else sb.append(element);
        }

        return sb.toString();
    }

    private static DeviceMessage buildMessage(PaxMsgId messageId, String message){
        MessageWriter buffer = new MessageWriter();

        // Begin Message
        buffer.add(ControlCodes.STX);

        // Add Message Id
        buffer.add(messageId);
        buffer.add(ControlCodes.FS);

        // Add Version
        buffer.addRange(version.getBytes());
        buffer.add(ControlCodes.FS);

        // Add Message
        buffer.addRange(message.getBytes());

        // End the message
        buffer.add(ControlCodes.ETX);

        byte lrc = calculateLRC(buffer.toArray());
        buffer.add(lrc);

        return new DeviceMessage(buffer.toArray());
    }

    public static DeviceMessage buildMessage(String message) {
        JsonDoc json = new JsonDoc();
        json.set("message", message);
        json.set("data", "", true);

        return compileMessage(json.toString());
    }

    public static DeviceMessage buildMessage(UpaMessageId messageType, String requestId, JsonDoc body) {

        JsonDoc data = new JsonDoc();
        JsonDoc json = new JsonDoc();

        data.set("EcrId", "13");
        data.set("requestId", requestId);
        data.set("command", messageType.toString());

        if (body != null) {
            data.set("data", body);
        }

        json.set("data", data);
        json.set("message", "MSG");

        return buildMessage(json);
    }

    public static <T extends IRawRequestBuilder> DeviceMessage buildMessage(T doc) {

        IRawRequestBuilder iRawRequestBuilder = doc;
        byte[] buffer;
        if (doc instanceof JsonDoc) {
            buffer = compileRawMessage(((JsonDoc) iRawRequestBuilder).toString());
        } else {
            buffer = compileRawMessage(((Element) iRawRequestBuilder).toString());
        }

        return new DeviceMessageWithDocument(iRawRequestBuilder, buffer);
    }

    public static DeviceMessage buildRequest(String message, MessageFormat format) {
        MessageWriter buffer = new MessageWriter();

        // beginning sentinel
        if(format.equals(MessageFormat.Visa2nd))
            buffer.add(ControlCodes.STX);
        else {
            buffer.add((byte)(message.length() >>> 8));
            buffer.add((byte)message.length());
        }

        // put message
        buffer.addRange(message.getBytes());

        // ending sentinel
        if(format.equals(MessageFormat.Visa2nd)) {
            buffer.add(ControlCodes.ETX);

            byte lrc = calculateLRC(buffer.toArray());
            buffer.add(lrc);
        }

        return new DeviceMessage(buffer.toArray());
    }

    public static DeviceMessage buildRequest(PaxMsgId messageId, Object... elements){
        String message = getElementString(elements);
        return buildMessage(messageId, message);
    }

    public static DeviceMessage buildRequest(byte[] message) {
        MessageWriter buffer = new MessageWriter();

        // beginning sentinel
        buffer.add(ControlCodes.STX);

        // put message
        buffer.addRange(message);

        // ending sentinel
        buffer.add(ControlCodes.ETX);

        byte lrc = calculateLRC(buffer.toArray());
        buffer.add(lrc);

        return new DeviceMessage(buffer.toArray());
    }

    public static byte calculateLRC(byte[] buffer) {
        int length = buffer.length;
        if(buffer[buffer.length - 1] != ControlCodes.ETX.getByte()) {
            length--;
        }

        byte lrc = (byte)0x00;
        for (int i = 1; i < length; i++) {
            lrc = (byte) (lrc ^ buffer[i]);
        }
        return lrc;
    }

    public static boolean checkLRC(String message) {
        byte[] messageBuffer = message.getBytes();

        byte expected = messageBuffer[messageBuffer.length - 1];
        byte actual = calculateLRC(message.substring(0, message.length() - 1).getBytes());

        return (expected == actual);
    }

    public static DeviceMessage compileMessage(String body) {

        byte[] range = compileRawMessage(body);

        DeviceMessage deviceMessage = new DeviceMessage(range);
        return deviceMessage;
    }

    private static byte[] compileRawMessage(String body) {
        MessageWriter buffer = new MessageWriter();

        buffer.add(ControlCodes.STX.getByte());
        buffer.add(ControlCodes.LF.getByte());
        buffer.addRange(body.getBytes());
        buffer.add(ControlCodes.LF.getByte());
        buffer.add(ControlCodes.ETX.getByte());
        buffer.add(ControlCodes.LF.getByte());

        return buffer.toArray();
    }
}
