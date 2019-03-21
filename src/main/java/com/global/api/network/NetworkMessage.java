package com.global.api.network;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DataElementId;
import com.global.api.utils.*;
import com.global.api.network.enums.Iso8583MessageType;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class NetworkMessage {
    private String messageTypeIndicator;
    private HashMap<DataElementId, Iso8583Element> elements;
    private Iso8583Bitmap bitmap;
    private Iso8583Bitmap secondaryBitmap;
    private Iso8583MessageType messageType;
    private Iso8583ElementFactory factory;

    public boolean isDataCollect(PaymentMethodType paymentMethodType) {
        String functionCode = getString(DataElementId.DE_024);
        String reasonCode = getString(DataElementId.DE_025);
        if(messageTypeIndicator.equals("1200") && functionCode.equals("200")) {
            return true;
        }
        else if (messageTypeIndicator.equals("1220") || messageTypeIndicator.equals("1221")) {
            if (functionCode.equals("201") || functionCode.equals("202")) {
                if(paymentMethodType != null && (paymentMethodType.equals(PaymentMethodType.Debit) || paymentMethodType.equals(PaymentMethodType.EBT))) {
                    return reasonCode.equals("1379");
                }
                return (reasonCode.equals("1376") || reasonCode.equals("1377") || reasonCode.equals("1378") || reasonCode.equals("1381"));
            }
            return false;
        }
        return false;
    }
    public String getMessageTypeIndicator() {
        return messageTypeIndicator;
    }
    public void setMessageTypeIndicator(String messageTypeIndicator) {
        this.messageTypeIndicator = messageTypeIndicator;
    }
    public Iso8583Bitmap getBitmap() {
        return bitmap;
    }
    private void setBitmap(Iso8583Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    private void setFactory(Iso8583ElementFactory factory) {
        this.factory = factory;
    }

    public NetworkMessage() {
        this(Iso8583MessageType.CompleteMessage);
    }
    public NetworkMessage(Iso8583MessageType messageType) {
        this.messageType = messageType;
        elements = new HashMap<DataElementId, Iso8583Element>();
        factory = Iso8583ElementFactory.getConfiguredFactory(messageType);
    }

    public boolean has(DataElementId id) {
        return elements.containsKey(id);
    }

    public BigDecimal getAmount(DataElementId id) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            return StringUtils.toAmount(new String(element.getBuffer()));
        }
        return null;
    }
    public byte[] getByteArray(DataElementId id) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            return element.getBuffer();
        }
        return null;
    }
    public Date getDate(DataElementId id, SimpleDateFormat formatter) {
        String value = getString(id);
        if(!StringUtils.isNullOrEmpty(value)) {
            try {
                return formatter.parse(value);
            }
            catch(ParseException e) {
                return null;
            }
        }
        return null;
    }
    public String getString(DataElementId id) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            return new String(element.getBuffer());
        }
        return null;
    }

    public <TResult extends IDataElement<TResult>> TResult getDataElement(DataElementId id, Class<TResult> clazz) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            return element.getConcrete(clazz);
        }
        return null;
    }
    public <TResult extends Enum<TResult> & IStringConstant> TResult getStringConstant(DataElementId id, Class<TResult> clazz) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            String value = new String(element.getBuffer());

            TResult rvalue = ReverseStringEnumMap.parse(StringUtils.trim(value), clazz);
            if(rvalue == null) {
                rvalue = ReverseStringEnumMap.parse(value, clazz);
            }
            return rvalue;
        }
        return null;
    }
    public <TResult extends Enum<TResult> & IByteConstant> TResult getByteConstant(DataElementId id, Class<TResult> clazz) {
        if(elements.containsKey(id)) {
            Iso8583Element element = elements.get(id);
            return ReverseByteEnumMap.parse(element.getBuffer()[0], clazz);
        }
        return null;
    }

    public NetworkMessage set(DataElementId id, String value) {
        if(value != null) {
            return set(id, value.getBytes());
        }
        return this;
    }
    public NetworkMessage set(DataElementId id, IStringConstant constant) {
        if(constant != null) {
            return set(id, constant.getBytes());
        }
        return this;
    }
    public NetworkMessage set(DataElementId id, IByteConstant constant) {
        if(constant != null) {
            return set(id, new byte[] { constant.getByte() });
        }
        return this;
    }
    public NetworkMessage set(DataElementId id, IDataElement element) {
        if(element != null) {
            return set(id, element.toByteArray());
        }
        return this;
    }
    public NetworkMessage set(DataElementId id, byte[] buffer) {
        Iso8583Element element = factory.createElement(id, buffer);
        elements.put(id, element);
        return this;
    }

    public byte[] buildMessage() {
        return buildMessage(false);
    }
    public byte[] buildMessage(boolean addBitmapAsString) {
        MessageWriter mw = new MessageWriter();

        // put the MTI
        if(!StringUtils.isNullOrEmpty(messageTypeIndicator)) {
            mw.addRange(messageTypeIndicator.getBytes());
        }

        // deal with the bitmaps
        generateBitmaps();
        if(addBitmapAsString) {
            mw.addRange(bitmap.toHexString().getBytes());
        }
        else {
            mw.addRange(bitmap.toByteArray());
        }

        // primary bitmap
        DataElementId currentElement = bitmap.getNextDataElement();
        do {
            Iso8583Element element = elements.get(currentElement);
            mw.addRange(element.getSendBuffer());

            currentElement = bitmap.getNextDataElement();
        }
        while(currentElement != null);

        // secondary bitmap
        if(messageType.equals(Iso8583MessageType.CompleteMessage)) {
            currentElement = secondaryBitmap.getNextDataElement();
            while(currentElement != null){
                Iso8583Element element = elements.get(currentElement);
                mw.addRange(element.getSendBuffer());

                currentElement = secondaryBitmap.getNextDataElement();
            }
        }

        return mw.toArray();
    }

    private void generateBitmaps() {
        bitmap = new Iso8583Bitmap(new byte[8]);

        // check if we need a secondary bitmap
        if(messageType.equals(Iso8583MessageType.CompleteMessage)) {
            secondaryBitmap = new Iso8583Bitmap(new byte[8], 64);
            bitmap.setDataElement(DataElementId.DE_001);
        }

        for(DataElementId elementType: elements.keySet()) {
            if(secondaryBitmap != null && elementType.getValue() > 64) {
                secondaryBitmap.setDataElement(elementType);
            }
            else {
                bitmap.setDataElement(elementType);
            }
        }

        // put the finished secondary bitmap to the elements
        if(messageType.equals(Iso8583MessageType.CompleteMessage)) {
            set(DataElementId.DE_001, secondaryBitmap.toByteArray());
        }
    }

    public static NetworkMessage parse(String input, Iso8583MessageType messageType) {
        MessageReader mr = new MessageReader(input.getBytes());
        Iso8583Bitmap bitmap = new Iso8583Bitmap(StringUtils.bytesFromHex(mr.readString(16)));

        return parseMessage(bitmap, mr, messageType);
    }
    public static NetworkMessage parse(byte[] input, Iso8583MessageType messageType) {
        MessageReader mr = new MessageReader(input);
        Iso8583Bitmap bitmap = new Iso8583Bitmap(mr.readBytes(8));

        return parseMessage(bitmap, mr, messageType);
    }
    private static NetworkMessage parseMessage(Iso8583Bitmap bitmap, MessageReader mr, Iso8583MessageType messageType) {
        NetworkMessage message = new NetworkMessage(messageType);
        message.setBitmap(bitmap);

        // initialize the factory
        message.setFactory(Iso8583ElementFactory.getConfiguredFactory(mr, messageType));

        // read the primary bitmap
        DataElementId currentElement = bitmap.getNextDataElement();
        do {
            message.elements.put(currentElement, message.factory.createElement(currentElement));
            currentElement = bitmap.getNextDataElement();
        }
        while(currentElement != null);

        // check for secondary bitmap
        if(message.has(DataElementId.DE_001)) {
            byte[] secondaryBuffer = message.getByteArray(DataElementId.DE_001);
            Iso8583Bitmap secondaryMap = new Iso8583Bitmap(secondaryBuffer, 64);

            currentElement = secondaryMap.getNextDataElement();
            while(currentElement != null) {
                message.elements.put(currentElement, message.factory.createElement(currentElement));
                currentElement = secondaryMap.getNextDataElement();
            }
        }

        // return the document
        return message;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // put the MTI
        if(!StringUtils.isNullOrEmpty(messageTypeIndicator)) {
            sb.append(String.format("MTI: %s\r\n", messageTypeIndicator));
        }

        // deal with the bitmaps
        generateBitmaps();
        sb.append(String.format("P_BITMAP: %s\r\n", bitmap.toHexString()));

        // primary bitmap
        DataElementId currentElement = bitmap.getNextDataElement();
        do {
            Iso8583Element element = elements.get(currentElement);
            if(currentElement.equals(DataElementId.DE_001)) {
                sb.append(String.format("S_BITMAP: %s\r\n", secondaryBitmap.toHexString()));
            }
            else {
                // special handling for DE 55
                if(element.getId().equals(DataElementId.DE_055)) {
                    byte[] buffer = element.getBuffer();
                    sb.append(String.format("%s: %s%s\r\n", element.getId(), StringUtils.padLeft(buffer.length, 3, '0'), StringUtils.hexFromBytes(buffer)));
                }
                else sb.append(String.format("%s: %s\r\n", element.getId(), new String(element.getSendBuffer())));
            }

            currentElement = bitmap.getNextDataElement();
        }
        while(currentElement != null);

        // secondary bitmap
        if(messageType.equals(Iso8583MessageType.CompleteMessage)) {
            currentElement = secondaryBitmap.getNextDataElement();
            while(currentElement != null){
                Iso8583Element element = elements.get(currentElement);
                sb.append(String.format("%s: %s\r\n", element.getId(), new String(element.getSendBuffer())));

                currentElement = secondaryBitmap.getNextDataElement();
            }
        }

        return sb.toString();
    }
}
