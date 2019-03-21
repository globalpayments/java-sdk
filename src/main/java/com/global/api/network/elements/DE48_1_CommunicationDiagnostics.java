package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;

public class DE48_1_CommunicationDiagnostics implements IDataElement<DE48_1_CommunicationDiagnostics> {
    private int communicationAttempts;
    private DE48_ConnectionResult connectionResult;
    private DE48_HostConnected hostConnected;

    public int getCommunicationAttempts() {
        return communicationAttempts;
    }
    public void setCommunicationAttempts(int communicationAttempts) {
        this.communicationAttempts = communicationAttempts;
    }
    public DE48_ConnectionResult getConnectionResult() {
        return connectionResult;
    }
    public void setConnectionResult(DE48_ConnectionResult connectionResult) {
        this.connectionResult = connectionResult;
    }
    public DE48_HostConnected getHostConnected() {
        return hostConnected;
    }
    public void setHostConnected(DE48_HostConnected hostConnected) {
        this.hostConnected = hostConnected;
    }

    public DE48_1_CommunicationDiagnostics fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        communicationAttempts = sp.readInt(1);
        connectionResult = sp.readStringConstant(2, DE48_ConnectionResult.class);
        hostConnected = sp.readStringConstant(1, DE48_HostConnected.class);

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = communicationAttempts + "";
        return rvalue.concat(connectionResult.getValue())
                .concat(hostConnected.getValue()).getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
