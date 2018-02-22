package com.global.api.terminals;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.MessageFormat;
import com.global.api.entities.enums.PaxMsgId;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.MessageWriter;

public class TerminalUtilities {
    private static final String version = "1.35";

    private static String getElementString(Object[] elements) {
        StringBuilder sb = new StringBuilder();
        for(Object element: elements){
            if(element instanceof ControlCodes)
                sb.append((char)((ControlCodes) element).getByte());
            else if(element instanceof IRequestSubGroup)
                sb.append(((IRequestSubGroup) element).getElementString());
            else if(element instanceof String[]){
                for(String sub_element: (String[])element){
                    sb.append(ControlCodes.FS.getByte());
                    sb.append(sub_element);
                }
            }
            else if(element instanceof IStringConstant)
                sb.append(((IStringConstant) element).getValue());
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

    public static byte calculateLRC(byte[] buffer) {
        int length = buffer.length;
        if(buffer[buffer.length - 1] != ControlCodes.ETX.getByte())
            length--;

        byte lrc = (byte)0x00;
        for (int i = 1; i < length; i++)
            lrc = (byte)(lrc ^ buffer[i]);
        return lrc;
    }
}
