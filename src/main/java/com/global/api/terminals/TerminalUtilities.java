package com.global.api.terminals;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.upa.Entities.Enums.UpaMessageId;
import com.global.api.utils.Element;
import com.global.api.utils.IRawRequestBuilder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.MessageWriter;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;

import static java.lang.Math.*;

public class TerminalUtilities {
    private static final String version = "1.35";
    private static final int BASE_BIT = 1024;
    private static final int MAX_WIDTH = 370;
    private static final int MAX_HEIGHT = 13930;
    private static final int MAX_BYTES = 3145728;

    public static String getElementString(Object... elements) {
        StringBuilder sb = new StringBuilder();
        for(Object element: elements){
            if(element instanceof ControlCodes) {
                sb.append((char) ((ControlCodes) element).getByte());
            }
            else if(element instanceof IRequestSubGroup) {
                sb.append(((IRequestSubGroup) element).getElementString());
            }
            else if(element instanceof String[]) {
                for(String sub_element: (String[])element){
                    sb.append(ControlCodes.FS.getByte());
                    sb.append(sub_element);
                }
            }
            else if(element instanceof IStringConstant) {
                sb.append(((IStringConstant) element).getValue());
            }
            else if(element instanceof IByteConstant) {
                sb.append(((IByteConstant) element).getByte());
            }
            else sb.append(element);
        }

        return sb.toString();
    }

    private static DeviceMessage buildMessage(PaxMsgId messageId, String message){
        MessageWriter buffer = new MessageWriter();

        // Begin Message
        buffer.add(ControlCodes.STX);

        // Add Message Id
        buffer.add(messageId);
        buffer.add(ControlCodes.FS);

        // Add Version
        buffer.addRange(version.getBytes());
        buffer.add(ControlCodes.FS);

        // Add Message
        buffer.addRange(message.getBytes());

        // End the message
        buffer.add(ControlCodes.ETX);

        byte lrc = calculateLRC(buffer.toArray());
        buffer.add(lrc);

        return new DeviceMessage(buffer.toArray());
    }

    public static DeviceMessage buildMessage(String message) {
        JsonDoc json = new JsonDoc();
        json.set("message", message);
        json.set("data", "", true);

        DeviceMessage deviceMessage = compileMessage(json.toString());
        deviceMessage.setRawRequest(json);
        return deviceMessage;
    }

    public static DeviceMessage buildMessage(UpaMessageId messageType, String requestId, JsonDoc body) {

        JsonDoc data = new JsonDoc();
        JsonDoc json = new JsonDoc();
        data.set("EcrId", "13");
        data.set("requestId", requestId);
        data.set("command", messageType.toString());
        if (body != null) {
            data.set("data", body);
        }
        json.set("data", data);
        json.set("message", "MSG");

        DeviceMessage deviceMessage = compileMessage(json.toString());
        deviceMessage.setRawRequest(json);

        return deviceMessage;
    }

    public static <T extends IRawRequestBuilder> DeviceMessage buildMessage(T doc) {

        IRawRequestBuilder iRawRequestBuilder = doc;
        byte[] buffer;
        if (doc instanceof JsonDoc) {
            buffer = compileRawMessage(((JsonDoc) iRawRequestBuilder).toString());
        } else {
            buffer = compileRawMessage(((Element) iRawRequestBuilder).toString());
        }

        return new DeviceMessageWithDocument(iRawRequestBuilder, buffer);
    }

    public static DeviceMessage buildRequest(String message, MessageFormat format) {
        MessageWriter buffer = new MessageWriter();

        // beginning sentinel
        if(format.equals(MessageFormat.Visa2nd))
            buffer.add(ControlCodes.STX);
        else {
            buffer.add((byte)(message.length() >>> 8));
            buffer.add((byte)message.length());
        }

        // put message
        buffer.addRange(message.getBytes());

        // ending sentinel
        if(format.equals(MessageFormat.Visa2nd)) {
            buffer.add(ControlCodes.ETX);

            byte lrc = calculateLRC(buffer.toArray());
            buffer.add(lrc);
        }

        return new DeviceMessage(buffer.toArray());
    }

    public static DeviceMessage buildRequest(PaxMsgId messageId, Object... elements){
        String message = getElementString(elements);
        return buildMessage(messageId, message);
    }

