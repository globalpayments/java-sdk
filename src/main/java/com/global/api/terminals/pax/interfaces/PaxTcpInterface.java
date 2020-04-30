package com.global.api.terminals.pax.interfaces;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.EnumUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class PaxTcpInterface implements IDeviceCommInterface {
    private Socket client;
    private DataOutputStream out;
    private InputStream in;
    private ITerminalConfiguration settings;
    private int nakCount = 0;


    private IMessageSentInterface onMessageSent;

    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }
    
	public void setBroadcastMessageHandler(IBroadcastMessageInterface broadcastInterface) {
        // not required for this connection mode
	}

    public PaxTcpInterface(ITerminalConfiguration settings) {
        this.settings = settings;
    }

    public void connect() {
        if(client == null) {
            try {
                client = new Socket(settings.getIpAddress(), Integer.parseInt(settings.getPort()));
                if(client.isConnected()) {
                    out = new DataOutputStream(client.getOutputStream());
                    in = client.getInputStream();
                    client.setKeepAlive(true);
                }
                else throw new IOException("Client failed to connect");
            }
            catch(IOException exc) {
                //TODO: Handle this exception
            }
        }
    }

    public void disconnect() {
        try {
            if (!client.isClosed()) {
                in.close();
                out.close();
                client.close();
            }
            client = null;
        } catch(IOException e) {
            // Eating the close exception
        }
    }

    public byte[] send(IDeviceMessage message) throws MessageException {
        connect();

        byte[] buffer = message.getSendBuffer();
        try {
            if(onMessageSent != null)
                onMessageSent.messageSent(message.toString());

            for(int i = 0; i < 3; i++) {
                out.write(buffer);

                byte[] rvalue = getTerminalResponse();
                if (rvalue != null) {
                    byte lrc = rvalue[rvalue.length - 1]; // should be the lrc
                    if (lrc != TerminalUtilities.calculateLRC(rvalue))
                        sendControlCode(ControlCodes.NAK);
                    else {
                        sendControlCode(ControlCodes.ACK);
                        return rvalue;
                    }
                }
            }
            throw new MessageException("Terminal did not respond in the given timeout.");
        }
        catch(Exception exc) {
            throw new MessageException(exc.getMessage(), exc);
        }
        finally {
            disconnect();
        }
    }

    private void sendControlCode(ControlCodes code) throws MessageException {
        try {
            if (code != ControlCodes.NAK) {
                nakCount = 0;
                out.write((int) code.getByte());
            } else if (++nakCount == 3) {
                sendControlCode(ControlCodes.EOT);
            }
        } catch(IOException e) {
            throw new MessageException("Failed to send control code.");
        }
    }

    private byte[] getTerminalResponse() throws MessageException {
        try {
            byte[] buffer = new byte[4096];
            int bytesReceived = awaitResponse(in, buffer);

            if(bytesReceived > 0) {
                byte[] rec_buffer = new byte[bytesReceived];
                System.arraycopy(buffer, 0, rec_buffer, 0, bytesReceived);

                ControlCodes code = EnumUtils.parse(ControlCodes.class, rec_buffer[0]); // queue.readCode();
                if (code.equals(ControlCodes.NAK))
                    return null;
                else if (code.equals(ControlCodes.EOT))
                    throw new MessageException("Terminal returned EOT for the current message");
                else if (code.equals(ControlCodes.ACK))
                    return getTerminalResponse();
                else if (code.equals(ControlCodes.STX)) {
                    return rec_buffer;
                } else throw new MessageException(String.format("Unknown message received: %s", code));
            }

            return null;
        }
        catch(IOException exc) {
            return null;
        }
    }

    private int awaitResponse(InputStream in, byte[] buffer) throws IOException {
        long t = System.currentTimeMillis();
        do {
            if(in.available() > 0) {
                return in.read(buffer);
            }

            if (System.currentTimeMillis() - t >= settings.getTimeout())
                break;
        }
        while(true);
        throw new IOException("Terminal did not respond in the given timeout");
    }
}