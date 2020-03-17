package com.global.api.gateways;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.gateways.events.*;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;

public class NetworkGateway {
    private SSLSocket client;
    private DataOutputStream out;
    private InputStream in;
    private int connectionFaults = 0;

    private String primaryEndpoint;
    private Integer primaryPort;
    private String secondaryEndpoint;
    private Integer secondaryPort;

    String currentEndpoint;

    private boolean enableLogging = false;
    private boolean forceGatewayTimeout = false;
    private int timeout;

    private String connectorName = "NetworkGateway";
    private IGatewayEventHandler gatewayEventHandler;

    public String getPrimaryEndpoint() {
        return primaryEndpoint;
    }
    public void setPrimaryEndpoint(String primaryEndpoint) {
        this.primaryEndpoint = primaryEndpoint;
    }
    public Integer getPrimaryPort() {
        return primaryPort;
    }
    public void setPrimaryPort(Integer primaryPort) {
        this.primaryPort = primaryPort;
    }
    public String getSecondaryEndpoint() {
        return secondaryEndpoint;
    }
    public void setSecondaryEndpoint(String secondaryEndpoint) {
        this.secondaryEndpoint = secondaryEndpoint;
    }
    public Integer getSecondaryPort() {
        return secondaryPort;
    }
    public void setSecondaryPort(Integer secondaryPort) {
        this.secondaryPort = secondaryPort;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    boolean isEnableLogging() {
        return enableLogging;
    }
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    private boolean isForceGatewayTimeout() {
        return forceGatewayTimeout;
    }
    public void setForceGatewayTimeout(boolean forceGatewayTimeout) {
        this.forceGatewayTimeout = forceGatewayTimeout;
    }
    public void setGatewayEventHandler(IGatewayEventHandler eventHandler) { this.gatewayEventHandler = eventHandler; }

    // establish connection
    private void connect(String endpoint, Integer port) throws ApiException {
        currentEndpoint = endpoint.equals(primaryEndpoint) ? "primary" : "secondary";

        // create the connection event
        ConnectionEvent connectionEvent = new ConnectionEvent(connectorName);
        connectionEvent.setEndpoint(endpoint);
        connectionEvent.setPort(port.toString());
        connectionEvent.setHost(currentEndpoint);
        connectionEvent.setConnectionAttempts(connectionFaults);
        raiseGatewayEvent(connectionEvent);

        DateTime connectionStarted = DateTime.now(DateTimeZone.UTC);
        if(client == null) {
            try {
                // connection started
                connectionEvent.setConnectionStarted(connectionStarted);

                try {
                    SSLSocketFactory factory = new SSLSocketFactoryEx();
                    client = (SSLSocket) factory.createSocket(endpoint, port);
                    client.startHandshake();

                    raiseGatewayEvent(new SslHandshakeEvent(connectorName, null));
                }
                catch(Exception exc) {
                    raiseGatewayEvent(new SslHandshakeEvent(connectorName, exc));
                }

                if(client != null && client.isConnected()) {
                    // connection completed
                    raiseGatewayEvent(new ConnectionCompleteEvent(connectorName, connectionStarted, DateTime.now(DateTimeZone.UTC)));

                    out = new DataOutputStream(client.getOutputStream());
                    in = client.getInputStream();
                    client.setKeepAlive(true);
                    connectionFaults = 0;
                }
                else {
                    // connection fail over
                    raiseGatewayEvent(new FailOverEvent(connectorName, connectionStarted, DateTime.now(DateTimeZone.UTC)));

                    if(connectionFaults++ != 3) {
                        if(endpoint.equals(primaryEndpoint) && secondaryEndpoint != null) {
                            connect(secondaryEndpoint, secondaryPort);
                        }
                        else {
                            connect(primaryEndpoint, primaryPort);
                        }
                    }
                    else {
                        throw new IOException("Failed to connect to primary or secondary processing endpoints.");
                    }
                }
            }
            catch(Exception exc) {
                throw new GatewayException(exc.getMessage(), exc);
            }
        }
    }

    // close connection
    private void disconnect() {
        try {
            if (client != null && !client.isClosed()) {
                in.close();
                out.close();
                client.close();
            }
            client = null;
        }
        catch(IOException exc) {
            // eat the close exception
        }
    }

    public byte[] send(IDeviceMessage message) throws ApiException {
        boolean timedOut = false;
        connect(getPrimaryEndpoint(), getPrimaryPort());

        byte[] buffer = message.getSendBuffer();
        try {
            for(int i = 0; i < 2; i++) {
                raiseGatewayEvent(new RequestSentEvent(connectorName));
                DateTime requestSent = DateTime.now(DateTimeZone.UTC);
                out.write(buffer);

                byte[] rvalue = getGatewayResponse();
                if (rvalue != null && !isForceGatewayTimeout()) {
                    raiseGatewayEvent(new ResponseReceivedEvent(connectorName, requestSent));
                    return rvalue;
                }

                // did not get a response, switch endpoints and try again
                timedOut = true;
                if(!currentEndpoint.equals("secondary") && !StringUtils.isNullOrEmpty(secondaryEndpoint) && i < 1) {
                    raiseGatewayEvent(new TimeoutEvent(connectorName, GatewayEventType.TimeoutFailOver));

                    disconnect();
                    connect(getSecondaryEndpoint(), getSecondaryPort());
                }
            }

            raiseGatewayEvent(new TimeoutEvent(connectorName, GatewayEventType.Timeout));
            throw new GatewayTimeoutException();
        }
        catch(GatewayTimeoutException exc) {
            throw exc;
        }
        catch(Exception exc) {
            if(timedOut) {
                throw new GatewayTimeoutException(exc);
            }
            else {
                throw new GatewayException(exc.getMessage(), exc);
            }
        }
        finally {
            disconnect();
            raiseGatewayEvent(new DisconnectEvent(connectorName));

            // remove the force timeout
            if(isForceGatewayTimeout()) {
                setForceGatewayTimeout(false);
            }
        }
    }

    private byte[] getGatewayResponse() throws IOException, GatewayTimeoutException {
        byte[] buffer = new byte[2048];
        int bytesReceived = awaitResponse(in, buffer);

        if(bytesReceived > 0) {
            byte[] rec_buffer = new byte[bytesReceived];
            System.arraycopy(buffer, 0, rec_buffer, 0, bytesReceived);
            return rec_buffer;
        }

        return null;
    }

    private int awaitResponse(InputStream in, byte[] buffer) throws GatewayTimeoutException, IOException {
        long t = System.currentTimeMillis();

        int position = 0;
        Integer messageLength = null;
        do {
            if(messageLength == null) {
                byte[] lengthBuffer = new byte[2];
                int length = in.read(lengthBuffer, 0, 2);
                if(length == 2) {
                    messageLength = new BigInteger(lengthBuffer).intValue() - 2;
                }
            }

            if(messageLength != null) {
                int currLength = in.read(buffer, position, messageLength);
                if (currLength == messageLength) {
                    return messageLength;
                }
                else {
                    position += currLength;
                }
            }
        }
        while((System.currentTimeMillis() - t) <= 20000);

        throw new GatewayTimeoutException();
    }

    private void raiseGatewayEvent(final IGatewayEvent event) {
        new Thread(new Runnable() {
            public void run() {
                if(gatewayEventHandler != null) {
                    gatewayEventHandler.eventRaised(event);
                }
            }
        }).start();
    }
}
