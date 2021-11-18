package com.global.api.terminals.upa.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.upa.Entities.Constants;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MessageWriter;

public class UpaTcpInterface implements IDeviceCommInterface {
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private ITerminalConfiguration settings;
    private IMessageSentInterface onMessageSent;
    private MessageWriter data;
    private String responseMessageString;
    private byte[] buffer;
    private boolean readyReceived;

    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public UpaTcpInterface(ITerminalConfiguration settings) {
        this.settings = settings;
    }

    public void connect() {
        if(client == null) {
            try {
                client = new Socket(settings.getIpAddress(), settings.getPort());
                if(client.isConnected()) {
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

        } catch(IOException e) {
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
            if(onMessageSent != null)
                onMessageSent.messageSent(message.toString());

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

            return responseMessageString.getBytes();
        }
        catch(Exception exc) {
            throw new MessageException(exc.getMessage(), exc);
        }
        finally {
            disconnect();
            try {
                // a little padding here
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getTerminalResponse() throws Exception {
        Timestamp timestamp;

        try {
            validateResponsePacket();
            buffer = data.toArray();

            if(buffer.length > 0) {
                JsonDoc responseObj = JsonDoc.parse(
                    new String(data.toArray(), StandardCharsets.UTF_8)
                );

                timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println(timestamp.toString() + " Received: " + responseObj);
                System.out.println("");

                String message = responseObj.getString("message");

                switch (message) {
                    case Constants.ACK_MESSAGE:
                        break;
                    case Constants.NAK_MESSAGE:
                        break;
                    case Constants.READY_MESSAGE:
                        readyReceived = true;                                     
                        break;
                    case Constants.BUSY_MESSAGE:
                        break;
                    case Constants.TIMEOUT_MESSAGE:
                        break;
                    case Constants.DATA_MESSAGE:
                        responseMessageString = new String(buffer, StandardCharsets.UTF_8);
                        String eval = responseObj.get("data").getString("response");
                        if (eval.equals("Reboot")) {
                            readyReceived = true; // since reboot doesn't return READY
                        }
                        sendAckMessageToDevice();
                        break;
                    default:
                        throw new Exception("Message field value is unknown in API Response.");
                }
            }
        }
        catch(IOException exc) {
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
                        throw new IOException("The bytes of the start response packet is not the expected bytes.");
                    }

                    i += 1;
                    continue;
                }

                if (buffer[i] == etx) {
                    if ((buffer[i - 1] & buffer[i + 1]) != lf) {
                        throw new IOException("The bytes of the end response packet is not the expected bytes.");
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

        try {
            if(onMessageSent != null)
                onMessageSent.messageSent(message.toString());

            out.write(sendBuffer);
            out.flush();
        } catch(IOException exc) {
            throw new IOException(exc.getMessage(), exc);
        }
    }
}
