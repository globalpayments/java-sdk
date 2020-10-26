package com.global.api.terminals.ingenico.interfaces;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.ingenico.pat.PATRequest;
import com.global.api.terminals.ingenico.responses.BroadcastMessage;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;
import com.global.api.utils.MessageWriter;

public class IngenicoTcpInterface implements IDeviceCommInterface {
	private Socket _socket;
	private ServerSocket _serverSocket;
	private DataOutputStream _out;
	private DataInputStream _in;
	private ITerminalConfiguration _settings;
	private Thread _dataReceiving;
	private byte[] _terminalResponse = null;
	private Exception _exception = null;

	private boolean _readData;
	private boolean _isResponseNeeded;
	private boolean _isKeepAlive;

	private IMessageSentInterface _onMessageSent;
	private IBroadcastMessageInterface _onBroadcastMessage;
	private IPayAtTableRequestInterface _onPayAtTableRequest;

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		_onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		_onBroadcastMessage = onBroadcastMessage;
	}

	public void setOnPayAtTableRequestHandler(IPayAtTableRequestInterface onPayAtTable) {
		_onPayAtTableRequest = onPayAtTable;
	}

	public IngenicoTcpInterface(ITerminalConfiguration settings) throws ConfigurationException {
		_settings = settings;
		_socket = new Socket();
		initializeServer();
		connect();
	}

	private int tryParse(String value, int defaultVal) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	private byte[] removeHeader(byte[] buffer) {
		return Arrays.copyOfRange(buffer, 2, buffer.length);
	}

	private boolean isBroadcast(byte[] buffer) {
		return TerminalUtilities.getString(buffer).contains(new INGENICO_GLOBALS().BROADCAST);
	}

	private boolean isKeepAlive(byte[] buffer) {
		return TerminalUtilities.getString(buffer).contains(new INGENICO_GLOBALS().TID_CODE);
	}

	private synchronized void initializeServer() throws ConfigurationException {
		if (_serverSocket == null) {
			if (_settings.getPort().isEmpty())
				if (tryParse(_settings.getPort(), 0) == 0) {
					throw new ConfigurationException("Invalid port number.");
				}

			try {
				int port = tryParse(_settings.getPort(), 0);
				_serverSocket = new ServerSocket(port);
				_socket = _serverSocket.accept();
				_socket.setSoTimeout(_settings.getTimeout());
				_out = new DataOutputStream(_socket.getOutputStream());
				_in = new DataInputStream(_socket.getInputStream());
				_exception = null;
				_readData = true;
				_isKeepAlive = false;
			} catch (IOException e) {
				throw new ConfigurationException(e.getMessage());
			}
		} else {
			throw new ConfigurationException("Server already initialized.");
		}
	}

	private byte[] keepAliveResponse(byte[] buffer) {
		if (buffer.length > 0) {
			int tIdIndex = TerminalUtilities.getString(buffer).indexOf(new INGENICO_GLOBALS().TID_CODE);
			byte[] tIdCode = Arrays.copyOfRange(buffer, tIdIndex + 10, 62);
			String terminalId = TerminalUtilities.getString(tIdCode);
			String response = String.format(new INGENICO_GLOBALS().KEEP_ALIVE_RESPONSE, terminalId);
			response = TerminalUtilities.calculateHeader(response.getBytes(StandardCharsets.UTF_8)) + response;

			return response.getBytes(StandardCharsets.UTF_8);
		}

		return null;
	}

	private void analyzeReceivedData() throws ApiException {
		try {
			byte[] headerBuffer = new byte[2];

			while (_readData) {
				if (_settings.getConnectionMode() == ConnectionModes.PAY_AT_TABLE) {
					byte[] buffer = new byte[8192];
					_in.read(buffer, 0, buffer.length);

					MessageWriter byteArr = new MessageWriter();
					for (int i = 0; i < buffer.length; i++) {
						byteArr.add(buffer[i]);

						if (buffer[i] == ControlCodes.ETX.getByte()) {
							byteArr.add(buffer[i + 1]);
							break;
						}
					}

					Integer arrLen = byteArr.toArray().length;
					if (arrLen > 0) {
						String raw = TerminalUtilities.getString(byteArr.toArray());
						String dataETX = raw.substring(1, raw.length() - 2);

						String receivedLRC = raw.substring(raw.length() - 1);

						byte[] calculateLRC = TerminalUtilities.calculateLRC(dataETX);
						String calculatedLRC = new String(calculateLRC, StandardCharsets.UTF_8);

						if (calculatedLRC.contentEquals(receivedLRC)) {
							String data = raw.substring(1, raw.length() - 2);

							PATRequest patRequest = new PATRequest(data.getBytes());
							if (_onPayAtTableRequest != null) {
								_onPayAtTableRequest.onPayAtTableRequest(patRequest);
							}
						}
					}
				} else {
					int readHeader = _in.read(headerBuffer, 0, headerBuffer.length);

					if (!_readData) {
						break;
					}

					if (!_isKeepAlive && _isResponseNeeded) {
						_socket.setSoTimeout(_settings.getTimeout());
					}

					if (readHeader == -1) {
						_exception = new ApiException("Terminal disconnected");
					}

					int dataLength = TerminalUtilities.headerLength(headerBuffer);
					if (dataLength > 0) {
						byte[] dataBuffer = new byte[dataLength];

						boolean incomplete = true;
						int offset = 0;
						int tempLength = dataLength;

						do {
							int bytesReceived = _in.read(dataBuffer, offset, tempLength);

							if (!_readData) {
								break;
							}

							if (bytesReceived != tempLength) {
								offset += bytesReceived;
								tempLength -= bytesReceived;
							} else {
								incomplete = false;
							}
						} while (incomplete);

						byte[] readBuffer = new byte[dataLength];
						System.arraycopy(dataBuffer, 0, readBuffer, 0, dataLength);

						if (isBroadcast(readBuffer)) {
							BroadcastMessage broadcastMessage = new BroadcastMessage(readBuffer);
							if (_onBroadcastMessage != null) {
								_onBroadcastMessage.broadcastReceived(broadcastMessage.getCode(),
										broadcastMessage.getMessage());
							}
						} else if (isKeepAlive(readBuffer) && new INGENICO_GLOBALS().KEEPALIVE) {
							_isKeepAlive = true;
							byte[] kResponse = keepAliveResponse(readBuffer);
							_out.write(kResponse);
							_out.flush();
						} else {
							_terminalResponse = readBuffer;
						}
					} else {
						_exception = new ApiException("No data received");
					}
				}
			}
		} catch (Exception e) {
			if (_isResponseNeeded || _isKeepAlive) {
				_exception = new ApiException("Socket Error: " + e.getMessage());
			}

			if (_readData) {
				analyzeReceivedData();
			}
		}
	}

	public void connect() throws ConfigurationException {
		if (_serverSocket != null) {
			if (_dataReceiving == null) {
				new Thread(new Runnable() {
					public void run() {
						try {
							analyzeReceivedData();
						} catch (ApiException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} else {
			throw new ConfigurationException("Server already started.");
		}
	}

	public void disconnect() {
		try {
			if (!_serverSocket.isClosed() || _serverSocket != null) {
				if (!_isKeepAlive) {
					_socket.setSoTimeout(1000);
				}

				_readData = false;
				_socket.close();
				_serverSocket.close();
				_serverSocket = null;
				_socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] send(IDeviceMessage message) throws ApiException {
		byte[] buffer = message.getSendBuffer();
		_terminalResponse = null;
		_exception = null;
		_isResponseNeeded = true;

		try {
			if (_serverSocket == null) {
				throw new ConfigurationException("Server is not running.");
			} else if (_socket == null) {
				_socket = _serverSocket.accept();
			}

			_out.write(buffer);
			_out.flush();

			if (_settings.getConnectionMode() == ConnectionModes.PAY_AT_TABLE) {
				String data = TerminalUtilities.getString(buffer);

				if (_onMessageSent != null) {
					String messageSent = data.substring(1, data.length() - 3);
					_onMessageSent.messageSent(messageSent);
				}

				return null;
			}

			if (_onMessageSent != null) {
				String messageSent = TerminalUtilities.getString(removeHeader(buffer));
				_onMessageSent.messageSent(messageSent);
			}

			while (_terminalResponse == null) {
				Thread.sleep(100);
				if (_exception != null) {
					throw new ApiException(_exception.getMessage());
				}

				if (_terminalResponse != null) {
					_isResponseNeeded = false;
					return _terminalResponse;
				}
			}
		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		}

		return _terminalResponse;
	}
}
