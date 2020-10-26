package com.global.api.terminals.ingenico.interfaces;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.ingenico.responses.BroadcastMessage;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.ingenico.variables.INGENICO_RESP;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;
import com.global.api.utils.MessageWriter;

public class IngenicoSerialInterface implements IDeviceCommInterface {
	private ITerminalConfiguration _settings;
	private SerialPort _serialPort;
	private OutputStream _out;
	private Exception _exception;

	private MessageWriter _messageResponse;
	private byte[] _bufferReceived;
	private String _bReceived;

	private boolean _isAcknowledge;
	private boolean _isBroadcast;
	private boolean _isXML;
	private boolean _isFinalResult;
	private boolean _transComplete;
	private String _broadcastStr;
	private String _finalResponse;
	private String _operatingSystem;
	private String responseData;
	private String actualReceived;
	private StringBuilder _appendReport = new StringBuilder();

	private final Object _lock;
	private volatile boolean _exit;
	private volatile boolean _hasReceived;

	private IMessageSentInterface _onMessageSent;
	private IBroadcastMessageInterface _onBroadcastMessage;
	
	public IngenicoSerialInterface(ITerminalConfiguration settings) throws ConfigurationException {
		_lock = new Object();
		_bReceived = new String();
		_settings = settings;
		connect();
	}

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		_onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		_onBroadcastMessage = onBroadcastMessage;
	}

	public void setOnPayAtTableRequestHandler(IPayAtTableRequestInterface onPayAtTable) {
		// not required for this connection mode
	}

	private final class SerialPortDataReceived implements SerialPortPacketListener {
		public int getPacketSize() {
			return _serialPort.bytesAvailable();
		}

		public int getListeningEvents() {
			return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
		}

		public void serialEvent(SerialPortEvent event) {
			try {
				if (event.getEventType() == getListeningEvents() && !_exit) {
					_bufferReceived = event.getReceivedData();
					Thread.sleep(100);

					if (_bufferReceived.length < 3) {
						Integer ascii = 0;
						for (byte b : _bufferReceived) {
							ascii = (int) b;
						}

						if (ascii.byteValue() == ControlCodes.ACK.getByte()) {
							_isAcknowledge = true;
						} else if (ascii.byteValue() == ControlCodes.ENQ.getByte()) {
							_out.write(ControlCodes.ACK.getBytes(), 0, 1);
						}
					} else {
						if (!_transComplete) {
							_bReceived = new String(_bufferReceived, StandardCharsets.UTF_8);

							if (_bReceived.contains(new INGENICO_RESP().XML) && !_isXML) {
								_isXML = true;
							} else if (_bReceived.contains(new INGENICO_GLOBALS().BROADCAST)) {
								_isBroadcast = true;
								_broadcastStr = _bReceived;
							} else if (!_bReceived.contains(new INGENICO_GLOBALS().BROADCAST)
									&& !_bReceived.contains(new INGENICO_RESP().INVALID)
									&& (!_bReceived.contains(new INGENICO_RESP().XML)
											&& !_bReceived.contains(new INGENICO_RESP().ENDXML)
											&& !_bReceived.contains(new INGENICO_RESP().LFTAG))) {
								_isFinalResult = true;
								_finalResponse = _bReceived;
							}
						}
					}
				}
			} catch (InterruptedException e) {
				_exception = new InterruptedException(e.getMessage());
			} catch (IOException e) {
				_exception = new IOException(e.getMessage());
			} finally {
				synchronized (_lock) {
					_hasReceived = true;
					_lock.notify();
				}
			}
		}
	}

	public void connect() throws ConfigurationException {
		if (_settings == null) {
			throw new ConfigurationException("Please create connection between device and serial port.");
		}

		if (_serialPort == null) {
			StringBuilder systemPort = new StringBuilder();
			String operatingSystem = System.getProperty("os.name").toLowerCase();

			if (operatingSystem.indexOf("win") >= 0) {
				systemPort.append("COM");
				systemPort.append(_settings.getPort());
				_operatingSystem = new INGENICO_GLOBALS().WINDOWS_ENV;
			} else if (operatingSystem.indexOf("nux") >= 0) {
				systemPort.append("ttyS");
				systemPort.append(_settings.getPort());
				_operatingSystem = new INGENICO_GLOBALS().LINUX_ENV;
			}

			String portName = systemPort.toString();
			for (SerialPort port : SerialPort.getCommPorts()) {
				if (port.getSystemPortName().contains(portName)) {
					_serialPort = port;
					break;
				}
			}

			if (_serialPort == null) {
				throw new ConfigurationException("Cannot connect to the terminal.");
			}

			_serialPort.openPort();
			_serialPort.setComPortParameters(_settings.getBaudRate().getValue(), _settings.getDataBits().getValue(),
					_settings.getStopBits().getValue(), _settings.getParity().getValue());
			_serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, _settings.getTimeout(), 0);

			if (_serialPort.isOpen()) {
				_serialPort.addDataListener(new SerialPortDataReceived());
				_out = _serialPort.getOutputStream();
			}
		} else {
			throw new ConfigurationException("Serial port is already open.");
		}
	}

	public void disconnect() {
		_serialPort.closePort();
		_serialPort = null;
	}

	public byte[] send(final IDeviceMessage message) throws ApiException {
		try {
			if (_serialPort != null) {
				_exception = null;
				_transComplete = false;

				if (_onMessageSent != null) {
					String messageSent = TerminalUtilities.getString(message.getSendBuffer());
					messageSent = messageSent.substring(1, messageSent.length() - 2);
					_onMessageSent.messageSent(messageSent);
				}

				WriteMessage writeMessage = new WriteMessage(message);
				writeMessage.start();
				
				while (true) {
					if (!writeMessage.waitTask(_settings.getTimeout())) {
						if (!_hasReceived) {
							writeMessage.stop();

							if (_exception != null) {
								throw new InterruptedException(_exception.getMessage());
							} else {
								throw new InterruptedException("Terminal did not respond within timeout.");
							}
						} else {
							_hasReceived = false;
							continue;
						}
					} else {
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		} finally {
			_exit = true;
		}

		return _messageResponse.toArray();
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
		private IDeviceMessage _message;
		private Thread _thread;

		public WriteMessage(IDeviceMessage message) {
			_message = message;
		}

		public void start() {
			_thread = new Thread(this);
			_exit = false;
			_thread.start();
		}

		public void stop() throws ConfigurationException {
			_transComplete = true;
			_exit = true;
		}

		public boolean waitTask(long timeout) {
			synchronized (_lock) {
				boolean result = true;

				try {
					_lock.wait(timeout);

					if (!_transComplete) {
						result = false;
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
				_messageResponse = new MessageWriter();

				if (_serialPort == null) {
					throw new ApiException("Serial port is not open.");
				}

				do {
					_out.write(ControlCodes.ENQ.getBytes(), 0, 1);

					if (!_isAcknowledge) {
						Thread.sleep(1000);
						_out.write(ControlCodes.EOT.getBytes(), 0, 1);
						enquiryCount++;

						if (enquiryCount.equals(3)) {
							_exception = new ApiException(
									"Terminal did not respond in enquiry three (3) times. Send Aborted");
							throw _exception;
						}
					} else {
						do {
							byte[] msg = message.getSendBuffer();
							_out.write(msg, 0, msg.length);

							if (_isAcknowledge) {
								Thread.sleep(1000);
								_out.write(ControlCodes.EOT.getBytes(), 0, 1);
								_isAcknowledge = false;
								break;
							}
						} while (true);

						do {
							Thread.sleep(100);
							if (_isBroadcast) {
								BroadcastMessage broadcastMsg = new BroadcastMessage(
										_broadcastStr.getBytes(StandardCharsets.UTF_8));
								if (_onBroadcastMessage != null) {
									_onBroadcastMessage.broadcastReceived(broadcastMsg.getCode(),
											broadcastMsg.getMessage());
									_broadcastStr = "";
									_isBroadcast = false;
								}
							}

							if (_isXML) {
								while (!_transComplete) {
									if (_appendReport.toString().contains(new INGENICO_RESP().ENDXML)) {
										String xmlData = _appendReport.toString();
										_appendReport = new StringBuilder();

										if (messageReceived(xmlData)) {
											_out.write(ControlCodes.ACK.getBytes(), 0, 1);
											_appendReport = new StringBuilder();
											_isXML = false;
											_transComplete = true;
										}
									} else {
										if (!_bReceived.isEmpty()) {
											_appendReport.append(_bReceived);
											_bReceived = "";
										}
									}

									Thread.sleep(100);
								}
							}

							if (_isFinalResult) {
								String validate = TerminalUtilities.getString(message.getSendBuffer());
								String received = _finalResponse;
								String referenceNumber = validate.substring(1, 3);

								if (received.contains(referenceNumber)) {
									do {
										if (_operatingSystem.equals(new INGENICO_GLOBALS().WINDOWS_ENV)) {
											responseData = received.substring(0, received.length() - 1);
											actualReceived = received.substring(0, received.length() - 1);
										} else {
											responseData = received.substring(1, received.length());
											actualReceived = received.substring(1, received.length());	
										}
										
										boolean validateLRC = validateResponseLRC(responseData, actualReceived);
										if (validateLRC) {
											if (messageReceived(responseData)) {
												_out.write(ControlCodes.ACK.getBytes(), 0, 1);
												_finalResponse = "";
												_isFinalResult = false;
												_transComplete = true;
											}
										}
									} while (!_transComplete);
								}
							}
						} while (!_transComplete);
						break;
					}
				} while (true);
			} catch (Exception e) {
				throw new ApiException(e.getMessage());
			} finally {
				synchronized (_lock) {
					_lock.notify();
				}
			}
		}

		public void run() {
			new Thread(new Runnable() {
				public void run() {
					try {
						writeMessage(_message);
					} catch (ApiException e) {
						_exception = new ApiException(e.getMessage());
					}
				}
			}).start();
		}
	}

	private boolean messageReceived(String message) {
		if (_messageResponse == null) {
			return false;
		}

		for (char c : message.toCharArray()) {
			byte b = (byte) c;

			if (b == ControlCodes.STX.getByte() || b == ControlCodes.ETX.getByte()) {
				continue;
			}

			_messageResponse.add(b);
		}

		return true;
	}
}
