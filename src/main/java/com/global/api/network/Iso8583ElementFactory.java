package com.global.api.network;

import com.global.api.network.enums.DataElementId;
import com.global.api.network.enums.DataElementType;
import com.global.api.utils.MessageReader;
import com.global.api.network.enums.Iso8583MessageType;

import java.util.HashMap;

class Iso8583ElementFactory {
    private HashMap<DataElementId, String> elementDescriptions;
    private HashMap<DataElementId, Integer> elementLengths;
    private HashMap<DataElementId, DataElementType> elementTypes;
    private MessageReader messageReader;

    static Iso8583ElementFactory getConfiguredFactory(Iso8583MessageType messageType) {
        return getConfiguredFactory(null, messageType);
    }
    static Iso8583ElementFactory getConfiguredFactory(MessageReader mr, Iso8583MessageType messageType) {
        Iso8583ElementFactory factory = new Iso8583ElementFactory(mr);

        if(messageType.equals(Iso8583MessageType.CompleteMessage)) {
            factory.addElementMapping(DataElementId.DE_001, DataElementType.BINARY, "Secondary BitmapElement", 8);
            factory.addElementMapping(DataElementId.DE_002, DataElementType.LLVAR, "Primary Account Number (PAN)", 19);
            factory.addElementMapping(DataElementId.DE_003, DataElementType.NUMERIC, "Processing Code", 6);
            factory.addElementMapping(DataElementId.DE_004, DataElementType.NUMERIC, "Amount, Transaction", 12);
            factory.addElementMapping(DataElementId.DE_007, DataElementType.NUMERIC, "Date and Time, Transmission", 10);
            factory.addElementMapping(DataElementId.DE_011, DataElementType.NUMERIC, "System Trace Audit Number (STAN)", 6);
            factory.addElementMapping(DataElementId.DE_012, DataElementType.NUMERIC, "Date and Time, Transaction", 12);
            factory.addElementMapping(DataElementId.DE_014, DataElementType.NUMERIC, "Date, Expiration", 4);
            factory.addElementMapping(DataElementId.DE_015, DataElementType.NUMERIC, "Date, Settlement", 6);
            factory.addElementMapping(DataElementId.DE_017, DataElementType.NUMERIC, "Date, Capture", 4);
            factory.addElementMapping(DataElementId.DE_018, DataElementType.NUMERIC, "Merchant Type", 4);
            factory.addElementMapping(DataElementId.DE_019, DataElementType.NUMERIC, "Country Code, Acquiring Institution", 3);
            factory.addElementMapping(DataElementId.DE_022, DataElementType.ALPHA_NUMERIC, "Point of Service Data Code", 12);
            factory.addElementMapping(DataElementId.DE_023, DataElementType.NUMERIC, "Card Sequence Number", 3);
            factory.addElementMapping(DataElementId.DE_024, DataElementType.NUMERIC, "Function Code", 3);
            factory.addElementMapping(DataElementId.DE_025, DataElementType.NUMERIC, "Message Reason Code", 4);
            factory.addElementMapping(DataElementId.DE_030, DataElementType.NUMERIC, "Amounts, Original", 24);
            factory.addElementMapping(DataElementId.DE_032, DataElementType.LLVAR, "Acquiring Institution Identification Code", 11);
            factory.addElementMapping(DataElementId.DE_034, DataElementType.LLVAR, "Primary Account Number, Extended", 28);
            factory.addElementMapping(DataElementId.DE_035, DataElementType.LLVAR, "Track 2 Data", 37);
            factory.addElementMapping(DataElementId.DE_037, DataElementType.ALPHA_NUMERIC_PAD, "Retrieval Reference Number", 12);
            factory.addElementMapping(DataElementId.DE_038, DataElementType.ALPHA_NUMERIC_PAD, "Approval Code", 6);
            factory.addElementMapping(DataElementId.DE_039, DataElementType.NUMERIC, "Action Code", 3);
            factory.addElementMapping(DataElementId.DE_041, DataElementType.ALPHA_NUMERIC_SPECIAL, "Card Acceptor Terminal Identification Code", 8);
            factory.addElementMapping(DataElementId.DE_042, DataElementType.ALPHA_NUMERIC_SPECIAL, "Card Acceptor Identification Code", 15);
            factory.addElementMapping(DataElementId.DE_043, DataElementType.LLVAR, "Card Acceptor Name/Location", 99);
            factory.addElementMapping(DataElementId.DE_044, DataElementType.LLVAR, "Additional Response Data", 99);
            factory.addElementMapping(DataElementId.DE_045, DataElementType.LLVAR, "Track 1 Data", 76);
            factory.addElementMapping(DataElementId.DE_046, DataElementType.LLLVAR, "Amounts, Fees", 204);
            factory.addElementMapping(DataElementId.DE_048, DataElementType.LLLVAR, "Message Control", 999);
            factory.addElementMapping(DataElementId.DE_049, DataElementType.NUMERIC, "Currency Code, Transaction", 3);
            factory.addElementMapping(DataElementId.DE_050, DataElementType.NUMERIC, "Currency Code, Reconciliation", 3);
            factory.addElementMapping(DataElementId.DE_052, DataElementType.BINARY, "Personal Identification Number (PIN) Data", 8);
            factory.addElementMapping(DataElementId.DE_053, DataElementType.LLVAR, "Security Related Control Information", 48);
            factory.addElementMapping(DataElementId.DE_054, DataElementType.LLLVAR, "Amounts, Additional", 120);
            factory.addElementMapping(DataElementId.DE_055, DataElementType.LLLVAR, "Integrated Circuit Card (ICC) Data", 512);
            factory.addElementMapping(DataElementId.DE_056, DataElementType.LLVAR, "Original Data Elements", 35);
            factory.addElementMapping(DataElementId.DE_058, DataElementType.LLVAR, "Authorizing Agent Institution Identification Code", 11);
            factory.addElementMapping(DataElementId.DE_059, DataElementType.LLLVAR, "Transport Data", 999);
            factory.addElementMapping(DataElementId.DE_062, DataElementType.LLLVAR, "Card Issuer Data", 999);
            factory.addElementMapping(DataElementId.DE_063, DataElementType.LLLVAR, "Product Data", 999);
            factory.addElementMapping(DataElementId.DE_072, DataElementType.LLLVAR, "Data Record", 999);
            factory.addElementMapping(DataElementId.DE_073, DataElementType.NUMERIC, "Date, Action", 6);
            factory.addElementMapping(DataElementId.DE_096, DataElementType.LLLVAR, "Key Management Data", 999);
            factory.addElementMapping(DataElementId.DE_097, DataElementType.NUMERIC, "Amount, Net Reconciliation", 16);
            factory.addElementMapping(DataElementId.DE_102, DataElementType.LLVAR, "Account Identification 1", 28);
            factory.addElementMapping(DataElementId.DE_103, DataElementType.LLVAR, "Check MICR Data (Account Identification 2)", 28);
            factory.addElementMapping(DataElementId.DE_115, DataElementType.LLLVAR, "eWIC Overflow Data", 999);
            factory.addElementMapping(DataElementId.DE_116, DataElementType.LLLVAR, "eWIC Overflow Data", 999);
            factory.addElementMapping(DataElementId.DE_117, DataElementType.LLLVAR, "eWIC Data", 999);
            factory.addElementMapping(DataElementId.DE_123, DataElementType.LLLVAR, "Reconciliation Totals", 999);
            factory.addElementMapping(DataElementId.DE_124, DataElementType.LLLVAR, "Sundry Data", 999);
            factory.addElementMapping(DataElementId.DE_125, DataElementType.LLLVAR, "Extended Response Data 1", 999);
            factory.addElementMapping(DataElementId.DE_126, DataElementType.LLLVAR, "Extended Response Data 2", 999);
            factory.addElementMapping(DataElementId.DE_127, DataElementType.LLLVAR, "Forwarding Data", 999);
        }
        else if(messageType.equals(Iso8583MessageType.SubElement_DE_048)) {
            factory.addElementMapping(DataElementId.DE_001, DataElementType.NUMERIC, "Communication Diagnostics", 4);
            factory.addElementMapping(DataElementId.DE_002, DataElementType.ALPHA_NUMERIC_SPECIAL, "Hardware & Software Configuration", 20);
            factory.addElementMapping(DataElementId.DE_003, DataElementType.ALPHA, "Language Code", 2);
            factory.addElementMapping(DataElementId.DE_004, DataElementType.NUMERIC, "Batch Number", 10);
            factory.addElementMapping(DataElementId.DE_005, DataElementType.NUMERIC, "Shift Number", 3);
            factory.addElementMapping(DataElementId.DE_006, DataElementType.LVAR, "Clerk Id", 9);
            factory.addElementMapping(DataElementId.DE_007, DataElementType.NUMERIC, "Multiple Transaction Control", 9);
            factory.addElementMapping(DataElementId.DE_008, DataElementType.LLLVAR, "Customer Data", 250);
            factory.addElementMapping(DataElementId.DE_009, DataElementType.LLVAR, "Track 2 for Second Card", 37);
            factory.addElementMapping(DataElementId.DE_010, DataElementType.LLVAR, "Track 1 for Second Card", 76);
            factory.addElementMapping(DataElementId.DE_011, DataElementType.ALPHA_NUMERIC_PAD, "Card Type", 4);
            factory.addElementMapping(DataElementId.DE_012, DataElementType.BINARY, "Administratively Directed Task", 1);
            factory.addElementMapping(DataElementId.DE_013, DataElementType.LLVAR, "RFID Data", 99);
            factory.addElementMapping(DataElementId.DE_014, DataElementType.ALPHA_NUMERIC_SPECIAL, "PIN Encryption Methodology", 2);
            factory.addElementMapping(DataElementId.DE_033, DataElementType.LLVAR, "POS Configuration", 99);
            factory.addElementMapping(DataElementId.DE_034, DataElementType.LLVAR, "Message Configuration", 99);
            factory.addElementMapping(DataElementId.DE_035, DataElementType.LLVAR, "Name 1", 99);
            factory.addElementMapping(DataElementId.DE_036, DataElementType.LLVAR, "Name 2", 99);
            factory.addElementMapping(DataElementId.DE_037, DataElementType.LLVAR, "Secondary Account Number", 28);
            factory.addElementMapping(DataElementId.DE_039, DataElementType.LLVAR, "Prior Message Information", 99);
            factory.addElementMapping(DataElementId.DE_040, DataElementType.LLVAR, "Address 1", 99);
            factory.addElementMapping(DataElementId.DE_041, DataElementType.LLVAR, "Address 2", 99);
            factory.addElementMapping(DataElementId.DE_042, DataElementType.LLVAR, "Address 3", 99);
            factory.addElementMapping(DataElementId.DE_043, DataElementType.LLVAR, "Address 4", 99);
            factory.addElementMapping(DataElementId.DE_044, DataElementType.LLVAR, "Address 5", 99);
            factory.addElementMapping(DataElementId.DE_045, DataElementType.LLVAR, "Address 6", 99);
            factory.addElementMapping(DataElementId.DE_046, DataElementType.LLVAR, "Address 7", 99);
            factory.addElementMapping(DataElementId.DE_047, DataElementType.LLVAR, "Address 8", 99);
            factory.addElementMapping(DataElementId.DE_048, DataElementType.LLVAR, "Address 9", 99);
            factory.addElementMapping(DataElementId.DE_049, DataElementType.LLVAR, "Address 10", 99);
        }

        return factory;
    }

    private Iso8583ElementFactory(MessageReader mr) {
        messageReader = mr;
        elementTypes = new HashMap<DataElementId, DataElementType>();
        elementDescriptions = new HashMap<DataElementId, String>();
        elementLengths = new HashMap<DataElementId, Integer>();
    }

    private void addElementMapping(DataElementId id, DataElementType type, String description, int length) {
        elementTypes.put(id, type);
        elementDescriptions.put(id, description);
        elementLengths.put(id, length);
    }

    Iso8583Element createElement(DataElementId id) {
        DataElementType type = elementTypes.get(id);
        String description = elementDescriptions.get(id);
        Integer length = elementLengths.get(id);

        return Iso8583Element.inflate(id, type, description, length, messageReader);
    }
    Iso8583Element createElement(DataElementId id, byte[] buffer) {
        DataElementType type = elementTypes.get(id);
        String description = elementDescriptions.get(id);
        Integer length = elementLengths.get(id);

        return Iso8583Element.inflate(id, type, description, length, buffer);
    }
}
