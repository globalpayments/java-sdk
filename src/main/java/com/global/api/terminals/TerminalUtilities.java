package com.global.api.terminals;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.Extensions;
import com.global.api.utils.MessageWriter;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.convert.Converter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TerminalUtilities {
	private static final String version = "1.35";

	public static String getElementString(Object... elements) {
		StringBuilder sb = new StringBuilder();
		for (Object element : elements) {
			if (element instanceof ControlCodes) {
				sb.append((char) ((ControlCodes) element).getByte());
			} else if (element instanceof IRequestSubGroup) {
				sb.append(((IRequestSubGroup) element).getElementString());
			} else if (element instanceof String[]) {
				for (String sub_element : (String[]) element) {
					sb.append(ControlCodes.FS.getByte());
					sb.append(sub_element);
				}
			} else if (element instanceof IStringConstant) {
				sb.append(((IStringConstant) element).getValue());
			} else if (element instanceof IByteConstant) {
				sb.append(((IByteConstant) element).getByte());
			} else
				sb.append(element);
		}

		return sb.toString();
	}

	public static String getString(byte[] buffer) {
		return new String(buffer, StandardCharsets.UTF_8);
	}

	public static Integer headerLength(byte[] buffer) {
		String fHex = String.format("%02X", buffer[0]);
		String sHex = String.format("%02X", (buffer[1] & 255));
		String hex = fHex + sHex;
		return Integer.parseInt(hex, 16);
	}

	public static String calculateHeader(byte[] buffer) {
		String hex = String.format("%04X", buffer.length);
		String fDigit = Character.toString(hex.charAt(0)) + hex.charAt(1);
		String sDigit = Character.toString(hex.charAt(2)) + hex.charAt(3);

		return String.format("%c%c", (char) Extensions.parseUnsignedInt(fDigit, 16), (char) Extensions.parseUnsignedInt(sDigit, 16));
	}

	

	public static DeviceMessage buildIngenicoRequest(String message, ConnectionModes settings) throws BuilderException {
		MessageWriter buffer = new MessageWriter();
		byte[] lrc;

		switch (settings) {
		case SERIAL:
		case PAY_AT_TABLE:
			buffer.add(ControlCodes.STX.getByte());
			for (char c : message.toCharArray())
				buffer.add((byte) c);
			buffer.add(ControlCodes.ETX.getByte());
			lrc = calculateLRC(message);
			buffer.add(lrc[0]);
			break;
		case TCP_IP_SERVER:
			String msg = calculateHeader(message.getBytes(StandardCharsets.UTF_8)) + message;
			for (char c : msg.toCharArray())
				buffer.add((byte) c);
			break;
		default:
			throw new BuilderException("Failed to build request message. Unknown Connection mode.");
		}

		return new DeviceMessage(buffer.toArray());
	}

	private static DeviceMessage buildMessage(PaxMsgId messageId, String message) {
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

	public static DeviceMessage buildRequest(String message, MessageFormat format) {
		MessageWriter buffer = new MessageWriter();

		// beginning sentinel
		if (format.equals(MessageFormat.Visa2nd))
			buffer.add(ControlCodes.STX);
		else {
			buffer.add((byte) (message.length() >>> 8));
			buffer.add((byte) message.length());
		}

		// put message
		buffer.addRange(message.getBytes());

		// ending sentinel
		if (format.equals(MessageFormat.Visa2nd)) {
			buffer.add(ControlCodes.ETX);

			byte lrc = calculateLRC(buffer.toArray());
			buffer.add(lrc);
		}

		return new DeviceMessage(buffer.toArray());
	}

	public static DeviceMessage buildRequest(PaxMsgId messageId, Object... elements) {
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
		if (buffer[buffer.length - 1] != ControlCodes.ETX.getByte()) {
			length--;
		}

		byte lrc = (byte) 0x00;
		for (int i = 1; i < length; i++) {
			lrc = (byte) (lrc ^ buffer[i]);
		}
		return lrc;
	}

	public static byte[] buildSignatureImage(String pathData) {
		String[] coordinates = pathData.split("\\^");

		BufferedImage bmp = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D gfx = bmp.createGraphics();
		gfx.setColor(Color.WHITE);
		gfx.fillRect(0, 0, 150, 100);
		gfx.setColor(Color.BLACK);

		int index = 0;
		String coordinate = coordinates[index++];
		do {
			if (coordinate.equals("0[COMMA]65535"))
				coordinate = coordinates[index++];
			Point start = toPoint(coordinate);

			coordinate = coordinates[index++];
			if (coordinate.equals("0[COMMA]65535")) {
				gfx.fillRect(start.x, start.y, 1, 1);
			} else {
				Point end = toPoint(coordinate);
				gfx.drawLine(start.x, start.y, end.x, end.y);
			}
		} while (!coordinates[index].equals("~"));
		gfx.dispose();

		// save to a memory stream and return the byte array
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ImageIO.write(bmp, "bmp", buffer);
			buffer.flush();

			byte[] rvalue = buffer.toByteArray();
			buffer.close();

			return rvalue;
		} catch (IOException exc) {
			return null;
		}
	}

	private static Point toPoint(String coordinate) {
		String[] xy = coordinate.split("\\[COMMA]");

		Point rvalue = new Point();
		rvalue.x = Integer.parseInt(xy[0]);
		rvalue.y = Integer.parseInt(xy[1]);

		return rvalue;
	}

	public static byte[] calculateLRC(String requestMessage) {
		byte[] cCode = new byte[] { ControlCodes.ETX.getByte() };
		int index1 = requestMessage.getBytes().length;
		int index2 = cCode.length;
		byte[] bytes = new byte[index1 + index2];
		System.arraycopy(requestMessage.getBytes(), 0, bytes, 0, index1);
		System.arraycopy(cCode, 0, bytes, index1, index2);

		byte lrc = 0;
		for (int i = 0; i < bytes.length; i++) {
			lrc ^= bytes[i];
		}
		bytes = new byte[] { lrc };
		return bytes;
	}
}
