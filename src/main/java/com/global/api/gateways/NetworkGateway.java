package com.global.api.gateways;

import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.GatewayComsException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.gateways.events.*;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import com.global.api.utils.GnapUtils;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkGateway {
    private SSLSocket client;
    private DataOutputStream out;
    private InputStream in;
    private int connectionFaults = 0;

    private String primaryEndpoint;
    private Integer primaryPort;
    private String secondaryEndpoint;
    private Integer secondaryPort;

    protected Host currentHost;

    private boolean enableLogging = false;
    private HashMap<Host, ArrayList<HostError>> simulatedHostErrors;
    private int timeout;

    private String connectorName = "NetworkGateway";
    private IGatewayEventHandler gatewayEventHandler;
    @Getter @Setter
    private Target target;

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
    protected boolean isEnableLogging() {
        return enableLogging;
    }
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        if(target.equals(Target.GNAP))
        {
            GnapUtils.enableLogging(enableLogging);
        }else if(target.equals(Target.NTS)){
            NtsUtils.enableLogging();
        }
    }
    public void setGatewayEventHandler(IGatewayEventHandler eventHandler) { this.gatewayEventHandler = eventHandler; }
    public HashMap<Host, ArrayList<HostError>> getSimulatedHostErrors() {
        return simulatedHostErrors;
    }

    public void setSimulatedHostErrors(HashMap<Host, ArrayList<HostError>> simulatedHostErrors) {
        this.simulatedHostErrors = simulatedHostErrors;
    }
    private boolean isForcedError(HostError error) {
        if(simulatedHostErrors != null && simulatedHostErrors.containsKey(currentHost)) {
            return simulatedHostErrors.get(currentHost).contains(error);
        }
        return false;
    }

    // establish connection
    private void connect(String endpoint, Integer port) throws GatewayComsException {
        currentHost = endpoint.equals(primaryEndpoint) ? Host.Primary : Host.Secondary;

        // create the connection event
        ConnectionEvent connectionEvent = new ConnectionEvent(connectorName);
        connectionEvent.setEndpoint(endpoint);
        connectionEvent.setPort(port.toString());
        connectionEvent.setHost(currentHost.getValue());
        connectionEvent.setConnectionAttempts(connectionFaults);
        raiseGatewayEvent(connectionEvent);

        DateTime connectionStarted = DateTime.now(DateTimeZone.UTC);
        if(client == null || out == null || in == null || !client.isConnected()) {
            if(client != null) {
                disconnect();
            }

            try {
                // connection started
                connectionEvent.setConnectionStarted(connectionStarted);

                // check for simulated connection error
                if(!isForcedError(HostError.Connection)) {
                    try {
                        SSLSocketFactory factory = new SSLSocketFactoryEx();
                        client = (SSLSocket) factory.createSocket();
                        client.connect(new InetSocketAddress(endpoint, port), 5000);
                        client.startHandshake();

                        raiseGatewayEvent(new SslHandshakeEvent(connectorName, null));
                    }
                    catch(Exception exc) {
                        raiseGatewayEvent(new SslHandshakeEvent(connectorName, exc));
                        if(client != null && client.isConnected()) {
                            disconnect();
                        }
                    }
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
                throw new GatewayComsException(exc);
            }
        }
    }

    // close connection
    private void disconnect() {
        try {
            if (client != null && !client.isClosed()) {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.close();
                }
                client.close();
            }
            client = null;
        }
        catch(IOException exc) {
            // eat the close exception
        }
    }

    public byte[] send(IDeviceMessage message) throws GatewayTimeoutException, GatewayComsException {
        /*
        1) if the initial attempt to connect fails (on both hosts) a GatewayComsException is thrown
        2) if the send/receive fails, no exception is thrown (timeout flag is tripped) and fail over occurs
        3) if timeout flag is set, Failure to connect to secondary host will throw GatewayTimeoutException
        4) if timeout flag is not set, failure to connect to the secondary host will throw GatewayComsException
        5) if connection to secondary host is successful, return to step 2
        6) if no response from the secondary host, GatewayTimeoutException is thrown
         */
        boolean timeout = false;
        connect(getPrimaryEndpoint(), getPrimaryPort());

        byte[] buffer = message.getSendBuffer();
        try {
            for(int i = 0; i < 2; i++) {
                raiseGatewayEvent(new RequestSentEvent(connectorName));
                DateTime requestSent = DateTime.now(DateTimeZone.UTC);
                try {
                    if(!isForcedError(HostError.SendFailure)) {
                        out.write(buffer);
                    }
                    else throw new IOException("Simulated IO Exception on request send.");

                    byte[] rvalue = getGatewayResponse();
                    if (rvalue != null && !isForcedError(HostError.Timeout)) {
                        raiseGatewayEvent(new ResponseReceivedEvent(connectorName, requestSent));
                        return rvalue;
                    }
                    timeout = true;
                }
                catch(IOException exc) {
                    /* Exception occurred on message send, do not trip timeout */
                }

                // did not get a response, switch endpoints and try again
                if(!currentHost.equals(Host.Secondary) && !StringUtils.isNullOrEmpty(secondaryEndpoint) && i < 1) {
                    raiseGatewayEvent(new TimeoutEvent(connectorName, GatewayEventType.TimeoutFailOver));

                    disconnect();
                    connect(getSecondaryEndpoint(), getSecondaryPort());
                }
            }

            raiseGatewayEvent(new TimeoutEvent(connectorName, GatewayEventType.Timeout));
            if(timeout) {
                throw new GatewayTimeoutException();
            }
            else throw new GatewayComsException();
        }
        catch(GatewayComsException exc) {
            if(timeout) {
                throw new GatewayTimeoutException(exc);
            }
            throw exc;
        }
        finally {
            disconnect();
            raiseGatewayEvent(new DisconnectEvent(connectorName));

            // remove simulated errors
            if(simulatedHostErrors != null) {
                simulatedHostErrors = null;
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
                    if(target!=null && target.equals(Target.GNAP)) {
                        messageLength = new BigInteger(lengthBuffer).intValue();
                    }else{
                        messageLength = new BigInteger(lengthBuffer).intValue() - 2;
                    }
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

            try {
                Thread.sleep(50);
            }
            catch(InterruptedException e) { break; }
        }
        while((System.currentTimeMillis() - t) <= 20000);

        throw new GatewayTimeoutException();
    }

    private void raiseGatewayEvent(final IGatewayEvent event) {
        if(gatewayEventHandler != null) {
            new Thread(new Runnable() {
                public void run() {
                    gatewayEventHandler.eventRaised(event);
                }
            }).start();
        }
    }
}
