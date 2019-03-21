package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;

public class DE22_PosDataCode implements IDataElement<DE22_PosDataCode> {
    private CardDataInputCapability cardDataInputCapability = CardDataInputCapability.MagStripe_KeyEntrty;
    private CardHolderAuthenticationCapability cardHolderAuthenticationCapability = CardHolderAuthenticationCapability.None;
    private boolean cardCaptureCapability = false;
    private OperatingEnvironment operatingEnvironment = OperatingEnvironment.NoTerminalUsed;
    private DE22_CardHolderPresence cardHolderPresence = DE22_CardHolderPresence.CardHolder_Present;
    private DE22_CardPresence cardPresence = DE22_CardPresence.CardPresent;
    private DE22_CardDataInputMode cardDataInputMode = DE22_CardDataInputMode.MagStripe;
    private DE22_CardHolderAuthenticationMethod cardHolderAuthenticationMethod = DE22_CardHolderAuthenticationMethod.NotAuthenticated;
    private CardHolderAuthenticationEntity cardHolderAuthenticationEntity = CardHolderAuthenticationEntity.NotAuthenticated;
    private CardDataOutputCapability cardDataOutputCapability = CardDataOutputCapability.None;
    private TerminalOutputCapability terminalOutputCapability = TerminalOutputCapability.None;
    private PinCaptureCapability pinCaptureCapability = PinCaptureCapability.TwelveCharacters;

    public CardDataInputCapability getCardDataInputCapability() {
        return cardDataInputCapability;
    }
    public void setCardDataInputCapability(CardDataInputCapability cardDataInputCapability) {
        this.cardDataInputCapability = cardDataInputCapability;
    }
    public CardHolderAuthenticationCapability getCardHolderAuthenticationCapability() {
        return cardHolderAuthenticationCapability;
    }
    public void setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability cardHolderAuthenticationCapability) {
        this.cardHolderAuthenticationCapability = cardHolderAuthenticationCapability;
    }
    public boolean getCardCaptureCapability() {
        return cardCaptureCapability;
    }
    public void setCardCaptureCapability(boolean cardCaptureCapability) {
        this.cardCaptureCapability = cardCaptureCapability;
    }
    public OperatingEnvironment getOperatingEnvironment() {
        return operatingEnvironment;
    }
    public void setOperatingEnvironment(OperatingEnvironment operatingEnvironment) {
        this.operatingEnvironment = operatingEnvironment;
    }
    public DE22_CardHolderPresence getCardHolderPresence() {
        return cardHolderPresence;
    }
    public void setCardHolderPresence(DE22_CardHolderPresence cardHolderPresence) {
        this.cardHolderPresence = cardHolderPresence;
    }
    public DE22_CardPresence getCardPresence() {
        return cardPresence;
    }
    public void setCardPresence(DE22_CardPresence cardPresence) {
        this.cardPresence = cardPresence;
    }
    public DE22_CardDataInputMode getCardDataInputMode() {
        return cardDataInputMode;
    }
    public void setCardDataInputMode(DE22_CardDataInputMode cardDataInputMode) {
        this.cardDataInputMode = cardDataInputMode;
    }
    public DE22_CardHolderAuthenticationMethod getCardHolderAuthenticationMethod() {
        return cardHolderAuthenticationMethod;
    }
    public void setCardHolderAuthenticationMethod(DE22_CardHolderAuthenticationMethod cardHolderAuthenticationMethod) {
        this.cardHolderAuthenticationMethod = cardHolderAuthenticationMethod;
    }
    public CardHolderAuthenticationEntity getCardHolderAuthenticationEntity() {
        return cardHolderAuthenticationEntity;
    }
    public void setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity cardHolderAuthenticationEntity) {
        this.cardHolderAuthenticationEntity = cardHolderAuthenticationEntity;
    }
    public CardDataOutputCapability getCardDataOutputCapability() {
        return cardDataOutputCapability;
    }
    public void setCardDataOutputCapability(CardDataOutputCapability cardDataOutputCapability) {
        this.cardDataOutputCapability = cardDataOutputCapability;
    }
    public TerminalOutputCapability getTerminalOutputCapability() {
        return terminalOutputCapability;
    }
    public void setTerminalOutputCapability(TerminalOutputCapability terminalOutputCapability) {
        this.terminalOutputCapability = terminalOutputCapability;
    }
    public PinCaptureCapability getPinCaptureCapability() {
        return pinCaptureCapability;
    }
    public void setPinCaptureCapability(PinCaptureCapability pinCaptureCapability) {
        this.pinCaptureCapability = pinCaptureCapability;
    }

    public DE22_PosDataCode fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        setCardDataInputCapability(sp.readStringConstant(1, CardDataInputCapability.class));
        setCardHolderAuthenticationCapability(sp.readStringConstant(1, CardHolderAuthenticationCapability.class));
        setCardCaptureCapability(sp.readBoolean());
        setOperatingEnvironment(sp.readStringConstant(1, OperatingEnvironment.class));
        setCardHolderPresence(sp.readStringConstant(1, DE22_CardHolderPresence.class));
        setCardPresence(sp.readStringConstant(1, DE22_CardPresence.class));
        setCardDataInputMode(sp.readStringConstant(1, DE22_CardDataInputMode.class));
        setCardHolderAuthenticationMethod(sp.readStringConstant(1, DE22_CardHolderAuthenticationMethod.class));
        setCardHolderAuthenticationEntity(sp.readStringConstant(1, CardHolderAuthenticationEntity.class));
        setCardDataOutputCapability(sp.readStringConstant(1, CardDataOutputCapability.class));
        setTerminalOutputCapability(sp.readStringConstant(1, TerminalOutputCapability.class));
        setPinCaptureCapability(sp.readStringConstant(1, PinCaptureCapability.class));

        return this;
    }

    public byte[] toByteArray() {
        return getCardDataInputCapability().getValue()
                .concat(getCardHolderAuthenticationCapability().getValue())
                .concat(getCardCaptureCapability() ? "1" : "0")
                .concat(getOperatingEnvironment().getValue())
                .concat(getCardHolderPresence().getValue())
                .concat(getCardPresence().getValue())
                .concat(getCardDataInputMode().getValue())
                .concat(getCardHolderAuthenticationMethod().getValue())
                .concat(getCardHolderAuthenticationEntity().getValue())
                .concat(getCardDataOutputCapability().getValue())
                .concat(getTerminalOutputCapability().getValue())
                .concat(getPinCaptureCapability().getValue())
                .getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}