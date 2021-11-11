package com.global.api.network.elements;

import com.global.api.network.NetworkMessage;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE48_CardType;
import com.global.api.network.enums.DE48_AdministrativelyDirectedTaskCode;
import com.global.api.network.enums.DataElementId;
import com.global.api.network.enums.Iso8583MessageType;
import com.global.api.utils.StringUtils;

import java.util.LinkedHashMap;

public class DE48_MessageControl implements IDataElement<DE48_MessageControl> {
    private DE48_1_CommunicationDiagnostics communicationDiagnostics;
    private DE48_2_HardwareSoftwareConfig hardwareSoftwareConfig;
    private String languageCode;
    private int batchNumber;
    private String shiftNumber;
    private String clerkId;
    private DE48_8_CustomerData customerData;
    private String track2ForSecondCard;
    private String track1ForSecondCard;
    private DE48_CardType cardType;
    private DE48_AdministrativelyDirectedTaskCode administrativelyDirectedTaskCode;
    private String rfidData;
    private DE48_14_PinEncryptionMethodology pinEncryptionMethodology;
    private DE48_33_PosConfiguration posConfiguration;
    private DE48_34_MessageConfiguration messageConfiguration;
    private DE48_Name name1;
    private DE48_Name name2;
    private String secondaryAccountNumber;
    private DE48_39_PriorMessageInformation priorMessageInformation;
    private LinkedHashMap<DataElementId, DE48_Address> addresses;
    private int addressIndex = 0;
    private DataElementId[] addressElementIds = new DataElementId[] {
            DataElementId.DE_040,
            DataElementId.DE_041,
            DataElementId.DE_042,
            DataElementId.DE_043,
            DataElementId.DE_044,
            DataElementId.DE_045,
            DataElementId.DE_046,
            DataElementId.DE_047,
            DataElementId.DE_048,
            DataElementId.DE_049,
    };
    private int sequenceNumber;

    public DE48_1_CommunicationDiagnostics getCommunicationDiagnostics() {
        return communicationDiagnostics;
    }
    public void setCommunicationDiagnostics(DE48_1_CommunicationDiagnostics communicationDiagnostics) {
        this.communicationDiagnostics = communicationDiagnostics;
    }
    public DE48_2_HardwareSoftwareConfig getHardwareSoftwareConfig() {
        return hardwareSoftwareConfig;
    }
    public void setHardwareSoftwareConfig(DE48_2_HardwareSoftwareConfig hardwareSoftwareConfig) {
        this.hardwareSoftwareConfig = hardwareSoftwareConfig;
    }
    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public int getBatchNumber() {
        return batchNumber;
    }
    public void setBatchNumber(int batchNumber) {
        this.batchNumber = batchNumber;
    }
    public String getShiftNumber() {
        return shiftNumber;
    }
    public void setShiftNumber(String shiftNumber) {
        this.shiftNumber = shiftNumber;
    }
    public String getClerkId() {
        return clerkId;
    }
    public void setClerkId(String clerkId) {
        this.clerkId = clerkId;
    }
    public DE48_8_CustomerData getCustomerData() {
        return customerData;
    }
    public void setCustomerData(DE48_8_CustomerData customerData) {
        this.customerData = customerData;
    }
    public String getTrack2ForSecondCard() {
        return track2ForSecondCard;
    }
    public void setTrack2ForSecondCard(String track2ForSecondCard) {
        this.track2ForSecondCard = track2ForSecondCard;
    }
    public String getTrack1ForSecondCard() {
        return track1ForSecondCard;
    }
    public void setTrack1ForSecondCard(String track1ForSecondCard) {
        this.track1ForSecondCard = track1ForSecondCard;
    }
    public DE48_CardType getCardType() {
        return cardType;
    }
    public void setCardType(DE48_CardType cardType) {
        this.cardType = cardType;
    }
    public DE48_AdministrativelyDirectedTaskCode getAdministrativelyDirectedTaskCode() {
        return administrativelyDirectedTaskCode;
    }
    public void setAdministrativelyDirectedTaskCode(DE48_AdministrativelyDirectedTaskCode adminitrativelyDirectedTaskCode) {
        this.administrativelyDirectedTaskCode = adminitrativelyDirectedTaskCode;
    }
    public String getRfidData() {
        return rfidData;
    }
    public void setRfidData(String rfidData) {
        this.rfidData = rfidData;
    }
    public DE48_14_PinEncryptionMethodology getPinEncryptionMethodology() {
        return pinEncryptionMethodology;
    }
    public void setPinEncryptionMethodology(DE48_14_PinEncryptionMethodology pinEncryptionMethodology) {
        this.pinEncryptionMethodology = pinEncryptionMethodology;
    }
    public DE48_33_PosConfiguration getPosConfiguration() {
        return posConfiguration;
    }
    public void setPosConfiguration(DE48_33_PosConfiguration posConfiguration) {
        this.posConfiguration = posConfiguration;
    }
    public DE48_34_MessageConfiguration getMessageConfiguration() {
        return messageConfiguration;
    }
    public void setMessageConfiguration(DE48_34_MessageConfiguration messageConfiguration) {
        this.messageConfiguration = messageConfiguration;
    }
    public DE48_Name getName1() {
        return name1;
    }
    public void setName1(DE48_Name name1) {
        this.name1 = name1;
    }
    public DE48_Name getName2() {
        return name2;
    }
    public void setName2(DE48_Name name2) {
        this.name2 = name2;
    }
    public String getSecondaryAccountNumber() {
        return secondaryAccountNumber;
    }
    public void setSecondaryAccountNumber(String secondaryAccountNumber) {
        this.secondaryAccountNumber = secondaryAccountNumber;
    }
    public void addAddress(DE48_Address address) {
        addresses.put(addressElementIds[addressIndex++], address);
    }
    public DE48_39_PriorMessageInformation getPriorMessageInformation() {
        return priorMessageInformation;
    }
    public void setPriorMessageInformation(DE48_39_PriorMessageInformation priorMessageInformation) {
        this.priorMessageInformation = priorMessageInformation;
    }