    public static DeviceMessage buildRequest(byte[] message) {
        MessageWriter buffer = new MessageWriter();

        // beginning sentinel
        buffer.add(ControlCodes.STX);

        // put message
        buffer.addRange(message);

        // ending sentinel
        buffer.add(ControlCodes.ETX);

        byte lrc = calculateLRC(buffer.toArray());
        buffer.add(lrc);

        return new DeviceMessage(buffer.toArray());
    }

    public static byte calculateLRC(byte[] buffer) {
        int length = buffer.length;
        if(buffer[buffer.length - 1] != ControlCodes.ETX.getByte()) {
            length--;
        }

        byte lrc = (byte)0x00;
        for (int i = 1; i < length; i++) {
            lrc = (byte) (lrc ^ buffer[i]);
        }
        return lrc;
    }

    public static String buildBitMapUPAContent(String filePath) throws BuilderException {
        if (filePath == null || filePath.isEmpty() || !new File(filePath).exists()) {
            throw new BuilderException(String.format("The image %s does not exist.", filePath));
        }
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            String format = filePath.substring(filePath.lastIndexOf('.') + 1);

            // Verificar si hay un ImageWriter disponible para el formato
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new BuilderException(String.format("Unsupported image format: %s", format));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            byte[] imageBytes = baos.toByteArray();
            validateImage(image, imageBytes, null);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return String.format("data:%s;base64,%s", format, base64Image);
        } catch (IOException e) {
            throw new BuilderException(String.format("Error processing image %s: %s", filePath, e.getMessage()));
        }
    }

    private static void validateImage(BufferedImage image, byte[] imageBytes, UpaMessageId transType) throws BuilderException {
        if (imageBytes.length > MAX_BYTES) {
            double maxMb = ((double) MAX_BYTES) / (BASE_BIT * BASE_BIT);
            throw new BuilderException(String.format("The image must not exceed %.2fMB.", maxMb));
        }
        if (image.getWidth() > MAX_WIDTH) {
            throw new BuilderException(String.format("The maximum width must be %dpx.", MAX_WIDTH));
        }
        if (image.getHeight() > MAX_HEIGHT) {
            throw new BuilderException(String.format("The maximum height must be %dpx.", MAX_HEIGHT));
        }

        int multiplier = transType == UpaMessageId.InjectUDDataFile ? 3 : 1;
        int maxBytesForValidation = MAX_BYTES * multiplier;

        if (imageBytes.length > maxBytesForValidation) {
            double maxMb = round((double) maxBytesForValidation / BASE_BIT) / BASE_BIT;
            throw new BuilderException(String.format("The image must not exceed %.2fMB.", maxMb));
        }
    }
    public static boolean checkLRC(String message) {
        byte[] messageBuffer = message.getBytes();

        byte expected = messageBuffer[messageBuffer.length - 1];
        byte actual = calculateLRC(message.substring(0, message.length() - 1).getBytes());

        return (expected == actual);
    }

    public static DeviceMessage compileMessage(String body) {

        byte[] range = compileRawMessage(body);

        DeviceMessage deviceMessage = new DeviceMessage(range);

        return deviceMessage;
    }

    private static byte[] compileRawMessage(String body) {
        MessageWriter buffer = new MessageWriter();

        buffer.add(ControlCodes.STX.getByte());
        buffer.add(ControlCodes.LF.getByte());
        buffer.addRange(body.getBytes());
        buffer.add(ControlCodes.LF.getByte());
        buffer.add(ControlCodes.ETX.getByte());
        buffer.add(ControlCodes.LF.getByte());

        return buffer.toArray();
    }

    public static String buildStringFromFile(String filePath) throws IOException, BuilderException {
        if (filePath == null || filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
            throw new BuilderException("The file " + filePath + " does not exist.");
        }
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }

    public static String buildToBase64Content(String filePath, UpaMessageId transType, boolean imageValidation) throws BuilderException {
        if (filePath == null || filePath.isEmpty() || !new File(filePath).exists()) {
            throw new BuilderException(String.format("The file %s does not exist.", filePath));
        }

        try {
            byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());

            if (imageValidation) {
                BufferedImage image = ImageIO.read(new File(filePath));
                validateImage(image, fileBytes, transType);
            }

            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            throw new BuilderException("Error reading the file.");
        }
    }

}
