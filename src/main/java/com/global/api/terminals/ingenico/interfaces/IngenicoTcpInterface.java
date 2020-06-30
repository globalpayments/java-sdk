package com.global.api.terminals.ingenico.interfaces;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.ingenico.responses.BroadcastMessage;
import com.global.api.terminals.ingenico.variables.INGENICO_GLOBALS;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class IngenicoTcpInterface implements IDeviceCommInterface {
	private Socket socket;
	private ServerSocket serverSocket;
	private DataOutputStream out;
	private DataInputStream in;
	private ITerminalConfiguration settings;
	private Thread dataReceiving;
	private byte[] terminalResponse = null;
	private Exception exception = null;

	private boolean readData;
	private boolean disposable;
	private boolean isResponseNeeded;
	private boolean isKeepAlive;

	private IMessageSentInterface onMessageSent;
	private IBroadcastMessageInterface onBroadcastMessage;

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		this.onBroadcastMessage = onBroadcastMessage;
	}

	public IngenicoTcpInterface(ITerminalConfiguration settings) throws ConfigurationException {
		this.settings = settings;
		socket = new Socket();
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
		if (serverSocket == null) {
			if (settings.getPort().isEmpty())
				if (tryParse(settings.getPort(), 0) == 0) {
					throw new ConfigurationException("Invalid port number.");
				}

			try {
				int port = tryParse(settings.getPort(), 0);
				serverSocket = new ServerSocket(port);
				socket = serverSocket.accept();
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				exception = null;
				readData = true;
				disposable = false;
				isKeepAlive = false;
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
		} else {
			return null;
		}
	}

	private void analyzeReceivedData() throws ApiException {
		try {
			byte[] headerBuffer = new byte[2];

			while (readData) {
				if (!readData) {
					break;
				}

				in.read(headerBuffer, 0, headerBuffer.length);
				int dataLength = TerminalUtilities.headerLength(headerBuffer);
				byte[] dataBuffer = new byte[dataLength + 2];
				Thread.sleep(1000);
				in.read(dataBuffer, 0, dataBuffer.length);
				
				if (exception != null) {
					dataBuffer = null;
				}

				if (isBroadcast(dataBuffer)) {
					BroadcastMessage broadcastMessage = new BroadcastMessage(dataBuffer);
					if (onBroadcastMessage != null) {
						onBroadcastMessage.broadcastReceived(broadcastMessage.getCode(), broadcastMessage.getMessage());
					}
				} else if (isKeepAlive(dataBuffer) && new INGENICO_GLOBALS().KEEPALIVE) {
					isKeepAlive = true;
					byte[] kResponse = keepAliveResponse(dataBuffer);
					out.write(kResponse);
					out.flush();
				} else {
					terminalResponse = dataBuffer;
				}
			}
		} catch (Exception e) {
			if (isResponseNeeded || isKeepAlive) {
				exception = new ApiException("Socket Error: " + e.getMessage());
			}

			if (!readData) {
				disposable = true;
			} else {
				analyzeReceivedData();
			}
		}
	}

	public void connect() throws ConfigurationException {
		if (serverSocket != null) {
			if (dataReceiving == null) {
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
			if (!serverSocket.isClosed() || serverSocket != null) {
				if (!isKeepAlive) {
					socket.setSoTimeout(1000);
				}

				readData = false;
				socket.close();
				serverSocket.close();
				serverSocket = null;
				socket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] send(IDeviceMessage message) throws ApiException {
		final byte[] buffer = message.getSendBuffer();
		terminalResponse = null;
		exception = null;
		isResponseNeeded = true;

		try {
			if (serverSocket == null) {
				throw new ConfigurationException("Server is not running.");
			} else if (socket == null) {
				socket = serverSocket.accept();
			}

			socket.setSoTimeout(settings.getTimeout());
			out.write(buffer);
			out.flush();
			if (onMessageSent != null) {
				String messageSent = TerminalUtilities.getString(removeHeader(buffer));
				onMessageSent.messageSent(messageSent);
			}

			while (terminalResponse == null) {
				Thread.sleep(100);
				if (exception != null) {
					throw new ApiException(exception.getMessage());
				}

				if (terminalResponse != null) {
					isResponseNeeded = false;
					return terminalResponse;
				}
			}
		} catch (Exception e) {
			throw new ApiException(e.getMessage());
		}

		return null;
	}
}
