package com.global.api.network;

import com.global.api.network.enums.*;
import com.global.api.utils.ReverseByteEnumMap;
import com.global.api.utils.StringParser;

public class NetworkMessageHeader {
    private NetworkTransactionType networkTransactionType;
    private MessageType messageType;
    private CharacterSet characterSet;
    private NetworkResponseCode responseCode;
    private NetworkResponseCodeOrigin responseCodeOrigin;
    private NetworkProcessingFlag processingFlag;
    private ProtocolType protocolType;
    private ConnectionType connectionType;
    private String nodeIdentification;
    private byte[] originCorrelation1;
    private String companyId;
    private byte[] originCorrelation2;
    private byte version;

    public NetworkTransactionType getNetworkTransactionType() {
        return networkTransactionType;
    }
    public void setNetworkTransactionType(NetworkTransactionType networkTransactionType) {
        this.networkTransactionType = networkTransactionType;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    public CharacterSet getCharacterSet() {
        return characterSet;
    }
    public void setCharacterSet(CharacterSet characterSet) {
        this.characterSet = characterSet;
    }
    public NetworkResponseCode getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(NetworkResponseCode responseCode) {
        this.responseCode = responseCode;
    }
    public NetworkResponseCodeOrigin getResponseCodeOrigin() {
        return responseCodeOrigin;
    }
    public void setResponseCodeOrigin(NetworkResponseCodeOrigin responseCodeOrigin) {
        this.responseCodeOrigin = responseCodeOrigin;
    }
    public NetworkProcessingFlag getProcessingFlag() {
        return processingFlag;
    }
    public void setProcessingFlag(NetworkProcessingFlag processingFlag) {
        this.processingFlag = processingFlag;
    }
    public ProtocolType getProtocolType() {
        return protocolType;
    }
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
    public ConnectionType getConnectionType() {
        return connectionType;
    }
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    public String getNodeIdentification() {
        return nodeIdentification;
    }
    public void setNodeIdentification(String nodeIdentification) {
        this.nodeIdentification = nodeIdentification;
    }
    public byte[] getOriginCorrelation1() {
        return originCorrelation1;
    }
    public void setOriginCorrelation1(byte[] originCorrelation1) {
        this.originCorrelation1 = originCorrelation1;
    }
    public String getCompanyId() {
        return companyId;
    }
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public byte[] getOriginCorrelation2() {
        return originCorrelation2;
    }
    public void setOriginCorrelation2(byte[] originCorrelation2) {
        this.originCorrelation2 = originCorrelation2;
    }
    public byte getVersion() {
        return version;
    }
    public void setVersion(byte version) {
        this.version = version;
    }

    public static NetworkMessageHeader parse(byte[] buffer) {
        NetworkMessageHeader header = new NetworkMessageHeader();

        StringParser sp = new StringParser(buffer);
        header.setNetworkTransactionType(sp.readStringConstant(2, NetworkTransactionType.class));
        sp.readBytes(2);
        header.setMessageType(sp.readByteConstant(MessageType.class));
        header.setCharacterSet(sp.readByteConstant(CharacterSet.class));
        header.setResponseCode(sp.readByteConstant(NetworkResponseCode.class));
        header.setResponseCodeOrigin(sp.readByteConstant(NetworkResponseCodeOrigin.class));
        header.setProcessingFlag(sp.readByteConstant(NetworkProcessingFlag.class));

        // protocol type is special when async
        byte protocolType = sp.readByte();
        if(protocolType == 0x07) {
            protocolType = 0x05;
        }

        header.setProtocolType(ReverseByteEnumMap.parse(protocolType, ProtocolType.class));
        header.setConnectionType(sp.readByteConstant(ConnectionType.class));
        header.setNodeIdentification(sp.readString(4));
        header.setOriginCorrelation1(sp.readBytes(2));
        header.setCompanyId(sp.readString(4));
        header.setOriginCorrelation2(sp.readBytes(8));
        header.setVersion(sp.readByte());

        return header;
    }
}
