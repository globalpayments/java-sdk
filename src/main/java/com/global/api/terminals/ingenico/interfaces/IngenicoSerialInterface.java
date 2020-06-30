package com.global.api.terminals.ingenico.interfaces;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.ingenico.responses.BroadcastMessage;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.ingenico.variables.INGENICO_RESP;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.MessageWriter;

public class IngenicoSerialInterface implements IDeviceCommInterface {
	private ITerminalConfiguration settings;
	private SerialPort serialPort;
	private Exception exception;

	private MessageWriter messageResponse;
	private byte[] bufferReceived;

	private boolean isAcknowledge;
	private boolean isBroadcast;
	private boolean isXML;
	private boolean isFinalResult;
	private boolean transComplete;
	private String appendReport;
	private String broadcastStr;
	private String finalResponse;
	private final Object lock;

	private IMessageSentInterface onMessageSent;
	private IBroadcastMessageInterface onBroadcastMessage;

	public IngenicoSerialInterface(ITerminalConfiguration settings) throws ConfigurationException {
		lock = new Object();
		this.settings = settings;
		connect();
	}

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		this.onBroadcastMessage = onBroadcastMessage;
	}

	private final class SerialPortDataReceived implements SerialPortPacketListener {
		public int getPacketSize() {
			return serialPort.bytesAvailable();
		}

		public int getListeningEvents() {
			return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
		}

		public void serialEvent(SerialPortEvent event) {
			try {
				if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
					bufferReceived = event.getReceivedData();

					Thread.sleep(100);
					serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, settings.getTimeout(), 0);

					if (bufferReceived.length < 3) {
						Integer ascii = 0;
						for (byte b : bufferReceived) {
							ascii = (int) b;
						}

						if (ascii.byteValue() == ControlCodes.ACK.getByte()) {
							isAcknowledge = true;
						} else if (ascii.byteValue() == ControlCodes.ENQ.getByte()) {
							serialPort.writeBytes(ControlCodes.ACK.getBytes(), ControlCodes.ACK.getBytes().length, 0);
						}
					} else {
						String bReceived = new String(bufferReceived, StandardCharsets.UTF_8);
						if (bReceived.contains(new INGENICO_RESP().XML)) {
							appendReport = bReceived;
							isXML = true;
						} else if (bReceived.contains(new INGENICO_GLOBALS().BROADCAST)) {
							isBroadcast = true;
							broadcastStr = bReceived;
						} else if (!bReceived.contains(new INGENICO_GLOBALS().BROADCAST)
								&& !bReceived.contains(new INGENICO_RESP().INVALID)
								&& !bReceived.contains(new INGENICO_RESP().XML)) {
							isFinalResult = true;
							finalResponse = bReceived;
						}
					}
				}
			} catch (InterruptedException e) {
				exception = new InterruptedException(e.getMessage());
			}
		}
	}

	public void connect() throws ConfigurationException {
		if (settings == null) {
			throw new ConfigurationException("Please create connection between device and serial port.");
		}

		if (serialPort == null) {
			StringBuilder systemPort = new StringBuilder();
			String operatingSystem = System.getProperty("os.name").toLowerCase();
			if (operatingSystem.indexOf("win") >= 0) {
				systemPort.append("COM");
				systemPort.append(settings.getPort());
			} else if (operatingSystem.indexOf("nux") >= 0) {
				systemPort.append("ttyS");
				systemPort.append(settings.getPort());
			}

			for (SerialPort port : SerialPort.getCommPorts()) {
				if (port.getSystemPortName().contains(systemPort.toString())) {
					serialPort = port;
					break;
				}
			}

			if (serialPort == null) {
				throw new ConfigurationException("Can't connect to the terminal.");
			}

			serialPort.openPort();
			serialPort.setComPortParameters(settings.getBaudRate().getValue(), settings.getDataBits().getValue(),
					settings.getStopBits().getValue(), settings.getParity().getValue());
			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, settings.getTimeout(), 0);

			if (serialPort.isOpen()) {
				serialPort.addDataListener(new SerialPortDataReceived());
			}
		} else {
			throw new ConfigurationException("Serial port already open.");
		}
	}

	public void disconnect() {
		serialPort.closePort();
		serialPort = null;
	}

	public byte[] send(final IDeviceMessage message) throws ApiException {
		try {
			if (serialPort != null) {
				if (onMessageSent != null) {
					String messageSent = TerminalUtilities.getString(message.getSendBuffer());
					messageSent = messageSent.substring(1, messageSent.length() - 3);
					onMessageSent.messageSent(messageSent);
				}

				WriteMessage writeMessage = new WriteMessage(message);
				writeMessage.start();
				if (!writeMessage.waitTask(settings.getTimeout())) {
					writeMessage.interrupt();
					throw new RuntimeException("Terminal did not respond within timeout.");
				}
			}
		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		} finally {
			exception = null;
			transComplete = false;
		}

		return messageResponse.toArray();
	}

	private boolean validateResponseLRC(String toCalculate, String actualResponse) {
		byte[] calculateLRC = TerminalUtilities.calculateLRC(toCalculate);
		byte[] actualLRC = TerminalUtilities.calculateLRC(actualResponse);

		if (calculateLRC[0] == actualLRC[0]) {
			return true;
		}

		return false;
	}

	private class WriteMessage implements Runnable {
		private IDeviceMessage message;
		private Thread thread;

		public WriteMessage(IDeviceMessage message) {
			this.message = message;
		}

		public void interrupt() {
			thread.interrupt();
			thread = null;
		}

		public void start() {
			thread = new Thread(this);
			thread.start();
		}

		public boolean waitTask(long timeout) {
			synchronized (lock) {
				boolean result = true;
				try {
					lock.wait(timeout);

					if (!transComplete) {
						result = false;
						serialPort.removeDataListener();
					} else {
						return result;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				return result;
			}
		}

		private void writeMessage(IDeviceMessage message) throws ApiException {
			try {
				Integer enquiryCount = 0;
				messageResponse = new MessageWriter();

				if (serialPort == null) {
					throw new ApiException("Serial port is not open.");
				}

				do {
					serialPort.writeBytes(ControlCodes.ENQ.getBytes(), ControlCodes.ENQ.getBytes().length, 0);
					if (!isAcknowledge) {
						Thread.sleep(1000);
						serialPort.writeBytes(ControlCodes.EOT.getBytes(), ControlCodes.EOT.getBytes().length, 0);
						enquiryCount++;

						if (enquiryCount.equals(3)) {
							throw new ApiException("Send aborted.");
						}
					} else {
						byte[] msg = message.getSendBuffer();
						serialPort.writeBytes(msg, msg.length, 0);
						enquiryCount = 0;

						do {
							if (!isAcknowledge) {
								enquiryCount++;
								Thread.sleep(1000);

								if (enquiryCount.equals(3)) {
									throw new ApiException("Send aborted.");
								}
							} else {
								serialPort.writeBytes(ControlCodes.EOT.getBytes(), ControlCodes.EOT.getBytes().length,
										0);
								isAcknowledge = false;
								break;
							}
						} while (true);

						do {
							Thread.sleep(100);
							if (isBroadcast) {
								BroadcastMessage broadcastMsg = new BroadcastMessage(
										broadcastStr.getBytes(StandardCharsets.UTF_8));
								if (onBroadcastMessage != null) {
									onBroadcastMessage.broadcastReceived(broadcastMsg.getCode(),
											broadcastMsg.getMessage());
									broadcastStr = "";
									isBroadcast = false;
								}
							}

							if (isXML) {
								do {
									Thread.sleep(100);
									String bReceived = new String(bufferReceived, StandardCharsets.UTF_8);
									if (!appendReport.contains(bReceived)) {
										appendReport += bReceived;
									}

									if (appendReport.contains(new INGENICO_RESP().ENDXML)) {
										String xmlData = appendReport.trim();
										if (messageReceived(xmlData)) {
											serialPort.writeBytes(ControlCodes.ACK.getBytes(),
													ControlCodes.ACK.getBytes().length, 0);
											appendReport = "";
											isXML = false;
											transComplete = true;
										}
									}
								} while (!transComplete);
							}

							if (isFinalResult) {
								String check = TerminalUtilities.getString(message.getSendBuffer());
								String bReceived = finalResponse;
								if (bReceived.contains(check.substring(1, 3))) {
									do {
										String rData = bReceived.substring(0, bReceived.length() - 2);
										bReceived = bReceived.substring(0, bReceived.length() - 2);
										boolean validateLRC = validateResponseLRC(rData, bReceived);
										if (validateLRC) {
											if (messageReceived(rData)) {
												serialPort.writeBytes(ControlCodes.ACK.getBytes(),
														ControlCodes.ACK.getBytes().length, 0);
												finalResponse = "";
												isFinalResult = false;
												transComplete = true;
											}
										}
									} while (!transComplete);
								}
							}
						} while (!transComplete);
						break;
					}
				} while (true);
			} catch (Exception e) {
				throw new ApiException(e.getMessage());
			} finally {
				synchronized (lock) {
					lock.notify();
				}
			}
		}

		public void run() {
			new Thread(new Runnable() {
				public void run() {
					try {
						writeMessage(message);
					} catch (ApiException e) {
						exception = new ApiException(e.getMessage());
					}
				}
			}).start();
		}
	}

	private boolean messageReceived(String message) {
		if (messageResponse == null) {
			return false;
		}

		for (char c : message.toCharArray()) {
			if (((byte) c) == ControlCodes.STX.getByte() || ((byte) c) == ControlCodes.ETX.getByte()) {
				continue;
			}

			messageResponse.add((byte) c);
		}

		return true;
	}
}
