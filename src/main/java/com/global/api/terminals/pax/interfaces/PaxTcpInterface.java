package com.global.api.terminals.pax.interfaces;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.messaging.IControlCodeReceivedInterface;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.AutoResetEvent;
import com.global.api.utils.MessageReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class PaxTcpInterface implements IDeviceCommInterface {
    private Socket client;
    private DataOutputStream out;
    private InputStream in;
    private ITerminalConfiguration settings;
    private AutoResetEvent await;
    private int nakCount = 0;

    private ControlCodes code;
    private byte[] messageReceived;

    private IMessageSentInterface onMessageSent;
    private IControlCodeReceivedInterface onControlCodeReceived;
    private IMessageReceivedInterface onMessageReceived;

    public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }

    public PaxTcpInterface(ITerminalConfiguration settings) {
        this.settings = settings;
        this.await = new AutoResetEvent(false);
    }

    public void connect() {}

    public void disconnect() {}

    public void _connect() {
        try{
            client = new Socket(settings.getIpAddress(), settings.getPort());

            if(client.isConnected()) {
                out = new DataOutputStream(client.getOutputStream());
                in = client.getInputStream();
                Thread thread = new Thread() {
                    public void run()  {
                        while(true){
                            try {
                                if(in.available() > 0) {
                                    byte[] buffer = new byte[4096];
                                    int bytesReceived = in.read(buffer);

                                    MessageReader queue = new MessageReader(buffer);
                                    while(queue.getLength() > 0) {
                                        ControlCodes rec_code = queue.readCode();
                                        if(rec_code == ControlCodes.ACK || rec_code == ControlCodes.NAK || rec_code == ControlCodes.EOT) {
                                            if(onControlCodeReceived != null)
                                                onControlCodeReceived.codeReceived(rec_code);
                                        }
                                        else if(rec_code == ControlCodes.STX) {
                                            byte[] rec_buffer = new byte[bytesReceived];
                                            rec_buffer[0] = rec_code.getByte();
                                            System.arraycopy(queue.readBytes(bytesReceived - 2), 0, rec_buffer, 1, bytesReceived -2);

                                            byte lrc = queue.readByte();
                                            if(lrc != TerminalUtilities.calculateLRC(rec_buffer))
                                                sendControlCode(ControlCodes.NAK);
                                            else {
                                                rec_buffer[bytesReceived - 1] = lrc;

                                                sendControlCode(ControlCodes.ACK);
                                                queue.purge();

                                                if(onMessageReceived != null)
                                                    onMessageReceived.messageReceived(rec_buffer);
                                            }
                                        }
                                    }
                                }

                                sleep(300);
                            } catch (Exception e) {
                                // This never needs to fail
                            }
                        }
                    }
                };
                thread.start();
            }
            else throw new IOException("Failed to connect.");
        } catch(IOException e) {
            // TODO: Handle this exception
        }
    }

    public void _disconnect() {
        try {
            if (!client.isClosed()) {
                in.close();
                out.close();
                client.close();
            }
        } catch(IOException e) {
            // Eating the close exception
        }
    }

    public byte[] send(IDeviceMessage message) throws MessageException {
        _connect();

        byte[] buffer = message.getSendBuffer();

        try {
            code = null;
            onControlCodeReceived = new IControlCodeReceivedInterface() {
                public void codeReceived(ControlCodes rec_code) {
                    code = rec_code;
                    onControlCodeReceived = null;
                    await.set();
                }
            };

            messageReceived = null;
            onMessageReceived = new IMessageReceivedInterface() {
                public void messageReceived(byte[] message) {
                    messageReceived = message;
                    onMessageReceived = null;
                    await.set();
                }
            };

            for (int i = 0; i < 3; i++) {
                if (onMessageSent != null)
                    onMessageSent.messageSent(message.toString());

                out.write(buffer);
                await.waitOne(1000);

                if(code == null)
                    throw new MessageException("Terminal did not respond in the given timeout.");

                if(code == ControlCodes.NAK) continue;
                else if (code == ControlCodes.EOT)
                    throw new MessageException("Terminal returned EOT for the current message.");
                else if (code == ControlCodes.ACK) {
                    await.waitOne(settings.getTimeout());
                    break;
                }
                else throw new MessageException(String.format("Unknown message received: %s", code.toString()));
            }
        } catch (Exception e){
            throw new MessageException("Failed to send message see inner exception for more details", e);
        }
        finally {
            _disconnect();
        }

        return messageReceived;
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
}