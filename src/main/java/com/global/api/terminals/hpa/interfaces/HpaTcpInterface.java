package com.global.api.terminals.hpa.interfaces;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.messaging.IPayAtTableRequestInterface;
import com.global.api.utils.AutoResetEvent;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;

public class HpaTcpInterface implements IDeviceCommInterface {
	private Socket client;
	private DataOutputStream out;
	private InputStream in;
	private AutoResetEvent await;
	private ITerminalConfiguration settings;
	private List<Byte> messageQueue;
	private String lastConnectionError;

	private IMessageSentInterface onMessageSent;
	private IMessageReceivedInterface onMessageReceived;

	public void setMessageSentHandler(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	public void setBroadcastMessageHandler(IBroadcastMessageInterface broadcastInterface) {
		// not required for this connection mode
	}

	public void setOnPayAtTableRequestHandler(IPayAtTableRequestInterface onPayAtTable) {
		// not required for this connection mode
	}

	public HpaTcpInterface(ITerminalConfiguration settings) {
		this.settings = settings;
		this.await = new AutoResetEvent(false);

		onMessageReceived = new IMessageReceivedInterface() {
			public void messageReceived(byte[] message) {
				if (messageQueue == null)
					messageQueue = new ArrayList<Byte>();

				for (byte b : message)
					messageQueue.add(b);

				try {
					Element msg = ElementTree.parse(message).get("SIP");
					int multiMessage = msg.getInt("MultipleMessage");
					if (multiMessage == 0)
						await.set();
					else
						messageQueue.add((byte) '\r'); // delimiter
				} catch (Exception e) {
					// this should never cause a failure
					messageQueue.add((byte) '\r'); // delimiter
				}
			}
		};
	}

	private void beginReceiveThread() {
		Thread receiveThread = new Thread() {
			public void run() {
				while (true) {
					try {
						if (in.available() > 0) {
							do {
								int length = getLength();
								if (length > 0) {
									byte[] buffer = new byte[8192];

									boolean incomplete = true;
									int offset = 0;
									int tempLength = length;
									do {
										int bytesReceived = in.read(buffer, offset, tempLength);
										if (bytesReceived != tempLength) {
											offset += bytesReceived;
											tempLength -= bytesReceived;
											sleep(10);
										} else
											incomplete = false;
									} while (incomplete);

									byte[] readBuffer = new byte[length];
									System.arraycopy(buffer, 0, readBuffer, 0, length);
									onMessageReceived.messageReceived(readBuffer);
								} else
									break;
							} while (true);
						}
						sleep(300);
					} catch (Exception e) {
						// This never needs to fail
					}
				}
			}
		};
		receiveThread.start();
	}

	public void connect() {
		if (client == null) {
			try {
				client = new Socket(settings.getIpAddress(), Integer.parseInt(settings.getPort()));

				if (client.isConnected()) {
					out = new DataOutputStream(client.getOutputStream());
					in = client.getInputStream();

					beginReceiveThread();
				}
			} catch (IOException e) {
				lastConnectionError = e.getMessage();
			}
		}
	}

	public void disconnect() {
		try {
			if (!client.isClosed()) {
				in.close();
				out.close();
				client.close();
			}
			messageQueue = null;
		} catch (IOException e) {
			// Eating the close exception
		} finally {
			in = null;
			out = null;
			client = null;
		}
	}

	public byte[] send(IDeviceMessage message) throws MessageException {
		connect();

		if (client == null || !client.isConnected()) {
			throw new MessageException(String.format("Could not connect to the device. %s", lastConnectionError));
		}

		String strMessage = message.toString();
		messageQueue = new ArrayList<Byte>();
		try {
			byte[] buffer = message.getSendBuffer();

			if (out != null) {
				out.write(buffer, 0, buffer.length);
				out.flush();

				if (message.isAwaitResponse()) {
					await.waitOne(settings.getTimeout());
					if (messageQueue.size() == 0) {
						throw new MessageException("Device did not response within the timeout");
					}

					return convertBytes(messageQueue.toArray(new Byte[messageQueue.size()]));
				} else
					return null;
			} else
				throw new MessageException("Device not connected");
		} catch (Exception exc) {
			throw new MessageException("Failed to send message see inner exception for more details", exc);
		} finally {
			if (onMessageSent != null) {
				onMessageSent.messageSent(strMessage.substring(2));
			}

			if (message.isKeepAlive()) {
				disconnect();
			}
		}
	}

	private int getLength() {
		try {
			byte[] lengthBuffer = new byte[2];
			int byteCount = in.read(lengthBuffer, 0, 2);

			if (byteCount != 2)
				return 0;
			return (short) (((lengthBuffer[0] & 0xFF) << 8) | (lengthBuffer[1] & 0xFF));
		} catch (IOException e) {
			return 0;
		}
	}

	private byte[] convertBytes(Byte[] buffer) {
		byte[] returnBuffer = new byte[buffer.length];

		int index = 0;
		for (Byte b : buffer)
			returnBuffer[index++] = b;

		return returnBuffer;
	}
}