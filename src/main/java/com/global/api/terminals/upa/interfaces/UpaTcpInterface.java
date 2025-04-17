package com.global.api.terminals.upa.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.upa.Entities.Constants;
import com.global.api.utils.*;

public class UpaTcpInterface implements IDeviceCommInterface, IUPAMessage {
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private final ConnectionConfig settings;
    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;

    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public void setMessageReceivedHandler(IMessageReceivedInterface onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public UpaTcpInterface(ConnectionConfig settings) {
        this.settings = settings;
    }

    public void connect() {
        if (client == null) {
            try {
                client = new Socket(settings.getIpAddress(), settings.getPort());
                if (client.isConnected()) {
                    client.setSoTimeout(settings.getTimeout());
                    client.setKeepAlive(true);

                    out = new DataOutputStream(client.getOutputStream());
                    in = new DataInputStream(client.getInputStream());
                }
                else throw new IOException("Client failed to connect");
            }
            catch(IOException exc) {
                // eat connection exception
            }
        }
    }

    public void disconnect() {
        try {
            if (client != null && !client.isClosed()) {
                in.close();
                out.close();
                client.close();
            }
            client = null;
        } catch (IOException e) {
            // Eating the close exception
        }
    }

    public byte[] send(IDeviceMessage message) throws MessageException {
        connect();
        if (client == null) {
            throw new MessageException("Unable to connect with device.");
        }

        byte[] sendBuffer = message.getSendBuffer();
        try {
            // send the message out
            out.write(sendBuffer);
            out.flush();

            // log the message sent
            raiseOnMessageSent(new String(sendBuffer));

            // read the response
            boolean readyReceived = false;
            byte[] responseMessage = null;
            do {
                byte[] rvalue = getTerminalResponse();
                if (rvalue != null) {
                    // split the messages
                    List<byte[]> messages = splitMessage(rvalue);

                    // loop the messages
                    for(byte[] response : messages) {
                        raiseOnMessageReceived(response);

                        String messageType = getMessageType(response);
                        switch(messageType) {
                            case Constants.ACK_MESSAGE:
                            case Constants.NAK_MESSAGE:
                            case Constants.TIMEOUT_MESSAGE:
                                break;
                            case Constants.BUSY_MESSAGE:
                                throw new MessageException("Device is busy.");
                            case Constants.READY_MESSAGE: {
                                readyReceived = true;
                            } break;
                            case Constants.DATA_MESSAGE: {
                                // Set readyReceived for reboot messages
                                if(isRebootMessage(response)) {
                                    readyReceived = true;
                                }

                                // Send the ACK
                                sendControlCode(ControlCodes.ACK);

                                // Set response message to return
                                responseMessage = trimControlCodes(response);
                            } break;
                            default:
                                throw new MessageException(String.format("Unknown message value: %s", messageType));
                        }
                    }
                }
            }
            while(!readyReceived);

            return responseMessage;
        }
        catch (Exception exc) {
            throw new MessageException(exc.getMessage(), exc);
        }
        finally {
            disconnect();
        }
    }

    private void raiseOnMessageSent(String message) {
        try {
            if (onMessageSent != null) {
                onMessageSent.messageSent(message);
            }

            if (settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "REQUEST");
                msg.set("message", message);

                settings.getRequestLogger().RequestSent(msg.toString());
            }
        }
        catch(IOException exc) {
            /* Logging should never interfere with processing */
        }
    }

