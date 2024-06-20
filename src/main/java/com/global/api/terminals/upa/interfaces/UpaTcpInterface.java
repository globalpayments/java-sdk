package com.global.api.terminals.upa.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.IUPAMessage;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.upa.Entities.Constants;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.StringUtils;
import org.joda.time.LocalTime;

public class UpaTcpInterface implements IDeviceCommInterface, IUPAMessage {
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private final ConnectionConfig settings;
    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;
    private MessageWriter data;
    private String responseMessageString;
    private boolean readyReceived;

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
                    out = new DataOutputStream(client.getOutputStream());
                    in = new DataInputStream(client.getInputStream());
                    client.setKeepAlive(true);
                    client.setSoTimeout(settings.getTimeout());
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
            if (!client.isClosed()) {
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

        readyReceived = false;
        byte[] sendBuffer = message.getSendBuffer();

        try {
            if (onMessageSent != null) {
                long currentMillis = System.currentTimeMillis();
                Timestamp t = new Timestamp(currentMillis);
                onMessageSent.messageSent(t + ":\n" + new String(sendBuffer, StandardCharsets.UTF_8));
            }

            if (settings.getRequestLogger() != null) {
                String formMsg = new String(sendBuffer, StandardCharsets.UTF_8);
                settings.getRequestLogger().RequestSent(formMsg);
            }

            out.write(sendBuffer);
            out.flush();
            long timeOfSend = System.currentTimeMillis();

            do {
                getTerminalResponse();

                if (System.currentTimeMillis() > timeOfSend + settings.getTimeout()) {
                    throw new TimeoutException("Terminal did not respond in the given timeout.");
                }

                Thread.sleep(100);
            } while (!readyReceived);

            //This check is put in place for UPA message "READY".
            if (getMessageType(message).equalsIgnoreCase(Constants.READY_MESSAGE) &&
                    StringUtils.isNullOrEmpty(responseMessageString)) {
                return readyMessageSent();
            } else {
                if(onMessageReceived != null){
                    onMessageReceived.messageReceived(responseMessageString.getBytes());
                }
                return responseMessageString.getBytes();
            }
        } catch (Exception exc) {
            throw new MessageException(exc.getMessage(), exc);
        }
        finally {
            if (client != null) disconnect();
            try {
                // a little padding here
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMessageType(IDeviceMessage message) {
        String messageType = "NONE";
        try {
            if (message != null) {
                String formMsg = new String(message.getSendBuffer(), StandardCharsets.UTF_8)
                        .trim();
                JsonDoc responseObj = JsonDoc.parse(
                        new String(formMsg.getBytes(), StandardCharsets.UTF_8)
                );
                messageType = responseObj.getString("message");
                String data = responseObj.getString("data");
                if (StringUtils.isNullOrEmpty(data)) {
                    responseMessageString = "";
                }
            }
        } catch (Exception e) {
            return messageType;
        }
        return messageType;
    }

    private void getTerminalResponse() throws Exception {
        try {
            validateResponsePacket();
            byte[] buffer = data.toArray();

            if (buffer.length > 0) {
                JsonDoc responseObj = JsonDoc.parse(
                        new String(data.toArray(), StandardCharsets.UTF_8)
                );

                String message = responseObj.getString("message");

                if (settings.getRequestLogger() != null) {
                    String formMsg = new String(buffer, StandardCharsets.UTF_8);
                    settings.getRequestLogger().ResponseReceived(formMsg);
                }

                switch (message) {
                    case Constants.ACK_MESSAGE:
                    case Constants.NAK_MESSAGE:
                    case Constants.TIMEOUT_MESSAGE:
                        break;
                    case Constants.BUSY_MESSAGE:
                        throw new Exception("Device is busy");
                    case Constants.DATA_MESSAGE:
                        responseMessageString = new String(buffer, StandardCharsets.UTF_8);
                        String eval = responseObj.get("data").getString("response");
                        if (eval.equals("Reboot")) {
                            readyReceived = true; // since reboot doesn't return READY
                        }
                        sendAckMessageToDevice();
                        break;
                    case Constants.READY_MESSAGE:
                        readyReceived = true;
                        break;
                    default:
                        throw new Exception("Message field value is unknown in API Response.");
                }
            }
        } catch (IOException exc) {
            throw new IOException(exc.getMessage(), exc);
        }
    }

    private void validateResponsePacket() throws IOException {
        try {
            byte[] buffer;
            List<Byte> receive = new ArrayList<>();
            data = new MessageWriter();

            byte stx = ControlCodes.STX.getByte();
            byte etx = ControlCodes.ETX.getByte();
            byte lf = ControlCodes.LF.getByte();

            do {
                receive.add(in.readByte());
            } while (in.available() > 0);

            buffer = new byte[receive.size()];

            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = receive.get(i);
            }

            for (int i = 0; i < buffer.length; i++) {
                if (i < 2) {
                    if (buffer[i] != stx && buffer[i + 1] != lf) {
                        throw new IOException("The bytes of the start response packet are not the expected bytes.");
                    }

                    i += 1;
                    continue;
                }

                if (buffer[i] == etx) {
                    if ((buffer[i - 1] & buffer[i + 1]) != lf) {
                        throw new IOException("The bytes of the end response packet are not the expected bytes.");
                    }

                    break;
                } else if (buffer[i] != lf) {
                    data.add(buffer[i]);
                }
            }
        } catch (SocketTimeoutException e) {
            client.setSoTimeout(0);
        }
    }

    private void sendAckMessageToDevice() throws IOException {
        JsonDoc json = new JsonDoc();
        json.set("data", "", true);
        json.set("message", "ACK");
        String body = json.toString();
        IDeviceMessage message = TerminalUtilities.compileMessage(body);
        byte[] sendBuffer = message.getSendBuffer();

        if (settings.getRequestLogger() != null) {
            String formMsg = new String(sendBuffer, StandardCharsets.UTF_8);
            settings.getRequestLogger().RequestSent(formMsg);
        }

        try {
            if (onMessageSent != null) {
                long currentMillis = System.currentTimeMillis();
                Timestamp t = new Timestamp(currentMillis);
                onMessageSent.messageSent(t + ":\n" + new String(sendBuffer, StandardCharsets.UTF_8));
            }

            out.write(sendBuffer);
            out.flush();
        } catch (IOException exc) {
            throw new IOException(exc.getMessage(), exc);
        }
    }

    private byte[] readyMessageSent() {
        if (onMessageSent != null) {
            JsonDoc jsonMsg = new JsonDoc();
            jsonMsg.set("message", "READY");
            long currentMillis = System.currentTimeMillis();
            Timestamp t = new Timestamp(currentMillis);
            onMessageSent.messageSent(t + ":\n" + jsonMsg.toString());
        }
        return Constants.READY_MESSAGE.getBytes();
    }
}