    // sequence number
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public DE48_MessageControl() {
        addresses = new LinkedHashMap<DataElementId, DE48_Address>();
        priorMessageInformation = new DE48_39_PriorMessageInformation();
    }

    public DE48_MessageControl fromByteArray(byte[] buffer) {
        NetworkMessage nm = NetworkMessage.parse(buffer, Iso8583MessageType.SubElement_DE_048);

        communicationDiagnostics = nm.getDataElement(DataElementId.DE_001, DE48_1_CommunicationDiagnostics.class);
        hardwareSoftwareConfig = nm.getDataElement(DataElementId.DE_002, DE48_2_HardwareSoftwareConfig.class);
        languageCode = nm.getString(DataElementId.DE_003);
        String _batchNumber = nm.getString(DataElementId.DE_004);
        if(!StringUtils.isNullOrEmpty(_batchNumber)) {
            sequenceNumber = Integer.parseInt(_batchNumber.substring(0, 6));
            batchNumber = Integer.parseInt(_batchNumber.substring(6));
        }
        shiftNumber = nm.getString(DataElementId.DE_005);
        clerkId = nm.getString(DataElementId.DE_006);
        customerData = nm.getDataElement(DataElementId.DE_008, DE48_8_CustomerData.class);
        track2ForSecondCard = nm.getString(DataElementId.DE_009);
        track1ForSecondCard = nm.getString(DataElementId.DE_010);
        cardType = nm.getStringConstant(DataElementId.DE_011, DE48_CardType.class);
        administrativelyDirectedTaskCode = nm.getByteConstant(DataElementId.DE_012, DE48_AdministrativelyDirectedTaskCode.class);
        rfidData = nm.getString(DataElementId.DE_013);
        pinEncryptionMethodology = nm.getDataElement(DataElementId.DE_014, DE48_14_PinEncryptionMethodology.class);
        posConfiguration = nm.getDataElement(DataElementId.DE_033, DE48_33_PosConfiguration.class);
        messageConfiguration = nm.getDataElement(DataElementId.DE_034, DE48_34_MessageConfiguration.class);
        name1 = nm.getDataElement(DataElementId.DE_035, DE48_Name.class);
        name2 = nm.getDataElement(DataElementId.DE_036, DE48_Name.class);
        secondaryAccountNumber = nm.getString(DataElementId.DE_037);
        priorMessageInformation = nm.getDataElement(DataElementId.DE_039, DE48_39_PriorMessageInformation.class);

        addressIndex = 0;
        for(DataElementId addressId: addressElementIds) {
            DE48_Address address = nm.getDataElement(addressId, DE48_Address.class);
            if(address != null) {
                addresses.put(addressId, address);
            }
        }

        return this;
    }

    public byte[] toByteArray() {
        String _batchNumber = StringUtils.padLeft(sequenceNumber, 6, '0')
                .concat(StringUtils.padLeft(batchNumber, 4, '0'));

        if(_batchNumber.equals("0000000000")) {
            _batchNumber = null;
        }

        NetworkMessage message = new NetworkMessage(Iso8583MessageType.SubElement_DE_048)
            .set(DataElementId.DE_001, communicationDiagnostics)
            .set(DataElementId.DE_002, hardwareSoftwareConfig)
            .set(DataElementId.DE_003, languageCode)
            .set(DataElementId.DE_004, _batchNumber)
            .set(DataElementId.DE_005, shiftNumber)
            .set(DataElementId.DE_006, clerkId)
            .set(DataElementId.DE_008, customerData)
            .set(DataElementId.DE_009, track2ForSecondCard)
            .set(DataElementId.DE_010, track1ForSecondCard)
            .set(DataElementId.DE_011, cardType)
            .set(DataElementId.DE_012, administrativelyDirectedTaskCode)
            .set(DataElementId.DE_013, rfidData)
            .set(DataElementId.DE_014, pinEncryptionMethodology)
            .set(DataElementId.DE_033, posConfiguration)
            .set(DataElementId.DE_034, messageConfiguration)
            .set(DataElementId.DE_035, name1)
            .set(DataElementId.DE_036, name2)
            .set(DataElementId.DE_037, secondaryAccountNumber)
            .set(DataElementId.DE_039, priorMessageInformation);

        for(DataElementId addressId: addresses.keySet()) {
            message.set(addressId, addresses.get(addressId));
        }

        return message.buildMessage();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