    private void raiseOnMessageReceived(byte[] message) {
        try {
            if(onMessageReceived != null) {
                onMessageReceived.messageReceived(message);
            }

            if(settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "RESPONSE");
                msg.set("message", new String(message));

                settings.getRequestLogger().ResponseReceived(msg.toString());
            }
        }
        catch(IOException exc) {
            /* NOM NOM */
        }
    }

    private byte[] getTerminalResponse() throws MessageException {
        byte[] buffer = new byte[65536];

        try {
            int bytesReceived = awaitResponse(in, buffer);
            if (bytesReceived > 0) {
                byte[] rec_buffer = new byte[bytesReceived];
                System.arraycopy(buffer, 0, rec_buffer, 0, bytesReceived);

                ControlCodes code = EnumUtils.parse(ControlCodes.class, rec_buffer[0]);
                if (code.equals(ControlCodes.STX)) {
                    return rec_buffer;
                }
                else throw new MessageException(String.format("Unknown message received: %s", code));
            }
            return null;
        }
        catch(IOException exc) {
            return null;
        }
    }

    private int awaitResponse(DataInputStream in, byte[] buffer) throws IOException {
        long t = System.currentTimeMillis();
        int index = 0;
        do {
            if (in.available() > 0) {
                index += in.read(buffer, index, 1460);

                // check we have a complete message
                if(buffer[index - 2] == ControlCodes.ETX.getByte()) {
                    return index;
                }
            }
        }
        while (System.currentTimeMillis() - t < settings.getTimeout());
        throw new IOException("Terminal did not respond in the given timeout");
    }

    private List<byte[]> splitMessage(byte[] buffer) throws MessageException {
        List<byte[]> rvalue = new ArrayList<>();

        MessageReader mr = new MessageReader(buffer);
        do {
            MessageWriter mw = new MessageWriter();

            byte stx = mr.readByte();
            if (stx == ControlCodes.STX.getByte()) {
                mw.add(stx); // STX
                mw.add(mr.readByte()); // should be the LF following the STX

                // read to the following ETX
                byte[] message = mr.readBytesToCode(ControlCodes.ETX, false);
                mw.addRange(message);

                // read the ETX, and LF
                byte etx = mr.readByte(); // should be the ETX
                if (etx == ControlCodes.ETX.getByte()) {
                    mw.add(etx); // ETX
                    mw.add(mr.readByte()); // should be the LF following the STX

                    rvalue.add(mw.toArray());
                }
                else {
                    throw new MessageException("Invalid message format: Message doesn't end with ETX");
                }
            }
            else {
                throw new MessageException("Invalid message format: Message doesn't begin with STX");
            }
        }
        while(mr.canRead());

        return rvalue;
    }

    private String getMessageType(byte[] buffer) {
        byte[] messageBytes = trimControlCodes(buffer);
        JsonDoc message = JsonDoc.parse(new String(messageBytes));
        return message.getString(Constants.COMMAND_MESSAGE);
    }

    private byte[] trimControlCodes(byte[] buffer) {
        MessageWriter mw = new MessageWriter();
        for(byte b :  buffer) {
            ControlCodes code = EnumUtils.parse(ControlCodes.class, b);
            if(code == null || code == ControlCodes.COLON || code == ControlCodes.COMMA) {
                mw.add(b);
            }
        }
        return mw.toArray();
    }

    private boolean isRebootMessage(byte[] buffer) {
        byte[] rec_buffer = trimControlCodes(buffer);

        try {
            JsonDoc response = JsonDoc.parse(new String(rec_buffer));
            String messageType = response.get(Constants.COMMAND_DATA).getString(Constants.COMMAND_USED);
            return messageType.equals(Constants.REBOOT);
        }
        catch(Exception exc) {
            return false;
        }
    }

    private void sendControlCode(ControlCodes code) throws MessageException {
        // remove the brackets from the code
        String value = code.toString().replaceAll("[\\[\\]]", "");

        // Build the message
        JsonDoc json = new JsonDoc();
        json.set(Constants.COMMAND_DATA, "", true);
        json.set(Constants.COMMAND_MESSAGE, value);
        String body = json.toString();

        // Send the message
        try {
            byte[] sendBuffer = TerminalUtilities.compileMessage(body).getSendBuffer();
            out.write(sendBuffer);
            out.flush();
        }
        catch(IOException exc) {
            // Wrap the IO Exception
            throw new MessageException(exc.getMessage(), exc);
        }
        finally {
            // Report
            raiseOnMessageSent(body);
        }
    }
}
