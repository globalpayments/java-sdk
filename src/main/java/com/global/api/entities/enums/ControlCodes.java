package com.global.api.entities.enums;

public enum ControlCodes implements IByteConstant {
    STX (0x02), // Denotes the beginning of a message frame
    ETX (0x03), // Denotes the ending of a message frame
    EOT (0x04), // Indicates communication session terminated
    ENQ (0x05), // Begin Session sent from the host to the POS
    ACK (0x06), // Acknowledge of message received
    NAK (0x15), // Indicates invalid message received
    FS (0x1C),  // Field separator
    GS (0x1D),  // Message ID follows (for non-PIN entry prompts)
    RS (0x1E),  // Message ID follows (for PIN entry prompts)
    US (0x1F),
    COMMA (0x2C),
    COLON (0x3A),
    PTGS (0x7C);

    private final byte code;
    ControlCodes(int code){ this.code = (byte)code; }
    public byte getByte() { return this.code; }

    @Override
    public String toString() {
        String rvalue = super.toString();
        return String.format("[%s]", rvalue);
    }
}
